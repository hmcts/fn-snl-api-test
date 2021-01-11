package uk.gov.hmcts.futurehearings.snl.acceptance.resources;

import static uk.gov.hmcts.futurehearings.snl.acceptance.common.TestingUtils.generateStringForGivenLength;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.TestingUtils.replaceCharacterSequence;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createStandardPayloadHeader;

import uk.gov.hmcts.futurehearings.snl.Application;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.TestingUtils;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.CommonDelegate;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.dto.DelegateDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.dto.SNLVerificationDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.error.SNLCommonErrorVerifier;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.success.SNLCommonSuccessVerifier;

import java.io.IOException;
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
    @DisplayName("Successfully validated response for a payload with all the mandatory required fields")
    public void test_successful_response_with_mandatory_elements_payload() throws Exception {

        this.setInputPayloadFileName("resource-by-location-all-mandatory.json");
        generateLocationPayloadWithRandomLocationIdHMCTS();
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @Test
    @DisplayName("Successfully validated response for a payload including optional fields")
    public void test_successful_response_with_optional_elements_payload() throws Exception {

        this.setInputPayloadFileName("resource-by-location-complete.json");
        generateLocationPayloadWithRandomLocationIdHMCTS();
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "LocationIdHMCTS Negative tests")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "Invalid Location id, C_FEFC2424-32A6-4B3A-BD97-023296C7F76DC_FEFC2424-32A6-4B3A-BD97-023296C7F76DC_FEFC2424-32A6-4B3A-BD97-023296C7F76D"}, nullValues = "NIL")
    //TODO - LocationIdHMCTS Empty values should not be ingested in the System - Data - "Single Space,' '"
    public void test_negative_response_with_mandatory_locationId_payload(final String locationIdHMCTSKey, final String locationIdHMCTSValue) throws Exception {

        this.setInputPayloadFileName("resource-by-location-all-mandatory.json");
        generateLocationPayloadWithRandomLocationIdHMCTS(locationIdHMCTSValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        SNLVerificationDTO snlVerificationDTO = null;
        switch (locationIdHMCTSValue) {
            case "":
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationIdHMCTS: must be at least 1 characters long]", null);
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
        generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(locationClusterValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    //TODO -HMCTSQA to add positive and negative tests for the Description.
    @ParameterizedTest(name = "locationClusterValue Negative tests")
    @CsvSource(value = {"Empty Space,''",
            "Single Space,' '",
            "Random_Cluster_Value,'Z'",
            "Random_Cluster_Value,'BR'",
            "Random_Cluster_Value,'RGB'",
            "Invalid_Cluster_Max_Value, C_FEFC2424"}, nullValues = "NIL")
    public void test_negative_response_with_mandatory_location_cluster_payload(final String locationClusterKey, final String locationClusterValue) throws Exception {

        this.setInputPayloadFileName("resource-by-location-mandatory-cluster.json");
        generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(locationClusterValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
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

    @ParameterizedTest(name = "locationDescription Positive Tests Scenario : {0}")
    @CsvSource(value = {"Location Description,' '", "Location Description,'This is location description'"})
    //TODO - LocationDescription should not accept empty and blank values.
    public void test_positive_response_for_location_description_with_mandatory_elements_payload(final String locationDescriptionKey,
                                                                                                final String locationDescriptionValue) throws Exception {

        this.setInputPayloadFileName("resource-by-location-mandatory-description.json");
        generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(locationDescriptionValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    //TODO: LocationDescription accepts empty space and single space eventhough it is a mandatory field. Defect needs to be raised.
    @ParameterizedTest(name = "locationDescription Negative tests")
    @CsvSource(value = {"Location Description More than Max Value, C_FEFC2424-32A6-4B3A-BD97-023296C7F76DC_FEFC2424-32A6-4B3A-BD97-023296C7F76DC_FEFC2424-32A6-4B3A-BD97-023296C7F76D"}, nullValues = "NIL")
    public void test_negative_response_with_mandatory_location_description_payload(final String locationDescriptionKey, final String locationDescriptionValue) throws Exception {

        this.setInputPayloadFileName("resource-by-location-mandatory-description.json");
        generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(locationDescriptionValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());

        SNLVerificationDTO snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationDescription: may only be 80 characters long]", null);

        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                snlVerificationDTO);
    }

    @ParameterizedTest(name = "locationPrimaryFlag Positive Tests Scenario : {0}")
    @CsvSource(value = {"Location Primary Flag, true", "Location Primary Flag, false"})
    //TODO - Negative testing of this scenario has to be done manually as Rest Assured is failing on the client side for incompatible datatypes against no string fields....
    public void test_positive_response_for_location_primary_flag_with_mandatory_elements_payload(final String locationPrimaryFlagKey,
                                                                                                 final String locationPrimaryFlagValue) throws Exception {

        this.setInputPayloadFileName("resource-by-location-mandatory-primary-flag.json");
        if (locationPrimaryFlagValue.trim().equals("true")) {
            generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"locationPrimaryFlag\": 0", "\"locationPrimaryFlag\":" + true);
        } else {
            generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"locationPrimaryFlag\": 0", "\"locationPrimaryFlag\":" + false);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @Disabled("TODO - Review this Test post the confirmation of date formats for the payloads")
    @ParameterizedTest(name = "locationActiveFrom Negative tests")
    @CsvSource({"Valid_Date_Format,2002-02-15T10:00:30:05+01:25",
            "Valid_Date_Format, 2002-02-01T10:00:30-05:00",
            "Valid_Date_Format, 2002-02-26T10:00:30.123Z"})
    public void test_positive_response_with_mandatory_location_activeFrom_payload(final String locationActiveFromKey, final String locationActiveFromValue) throws Exception {

        this.setInputPayloadFileName("resource-by-location-mandatory-activeFrom.json");
        generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(locationActiveFromValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @Disabled("TODO- Manually test this scenario and see if these issues are happening in Postman")
    @ParameterizedTest(name = "locationActiveFrom Negative tests")
    @CsvSource(value = {"Null_Value,NIL", "Empty_Space, ", "Invalid_Value, value",
            "Invalid_Date_Format, 2002-02-31T1000:30-05:00",
            "Invalid_Date_Format, 2002-02-31T10:00-30-05:00",
            "Invalid_Date_Format, 2002-10-02T15:00:00*05Z",
            "Invalid_Date_Format, 2002-10-02 15:00?0005Z",
            "Invalid_Date_Format, 2002-10-02T15:00:00",
            "Invalid_Date_Format,2002-02-31T10:00:30:05-01:75"
    }, nullValues = "NIL")
    //TODO Defect has to be raised around this area.
    public void test_negative_response_with_mandatory_location_activeFrom_payload(final String locationActiveFromKey, final String locationActiveFromValue) throws Exception {

        this.setInputPayloadFileName("resource-by-location-mandatory-activeFrom.json");
        generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(locationActiveFromValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        SNLVerificationDTO snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000",
                "'" + locationActiveFromValue + "' is not a valid value for field 'locationActiveFromr'",
                null);

        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                snlVerificationDTO);
    }

    @ParameterizedTest(name = "locationPostCode Positive Tests Scenario : {0}")
    @CsvSource(value = {"Valid Location PostCode,'HA2 0NB'", "Valid Location PostCode,'SW7'", "Valid Location PostCode,'SW78LU'"})
    public void test_positive_response_for_location_postcode_with_optional_elements_payload(final String locationPostCodeKey,
                                                                                            final String locationPostCodeValue) throws Exception {

        this.setInputPayloadFileName("resource-by-location-optional-postcode.json");
        generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(locationPostCodeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    //TODO: PostCode accepts empty space, single space and even doesn't validate postcode.
    // Any random postcode like HHKK7788WW is acceptable
    @ParameterizedTest(name = "locationPostCode Negative tests")
    @CsvSource(value = {"Invalid_PostCode_Max_Value, C_FEFC2424-32A6-4B3A-BD97-023296C7F76DC_FEFC2424"}, nullValues = "NIL")
    public void test_negative_response_with_optional_location_postcode_payload(final String locationPostCodeKey, final String locationPostCodeValue) throws Exception {

        this.setInputPayloadFileName("resource-by-location-optional-postcode.json");
        generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(locationPostCodeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        SNLVerificationDTO snlVerificationDTO = null;
        switch (locationPostCodeKey) {
            case "Invalid_PostCode_Max_Value":
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationPostCode: may only be 15 characters long]", null);
                break;
            default:
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", "'" + locationPostCodeValue + "' is not a valid value for field 'locationPostCode'", null);
                break;

        }
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                snlVerificationDTO);
    }

    @Disabled("TODO - Review this Test post the confirmation of date formats for the payloads")
    @ParameterizedTest(name = "locationActiveTo Negative tests")
    @CsvSource({"Valid_Date_Format,2002-02-15T10:00:30:05+01:25",
            "Valid_Date_Format, 2002-02-01T10:00:30-05:00",
            "Valid_Date_Format, 2002-02-26T10:00:30.123Z"})
    public void test_positive_response_with_mandatory_location_activeTo_payload(final String locationActiveFromKey, final String locationActiveFromValue) throws Exception {

        this.setInputPayloadFileName("resource-by-location-optional-activeTo.json");
        generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(locationActiveFromValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @Disabled("Disabling this tests as locationActiveTo is throwing exceptions from McGirr with an HTTPStatus Error of 500 for invalid date format.")
    @ParameterizedTest(name = "locationActiveTo Negative tests")
    @CsvSource(value = {"Null_Value,NIL", "Empty_Space, ", "Invalid_Value, value",
            "Invalid_Date_Format, 2002-02-31T1000:30-05:00",
            "Invalid_Date_Format, 2002-02-31T10:00-30-05:00",
            "Invalid_Date_Format, 2002-10-02T15:00:00*05Z",
            "Invalid_Date_Format, 2002-10-02 15:00?0005Z",
            "Invalid_Date_Format, 2002-10-02T15:00:00",
            "Invalid_Date_Format,2002-02-31T10:00:30:05-01:75"
    }, nullValues = "NIL")
    //TODO Defect has to be raised around this area.
    public void test_negative_response_with_mandatory_location_activeTo_payload(final String locationActiveFromKey, final String locationActiveFromValue) throws Exception {

        this.setInputPayloadFileName("resource-by-location-optional-activeTo.json");
        generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(locationActiveFromValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        SNLVerificationDTO snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000",
                "'" + locationActiveFromValue + "' is not a valid value for field 'locationActiveFromr'",
                null);

        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                snlVerificationDTO);
    }

    @ParameterizedTest(name = "locationSecurityGuardFlag Positive Tests Scenario : {0}")
    @CsvSource(value = {"Location Security Guard Flag, true", "Location Security Guard Flag, false"})
    public void test_positive_response_for_location_security_guard_flag_with_mandatory_elements_payload(final String locationSecurityGuardFlagKey,
                                                                                                        final String locationSecurityGuardFlagValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-security-guard-flag.json");
        if (locationSecurityGuardFlagValue.trim().equals("true")) {
            generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"locationSecurityGuardFlag\": 0", "\"locationSecurityGuardFlag\":" + true);
        } else {
            generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"locationSecurityGuardFlag\": 0", "\"locationSecurityGuardFlag\":" + false);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "locationSecurityGuardFlag Positive Tests Scenario : {0}")
    @CsvSource(value = {"Location Security Guard Flag,true", "Location Security Guard Flag,false"})
    public void test_positive_response_for_location_video_conf_flag_with_mandatory_elements_payload(final String locationVideoConfFlagKey,
                                                                                                    final String locationVideoConfFlagValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-video-conf-flag.json");
        if (locationVideoConfFlagValue.trim().equals("true")) {
            generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"locationVideoConfFlag\": 0", "\"locationVideoConfFlag\":" + true);
        } else {
            generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"locationVideoConfFlag\": 0", "\"locationVideoConfFlag\":" + false);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "locationSeatingCapacity Positive Tests Scenario : {0}")
    @CsvSource(value = {"Valid Value,1", "Valid Value,2147483647"})
    public void test_positive_response_for_location_seating_capacity_mandatory_elements_payload(final String locationSeatingCapacityKey,
                                                                                                final String locationSeatingCapacityValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-seating-capacity.json");
        generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"locationSeatingCapacity\": 0", "\"locationSeatingCapacity\":" + Integer.parseInt(locationSeatingCapacityValue));
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @Disabled("TODO - Disabling this tests as negative Seating capacities are not entertained as of now.")
    @ParameterizedTest(name = "locationSeatingCapacity Positive Tests Scenario : {0}")
    @CsvSource(value = {"Invalid Value,0", "Invalid Value,-2"})
    //TODO - Raise a Defect for this Negative values...
    public void test_negative_response_for_location_seating_capacity_mandatory_elements_payload(final String locationSeatingCapacityKey,
                                                                                                final String locationSeatingCapacityValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-seating-capacity.json");
        generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"locationSeatingCapacity\": 0", "\"locationSeatingCapacity\":" + Integer.parseInt(locationSeatingCapacityValue));
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, null, null, null));
    }

    @ParameterizedTest(name = "locationDataEquivalentFlag Positive Tests Scenario : {0}")
    @CsvSource(value = {"Valid Data,true", "Valid Data,false"})
    public void test_positive_response_for_location_recording_equivalent_flag_with_mandatory_elements_payload(final String locationDataEquivalentFlagKey,
                                                                                                              final String locationDataEquivalentFlagValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-recording-equivalent-flag.json");
        if (locationDataEquivalentFlagValue.trim().equals("true")) {
            generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"locationRecordingEqFlag\": 0", "\"locationRecordingEqFlag\":" + true);
        } else {
            generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"locationRecordingEqFlag\": 0", "\"locationRecordingEqFlag\":" + false);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "locationVCSite Positive Tests Scenario : {0}")
    @CsvSource(value = {"Valid Data,site", "Valid Data,12345", "Valid Data,£(%%()%£", "Valid Data 255,c"})
    public void test_positive_response_for_location_vc_site_with_mandatory_elements_payload(final String locationVCSiteKey,
                                                                                            final String locationVCSiteValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-vc-site.json");
        if (locationVCSiteKey.trim().equals("Valid Data 255")) {
            generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(
                    generateStringForGivenLength(255, locationVCSiteValue));
        }
        generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(locationVCSiteValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @Disabled("TODO - Raise a defect as these scenarios should be failing.....")
    @ParameterizedTest(name = "locationVCSite Positive Tests Scenario : {0}")
    @CsvSource(value = {"Invalid Data,''", "Invalid Data,' '", "Invalid Data 255,c"})
    public void test_negative_response_for_location_vc_site_with_mandatory_elements_payload(final String locationVCSiteKey,
                                                                                            final String locationVCSiteValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-vc-site.json");
        if (locationVCSiteKey.trim().equals("Invalid Data 255")) {
            generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(
                    generateStringForGivenLength(255, locationVCSiteValue) + locationVCSiteValue);
        } else {
            generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(locationVCSiteValue);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationVCSite: may only be 255 characters long]", null));
    }

    @ParameterizedTest(name = "locationVCSiteAddress Positive Tests Scenario : {0}")
    @CsvSource(value = {"Valid Data,site", "Valid Data,12345", "Valid Data,£(%%()%£", "Valid Data 1000,c"})
    public void test_positive_response_for_location_vc_site_address_with_mandatory_elements_payload(final String locationVCSiteKey,
                                                                                                    final String locationVCSiteValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-vc-site-address.json");
        if (locationVCSiteKey.trim().equals("Valid Data 100")) {
            generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(
                    generateStringForGivenLength(1000, locationVCSiteValue));
        }
        generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(locationVCSiteValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @Disabled("TODO - Raise a defect as these scenarios should be failing.....")
    @ParameterizedTest(name = "locationVCSiteAddress Positive Tests Scenario : {0}")
    @CsvSource(value = {"Invalid Data,''", "Invalid Data,' '", "Invalid Data 1000,c"})
    public void test_negative_response_for_location_vc_site_address_with_mandatory_elements_payload(final String locationVCSiteAddressKey,
                                                                                                    final String locationVCSiteAddressValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-vc-site-address.json");
        if (locationVCSiteAddressKey.trim().equals("Invalid Data 1000")) {
            generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(
                    generateStringForGivenLength(1000, locationVCSiteAddressValue) + locationVCSiteAddressValue);
        } else {
            generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(locationVCSiteAddressValue);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationVCSiteAddress: may only be 1000 characters long]", null));
    }

    @ParameterizedTest(name = "locationVCSiteAddress Positive Tests Scenario : {0}")
    @CsvSource(value = {"Valid Data,site", "Valid Data,12345", "Valid Data,£(%%()%£", "Valid Data 20,c"})
    public void test_positive_response_for_location_vc_number_with_mandatory_elements_payload(final String locationVCSiteKey,
                                                                                              final String locationVCSiteValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-vc-number.json");
        if (locationVCSiteKey.trim().equals("Valid Data 20")) {
            generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(
                    generateStringForGivenLength(20, locationVCSiteValue));
        } else {
            generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(locationVCSiteValue);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @Disabled("TODO - Raise a defect as these scenarios should be failing.....")
    @ParameterizedTest(name = "locationVCSiteAddress Positive Tests Scenario : {0}")
    @CsvSource(value = {"Invalid Data,''", "Invalid Data,' '", "Invalid Data 20,c"})
    public void test_negative_response_for_location_vc_number_with_mandatory_elements_payload(final String locationVCSiteAddressKey,
                                                                                              final String locationVCSiteAddressValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-vc-number.json");
        if (locationVCSiteAddressKey.trim().equals("Invalid Data 20")) {
            generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(
                    generateStringForGivenLength(20, locationVCSiteAddressValue) + locationVCSiteAddressValue);
        } else {
            generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(locationVCSiteAddressValue);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationVCNumber: may only be 20 characters long]", null));
    }

    @ParameterizedTest(name = "locationVCSiteAddress Positive Tests Scenario : {0}")
    @CsvSource(value = {"Valid Data,1234455679", "Valid Data,+41-0456-782194", "Valid Data,+41 0456 782194", "Valid Data,07293014536"})
    public void test_positive_response_for_location_vc_contact_phone_with_mandatory_elements_payload(final String locationVCSiteKey,
                                                                                                     final String locationVCSiteValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-vc-number.json");
        if (locationVCSiteKey.trim().equals("Valid Data 20")) {
            generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(
                    generateStringForGivenLength(20, locationVCSiteValue));
        } else {
            generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(locationVCSiteValue);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    //@Disabled("TODO - Raise a defect as these scenarios should be failing.....")
    @ParameterizedTest(name = "locationVCContactPhoneNumber Positive Tests Scenario : {0}")
    @CsvSource(value = {"Invalid Data,''", "Invalid Data,' '", "Invalid Data 20,c"})
    public void test_negative_response_for_location_vc_contact_phone_with_mandatory_elements_payload(final String locationVCSiteAddressKey,
                                                                                                     final String locationVCSiteAddressValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-vc-contact-phone.json");
        if (locationVCSiteAddressKey.trim().equals("Invalid Data 20")) {
            generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(
                    generateStringForGivenLength(20, locationVCSiteAddressValue) + locationVCSiteAddressValue);
        } else {
            generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(locationVCSiteAddressValue);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
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
            generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(
                    generateStringForGivenLength(255, locationVCEmailValue));
        } else {
            generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(locationVCEmailValue);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "locationVCEmail Positive Tests Scenario : {0}")
    @CsvSource(value = {"Invalid Data,xxxhotmail.com", "Invalid Data,test@yahoomilton-keynesschuk","Invalid Data 255,t"})
    public void test_negative_response_for_location_vc_email_with_mandatory_elements_payload(final String locationVCSEmailKey,
                                                                                             final String locationVCSEmailValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-optional-location-vc-contact-email.json");
        if (locationVCSEmailKey.trim().equals("Invalid Data 255")) {
            generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(
                    generateStringForGivenLength(255, locationVCSEmailValue) + locationVCSEmailValue);
        } else {
            generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(locationVCSEmailValue);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationVCEmail: may only be 255 characters long]", null));
    }

    @ParameterizedTest(name = "Mandatory Fields not available Negative Tests Scenario : {0} - {1}")
    @CsvSource(value = {
            "Checking Payload without the Location Id HMCTS, resources-by-location-mandatory-without-location-id-hmcts.json",
            "Checking Payload without the Location Cluster HMCTS, resources-by-location-mandatory-without-location-cluster.json",
            "Checking Payload without the Location Description HMCTS, resources-by-location-mandatory-without-location-description.json",
            "Checking Payload without the Location Active From HMCTS, resources-by-location-mandatory-without-location-active-from.json",
            "Checking Payload without the Location Primary Flag HMCTS, resources-by-location-mandatory-without-location-primary-flag.json"
    }, nullValues = "NIL")
    public void test_negative_response_mandatory_elements_payload(final String locationPayloadTestScenarioDescription,
                                                                  final String locationPayloadTestScenarioFileName) throws Exception {
        this.setInputPayloadFileName(locationPayloadTestScenarioFileName);
        generatePayloadWithRandomHMCTSID();
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, null, null, null));
    }

    private void generateLocationPayloadWithRandomLocationIdHMCTS() throws IOException {
        final int randomId = new Random().nextInt(99999999);
        generateLocationPayloadWithRandomLocationIdHMCTS(String.valueOf(randomId));
    }

    private void generateLocationPayloadWithRandomLocationIdHMCTS(final String randomID) throws IOException {
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + "/" + getInputPayloadFileName()), randomID));
    }

    private void generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(final String formatValue) throws IOException {
        final int randomId = new Random().nextInt(99999999);
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + "/" + getInputPayloadFileName()), randomId, formatValue));
    }

    private void generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace(final String token, final String value) throws IOException {
        final int randomId = new Random().nextInt(99999999);
        String formattedString = String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + "/" + getInputPayloadFileName()), randomId, value);
        this.setInputBodyPayload(replaceCharacterSequence(token, value, formattedString));
    }

}
