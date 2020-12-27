package uk.gov.hmcts.futurehearings.snl.acceptance.common.test;

import static io.restassured.config.EncoderConfig.encoderConfig;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createCompletePayloadHeader;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createHeaderWithAcceptTypeAtSystemValue;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createHeaderWithAllValuesEmpty;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createHeaderWithAllValuesNull;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createHeaderWithCorruptedHeaderKey;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createHeaderWithDeprecatedHeaderValue;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createHeaderWithDestinationSystemValue;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createHeaderWithRemovedHeaderKey;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createHeaderWithRequestCreatedAtSystemValue;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createHeaderWithRequestProcessedAtSystemValue;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createHeaderWithSourceSystemValue;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createStandardPayloadHeader;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createStandardPayloadHeaderWithDuplicateValues;

import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.CommonDelegate;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.dto.DelegateDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.dto.SNLVerificationDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.error.SNLErrorVerifier;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.success.SNLSuccessVerifier;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.restassured.RestAssured;
import io.restassured.http.Headers;
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
    private HttpStatus httpSuccessStatus;
    private String inputFileDirectory;
    private String outputFileDirectory;
    private String inputPayloadFileName;
    private String inputBodyPayload;
    private Map<String, String> urlParams;
    private SNLVerificationDTO snlVerificationDTO;

    @Autowired(required = false)
    public CommonDelegate commonDelegate;

    public SNLSuccessVerifier snlSuccessVerifier;

    public SNLErrorVerifier snlErrorVerifier;

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
                createCompletePayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.ACCEPTED, null, null, null));
    }

    @Test
    @DisplayName("Successfully validated response with mandatory header values")
    public void test_successful_response_with_a_mandatory_header() throws Exception {

        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.ACCEPTED, null, null, null));
    }


    @Test
    @DisplayName("Successfully validated response with a valid payload but a ,charset appended to the Content-Type")
    public void test_successful_response_for_content_type_with_charset_not_appended() throws Exception {
        RestAssured.config = RestAssured.config()
                .encoderConfig(encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false));
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.UNSUPPORTED_MEDIA_TYPE, null, null, null));
        RestAssured.config = RestAssured.config()
                .encoderConfig(encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(true));
    }


    @Test
    @DisplayName("API call with Standard Header but slight Error URL")
    void test_invalid_URL() throws Exception {
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURLForNotFound(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), HttpStatus.NOT_FOUND);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.NOT_FOUND, "9999", "HTTP 404 Not Found", null));
    }


    @Test
    @DisplayName("Successfully validated response with an empty payload")
    public void test_successful_response_for_empty_json_body() throws Exception {
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), HttpStatus.BAD_REQUEST);
        delegateDTO.inputPayload("{}");
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                getSnlVerificationDTO());
    }

    @Test
    @DisplayName("API call with Standard Header but unimplemented METHOD")
    void test_invalid_REST_method() throws Exception {
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), HttpMethod.PATCH, getHttpSuccessStatus());
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.NOT_FOUND, null, null, null));
    }

    @Test
    @DisplayName("Headers with all empty and null values")
    void test_no_headers_populated() throws Exception {
        //2 Sets of Headers Tested - Nulls and Empty
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createHeaderWithAllValuesEmpty(), getHttpMethod(), HttpStatus.FORBIDDEN);
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.FORBIDDEN, "9999", "Expected header 'Subscription-Key'", null));
        delegateDTO = buildDelegateDTO(getRelativeURL(),
                createHeaderWithAllValuesNull(), getHttpMethod(), HttpStatus.FORBIDDEN);
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.FORBIDDEN, "9999", "Expected header 'Subscription-Key'", null));
    }

    @ParameterizedTest(name = "Source System Header invalid values - Param : {0} --> {1}")
    @CsvSource(value = {"Null_Value, NIL", "Empty_Space,''", "Invalid_Source_System, SNL", "Invalid_Source_System, CfT", "Invalid_Source_System, Anybody", "Invalid_Source_System, S&amp;L"}, nullValues = "NIL")
    @Disabled("TODO - Enable this Test post Implementation and Testing of MCGIRRSD-1761")
    public void test_source_system_invalid_values(String sourceSystemKey, String sourceSystemVal) throws Exception {
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createHeaderWithSourceSystemValue(getApiSubscriptionKey(), sourceSystemVal), getHttpMethod(),
                HttpStatus.BAD_REQUEST);
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, null, null, null));
    }

    @ParameterizedTest(name = "Destination System Header with invalid values - Param : {0} --> {1}")
    @CsvSource(value = {"Null_Value, NIL", "Empty_Space,''", "Invalid_Destination_System, SNL", "Invalid_Destination_System, S&amp;L", "Invalid_Destination_System, CfT"}, nullValues = "NIL")
    @Disabled("TODO - Enable this Test post Implementation and Testing of MCGIRRSD-1774")
    void test_destination_system_invalid_values(String destinationSystemKey, String destinationSystemVal) throws Exception {
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createHeaderWithDestinationSystemValue(getApiSubscriptionKey(), destinationSystemVal), getHttpMethod(),
                HttpStatus.BAD_REQUEST);
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, null, null, null));
    }

    @ParameterizedTest(name = "Request Created At System Header invalid values - Param : {0} --> {1}")
    @CsvSource(value = {"Null_Value, NIL", "Empty_Space,\" \"", "Invalid_Value, value",
            "Invalid_Date_Format, 2002-02-31T10:00:30-05:00Z",
            "Invalid_Date_Format, 2002-02-31T1000:30-05:00",
            "Invalid_Date_Format, 2002-02-31T10:00-30-05:00",
            "Invalid_Date_Format, 2002-10-02T15:00:00*05Z",
            "Invalid_Date_Format, 2002-10-02T15:00?0005Z",
            "Invalid_Date_Format, 2002-10-02 15:00:00Z",
            "Invalid_Date_Format, 2002-10-02T15:00:00",
            "Invalid_Date_Format, 2002-10-02T15:00:00.05Z",
            "Invalid_Date_Format, 2019-10-12 07:20:50.52Z",
            "Invalid_Date, 2099-10-02T15:00:00Z"
    }, nullValues = "NIL")
    @Disabled("TODO - Enable the following tests after MCGIRRSD-1745 and MCGIRRSD-1776")
    void test_request_created_at_invalid_values(String requestCreatedAtKey, String requestCreatedAtVal) throws Exception {
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createHeaderWithRequestCreatedAtSystemValue(getApiSubscriptionKey(), requestCreatedAtVal), getHttpMethod(),
                HttpStatus.BAD_REQUEST);
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, null, null, null));
    }


    @ParameterizedTest(name = "Mandatory Keys truncated from the Header - Key : {0}")
    @ValueSource(strings = {"Accept"})
        //@Disabled("TODO - ADD The Following Headers once the HTTP Status validation is fixed MCGIRRSD-1745, " +
        // "Source-System, Destination-System, Request-Created-At,Request-Processed-At, Request-Type")
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
                        Arrays.asList(keyToBeTruncated)), getHttpMethod(), httpStatus);
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getSnlErrorVerifier(), snlVerificationDTO);

    }


    @ParameterizedTest(name = "Mandatory keys removed from the Header - Key : {0}")
    @ValueSource(strings = {"Accept"})
   /* @Disabled("TODO - ADD The Following Headers once the HTTP Status validation is fixed MCGIRRSD-1745, " +
     "Source-System, Destination-System, Request-Created-At,Request-Processed-At, Request-Type " +
     "Content-Type is giving and Error Desc 'HTTP 415 Unsupported Media Type' while this could not be recreated in Postman.")*/
    void test_with_keys_removed_from_header(String keyToBeRemoved) throws Exception {

        HttpStatus httpStatus = null;
        SNLVerificationDTO snlVerificationDTO = null;
        switch (keyToBeRemoved) {
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
                createHeaderWithRemovedHeaderKey(getApiSubscriptionKey(),
                        Arrays.asList(keyToBeRemoved)), getHttpMethod(), httpStatus);
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getSnlErrorVerifier(), snlVerificationDTO);

    }

    @ParameterizedTest(name = "Request Processed At System Header With Invalid Values - Param : {0} --> {1}")
    @CsvSource(value = {"Null_Value, NIL", "Blank,''", "Empty_Space,' '", "Invalid_Value, value",
            "Invalid_Date_Format, 2002-02-31T10:00:30-05:00Z",
            "Invalid_Date_Format, 2002-02-31T1000:30-05:00",
            "Invalid_Date_Format, 2002-02-31T10:00-30-05:00",
            "Invalid_Date_Format, 2002-10-02T15:00:00*05Z",
            "Invalid_Date_Format, 2002-10-02 15:00?0005Z",
            "Invalid_Date_Format, 2002-10-02 15:00:00Z",
            "Invalid_Date_Format, 2002-10-02T15:00:00",
            "Invalid_Date_Format,2002-10-02T15:00:00.05Z",
            "Invalid_Date_Format,2019-10-12 07:20:50.52Z",
            "Invalid_Date, 2099-10-02T15:00:00Z"
    }, nullValues = "NIL")
    @Disabled("TODO - Enable the following tests after MCGIRRSD-1745 and MCGIRRSD-1776")
    void test_request_processed_at_with_invalid_values(String requestProcessedAtKey, String requestProcessedAtVal) throws Exception {
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createHeaderWithRequestProcessedAtSystemValue(getApiSubscriptionKey(), requestProcessedAtVal),
                getHttpMethod(), HttpStatus.BAD_REQUEST);
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, null, null, null));
    }

    @ParameterizedTest(name = "Accept System Header with invalid format - Param : {0} --> {1}")
    @CsvSource({"Invalid_Value, Random", "Invalid_Format, application/pdf", "Invalid_Format, application/text"})
    @Disabled("TODO - Enable the following tests after MCGIRRSD-1803")
    void test_accept_at_with_invalid_values(String acceptTypeKey, String acceptTypeVal) throws Exception {

        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createHeaderWithAcceptTypeAtSystemValue(getApiSubscriptionKey(), acceptTypeVal),
                getHttpMethod(),
                HttpStatus.NOT_ACCEPTABLE);
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.NOT_ACCEPTABLE, "9999", "HTTP 406 Not Acceptable", null));
    }

    @ParameterizedTest(name = "Request Processed At System Header With Valid Date Format - Param : {0} --> {1}")
    @CsvSource({"Valid_Date_Format,2002-10-02T10:00:00-05:00",
            "Valid_Date_Format,2002-10-02T15:00:00Z",
            "Valid_Date,2099-10-02T15:00:00Z"
    })
        //TODO - The placement of a futuristic Date be it positive or negative is to be decided upoun the outcome of MCGIRRSD-1776
    void test_request_processed_at_with_valid_values(String requestProcessedAtKey, String requestProcessedAtVal) throws Exception {

        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createHeaderWithRequestProcessedAtSystemValue(getApiSubscriptionKey(), requestProcessedAtVal),
                getHttpMethod(),
                getHttpSuccessStatus());
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }


    @ParameterizedTest(name = "Request Created At System Header With Valid Date Format - Param : {0} --> {1}")
    @CsvSource({"Valid_Date_Format, 2012-03-19T07:22:00Z",
            "Valid_Date_Format, 2002-10-02T15:00:00-10:00",
            "Valid_Date_Format, 2002-10-02T15:00:00+05:00",
            "Valid_Date,2099-10-02T15:00:00Z"})
    void test_request_created_at_with_valid_values(String requestCreatedAtKey, String requestCreatedAtVal) throws Exception {
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createHeaderWithRequestCreatedAtSystemValue(getApiSubscriptionKey(), requestCreatedAtVal),
                getHttpMethod(),
                getHttpSuccessStatus());
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));

    }


    @ParameterizedTest(name = "Deprecated System headers with valid values - Param : {0} --> {1}")
    @CsvSource({"X-Accept, application/json",
            "X-Source-System, CFT",
            "X-Destination-System, S&L",
            "X-Request-Created-At, 2012-03-19T07:22:00Z",
            "X-Request-Processed-At, 2012-03-19T07:22:00Z",
            "X-Request-Type, Assault"
    })
    @Disabled("TODO - Retest Waiting for the HTTP Status code to return 400 rather than 200 for the respective header error.")
    void test_deprecated_header_values(String deprecatedHeaderKey, String deprecatedHeaderVal) throws Exception {

        final HttpStatus httpStatus = deprecatedHeaderKey.equalsIgnoreCase("X-Accept") ? HttpStatus.NOT_ACCEPTABLE : HttpStatus.BAD_REQUEST;
        /*final String expectedErrorMessage =
                deprecatedHeaderKey.equalsIgnoreCase("X-Accept") ||
                        deprecatedHeaderKey.equalsIgnoreCase("X-Content-Type") ?
                        "Missing/Invalid Media Type" : "Missing/Invalid Header " + deprecatedHeaderKey.replace("X-", "");*/
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createHeaderWithDeprecatedHeaderValue(getApiSubscriptionKey(), deprecatedHeaderKey, deprecatedHeaderVal),
                getHttpMethod(),
                getHttpSuccessStatus());
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.NOT_ACCEPTABLE, "9999", "HTTP 406 Not Acceptable", null));
    }


    @ParameterizedTest(name = "Duplicate System headers with valid values - Param : {0} --> {1}")
    @CsvSource(value = {
            //System Headers of Accept and Content-Type could not be duplicated as Rest Assured seems to remove the Duplication of valid same values.
            //This should be tested manually using Postman.
            "Source-System,NIL", "Source-System,''", "Source-System,CFT",
            "Destination-System,NIL", "Destination-System,''", "Destination-System,S&L",
            "Request-Created-At,NIL", "Request-Created-At,''", "Request-Created-At,2002-10-02T15:00:00Z",
            "Request-Processed-At,NIL", "Request-Processed-At,''", "Request-Processed-At,2002-10-02T15:00:00Z",
            "Request-Type,NIL", "Request-Type,''", "Request-Type,THEFT", "Request-Type,ASSAULT"
    }, nullValues = "NIL")
    @Disabled("TODO - Findout what McGirr Intend to do in order to tests this scenario,Most likely it would not be a defect as HMI is picking up on this one.")
    void test_duplicate_headers(String duplicateHeaderKey, String duplicateHeaderValue) throws Exception {

        final String expectedErrorMessage =
                "Missing/Invalid Header " + duplicateHeaderKey;
        Map<String, String> duplicateHeaderField = new HashMap<String, String>();
        duplicateHeaderField.put(duplicateHeaderKey, duplicateHeaderValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeaderWithDuplicateValues(getApiSubscriptionKey(),
                        duplicateHeaderField),
                getHttpMethod(),
                getHttpSuccessStatus());
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.NOT_ACCEPTABLE, "9999", "HTTP 406 Not Acceptable", null));

    }

    public DelegateDTO buildDelegateDTO(final String relativeURL,
                                        final Map<String, String> payloadHeader,
                                        final HttpMethod httpMethod,
                                        final HttpStatus httpSuccessStatus) throws IOException {
        return DelegateDTO.builder()
                .targetSubscriptionKey(getApiSubscriptionKey()).authorizationToken(getAuthorizationToken())
                .targetURL(relativeURL)
                .inputPayload(getInputBodyPayload())
                .standardHeaderMap(payloadHeader)
                .headers(null)
                .params(getUrlParams())
                .httpMethod(httpMethod)
                .status(httpSuccessStatus)
                .build();
    }

    public DelegateDTO buildDelegateDTO(final String relativeURL,
                                        Headers headers,
                                        final HttpMethod httpMethod,
                                        final HttpStatus httpSuccessStatus) throws IOException {
        return DelegateDTO.builder()
                .targetSubscriptionKey(getApiSubscriptionKey()).authorizationToken(getAuthorizationToken())
                .targetURL(relativeURL)
                .inputPayload(getInputBodyPayload())
                .standardHeaderMap(null)
                .headers(headers)
                .params(getUrlParams())
                .httpMethod(httpMethod)
                .status(httpSuccessStatus)
                .build();
    }
}
