package uk.gov.hmcts.futurehearings.snl.acceptance.resources;

import static uk.gov.hmcts.futurehearings.snl.acceptance.common.TestingUtils.generateStringForGivenLength;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.TestingUtils.replaceCharacterSequence;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createStandardPayloadHeader;

import org.junit.jupiter.params.provider.CsvFileSource;
import uk.gov.hmcts.futurehearings.snl.Application;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.TestingUtils;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.CommonDelegate;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.dto.DelegateDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.dto.SNLVerificationDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.error.SNLCommonErrorVerifier;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.success.SNLCommonSuccessVerifier;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Random;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;


@Slf4j
@SpringBootTest(classes = {Application.class})
@ActiveProfiles("acceptance")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SelectClasses(POSTResourcesByLocationPayloadValidationTest.class)
@IncludeTags("Post")
//@Disabled("This test is disabled till the till we have an agreement with S&L over the UAT")
class POSTResourcesByLocationPayloadValidationTest extends ResourcesPayloadValidationTest {

    @Qualifier("CommonDelegate")
    @Autowired(required = true)
    private CommonDelegate commonDelegate;

    @Value("${resourcesByLocationRootContext}")
    private String resourcesByLocationRootContext;

    @BeforeAll
    public void initialiseValues() throws Exception {
        super.initialiseValues();
        this.setRelativeURL(resourcesByLocationRootContext);
        this.setHttpMethod(HttpMethod.POST);
        this.setInputPayloadFileName("resource-by-location-complete.json");
        this.setHttpSuccessStatus(HttpStatus.CREATED);
        this.setRelativeURLForNotFound(this.getRelativeURL().replace("resources", "resource"));
        this.setSnlSuccessVerifier(new SNLCommonSuccessVerifier());
        this.setSnlErrorVerifier(new SNLCommonErrorVerifier());
        this.setInputBodyPayload(TestingUtils.readFileContents(String.format(INPUT_FILE_PATH, getInputFileDirectory()) +
                "/" + getInputPayloadFileName()));
    }

