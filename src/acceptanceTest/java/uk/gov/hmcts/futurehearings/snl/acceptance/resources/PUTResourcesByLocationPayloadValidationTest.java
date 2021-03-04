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
import java.util.UUID;

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
        this.setInputPayloadFileName("resource-by-location-all-mandatory-location-id-hmcts.json");
        locationIdHMCTS = String.valueOf(makePostResourcesByLocationAndFetchLocationId());
        this.setRelativeURL(String.format(resourcesByLocation_idRootContext, locationIdHMCTS));
        this.setHttpMethod(HttpMethod.PUT);
        this.setHttpSuccessStatus(HttpStatus.NO_CONTENT);
        this.setRelativeURLForNotFound(this.getRelativeURL().replace("resources", "resource"));
        this.setSnlSuccessVerifier(new SNLCommonSuccessVerifier());
        this.setSnlErrorVerifier(new SNLCommonErrorVerifier());
    }

    @Test
    @DisplayName("Successfully validated response for a payload with all the mandatory required fields")
    public void test_successful_response_with_mandatory_elements_payload() throws Exception {

        this.setInputPayloadFileName("resource-by-location-complete.json");
        generatePayloadWithHMCTSID(locationIdHMCTS, "/location/put/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "Update Positive Tests for Singular Fields : {0} - {1}")
    @CsvSource(value = {
            "locationDescription,x HMI Test - Updated",
            "locationCluster,KNT",
            "locationCluster,TV",
            "locationPostCode,SW7 1AB",
            "locationParentCode,301",
            "locationActiveFrom,2021-01-01",
            "locationActiveTo,2021-01-31",
            "locationVCSite,site - Updated",
            "locationVCSiteAddress,Site Address - Updated",
            "locationVCNumber,34",
            "locationVCContactPhone,Phone - Updated",
            "locationVCEmail,email@hotmail.com"
    }, nullValues = "NIL")
    public void test_positive_response_for_general_updated_payload(final String locationTemplateKey,
                                                                   final String locationTemplateValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-general-template.json");
        generatePayloadWithHMCTSIDAndField(locationTemplateKey, locationTemplateValue, "/location/put/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @Disabled("Disabling this tests as in general itself we will raise a defect as Blank, Empties and Null should not be accepted by the System")
    @ParameterizedTest(name = "Update Negative Tests for Singular Fields : {0} - {1}")
    @CsvSource(value = {
            "locationIdHMCTS,''", "locationIdHMCTS,' '", "locationIdHMCTS,C_FEFC242", //TODO - Defect to be raised for the blank Value, not to be updated.
            "locationCluster,''", "locationCluster,' '", "locationCluster,Z", "locationCluster,BR", "locationCluster,RGB", "locationCluster,C_FE",
            "locationDescription,''", "locationDescription,' '", "locationDescription,'CCCvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvbbCCCvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvccMMMBVBGTFHK'",
            "locationActiveFrom,''", "locationActiveFrom,' '", "locationActiveFrom,'CCCvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvbbCCCvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvccMMMBVBGTFHK'",
            "locationIdCaseHQ,'TestVal'" //Test to update a Non Existent Field
    }, nullValues = "NIL")
    public void test_negative_response_for_general_updated_payload(final String locationTemplateKey,
                                                                   final String locationTemplateValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-general-template.json");
        generatePayloadWithHMCTSIDAndField(locationTemplateKey, locationTemplateValue, "/location/put/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        SNLVerificationDTO snlVerificationDTO = null;
        switch (locationTemplateKey) {
            case "locationIdHMCTS":
                switch (locationTemplateValue) {
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
                break;
            case "locationCluster":
                switch (locationTemplateValue) {
                    case "C_FE":
                        snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location." + locationTemplateKey + ": may only be 3 characters long]", null);
                        break;
                    default:
                        snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", "'" + locationTemplateValue + "' is not a valid value for field 'locationCluster'", null);
                        break;
                }
                break;
            case "locationDescription":
                switch (locationTemplateValue) {
                    default:
                        snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000",
                                "'" + locationTemplateValue + "' is not a valid value for field 'locationDescription'",
                                null);
                        break;
                }
            case "locationActiveFrom":
                switch (locationTemplateValue) {
                    default:
                        snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000",
                                "'" + locationTemplateValue + "' is not a valid value for field 'locationActiveFromr'",
                                null);
                        break;
                }
                break;
            default:
                break;
        }
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                snlVerificationDTO);
    }

    @ParameterizedTest(name = "Update Tests for Singular Fields : {0} - {1}")
    @CsvSource(value = {
            "locationPrimaryFlag, true",
            "locationSecurityGuardFlag, false",
            "locationVideoConfFlag, true",
            "locationRecordingEqFlag, false",
            "locationSeatingCapacity, 53",
    }, nullValues = "NIL")
    public void test_positive_response_for_non_string_updated_payload(final String locationTemplateKey,
                                                                      final String locationTemplateValue) throws Exception {
        this.setInputPayloadFileName("resource-by-location-nonstring-based-template.json");
        if (locationTemplateKey.trim().equals("locationSeatingCapacity")) {
            generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"key\"" + " : 0", "\"" + locationTemplateKey + "\"" + ":" + Integer.parseInt(locationTemplateValue), "/location/put/");
        } else {
            generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"key\"" + " : 0", "\"" + locationTemplateKey + "\"" + ":" + Boolean.parseBoolean(locationTemplateValue), "/location/put/");
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    //TODO - The negative tests for the locationPrimaryFlag is to be tested manually as this can't be worked to get invalid date to
    private Integer makePostResourcesByLocationAndFetchLocationId() throws Exception {
        int randomId = new Random().nextInt(99999999);
        DelegateDTO delegateDTO = DelegateDTO.builder()
                .targetSubscriptionKey(getApiSubscriptionKey()).authorizationToken(getAuthorizationToken())
                .targetURL(resourcesByLocationRootContext)
                .inputPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH, getInputFileDirectory()) +
                        "/location/post/" + getInputPayloadFileName()), randomId))
                .standardHeaderMap(createCompletePayloadHeader())
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
