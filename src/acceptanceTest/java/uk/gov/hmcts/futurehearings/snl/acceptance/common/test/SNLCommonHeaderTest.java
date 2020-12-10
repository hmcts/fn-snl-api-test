package uk.gov.hmcts.futurehearings.snl.acceptance.common.test;

import static io.restassured.config.EncoderConfig.encoderConfig;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createCompletePayloadHeader;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createHeaderWithAllValuesEmpty;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createHeaderWithAllValuesNull;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createHeaderWithCorruptedHeaderKey;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createHeaderWithDestinationSystemValue;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createHeaderWithRequestCreatedAtSystemValue;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createHeaderWithSourceSystemValue;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createStandardPayloadHeader;

import uk.gov.hmcts.futurehearings.snl.acceptance.common.TestingUtils;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.CommonDelegate;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.dto.DelegateDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.dto.SNLVerificationDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.error.SNLErrorVerifier;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.success.SNLSuccessVerifier;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import io.restassured.RestAssured;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@Slf4j
@Setter
@Getter
@SuppressWarnings("java:S5786")
public abstract class SNLCommonHeaderTest {


    private static final String INPUT_FILE_PATH = "uk/gov/hmcts/futurehearings/snl/acceptance/%s/input";

    private String apiSubscriptionKey;
    private String authorizationToken;
    private String relativeURL;
    private String relativeURLForNotFound;
    private HttpMethod httpMethod;
    private HttpStatus httpSucessStatus;
    private String inputFileDirectory;
    private String outputFileDirectory;
    private String inputPayloadFileName;
    private Map<String, String> urlParams;
    private SNLVerificationDTO snlVerificationDTO;

    @Autowired(required = false)
    public CommonDelegate commonDelegate;

    public SNLSuccessVerifier hmiSuccessVerifier;

    public SNLErrorVerifier hmiErrorVerifier;

    @BeforeAll
    public void beforeAll(TestInfo info) {
        log.debug("Test execution Class Initiated: " + info.getTestClass().get().getName());
    }

    @BeforeEach
    public void beforeEach(TestInfo info) {
        log.debug("Before execution : " + info.getTestMethod().get().getName());
    }

    @AfterEach
    public void afterEach(TestInfo info) {
        log.debug("After execution : " + info.getTestMethod().get().getName());
    }

    @AfterAll
    public void afterAll(TestInfo info) {
        log.debug("Test execution Class Completed: " + info.getTestClass().get().getName());
    }