    @Test
    @DisplayName("Successfully validated response for a payload including optional fields")
    public void test_successful_response_with_optional_elements_payload() throws Exception {
        this.setInputPayloadFileName("resource-by-location-complete.json");
        generatePayloadWithRandomHMCTSID(8, "/location/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @Test
    @DisplayName("LocationIdHMCTS Positive tests")
    public void test_positive_response_with_mandatory_locationId_payload() throws Exception {
        this.setInputPayloadFileName("resource-by-location-all-mandatory-location-id-hmcts.json");
        generatePayloadWithRandomHMCTSID(8, "/location/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "LocationIdHMCTS Negative tests")
    @CsvSource(value = {"Empty Space,''", "Invalid Location id, C_FEFC242"}, nullValues = "NIL")
    //TODO - LocationIdHMCTS Empty values should not be ingested in the System - Data - "Single Space,' '" - MCGIRRSD-2194
    public void test_negative_response_with_mandatory_locationId_payload(final String locationIdHMCTSKey, final String locationIdHMCTSValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-all-mandatory-location-id-hmcts.json");
        generatePayloadWithHMCTSID(locationIdHMCTSValue, "/location/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        SNLVerificationDTO snlVerificationDTO = null;
        switch (locationIdHMCTSValue) {
            case "":
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationIdHMCTS: does not match the regex pattern ^[!-~]+$]", null);
                break;
            case " ":
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1001", "A Location resource with 'locationIdHMCTS' = ' ' already exists", null);
                break;
            default:
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationIdHMCTS: may only be 8 characters long]", null);
                break;

        }
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                snlVerificationDTO);
    }

    @ParameterizedTest(name = "locationCluster Positive Tests Scenario : {0}")
    @CsvSource(value = {"Location Cluster,'TV'", "Location Cluster,'KNT'"})
    public void test_positive_response_for_location_cluster_with_mandatory_elements_payload(final String locationClusterKey,
                                                                                            final String locationClusterValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-mandatory-cluster.json");
        generatePayloadWithRandomHMCTSIDAndField(8, locationClusterValue, "/location/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "locationClusterValue Negative tests")
    @CsvSource(value = {"Empty Space,''",
            "Single Space,' '",
            "Random_Cluster_Value,'Z'",
            "Random_Cluster_Value,'BR'",
            "Random_Cluster_Value,'RGB'",
            "Invalid_Cluster_Max_Value, C_FE"}, nullValues = "NIL")
    public void test_negative_response_with_mandatory_location_cluster_payload(final String locationClusterKey,
                                                                               final String locationClusterValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-mandatory-cluster.json");
        generatePayloadWithRandomHMCTSIDAndField(8, locationClusterValue, "/location/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        SNLVerificationDTO snlVerificationDTO = null;
        switch (locationClusterKey) {
            case "Invalid_Cluster_Max_Value":
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationCluster: may only be 3 characters long]", null);
                break;
            default:
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", "'" + locationClusterValue + "' is not a valid value for field 'locationCluster'", null);
                break;

        }
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                snlVerificationDTO);
    }

    @ParameterizedTest(name = " Positive Tests Scenario : {0}")
    @CsvSource(value = {"Location Description,'x HMI Test This is location description'"})
    public void test_positive_response_for_location_description_with_mandatory_elements_payload(final String locationDescriptionKey,
                                                                                                final String locationDescriptionValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-mandatory-description.json");
        generatePayloadWithRandomHMCTSIDAndField(8, locationDescriptionValue, "/location/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = " Negative tests")
    @CsvSource(value = {"Invalid Data,''", "Invalid Data,' '", "Invalid Data : Description Length 81, C"}, nullValues = "NIL")
    @Disabled("Disabling the location Description")
    //TODO:  Accepts empty space and single space even though it is a mandatory field. Defect MCGIRRSD-2194
    public void test_negative_response_with_mandatory_location_description_payload(final String locationDescriptionKey,
                                                                                   final String locationDescriptionValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-mandatory-description.json");
        if (locationDescriptionKey.trim().equals("Invalid Data : Description Length 81")) {
            generatePayloadWithRandomHMCTSIDAndField(
                    generateStringForGivenLength(81, locationDescriptionValue), "/location/post/");
        } else {
            generatePayloadWithRandomHMCTSIDAndField(locationDescriptionValue, "/location/post/");
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        SNLVerificationDTO snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationDescription: may only be 80 characters long]", null);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                snlVerificationDTO);
    }

    @ParameterizedTest(name = "locationPrimaryFlag Positive Tests Scenario : {0}")
    @CsvSource(value = {"Location Primary Flag, true", "Location Primary Flag, false"})
    public void test_positive_response_for_location_primary_flag_with_mandatory_elements_payload(final String locationPrimaryFlagKey,
                                                                                                 final String locationPrimaryFlagValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-mandatory-primary-flag.json");
        if (locationPrimaryFlagValue.trim().equals("true")) {
            generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"locationPrimaryFlag\": 0", "\"locationPrimaryFlag\":" + true, "/location/post/");
        } else {
            generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"locationPrimaryFlag\": 0", "\"locationPrimaryFlag\":" + false, "/location/post/");
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "locationPrimaryFlag Negative Tests Scenario : {0}")
    @CsvSource(value = {"Invalid Value Int,0", "Invalid Value Float,1.0", "Invalid Value Negative Int,-1", "Invalid Value Negative Float,-1.4"})
    public void test_negative_response_for_location_primary_flag_with_mandatory_elements_payload(final String locationPrimaryFlagKey,
                                                                                                 final String locationPrimaryFlagValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-mandatory-primary-flag.json");
        SNLVerificationDTO snlVerificationDTO = null;
        switch (locationPrimaryFlagKey) {
            case "Invalid Value Int":
            case "Invalid Value Negative Int":
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationPrimaryFlag: integer found, boolean expected]", null);
                generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"locationPrimaryFlag\": 0", "\"locationPrimaryFlag\":" + Integer.parseInt(locationPrimaryFlagValue), "/location/post/");
                break;
            case "Invalid Value Float":
            case "Invalid Value Negative Float":
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationPrimaryFlag: number found, boolean expected]", null);
                generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"locationPrimaryFlag\": 0", "\"locationPrimaryFlag\":" + Float.parseFloat(locationPrimaryFlagValue), "/location/post/");
                break;
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                snlVerificationDTO);
    }

