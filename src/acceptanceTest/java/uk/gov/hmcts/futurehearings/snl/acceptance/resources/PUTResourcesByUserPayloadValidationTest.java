package uk.gov.hmcts.futurehearings.snl.acceptance.resources;

import static uk.gov.hmcts.futurehearings.snl.acceptance.common.TestingUtils.generateStringForGivenLength;
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
@SelectClasses(POSTResourcesByUserHeaderValidationTest.class)
@IncludeTags("Put")
public class PUTResourcesByUserPayloadValidationTest extends ResourcesPayloadValidationTest {

    @Qualifier("CommonDelegate")
    @Autowired(required = true)
    private CommonDelegate commonDelegate;

    @Value("${resourcesByUserRootContext}")
    private String resourcesByUserRootContext;

    @Value("${resourcesByUser_idRootContext}")
    private String resourcesByUser_idRootContext;

    public String personIdHMCTS = null;

    @BeforeAll
    public void initialiseValues() throws Exception {
        super.initialiseValues();
        this.setHttpMethod(HttpMethod.PUT);
        this.setInputPayloadFileName("resources-by-user-mandatory-ui-based.json");
        personIdHMCTS = makePostResourcesByUserAndFetchUserId();
        this.setRelativeURL(String.format(resourcesByUser_idRootContext, personIdHMCTS));
        this.setHttpSuccessStatus(HttpStatus.NO_CONTENT);
        this.setRelativeURLForNotFound(this.getRelativeURL().replace("resources", "resource"));
        this.setSnlSuccessVerifier(new SNLCommonSuccessVerifier());
        this.setSnlErrorVerifier(new SNLCommonErrorVerifier());
    }

    @Test
    @DisplayName("Successfully validated response for a payload with all the mandatory required fields")
    public void test_successful_response_with_mandatory_elements_payload() throws Exception {

        this.setInputPayloadFileName("resources-by-user-complete.json");
        generatePayloadWithHMCTSID(personIdHMCTS, "/user/put/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "Update Positive Tests for Singular Fields : {0} - {1}")
    @CsvSource(value = {
            "personFirstName,x – HMI Test - Updated",
            "personLastName,x – HMI Test - Updated",
            "personRegistry,KNT",
            "personSalutation,1234",
            "personContactEmail,snlqa-updated@test.com",
            "personRoleId,130",
            "personVenueId,301",
            "personActiveDate,1999-10-02",
            "personInactiveDate,2000-12-19"
    }, nullValues = "NIL")
    public void test_positive_response_for_general_updated_payload(final String locationTemplateKey,
                                                                   final String locationTemplateValue) throws Exception {
        this.setInputPayloadFileName("resources-by-user-general-template.json");
        generatePayloadWithHMCTSIDAndField(locationTemplateKey, locationTemplateValue, "/user/put/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @Disabled("Disabling this tests as in general itself we will raise a defect as Blank, Empties and Null should not be accepted by the System")
    @ParameterizedTest(name = "Update Negative Tests for Singular Fields : {0} - {1}")
    @CsvSource(value = {
            "personIdHMCTS,''", "personIdHMCTS,' '", "personIdHMCTS,C", //TODO - Defect to be raised for the blank Value, not to be updated.
            "personFirstName,''", "personFirstName,' '", "personFirstName,C",
            "personLastName,''", "personLastName,' '", "personLastName,C",
            "personRegistry,''", "personRegistry,' '", "personRegistry,Z", "personRegistry,BR", "personRegistry,RGB", "personRegistry,C_FE",
            "personContactEmail,''", "personContactEmail,' '", "personContactEmail,'xxxtest.com'","personContactEmail,'x'",
            "personActiveDate,''", "personActiveDate,' '", "personActiveDate,'13-11-1988'","personActiveDate,'13-NOV-1988'","personActiveDate,'1988-02-31'"
    }, nullValues = "NIL")
    public void test_negative_response_for_general_updated_payload(final String locationTemplateKey,
                                                                   String locationTemplateValue) throws Exception {
        SNLVerificationDTO snlVerificationDTO = null;
        switch (locationTemplateKey) {
            case "personIdHMCTS":
                switch (locationTemplateValue) {
                    case "":
                        snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationIdHMCTS: must be at least 1 characters long]", null);
                        break;
                    case " ":
                        snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1001", "A Location resource with 'locationIdHMCTS' = ' ' already exists", null);
                        break;
                    default:
                        locationTemplateValue = generateStringForGivenLength(101, locationTemplateValue);//making the value to the Just beyond max length of the Field
                        snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationIdHMCTS: may only be 8 characters long]", null);
                        break;
                }
                break;
            case "personFirstName":
                switch (locationTemplateValue) {
                    case "":
                        snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationIdHMCTS: must be at least 1 characters long]", null);
                        break;
                    case " ":
                        snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1001", "A Location resource with 'locationIdHMCTS' = ' ' already exists", null);
                        break;
                    default:
                        locationTemplateValue = generateStringForGivenLength(81, locationTemplateValue);//making the value to the Just beyond max length of the Field
                        snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationIdHMCTS: may only be 8 characters long]", null);
                        break;
                }
                break;
            case "personLastName":
                switch (locationTemplateValue) {
                    case "":
                        snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationIdHMCTS: must be at least 1 characters long]", null);
                        break;
                    case " ":
                        snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1001", "A Location resource with 'locationIdHMCTS' = ' ' already exists", null);
                        break;
                    default:
                        locationTemplateValue = generateStringForGivenLength(81, locationTemplateValue);//making the value to the Just beyond max length of the Field
                        snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationIdHMCTS: may only be 8 characters long]", null);
                        break;
                }
                break;
            case "personContactEmail":
                switch (locationTemplateValue) {
                    case "":
                        snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationIdHMCTS: must be at least 1 characters long]", null);
                        break;
                    case " ":
                        snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1001", "A Location resource with 'locationIdHMCTS' = ' ' already exists", null);
                        break;
                    default:
                        locationTemplateValue = generateStringForGivenLength(81, locationTemplateValue);//making the value to the Just beyond max length of the Field
                        snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.locationRequest.location.locationIdHMCTS: may only be 8 characters long]", null);
                        break;
                }
                break;
            case "personRegistry":
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
        this.setInputPayloadFileName("resources-by-user-general-template.json");
        generatePayloadWithHMCTSIDAndField(locationTemplateKey, locationTemplateValue, "/location/put/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                snlVerificationDTO);
    }


    private String makePostResourcesByUserAndFetchUserId() throws Exception {
        String randomString = UUID.randomUUID().toString();
        DelegateDTO delegateDTO = DelegateDTO.builder()
                .targetSubscriptionKey(getApiSubscriptionKey()).authorizationToken(getAuthorizationToken())
                .targetURL(resourcesByUserRootContext)
                .inputPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH, getInputFileDirectory()) +
                        "/user/post/" + getInputPayloadFileName()), randomString))
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
        return randomString;
    }
}