    @Test
    @DisplayName("Successfully validated response with all the header values")
    public void test_successful_response_with_a_complete_header() throws Exception {

        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createCompletePayloadHeader(getApiSubscriptionKey()), getHttpSucessStatus());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getHmiSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.ACCEPTED, null, null, null));
    }

    @Test
    @DisplayName("Successfully validated response with mandatory header values")
    public void test_successful_response_with_a_mandatory_header() throws Exception {

        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpSucessStatus());
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getHmiSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.ACCEPTED, null, null, null));
    }


    @Test
    @DisplayName("Successfully validated response with a valid payload but a ,charset appended to the Content-Type")
    void test_successful_response_for_content_type_with_charset_not_appended() throws Exception {
        RestAssured.config = RestAssured.config()
                .encoderConfig(encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false));
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getHmiSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.UNSUPPORTED_MEDIA_TYPE, null, null, null));
        RestAssured.config = RestAssured.config()
                .encoderConfig(encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(true));
    }


    @Test
    @DisplayName("API call with Standard Header but slight Error URL")
    void test_invalid_URL() throws Exception {
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURLForNotFound(),
                createStandardPayloadHeader(getApiSubscriptionKey()), HttpStatus.NOT_FOUND);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getHmiErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.NOT_FOUND, "9999", "HTTP 404 Not Found", null));
    }


    @Test
    @DisplayName("Successfully validated response with an empty payload")
    public void test_successful_response_for_empty_json_body() throws Exception {
        this.setInputFileDirectory("common");
        this.setInputPayloadFileName("empty-json-payload.json");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), HttpStatus.BAD_REQUEST);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getHmiErrorVerifier(),
                getSnlVerificationDTO());
    }


    @Test
    @DisplayName("API call with Standard Header but unimplemented METHOD")
    void test_invalid_REST_method() throws Exception {
        setHttpMethod(HttpMethod.TRACE);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpSucessStatus());
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getHmiSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.NOT_FOUND, null, null, null));
    }

    @Test
    @DisplayName("Headers with all empty and null values")
    void test_no_headers_populated() throws Exception {
        //2 Sets of Headers Tested - Nulls and Empty
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createHeaderWithAllValuesEmpty(), HttpStatus.UNAUTHORIZED);
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getHmiErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.UNAUTHORIZED, null, null, null));
        delegateDTO = buildDelegateDTO(getRelativeURL(),
                createHeaderWithAllValuesNull(), HttpStatus.UNAUTHORIZED);
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getHmiErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.UNAUTHORIZED, null, null, null));
    }

    /*
    @Test
    @DisplayName("Subscription Key Truncated in the Header")
    @Disabled("Initial Setup")
    void test_subscriptionkey_key_truncated() throws Exception {
        commonDelegate.test_expected_response_for_supplied_header(getApiSubscriptionKey(),
                getAuthorizationToken(),
                getRelativeURL(), getInputPayloadFileName(),
                createHeaderWithCorruptedHeaderKey(getApiSubscriptionKey(),
                        Arrays.asList("Ocp-Apim-Subscription-Key")), null,
                getUrlParams(), getHttpMethod(),
                HttpStatus.UNAUTHORIZED,
                getHmiErrorVerifier(),
                new SNLDTO(HttpStatus.UNAUTHORIZED,null,null,null));
    }

    @Test
    @DisplayName("Subscription Key Value Truncated in the Header")
    @Disabled("Initial Setup")
    void test_subscriptionkey_value_truncated() throws Exception {
        commonDelegate.test_expected_response_for_supplied_header(
                getApiSubscriptionKey().substring(0, getApiSubscriptionKey().length() - 1),
                getAuthorizationToken(),
                getRelativeURL(), getInputPayloadFileName(),
                createStandardPayloadHeader("  "),
                null,
                getUrlParams(),
                getHttpMethod(),
                HttpStatus.UNAUTHORIZED,
                getHmiErrorVerifier(),
                new SNLDTO(HttpStatus.UNAUTHORIZED,null,null,null));
    }


    @ParameterizedTest(name = "Subscription Key with invalid values  - Param : {0} --> {1}")
    @CsvSource(value = {"Null_Value, null", "Empty_Space,''", "Tab, \"\\t\"", "Newline, \"\\n\"", "Wrong_Value,c602c8ed3b8147be910449b563dce008"}, nullValues = "NIL")
    @Disabled("Initial Setup")
    void test_subscription_key_invalid_values(String subKey, String subKeyVal) throws Exception {
        commonDelegate.test_expected_response_for_supplied_header(getApiSubscriptionKey(),
                getAuthorizationToken(),
                getRelativeURL(), getInputPayloadFileName(),
                createStandardPayloadHeader(subKeyVal),
                null,
                getUrlParams(),
                getHttpMethod(),
                HttpStatus.UNAUTHORIZED,
                getHmiErrorVerifier(),
                new SNLDTO(HttpStatus.UNAUTHORIZED,null,null,null));
    }
    */


    @ParameterizedTest(name = "Source System Header invalid values - Param : {0} --> {1}")
    @CsvSource(value = {"Null_Value, NIL", "Empty_Space,''", "Invalid_Source_System, SNL", "Invalid_Source_System, CfT", "Invalid_Source_System, S&amp;L"}, nullValues = "NIL")
    void test_source_system_invalid_values(String sourceSystemKey, String sourceSystemVal) throws Exception {
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createHeaderWithSourceSystemValue(getApiSubscriptionKey(), sourceSystemVal), HttpStatus.BAD_REQUEST);
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getHmiErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, null, null, null));
    }


    @ParameterizedTest(name = "Destination System Header with invalid values - Param : {0} --> {1}")
    @CsvSource(value = {"Null_Value, NIL", "Empty_Space,''", "Invalid_Destination_System, SNL", "Invalid_Destination_System, S&amp;L", "Invalid_Destination_System, CfT"}, nullValues = "NIL")
    void test_destination_system_invalid_values(String destinationSystemKey, String destinationSystemVal) throws Exception {
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createHeaderWithDestinationSystemValue(getApiSubscriptionKey(), destinationSystemVal), HttpStatus.BAD_REQUEST);
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getHmiErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, null, null, null));
    }


    @ParameterizedTest(name = "Request Created At System Header invalid values - Param : {0} --> {1}")
    @CsvSource(value = {"Null_Value, NIL", "Empty_Space,\" \"", "Invalid_Value, value",
            "Invalid_Date_Format, 2002-02-31T10:00:30-05:00Z",
            "Invalid_Date_Format, 2002-02-31T1000:30-05:00",
            "Invalid_Date_Format, 2002-02-31T10:00-30-05:00",
            "Invalid_Date_Format, 2002-10-02T15:00:00*05Z",
            "Invalid_Date_Format, 2002-10-02T15:00?0005Z",
            "Invalid_Date_Format, 2002-10-02 15:00:00",
    }, nullValues = "NIL")
    void test_request_created_at_invalid_values(String requestCreatedAtKey, String requestCreatedAtVal) throws Exception {
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createHeaderWithRequestCreatedAtSystemValue(getApiSubscriptionKey(), requestCreatedAtVal), HttpStatus.BAD_REQUEST);
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getHmiErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, null, null, null));
    }


    @ParameterizedTest(name = "Mandatory Keys truncated from the Header - Key : {0}")
    @ValueSource(strings = {"Accept"})
    //TODO - ADD The Following Headers once the HTTP Status validation is ßßfixed MCGIRRSD-1745
    //"Source-System","Destination-System", "Request-Created-At","Request-Processed-At", "Request-Type"
    //Content-Type is giving and Error Desc 'HTTP 415 Unsupported Media Type' while this could not be recreated in Postman.
    void test_header_keys_truncated(String keyToBeTruncated) throws Exception {

        HttpStatus httpStatus = null;
        SNLVerificationDTO snlVerificationDTO = null;
        switch (keyToBeTruncated) {
            case "Content-Type":
                httpStatus = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
                snlVerificationDTO = new SNLVerificationDTO(httpStatus, "9999", "Expected header 'Content-Type=application/json; charset=utf-8'", null);
                break;
            case "Accept":
            case "Source-System":
            case "Destination-System":
            case "Request-Created-At":
            case "Request-Processed-At":
            case "Request-Type":
                httpStatus = HttpStatus.NOT_ACCEPTABLE;
                snlVerificationDTO = new SNLVerificationDTO(httpStatus, "9999", "Expected header 'Accept=application/json; version=1.2'", null);
                break;
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createHeaderWithCorruptedHeaderKey(getApiSubscriptionKey(),
                        Arrays.asList(keyToBeTruncated)), httpStatus);
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getHmiErrorVerifier(), snlVerificationDTO);

    }

    /*
    @ParameterizedTest(name = "Mandatory keys removed from the Header - Key : {0}")
    @ValueSource(strings = {"Content-Type", "Accept", "Source-System",
            "Destination-System", "Request-Created-At",
            "Request-Processed-At", "Request-Type"})
    @Disabled("Initial Setup")
    void test_with_keys_removed_from_header(String keyToBeRemoved) throws Exception {
        final HttpStatus httpStatus = keyToBeRemoved.equalsIgnoreCase("Accept") ? HttpStatus.NOT_ACCEPTABLE : HttpStatus.BAD_REQUEST;
        final String expectedErrorMessage =
                keyToBeRemoved.equalsIgnoreCase("Accept") ||
                        keyToBeRemoved.equalsIgnoreCase("Content-Type") ?
                        "Missing/Invalid Media Type" : "Missing/Invalid Header " + keyToBeRemoved;

        commonDelegate.test_expected_response_for_supplied_header(getApiSubscriptionKey(),
                getAuthorizationToken(),
                getRelativeURL(), getInputPayloadFileName(),
                createHeaderWithRemovedHeaderKey(getApiSubscriptionKey(),
                        Arrays.asList(keyToBeRemoved)),
                null,
                getUrlParams(),
                getHttpMethod(),
                httpStatus,
                getHmiErrorVerifier(),
                new SNLDTO(HttpStatus.BAD_REQUEST,null,null,null));
    }


    @ParameterizedTest(name = "Request Processed At System Header With Invalid Values - Param : {0} --> {1}")
    @CsvSource(value = {"Null_Value, NIL", "Empty_Space,''", "Invalid_Value, value",
            "Invalid_Date_Format, 2002-02-31T10:00:30-05:00Z",
            "Invalid_Date_Format, 2002-02-31T1000:30-05:00",
            "Invalid_Date_Format, 2002-02-31T10:00-30-05:00",
            "Invalid_Date_Format, 2002-10-02T15:00:00*05Z",
            "Invalid_Date_Format, 2002-10-02 15:00?0005Z",
            "Invalid_Date_Format, 2002-10-02T15:00:00",
    }, nullValues = "NIL")
    @Disabled("Initial Setup")
    void test_request_processed_at_with_invalid_values(String requestProcessedAtKey, String requestProcessedAtVal) throws Exception {
        commonDelegate.test_expected_response_for_supplied_header(getApiSubscriptionKey(),
                getAuthorizationToken(),
                getRelativeURL(), getInputPayloadFileName(),
                createHeaderWithRequestProcessedAtSystemValue(getApiSubscriptionKey(), requestProcessedAtVal),
                null,
                getUrlParams(),
                getHttpMethod(),
                HttpStatus.BAD_REQUEST,
                getHmiErrorVerifier(),
                new SNLDTO(HttpStatus.BAD_REQUEST,null,null,null));
    }


    @ParameterizedTest(name = "Request Type System Header with invalid values - Param : {0} --> {1}")
    @CsvSource(value = {"Null_Value, NIL", "Invalid_Value, Robbery"}, nullValues = "NIL")
    @Disabled("Initial Setup")
    void test_request_type_at_with_invalid_values(String requestTypeKey, String requestTypeVal) throws Exception {
        commonDelegate.test_expected_response_for_supplied_header(getApiSubscriptionKey(),
                getAuthorizationToken(),
                getRelativeURL(), getInputPayloadFileName(),
                createHeaderWithRequestTypeAtSystemValue(getApiSubscriptionKey(), requestTypeVal),
                null,
                getUrlParams(),
                getHttpMethod(),
                HttpStatus.BAD_REQUEST,
                getHmiErrorVerifier(),
                new SNLDTO(HttpStatus.BAD_REQUEST,null,null,null));
    }


    @ParameterizedTest(name = "Accept System Header with invalid format - Param : {0} --> {1}")
    @CsvSource({"Invalid_Value, Random", "Invalid_Format, application/pdf", "Invalid_Format, application/text"})
    @Disabled("Initial Setup")
    void test_accept_at_with_invalid_values(String acceptTypeKey, String acceptTypeVal) throws Exception {
        commonDelegate.test_expected_response_for_supplied_header(getApiSubscriptionKey(),
                getAuthorizationToken(),
                getRelativeURL(), getInputPayloadFileName(),
                createHeaderWithAcceptTypeAtSystemValue(getApiSubscriptionKey(), acceptTypeVal),
                null,
                getUrlParams(),
                getHttpMethod(),
                HttpStatus.NOT_ACCEPTABLE,
                getHmiErrorVerifier(),
                new SNLDTO(HttpStatus.NOT_ACCEPTABLE,null,null,null));
    }


    @ParameterizedTest(name = "Request Type System Header with valid values - Value : {0}")
    @ValueSource(strings = {"Assault", "Theft"})
    @Disabled("Initial Setup")
    void test_request_type_at_with_valid_values(String requestType) throws Exception {
        commonDelegate.test_expected_response_for_supplied_header(getApiSubscriptionKey(),
                getAuthorizationToken(),
                getRelativeURL(), getInputPayloadFileName(),
                createHeaderWithRequestTypeAtSystemValue(getApiSubscriptionKey(), requestType),
                null,
                getUrlParams(),
                getHttpMethod(),
                getHttpSucessStatus(),
                getHmiSuccessVerifier(),
                new SNLDTO(HttpStatus.NOT_ACCEPTABLE,null,null,null));
    }


    @ParameterizedTest(name = "Request Processed At System Header With Valid Date Format - Param : {0} --> {1}")
    @CsvSource({"Valid_Date_Format,2002-10-02T10:00:00-05:00",
            "Valid_Date_Format,2002-10-02T15:00:00Z",
            "Valid_Date_Format,2002-10-02T15:00:00.05Z",
            "Valid_Date_Format,2019-10-12 07:20:50.52Z"
    })
    @Disabled("Initial Setup")
    void test_request_processed_at_with_valid_values(String requestProcessedAtKey, String requestProcessedAtVal) throws Exception {
        commonDelegate.test_expected_response_for_supplied_header(getApiSubscriptionKey(),
                getAuthorizationToken(),
                getRelativeURL(), getInputPayloadFileName(),
                createHeaderWithRequestProcessedAtSystemValue(getApiSubscriptionKey(), requestProcessedAtVal),
                null,
                getUrlParams(),
                getHttpMethod(),
                getHttpSucessStatus(),
                getHmiSuccessVerifier(),
                new SNLDTO(HttpStatus.NOT_ACCEPTABLE,null,null,null));
    }

    @ParameterizedTest(name = "Request Created At System Header With Valid Date Format - Param : {0} --> {1}")
    @CsvSource({"Valid_Date_Format, 2012-03-19T07:22:00Z", "Valid_Date_Format, 2002-10-02T15:00:00Z",
            "Valid_Date_Format, 2002-10-02T15:00:00.05Z",
            "Valid_Date_Format, 2019-10-12 07:20:50.52Z"})
    @Disabled("Initial Setup")
    void test_request_created_at_with_valid_values(String requestCreatedAtKey, String requestCreatedAtVal) throws Exception {
        commonDelegate.test_expected_response_for_supplied_header(getApiSubscriptionKey(),
                getAuthorizationToken(),
                getRelativeURL(), getInputPayloadFileName(),
                createHeaderWithRequestCreatedAtSystemValue(getApiSubscriptionKey(), requestCreatedAtVal),
                null,
                getUrlParams(),
                getHttpMethod(),
                getHttpSucessStatus(),
                getHmiSuccessVerifier(),
                new SNLDTO(HttpStatus.NOT_ACCEPTABLE,null,null,null));

    }


    @ParameterizedTest(name = "Deprecated System headers with valid values - Param : {0} --> {1}")
    @CsvSource({"X-Accept, application/json",
            "X-Source-System, CFT",
            "X-Destination-System, S&L",
            "X-Request-Created-At, 2012-03-19T07:22:00Z",
            "X-Request-Processed-At, 2012-03-19T07:22:00Z",
            "X-Request-Type, Assault"
    })
    @Disabled("Initial Setup")
    void test_deprecated_header_values(String deprecatedHeaderKey, String deprecatedHeaderVal) throws Exception {

        final HttpStatus httpStatus = deprecatedHeaderKey.equalsIgnoreCase("X-Accept") ? HttpStatus.NOT_ACCEPTABLE : HttpStatus.BAD_REQUEST;
        final String expectedErrorMessage =
                deprecatedHeaderKey.equalsIgnoreCase("X-Accept") ||
                        deprecatedHeaderKey.equalsIgnoreCase("X-Content-Type") ?
                        "Missing/Invalid Media Type" : "Missing/Invalid Header " + deprecatedHeaderKey.replace("X-", "");
        commonDelegate.test_expected_response_for_supplied_header(getApiSubscriptionKey(),
                getAuthorizationToken(),
                getRelativeURL(), getInputPayloadFileName(),
                createHeaderWithDeprecatedHeaderValue(getApiSubscriptionKey(), deprecatedHeaderKey, deprecatedHeaderVal),
                null,
                getUrlParams(),
                getHttpMethod(),
                httpStatus,
                getHmiErrorVerifier(),
                new SNLDTO(HttpStatus.NOT_ACCEPTABLE,null,null,null));

    }

    @ParameterizedTest(name = "Duplicate System headers with valid values - Param : {0} --> {1}")
    @CsvSource(value = {
            //System Headers of Accept and Content-Type could not be duplicated as Rest Assured seems to remove the Duplication of valid same values.
            //This should be tested manually using Postman.
            "Source-System,NIL","Source-System,''","Source-System,CFT",
            "Destination-System,NIL","Destination-System,''","Destination-System,S&L",
            "Request-Created-At,NIL","Request-Created-At,''","Request-Created-At,2002-10-02T15:00:00Z",
            "Request-Processed-At,NIL","Request-Processed-At,''","Request-Processed-At,2002-10-02T15:00:00Z",
            "Request-Type,NIL","Request-Type,''","Request-Type,THEFT","Request-Type,ASSAULT"
    }, nullValues = "NIL")
    @Disabled("Initial Setup")
    void test_duplicate_headers(String duplicateHeaderKey, String duplicateHeaderValue) throws Exception {

        final String expectedErrorMessage =
                        "Missing/Invalid Header " + duplicateHeaderKey;
        Map<String,String> duplicateHeaderField  = new HashMap<String,String>();
        duplicateHeaderField.put(duplicateHeaderKey,duplicateHeaderValue);
        commonDelegate.test_expected_response_for_supplied_header(getApiSubscriptionKey(),
                getAuthorizationToken(),
                getRelativeURL(), getInputPayloadFileName(),
                null,
                createStandardPayloadHeaderWithDuplicateValues(getApiSubscriptionKey(),
                        duplicateHeaderField),
                getUrlParams(),
                getHttpMethod(),
                HttpStatus.BAD_REQUEST,
                getHmiErrorVerifier(),
                new SNLDTO(HttpStatus.BAD_REQUEST,null,null,null));

    }*/

    public DelegateDTO buildDelegateDTO(final String relativeURL,
                                        final Map<String, String> completePayloadHeader,
                                        final HttpStatus httpSucessStatus) throws IOException {
        return DelegateDTO.builder()
                .targetSubscriptionKey(getApiSubscriptionKey()).authorizationToken(getAuthorizationToken())
                .targetURL(relativeURL)
                .inputPayload(TestingUtils.readFileContents(String.format(INPUT_FILE_PATH, getInputFileDirectory()) +
                        "/" + getInputPayloadFileName()))
                .standardHeaderMap(completePayloadHeader)
                .headers(null)
                .params(getUrlParams())
                .httpMethod(getHttpMethod())
                .status(httpSucessStatus)
                .build();
    }
}