    @ParameterizedTest(name = "locationActiveFrom Positive tests")
    @CsvSource({"Valid_Date_Format,2020-12-15"})
    public void test_positive_response_with_mandatory_location_activeFrom_payload(final String locationActiveFromKey,
                                                                                  final String locationActiveFromValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-mandatory-activeFrom.json");
        generatePayloadWithRandomHMCTSIDAndField(8, locationActiveFromValue, "/location/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }


    @ParameterizedTest(name = "locationActiveFrom Negative tests")
    @CsvFileSource(resources = "/uk/gov/hmcts/futurehearings/snl/acceptance/common/data/negative-different-date-time-format-values.csv", numLinesToSkip = 1)
    //TODO Defect has to be raised around this area.
    //TODO Defect has to be raised for the data of 2015-12-11T09:28:30.45 and 2015-12-11T09:28:30
    public void test_negative_response_with_mandatory_location_activeFrom_payload(final String locationActiveFromKey,
                                                                                  final String locationActiveFromValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-mandatory-activeFrom.json");
        generatePayloadWithRandomHMCTSIDAndField(8, locationActiveFromValue, "/location/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("[$.locationRequest.location.locationActiveFrom: {0} is an invalid date]", locationActiveFromValue);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", errorDesc, null));
    }


    @ParameterizedTest(name = "locationPostCode Positive Tests Scenario : {0}")
    @CsvSource(value = {"Valid Location PostCode,'HA2 0NB'", "Valid Location PostCode,'SW7'", "Valid Location PostCode,'SW78LU'"})
    public void test_positive_response_for_location_postcode_with_optional_elements_payload(final String locationPostCodeKey,
                                                                                            final String locationPostCodeValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-postcode.json");
        generatePayloadWithRandomHMCTSIDAndField(locationPostCodeValue, "/location/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    //TODO: PostCode accepts empty space, single space and even doesn't validate postcode.
    // Any random postcode like HHKK7788WW is acceptable
    @ParameterizedTest(name = "locationPostCode Negative tests")
    @CsvSource(value = {"Invalid Data PostCode Max Value, C"}, nullValues = "NIL")
    public void test_negative_response_with_optional_location_postcode_payload(final String locationPostCodeKey,
                                                                               final String locationPostCodeValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-postcode.json");
        if (locationPostCodeKey.trim().equals("Invalid Data PostCode Max Value")) {
            generatePayloadWithRandomHMCTSIDAndField(
                    generateStringForGivenLength(16, locationPostCodeKey), "/location/post/");
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        SNLVerificationDTO snlVerificationDTO = null;
        switch (locationPostCodeKey) {
            case "Invalid_PostCode_Max_Value":
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationPostCode: may only be 15 characters long]", null);
                break;
            default:
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationPostCode: may only be 15 characters long]", null);
                break;

        }
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                snlVerificationDTO);
    }

    @Disabled("TODO - Review this Test post the confirmation of date formats for the payloads")
    @ParameterizedTest(name = "locationActiveTo Positive tests")
    @CsvSource({"Valid_Date_Format,2002-02-15"})
    public void test_positive_response_with_mandatory_location_activeTo_payload(final String locationActiveFromKey,
                                                                                final String locationActiveFromValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-activeTo.json");
        generatePayloadWithRandomHMCTSIDAndField(locationActiveFromValue, "/location/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }


    @ParameterizedTest(name = "locationActiveTo Negative tests")
    @CsvFileSource(resources = "/uk/gov/hmcts/futurehearings/snl/acceptance/common/data/negative-different-date-time-format-values.csv", numLinesToSkip = 1)
    //TODO Defect has to be raised around this area.
    //TODO Defect has to be raised for the data of 2015-12-11T09:28:30.45 and 2015-12-11T09:28:30
    public void test_negative_response_with_mandatory_location_activeTo_payload(final String locationActiveToKey,
                                                                                final String locationActiveToValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-activeTo.json");
        generatePayloadWithRandomHMCTSIDAndField(8, locationActiveToValue, "/location/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("[$.locationRequest.location.locationActiveTo: {0} is an invalid date]", locationActiveToValue);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", errorDesc, null));
    }


    @ParameterizedTest(name = "locationSecurityGuardFlag Positive Tests Scenario : {0}")
    @CsvSource(value = {"Location Security Guard Flag, true", "Location Security Guard Flag, false"})
    public void test_positive_response_for_location_security_guard_flag_with_mandatory_elements_payload(final String locationSecurityGuardFlagKey,
                                                                                                        final String locationSecurityGuardFlagValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-security-guard-flag.json");
        if (locationSecurityGuardFlagValue.trim().equals("true")) {
            generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"locationSecurityGuardFlag\": 0", "\"locationSecurityGuardFlag\":" + true, "/location/post/");
        } else {
            generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"locationSecurityGuardFlag\": 0", "\"locationSecurityGuardFlag\":" + false, "/location/post/");
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "locationSecurityGuardFlag Negative Tests Scenario : {0}")
    @CsvSource(value = {"Invalid Value Int,0", "Invalid Value Float,1.0", "Invalid Value Negative Int,-1", "Invalid Value Negative Float,-1.4"})
    public void test_negative_response_for_location_security_guard_flag_with_mandatory_elements_payload(final String locationSecurityGuardFlagKey,
                                                                                                        final String locationSecurityGuardFlagValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-security-guard-flag.json");
        SNLVerificationDTO snlVerificationDTO = null;
        switch (locationSecurityGuardFlagKey) {
            case "Invalid Value Int":
            case "Invalid Value Negative Int":
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationSecurityGuardFlag: integer found, boolean expected]", null);
                generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"locationSecurityGuardFlag\": 0", "\"locationSecurityGuardFlag\":" + Integer.parseInt(locationSecurityGuardFlagValue), "/location/post/");
                break;
            case "Invalid Value Float":
            case "Invalid Value Negative Float":
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationSecurityGuardFlag: number found, boolean expected]", null);
                generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"locationSecurityGuardFlag\": 0", "\"locationSecurityGuardFlag\":" + Float.parseFloat(locationSecurityGuardFlagValue), "/location/post/");
                break;
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                snlVerificationDTO);
    }

    @ParameterizedTest(name = "locationVideoConfFlag Positive Tests Scenario : {0}")
    @CsvSource(value = {"Location Video Config Flag,true", "Location Security Guard Flag,false"})
    public void test_positive_response_for_location_video_conf_flag_with_mandatory_elements_payload(final String locationVideoConfFlagKey,
                                                                                                    final String locationVideoConfFlagValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-video-conf-flag.json");
        if (locationVideoConfFlagValue.trim().equals("true")) {
            generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"locationVideoConfFlag\": 0", "\"locationVideoConfFlag\":" + true, "/location/post/");
        } else {
            generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"locationVideoConfFlag\": 0", "\"locationVideoConfFlag\":" + false, "/location/post/");
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "locationVideoConfFlag Negative Tests Scenario : {0}")
    @CsvSource(value = {"Invalid Value Int,0", "Invalid Value Float,1.0", "Invalid Value Negative Int,-1", "Invalid Value Negative Float,-1.4"})
    public void test_negative_response_for_location_video_conf_flag_with_mandatory_elements_payload(final String locationVideoConfFlagFlagKey,
                                                                                                    final String locationVideoConfFlagFlagValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-video-conf-flag.json");
        SNLVerificationDTO snlVerificationDTO = null;
        switch (locationVideoConfFlagFlagKey) {
            case "Invalid Value Int":
            case "Invalid Value Negative Int":
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationVideoConfFlag: integer found, boolean expected]", null);
                generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"locationVideoConfFlag\": 0", "\"locationVideoConfFlag\":" + Integer.parseInt(locationVideoConfFlagFlagValue), "/location/post/");
                break;
            case "Invalid Value Float":
            case "Invalid Value Negative Float":
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationSecurityGuardFlag: number found, boolean expected]", null);
                generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"locationVideoConfFlag\": 0", "\"locationSecurityGuardFlag\":" + Float.parseFloat(locationVideoConfFlagFlagValue), "/location/post/");
                break;
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                snlVerificationDTO);
    }

    @ParameterizedTest(name = "locationSeatingCapacity Positive Tests Scenario : {0}")
    @CsvSource(value = {"Valid Value,1", "Valid Value,2147483647"})
    public void test_positive_response_for_location_seating_capacity_mandatory_elements_payload(final String locationSeatingCapacityKey,
                                                                                                final String locationSeatingCapacityValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-seating-capacity.json");
        generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"locationSeatingCapacity\": 0", "\"locationSeatingCapacity\":" + Integer.parseInt(locationSeatingCapacityValue), "/location/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @Disabled("TODO - Disabling this tests as negative Seating capacities are not entertained as of now.")
    @ParameterizedTest(name = "locationSeatingCapacity Negative Tests Scenario : {0}")
    @CsvSource(value = {"Invalid Value,0", "Invalid Value,-2"})
    //TODO - Raise a Defect for this Negative values...
    public void test_negative_response_for_location_seating_capacity_mandatory_elements_payload(final String locationSeatingCapacityKey,
                                                                                                final String locationSeatingCapacityValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-seating-capacity.json");
        generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"locationSeatingCapacity\": 0", "\"locationSeatingCapacity\":" + Integer.parseInt(locationSeatingCapacityValue), "/location/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, null, null, null));
    }

    @ParameterizedTest(name = "locationRecordingEquivalentFlag Positive Tests Scenario : {0}")
    @CsvSource(value = {"Valid Data,true", "Valid Data,false"})
    public void test_positive_response_for_location_recording_equivalent_flag_with_mandatory_elements_payload(final String locationDataEquivalentFlagKey,
                                                                                                              final String locationDataEquivalentFlagValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-recording-equivalent-flag.json");
        if (locationDataEquivalentFlagValue.trim().equals("true")) {
            generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"locationRecordingEqFlag\": 0", "\"locationRecordingEqFlag\":" + true, "/location/post/");
        } else {
            generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"locationRecordingEqFlag\": 0", "\"locationRecordingEqFlag\":" + false, "/location/post/");
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "locationRecordingEquivalentFlag Negative Tests Scenario : {0}")
    @CsvSource(value = {"Invalid Value Int,0", "Invalid Value Float,1.0", "Invalid Value Negative Int,-1", "Invalid Value Negative Float,-1.4"})
    public void test_negative_response_for_location_equivalent_flag(final String locationRecordingEqFlagKey,
                                                                    final String locationRecordingEqFlagValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-recording-equivalent-flag.json");
        SNLVerificationDTO snlVerificationDTO = null;
        switch (locationRecordingEqFlagKey) {
            case "Invalid Value Int":
            case "Invalid Value Negative Int":
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationRecordingEqFlag: integer found, boolean expected]", null);
                generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"locationRecordingEqFlag\": 0", "\"locationRecordingEqFlag\":" + Integer.parseInt(locationRecordingEqFlagValue), "/location/post/");
                break;
            case "Invalid Value Float":
            case "Invalid Value Negative Float":
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationRecordingEqFlag: number found, boolean expected]", null);
                generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"locationRecordingEqFlag\": 0", "\"locationRecordingEqFlag\":" + Float.parseFloat(locationRecordingEqFlagValue), "/location/post/");
                break;
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                snlVerificationDTO);
    }

    @ParameterizedTest(name = "locationVCSite Positive Tests Scenario : {0}")
    @CsvSource(value = {"Valid Data,site", "Valid Data,12345", "Valid Data,£(%%()%£", "Valid Data 255,c"})
    public void test_positive_response_for_location_vc_site_with_mandatory_elements_payload(final String locationVCSiteKey,
                                                                                            final String locationVCSiteValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-vc-site.json");
        if (locationVCSiteKey.trim().equals("Valid Data 255")) {
            generatePayloadWithRandomHMCTSIDAndField(
                    generateStringForGivenLength(255, locationVCSiteValue), "/location/post/");
        }
        generatePayloadWithRandomHMCTSIDAndField(locationVCSiteValue, "/location/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @Disabled("TODO - Raise a defect as these scenarios should be failing.....")
    @ParameterizedTest(name = "locationVCSite Negative Tests Scenario : {0}")
    @CsvSource(value = {"Invalid Data,''", "Invalid Data,' '", "Invalid Data 256,c"})
    public void test_negative_response_for_location_vc_site_with_mandatory_elements_payload(final String locationVCSiteKey,
                                                                                            final String locationVCSiteValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-vc-site.json");
        if (locationVCSiteKey.trim().equals("Invalid Data 256")) {
            generatePayloadWithRandomHMCTSIDAndField(
                    generateStringForGivenLength(256, locationVCSiteValue), "/location/post/");
        } else {
            generatePayloadWithRandomHMCTSIDAndField(locationVCSiteValue, "/location/post/");
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationVCSite: may only be 255 characters long]", null));
    }

    @ParameterizedTest(name = "locationVCSiteAddress Positive Tests Scenario : {0}")
    @CsvSource(value = {"Valid Data,site", "Valid Data,12345", "Valid Data,£(%%()%£", "Valid Data 1000,c"})
    public void test_positive_response_for_location_vc_site_address_with_mandatory_elements_payload(final String locationVCSiteAddressKey,
                                                                                                    final String locationVCSiteAddressValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-vc-site-address.json");
        if (locationVCSiteAddressKey.trim().equals("Valid Data 1000")) {
            generatePayloadWithRandomHMCTSIDAndField(
                    generateStringForGivenLength(1000, locationVCSiteAddressValue), "/location/post/");
        }
        generatePayloadWithRandomHMCTSIDAndField(locationVCSiteAddressValue, "/location/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @Disabled("TODO - Raise a defect as these scenarios should be failing.....")
    @ParameterizedTest(name = "locationVCSiteAddress Negative Tests Scenario : {0}")
    @CsvSource(value = {"Invalid Data,''", "Invalid Data,' '", "Invalid Data 1001,c"})
    public void test_negative_response_for_location_vc_site_address_with_mandatory_elements_payload(final String locationVCSiteAddressKey,
                                                                                                    final String locationVCSiteAddressValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-vc-site-address.json");
        if (locationVCSiteAddressKey.trim().equals("Invalid Data 1001")) {
            generatePayloadWithRandomHMCTSIDAndField(
                    generateStringForGivenLength(1001, locationVCSiteAddressValue), "/location/post/");
        } else {
            generatePayloadWithRandomHMCTSIDAndField(locationVCSiteAddressValue, "/location/post/");
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationVCSiteAddress: may only be 1000 characters long]", null));
    }

    @ParameterizedTest(name = "locationVCNumber Positive Tests Scenario : {0}")
    @CsvSource(value = {"Valid Data,site", "Valid Data,12345", "Valid Data,£(%%()%£", "Valid Data 20,c"})
    public void test_positive_response_for_location_vc_number_with_mandatory_elements_payload(final String locationVCNumberKey,
                                                                                              final String locationVCNumberValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-vc-number.json");
        if (locationVCNumberKey.trim().equals("Valid Data 20")) {
            generatePayloadWithRandomHMCTSIDAndField(
                    generateStringForGivenLength(20, locationVCNumberValue), "/location/post/");
        } else {
            generatePayloadWithRandomHMCTSIDAndField(locationVCNumberValue, "/location/post/");
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @Disabled("TODO - Raise a defect as these scenarios should be failing.....")
    @ParameterizedTest(name = "locationVCNumber Negative Tests Scenario : {0}")
    @CsvSource(value = {"Invalid Data,''", "Invalid Data,' '", "Invalid Data 21,c"})
    public void test_negative_response_for_location_vc_number_with_mandatory_elements_payload(final String locationVCNumberKey,
                                                                                              final String locationVCNumberValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-vc-number.json");
        if (locationVCNumberKey.trim().equals("Invalid Data 20")) {
            generatePayloadWithRandomHMCTSIDAndField(
                    generateStringForGivenLength(21, locationVCNumberValue), "/location/post");
        } else {
            generatePayloadWithRandomHMCTSIDAndField(locationVCNumberValue, "/location/post/");
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationVCNumber: may only be 20 characters long]", null));
    }

    @ParameterizedTest(name = "locationVCContactPhoneNumber Positive Tests Scenario : {0}")
    @CsvSource(value = {"Valid Data,1234455679", "Valid Data,+41-0456-782194", "Valid Data,+41 0456 782194", "Valid Data,07293014536"})
    public void test_positive_response_for_location_vc_contact_phone_with_mandatory_elements_payload(final String locationVCContactPhoneKey,
                                                                                                     final String locationVCContactPhoneValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-vc-contact-phone.json");
        if (locationVCContactPhoneKey.trim().equals("Valid Data 20")) {
            generatePayloadWithRandomHMCTSIDAndField(
                    generateStringForGivenLength(20, locationVCContactPhoneValue), "/location/post/");
        } else {
            generatePayloadWithRandomHMCTSIDAndField(locationVCContactPhoneValue, "/location/post/");
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    //@Disabled("TODO - Raise a defect as these scenarios should be failing.....")
    @ParameterizedTest(name = "locationVCContactPhoneNumber Negative Tests Scenario : {0}")
    @CsvSource(value = {"Invalid Data,''", "Invalid Data,' '", "Invalid Data 21,c"})
    public void test_negative_response_for_location_vc_contact_phone_with_mandatory_elements_payload(final String locationVCContactPhoneKey,
                                                                                                     final String locationVCContactPhoneValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-vc-contact-phone.json");
        if (locationVCContactPhoneKey.trim().equals("Invalid Data 21")) {
            generatePayloadWithRandomHMCTSIDAndField(
                    generateStringForGivenLength(21, locationVCContactPhoneValue), "/location/post/");
        } else {
            generatePayloadWithRandomHMCTSIDAndField(locationVCContactPhoneValue, "/location/post/");
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationVCContactPhone: may only be 20 characters long]", null));
    }

    @ParameterizedTest(name = "locationVCEmail Positive Tests Scenario : {0}")
    @CsvSource(value = {"Valid Data,xxx@hotmail.com", "Valid Data,test@yahoo.milton-keynes.sch.uk", "Valid Data 255,c"})
    public void test_positive_response_for_location_vc_email_with_mandatory_elements_payload(final String locationVCEmailKey,
                                                                                             final String locationVCEmailValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-vc-contact-email.json");
        if (locationVCEmailKey.trim().equals("Valid Data 255")) {
            generatePayloadWithRandomHMCTSIDAndField(
                    generateStringForGivenLength(255, locationVCEmailValue), "/location/post/");
        } else {
            generatePayloadWithRandomHMCTSIDAndField(locationVCEmailValue, "/location/post/");
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "locationVCEmail Negative Tests Scenario : {0}")
    @CsvSource(value = {"Invalid Data,xxxhotmail.com", "Invalid Data,test@yahoomilton-keynesschuk", "Invalid Data 256,t"})
    public void test_negative_response_for_location_vc_email_with_mandatory_elements_payload(final String locationVCSEmailKey,
                                                                                             final String locationVCSEmailValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-vc-contact-email.json");
        if (locationVCSEmailKey.trim().equals("Invalid Data 255")) {
            generatePayloadWithRandomHMCTSIDAndField(
                    generateStringForGivenLength(256, locationVCSEmailValue), "/location/post/");
        } else {
            generatePayloadWithRandomHMCTSIDAndField(locationVCSEmailValue, "/location/post/");
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationVCEmail: may only be 255 characters long]", null));
    }

    @ParameterizedTest(name = "Schema Validation Checks for Unwanted Fields")
    @CsvSource(value = {
            "Checking Payload without an extra parameter (locationIdCaseHQ)in a mandatory payload, resource-by-location-all-mandatory-location-id-hmcts-extra-field.json",
            "Checking Payload without an extra parameter (locationIdCaseHQ)in a complete payload, resource-by-location-complete-extra-field.json"
    }, nullValues = "NIL")
    public void test_negative_response_payload_with_extra_fields(final String locationPayloadTestScenarioDescription,
                                                                  final String locationPayloadTestScenarioFileName) throws Exception {
        this.setInputPayloadFileName(locationPayloadTestScenarioFileName);
        generatePayloadWithRandomHMCTSID(8,"/location/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationIdCaseHQ: is not defined in the schema and the schema does not allow additional properties]", null));
    }

    @ParameterizedTest(name = "Mandatory Fields not available Negative Tests Scenario : {0} - {1}")
    @CsvSource(value = {
            "Checking Payload without the Location Id HMCTS, resources-by-location-mandatory-without-location-id-hmcts.json,locationIdHMCTS",
            "Checking Payload without the Location Cluster HMCTS, resources-by-location-mandatory-without-location-cluster.json,locationCluster",
            "Checking Payload without the Location Description HMCTS, resources-by-location-mandatory-without-location-description.json,locationDescription",
            "Checking Payload without the Location Active From HMCTS, resources-by-location-mandatory-without-location-active-from.json,locationActiveFrom",
            "Checking Payload without the Location Primary Flag HMCTS, resources-by-location-mandatory-without-location-primary-flag.json,locationPrimaryFlag"
    }, nullValues = "NIL")
    public void test_negative_response_mandatory_elements_payload(final String locationPayloadTestScenarioDescription,
                                                                  final String locationPayloadTestScenarioFileName,
                                                                  final String userSchemaElement) throws Exception {
        this.setInputPayloadFileName(locationPayloadTestScenarioFileName);
        generatePayloadWithRandomHMCTSID(8,"/location/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location." + userSchemaElement + ": is missing but it is required]", null));
    }
}
