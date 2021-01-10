package uk.gov.hmcts.futurehearings.snl.acceptance.resources;

import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createCompletePayloadHeader;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createStandardPayloadHeader;

import uk.gov.hmcts.futurehearings.snl.Application;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.RestClientTemplate;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.TestingUtils;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.CommonDelegate;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.dto.DelegateDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.dto.SNLVerificationDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.error.SNLCommonErrorVerifier;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.success.SNLCommonSuccessVerifier;

import java.io.IOException;
import java.util.Random;

import io.restassured.response.Response;
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
@SelectClasses(PUTResourcesByLocationPayloadValidationTest.class)
@IncludeTags("Put")
class PUTResourcesByLocationPayloadValidationTest extends ResourcesPayloadValidationTest {

    @Qualifier("CommonDelegate")
    @Autowired(required = true)
    private CommonDelegate commonDelegate;

    @Value("${resourcesByLocationRootContext}")
    private String resourcesByLocationRootContext;

    @Value("${resourcesByLocation_idRootContext}")
    private String resourcesByLocation_idRootContext;

    public String locationIdHMCTS = null;


    @BeforeAll
    public void initialiseValues() throws Exception {
        super.initialiseValues();
        this.setInputPayloadFileName("resource-by-location-all-mandatory.json");
        locationIdHMCTS = String.valueOf(makePostResourcesByLocationAndFetchLocationId());
        this.setRelativeURL(String.format(resourcesByLocation_idRootContext, locationIdHMCTS));
        this.setHttpMethod(HttpMethod.PUT);
        this.setHttpSuccessStatus(HttpStatus.NO_CONTENT);
        this.setRelativeURLForNotFound(this.getRelativeURL().replace("resources", "resource"));
        this.setSnlSuccessVerifier(new SNLCommonSuccessVerifier());
        this.setSnlErrorVerifier(new SNLCommonErrorVerifier());
        /*this.setInputBodyPayload(TestingUtils.readFileContents(String.format(INPUT_FILE_PATH, getInputFileDirectory()) +
                "/" + getInputPayloadFileName()));*/
    }

    @Test
    @DisplayName("Successfully validated response for a payload with all the mandatory required fields")
    public void test_successful_response_with_mandatory_elements_payload() throws Exception {

        this.setInputPayloadFileName("resource-by-location-all-mandatory.json");
        generatePayloadWithHMCTSID(locationIdHMCTS);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }


    /*@Test
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
        generateLocationPayloadWithRandomHMCTSIDAndField(locationClusterValue);
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
        generateLocationPayloadWithRandomHMCTSIDAndField(locationClusterValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        SNLVerificationDTO snlVerificationDTO = null;
        switch (locationClusterKey) {
            case "Invalid_Cluster_Max_Value":
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationCluster: may only be 3 characters long]", null);
                break;
            default:
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000",  "'"+locationClusterValue+"' is not a valid value for field 'locationCluster'", null);
                break;

        }
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                snlVerificationDTO);
    }

    @ParameterizedTest(name = "locationDescription Positive Tests Scenario : {0}")
    @CsvSource(value = {"Location Description,' '", "Location Description,'This is location description'"})
    public void test_positive_response_for_location_description_with_mandatory_elements_payload(final String locationDescriptionKey,
                                                                                                final String locationDescriptionValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-mandatory-description.json");
        generateLocationPayloadWithRandomHMCTSIDAndField(locationDescriptionValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    //TODO: LocationAddress accepts empty space and single space even though it is a mandatory field. Defect needs to be raised.
    @ParameterizedTest(name = "locationDescription Negative tests")
    @CsvSource(value = {"Location Description More than Max Value, C_FEFC2424-32A6-4B3A-BD97-023296C7F76DC_FEFC2424-32A6-4B3A-BD97-023296C7F76DC_FEFC2424-32A6-4B3A-BD97-023296C7F76D"}, nullValues = "NIL")
    public void test_negative_response_with_mandatory_location_description_payload(final String locationDescriptionKey, final String locationDescriptionValue) throws Exception {

        this.setInputPayloadFileName("resource-by-location-mandatory-description.json");
        generateLocationPayloadWithRandomHMCTSIDAndField(locationDescriptionValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());

        SNLVerificationDTO snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationDescription: may only be 80 characters long]", null);

        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                snlVerificationDTO);
    }

    @Disabled("needs to fix boolean formatter in json file")
    @ParameterizedTest(name = "locationPrimaryFlag Positive Tests Scenario : {0}")
    @CsvSource(value = {"Location Primary Flag, true", "Location Primary Flag, false"})
    public void test_positive_response_for_location_primary_flag_with_mandatory_elements_payload(final String locationPrimaryFlagKey,
                                                                                                 final String locationPrimaryFlagValue) throws Exception {

        this.setInputPayloadFileName("resource-by-location-mandatory-primary-flag.json");
        generateLocationPayloadWithRandomHMCTSIDAndField(locationPrimaryFlagValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @Disabled("Disabling this tests as locationPrimaryFlag in JSON doesn't accept boolean formatter")
    @ParameterizedTest(name = "locationPrimaryFlag Negative tests")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "Random_Cluster_Value,'false'", "Invalid_Cluster_Max_Value, C_FEFC2424-32A6-4B3A-BD97-023296C7F76DC_FEFC2424-32A6-4B3A-BD97-023296C7F76DC_FEFC2424-32A6-4B3A-BD97-023296C7F76D"}, nullValues = "NIL")
    public void test_negative_response_with_mandatory_location_primary_flag_payload(final String locationPrimaryFlagKey, final String locationPrimaryFlagValue) throws Exception {

        this.setInputPayloadFileName("resource-by-location-mandatory-primary-flag.json");
        generateLocationPayloadWithRandomHMCTSIDAndField(locationPrimaryFlagValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        SNLVerificationDTO snlVerificationDTO = null;
        switch (locationPrimaryFlagValue) {
            case "":
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000",  "'' is not a valid value for field 'locationCluster'", null);
                break;
            case " ":
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000",  "' ' is not a valid value for field 'locationCluster'", null);
                break;
            case "RGB":
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000",  "'RGB' is not a valid value for field 'locationCluster'", null);
                break;
            default:
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationCluster: may only be 3 characters long]", null);
                break;

        }
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                snlVerificationDTO);
    }

    @Disabled("Disabling this tests as locationActiveFrom is throwing exceptions from McGirr with an HTTPStatus Error of 500 for invalid date format.")
    @ParameterizedTest(name = "locationActiveFrom Negative tests")
    @CsvSource({"Null_Value, null", "Empty_Space,\" \"", "Invalid_Value, value",
            "Invalid_Date_Format, 2002-02-31T10:00:30-05:00Z",
            "Invalid_Date_Format, 2002-02-31T1000:30-05:00",
            "Invalid_Date_Format, 2002-02-31T10:00-30-05:00",
            "Invalid_Date_Format, 2002-10-02T15:00:00*05Z",
            "Invalid_Date_Format, 2002-10-02 15:00?0005Z",
            "Invalid_Date_Format, 2002-10-02T15:00:00",
    })
    //TODO Defect has to be raised around this area.
    public void test_negative_response_with_mandatory_location_activeFrom_payload(final String locationActiveFromKey, final String locationActiveFromValue) throws Exception {

        this.setInputPayloadFileName("resource-by-location-mandatory-activeFrom.json");
        generateLocationPayloadWithRandomHMCTSIDAndField(locationActiveFromValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        SNLVerificationDTO snlVerificationDTO = null;
        switch (locationActiveFromValue) {
            case "":
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000",  "'' is not a valid value for field 'locationActiveFrom'", null);
                break;
            case " ":
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000",  "' ' is not a valid value for field 'locationActiveFrom'", null);
                break;
            case "RGB":
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000",  "'RGB' is not a valid value for field 'locationActiveFrom'", null);
                break;
            default:
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationActiveFrom: may only be 3 characters long]", null);
                break;

        }
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                snlVerificationDTO);
    }

    @ParameterizedTest(name = "locationActiveFrom Positive Tests Scenario : {0}")
    @CsvSource(value = {"Location Active From, '2020-01-01T20:20:39+00:00'"})
    public void test_positive_response_for_location_active_from_with_mandatory_elements_payload(final String locationActiveFromKey,
                                                                                                 final String locationActiveFromValue) throws Exception {

        this.setInputPayloadFileName("resource-by-location-mandatory-activeFrom.json");
        generateLocationPayloadWithRandomHMCTSIDAndField(locationActiveFromValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }*/

    /*private void generateLocationPayloadWithRandomLocationIdHMCTS() throws IOException {
        final int randomId = new Random().nextInt(99999999);
        generateLocationPayloadWithRandomLocationIdHMCTS(String.valueOf(randomId));
    }

    private void generateLocationPayloadWithRandomLocationIdHMCTS(final String randomID) throws IOException {
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + "/" + getInputPayloadFileName()), randomID));
    }

    private void generateLocationPayloadWithRandomHMCTSIDAndField(final String formatValue) throws IOException {
        final int randomId = new Random().nextInt(99999999);
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + "/" + getInputPayloadFileName()), randomId, formatValue));
    }*/

    private Integer makePostResourcesByLocationAndFetchLocationId() throws Exception {
        int randomId = new Random().nextInt(99999999);
        DelegateDTO delegateDTO = DelegateDTO.builder()
                .targetSubscriptionKey(getApiSubscriptionKey()).authorizationToken(getAuthorizationToken())
                .targetURL(resourcesByLocationRootContext)
                .inputPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH, getInputFileDirectory()) +
                        "/" + getInputPayloadFileName()), randomId))
                .standardHeaderMap(createCompletePayloadHeader(getApiSubscriptionKey()))
                .headers(null)
                .params(getUrlParams())
                .httpMethod(HttpMethod.POST)
                .status(HttpStatus.CREATED)
                .build();
        Response response = RestClientTemplate.shouldExecute(TestingUtils.convertHeaderMapToRestAssuredHeaders(delegateDTO.standardHeaderMap()),
                delegateDTO.authorizationToken(),
                delegateDTO.inputPayload(), delegateDTO.targetURL(),
                delegateDTO.params(), delegateDTO.status(), delegateDTO.httpMethod());
        log.debug("POST Response : " + response.getBody().asString());
        return randomId;
    }
}
