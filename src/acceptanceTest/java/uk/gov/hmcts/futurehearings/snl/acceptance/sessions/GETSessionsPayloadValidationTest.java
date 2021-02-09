package uk.gov.hmcts.futurehearings.snl.acceptance.sessions;

import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createCompletePayloadHeader;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createStandardPayloadHeader;

import uk.gov.hmcts.futurehearings.snl.Application;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.TestingUtils;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.dto.DelegateDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.dto.SNLVerificationDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.error.SNLCommonErrorVerifier;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.success.SNLCommonSuccessVerifier;
import uk.gov.hmcts.futurehearings.snl.acceptance.sessions.dto.SessionsVerificationDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.sessions.verify.GETSessionsPayloadValidationVerifier;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest(classes = {Application.class})
@ActiveProfiles("acceptance")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings("java:S2187")
public class GETSessionsPayloadValidationTest extends SessionsPayloadValidationTest {

    private static final String INPUT_FILE_PATH = "uk/gov/hmcts/futurehearings/snl/acceptance/%s/input";

    @Value("${targetInstance}")
    private String targetInstance;

    @Value("${targetSubscriptionKey}")
    private String targetSubscriptionKey;

    @Value("${sessionsApiRootContext}")
    private String sessionsApiRootContext;

    @BeforeAll
    public void initialiseValues() throws Exception {
        super.initialiseValues();
        sessionsApiRootContext = String.format(sessionsApiRootContext, "12345");
        this.setRelativeURL(sessionsApiRootContext);
        this.setHttpMethod(HttpMethod.GET);
        this.setHttpSuccessStatus(HttpStatus.OK);
        this.setRelativeURLForNotFound(this.getRelativeURL().replace("sessions", "session"));
        this.setSnlSuccessVerifier(new SNLCommonSuccessVerifier());
        this.setSnlErrorVerifier(new SNLCommonErrorVerifier());
        this.setInputFileDirectory("sessions");
        this.setInputPayloadFileName("empty-json-payload.json");
        TestingUtils.readFileContents(String.format(INPUT_FILE_PATH, getInputFileDirectory()) +
                "/" + getInputPayloadFileName());
    }

    @Test
    @DisplayName("Successfully validated response with all the header values - Query Param : requestSessionType=ADHOC")
    public void test_successful_response_with_a_complete_header() throws Exception {

        Map<String, String> urlParams = Map.of("requestSessionType", "ADHOC");
        this.setUrlParams(urlParams);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createCompletePayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        SessionsVerificationDTO sessionsVerificationDTO = new SessionsVerificationDTO(getHttpSuccessStatus(), null, null, null);
        sessionsVerificationDTO.requestSessionType("ADHOC");
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                new GETSessionsPayloadValidationVerifier(), sessionsVerificationDTO);
    }

    @Test
    @DisplayName("Successfully validated response with mandatory header values - Query Param : requestSessionType=ADHOC")
    public void test_successful_response_with_a_mandatory_header() throws Exception {

        Map<String, String> urlParams = Map.of("requestSessionType", "ADHOC");
        this.setUrlParams(urlParams);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        SessionsVerificationDTO sessionsVerificationDTO = new SessionsVerificationDTO(getHttpSuccessStatus(), null, null, null);
        sessionsVerificationDTO.requestSessionType("ADHOC");
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                new GETSessionsPayloadValidationVerifier(),
                sessionsVerificationDTO);
    }

    @ParameterizedTest(name = "Parameterized for the request session type")
    @CsvFileSource(resources = "/uk/gov/hmcts/futurehearings/snl/acceptance/hearings/data/valid-person-authorised-session-types.csv", numLinesToSkip = 1)
    @DisplayName("Successfully validated response with mandatory header values - Query Param : requestSessionType=ADHOC")
    public void test_successful_response_for_various_request_session_types(final String key, final String value) throws Exception {

        Map<String, String> urlParams = Map.of("requestSessionType", value);
        this.setUrlParams(urlParams);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        SessionsVerificationDTO sessionsVerificationDTO = new SessionsVerificationDTO(getHttpSuccessStatus(), null, null, null);
        sessionsVerificationDTO.requestSessionType("ADHOC");
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                new SNLCommonSuccessVerifier(),
                sessionsVerificationDTO);
    }



    @DisplayName("Negative response with mandatory header values Param : {0} --> {1}")
    @ParameterizedTest(name = "caseIDHMCTS Negative tests")
    @CsvSource(value = {"Empty Space,''", "Blank Value Space,' '", "Invalid Look Up,HOC"}, nullValues = "NIL")
    public void test_negative_response_with_a_mandatory_header_invalid_session_type(final String key, final String value) throws Exception {

        Map<String, String> urlParams = Map.of("requestSessionType", value);
        this.setUrlParams(urlParams);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        SessionsVerificationDTO sessionsVerificationDTO = new SessionsVerificationDTO(getHttpSuccessStatus(), null, null, null);
        sessionsVerificationDTO.requestSessionType(value);
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                this.getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", "'" + value + "' is not a valid value for field 'requestSessionType'", null));
    }

    @Test
    @DisplayName("Successfully validated response with mandatory header values - Query Param : requestSessionType=ADHOC, requestDuration=360")
    public void test_successful_response_with_a_mandatory_header_and_request_duration() throws Exception {

        Map<String, String> urlParams = Map.of(
                "requestSessionType", "ADHOC",
                "requestDuration", "360");
        this.setUrlParams(urlParams);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        SessionsVerificationDTO sessionsVerificationDTO = new SessionsVerificationDTO(getHttpSuccessStatus(), null, null, null);
        sessionsVerificationDTO.requestSessionType("ADHOC");
        sessionsVerificationDTO.requestDuration("360");
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                new GETSessionsPayloadValidationVerifier(),
                sessionsVerificationDTO);
    }

    @DisplayName("Negative response with mandatory header values Param : {0} --> {1}")
    @ParameterizedTest(name = "Invalid locationDuration")
    @CsvSource(value = {"Empty Space,''", "Invalid Request Duration,0", "Invalid Request Duration,-1"}, nullValues = "NIL")
    //TODO - Raise Defects for "Blank Value Space,' '"  as it was throwing a 404 HTTP Status for this Condition
    public void test_negative_response_with_a_mandatory_header_invalid_duration(final String key, final String value) throws Exception {

        Map<String, String> urlParams = Map.of(
                "requestSessionType", "ADHOC",
                "requestDuration", value);
        this.setUrlParams(urlParams);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                this.getSnlSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.OK, null, null, null));
    }

    @ParameterizedTest(name = "Valid Request Session Types")
    @DisplayName("Successfully validated response with mandatory header values - Query Param : {0} --> {1}")
    @CsvSource(value = {"Valid Values,PUBLAW", "Valid Values,DJCC"}, nullValues = "NIL")
    public void test_successful_response_with_a_mandatory_header_and_request_judge_type(final String key, final String value) throws Exception {

        Map<String, String> urlParams = Map.of(
                "requestSessionType", "ADHOC",
                "requestJudgeType", value);
        this.setUrlParams(urlParams);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        SessionsVerificationDTO sessionsVerificationDTO = new SessionsVerificationDTO(getHttpSuccessStatus(), null, null, null);
        sessionsVerificationDTO.requestSessionType("ADHOC");
        sessionsVerificationDTO.requestJudgeType( value);
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                new GETSessionsPayloadValidationVerifier(),
                sessionsVerificationDTO);
    }

    @ParameterizedTest(name = "Valid Request Judge Types")
    @DisplayName("Successfully validated response with mandatory header values and Request Judge Types - Query Param : {0} --> {1}")
    @CsvFileSource(resources = "/uk/gov/hmcts/futurehearings/snl/acceptance/common/data/request-judge-type-values.csv", numLinesToSkip = 1)
    public void test_successful_response_with_various_request_judge_types(final String key, final String value) throws Exception {

        Map<String, String> urlParams = Map.of(
                "requestSessionType", "ADHOC",
                "requestJudgeType", value);
        this.setUrlParams(urlParams);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        SessionsVerificationDTO sessionsVerificationDTO = new SessionsVerificationDTO(getHttpSuccessStatus(), null, null, null);
        sessionsVerificationDTO.requestSessionType("ADHOC");
        sessionsVerificationDTO.requestJudgeType( value);
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getSnlSuccessVerifier(),
                sessionsVerificationDTO);
    }

    @ParameterizedTest(name = "Valid Request Session Types and Request Judge Types")
    @DisplayName("Successfully validated response with mandatory header values Request Session Type and Request Judge Type- Query Param : {0} --> {1}")
    @CsvFileSource(resources = "/uk/gov/hmcts/futurehearings/snl/acceptance/common/data/request-session-types-and-request-judge-type-values.csv", numLinesToSkip = 1)
    public void test_successful_response_with_a_request_session_and_type_request_judge_type(final String key, final String value) throws Exception {

        Map<String, String> urlParams = Map.of(
                "requestSessionType", key,
                "requestJudgeType", value);
        this.setUrlParams(urlParams);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        SessionsVerificationDTO sessionsVerificationDTO = new SessionsVerificationDTO(getHttpSuccessStatus(), null, null, null);
        sessionsVerificationDTO.requestSessionType(key);
        sessionsVerificationDTO.requestJudgeType( value);
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getSnlSuccessVerifier(),
                sessionsVerificationDTO);
    }

    @Test
    @DisplayName("Successfully validated response with mandatory header values - Query Param : requestSessionType=ADHOC, requestLocationId=301")
    //TODO - Raise a Defect only working for Room Id's and not for any Venue Id's....
    public void test_successful_response_with_a_mandatory_header_and_request_location_id() throws Exception {

        Map<String, String> urlParams = Map.of(
                "requestSessionType", "ADHOC",
                "requestLocationId", "301");
        this.setUrlParams(urlParams);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        SessionsVerificationDTO sessionsVerificationDTO = new SessionsVerificationDTO(getHttpSuccessStatus(), null, null, null);
        sessionsVerificationDTO.requestSessionType("ADHOC");
        sessionsVerificationDTO.requestLocationID("301");
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                new GETSessionsPayloadValidationVerifier(),
                sessionsVerificationDTO);
    }

    @DisplayName("Negative response with mandatory header values Param : {0} --> {1}")
    @ParameterizedTest(name = " requestLocationId Negative tests")
    @CsvSource(value = {"Invalid Location Id,299", "Invalid Location Id,0","Invalid Location Id,-1"}, nullValues = "NIL")
    public void test_negative_response_with_a_mandatory_header_and_request_location_id(final String key, final String value) throws Exception {

        Map<String, String> urlParams = Map.of("requestSessionType", "ADHOC",
                                                "requestLocationId",value);
        this.setUrlParams(urlParams);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        SessionsVerificationDTO sessionsVerificationDTO = new SessionsVerificationDTO(getHttpSuccessStatus(), null, null, null);
        sessionsVerificationDTO.requestSessionType(value);
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                this.getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1002", "'" + value + "' is not a valid value for parameter 'requestLocationId'", null));
    }

    @Test
    @DisplayName("Successfully validated response with mandatory header values - Query Param : requestSessionType=ADHOC, requestStartDate=2020-12-09T10:00:00Z")
    public void test_successful_response_with_a_mandatory_header_and_request_start_date() throws Exception {

        Map<String, String> urlParams = Map.of(
                "requestSessionType", "ADHOC",
                "requestStartDate", "2020-12-09T10:00:00Z");
        this.setUrlParams(urlParams);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        SessionsVerificationDTO sessionsVerificationDTO = new SessionsVerificationDTO(getHttpSuccessStatus(), null, null, null);
        sessionsVerificationDTO.requestSessionType("ADHOC");
        sessionsVerificationDTO.requestStartDate("2020-12-09T10:00:00Z");
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                new GETSessionsPayloadValidationVerifier(),
                sessionsVerificationDTO);
    }

    @DisplayName("Negative response with mandatory header values Param : {0} --> {1}")
    @ParameterizedTest(name = " requestStartDate Negative tests")
    @CsvSource(value = {"Invalid Request Start Date Format,2020-12-09T10:00:00.123Z",}, nullValues = "NIL")
    //TODO - Raise a Defect for the condition "Invalid Request Start Date Format,2020-12-9T10:00:00Z" as it does not seem to format these date types
    public void test_negative_response_with_a_mandatory_header_and_request_start_date(final String key, final String value) throws Exception {

        Map<String, String> urlParams = Map.of(
                "requestSessionType", "ADHOC",
                "requestStartDate", value);
        this.setUrlParams(urlParams);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        SessionsVerificationDTO sessionsVerificationDTO = new SessionsVerificationDTO(getHttpSuccessStatus(), null, null, null);
        sessionsVerificationDTO.requestSessionType("ADHOC");
        sessionsVerificationDTO.requestStartDate(value);
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                this.getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "9999", "Date '"+value+"' not in RFC 3339 format", null));
    }

    @Test
    @DisplayName("Successfully validated response with mandatory header values - Query Param : requestSessionType=ADHOC, requestEndDate=2020-12-09T10:00:00Z")
    public void test_successful_response_with_a_mandatory_header_and_request_end_date() throws Exception {

        Map<String, String> urlParams = Map.of(
                "requestSessionType", "ADHOC",
                "requestEndDate", "2020-12-09T10:00:00Z");
        this.setUrlParams(urlParams);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        SessionsVerificationDTO sessionsVerificationDTO = new SessionsVerificationDTO(getHttpSuccessStatus(), null, null, null);
        sessionsVerificationDTO.requestSessionType("ADHOC");
        sessionsVerificationDTO.requestEndDate("2020-12-09T10:00:00Z");
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                new GETSessionsPayloadValidationVerifier(),
                sessionsVerificationDTO);
    }

    @DisplayName("Negative response with mandatory header values Param : {0} --> {1}")
    @ParameterizedTest(name = " requestEndDate Negative tests")
    @CsvSource(value = {"Invalid Request End Date Format,2020-12-09T10:00:00.123Z",}, nullValues = "NIL")
    //TODO - Raise a Defect for the condition "Invalid Request Start Date Format,2020-12-9T10:00:00Z" as it does not seem to format these date types
    public void test_negative_response_with_a_mandatory_header_and_request_end_date(final String key, final String value) throws Exception {

        Map<String, String> urlParams = Map.of(
                "requestSessionType", "ADHOC",
                "requestEndDate", value);
        this.setUrlParams(urlParams);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        SessionsVerificationDTO sessionsVerificationDTO = new SessionsVerificationDTO(getHttpSuccessStatus(), null, null, null);
        sessionsVerificationDTO.requestSessionType("ADHOC");
        sessionsVerificationDTO.requestEndDate(value);
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                this.getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "9999", "Date '"+value+"' not in RFC 3339 format", null));
    }

    @Test
    @DisplayName("Successfully validated response with mandatory header values - Query Param : requestSessionType=ADHOC, requestStartDate=2020-12-01T10:00:00Z , requestStartDate=2020-12-09T10:00:00Z")
    public void test_successful_response_with_a_mandatory_header_between_start_and_end_dates() throws Exception {

        Map<String, String> urlParams = Map.of(
                "requestSessionType", "ADHOC",
                "requestStartDate", "2020-12-01T10:00:00Z",
                "requestEndDate", "2020-12-09T10:00:00Z");
        this.setUrlParams(urlParams);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        SessionsVerificationDTO sessionsVerificationDTO = new SessionsVerificationDTO(getHttpSuccessStatus(), null, null, null);
        sessionsVerificationDTO.requestSessionType("ADHOC");
        sessionsVerificationDTO.requestStartDate("2020-12-01T10:00:00Z");
        sessionsVerificationDTO.requestEndDate("2020-12-09T10:00:00Z");
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                new GETSessionsPayloadValidationVerifier(),
                sessionsVerificationDTO);
    }

    @Test
    @DisplayName("Successfully validated response with mandatory header values - Query Param : requestSessionType=ADHOC, requestStartDate=2020-12-01T10:00:00Z , requestStartDate=2020-12-09T10:00:00Z")
    public void test_successful_response_with_a_mandatory_header_between_location_and_judge_types() throws Exception {

        Map<String, String> urlParams = Map.of(
                "requestSessionType", "ADHOC",
                "requestJudgeType", "CHC",
                "requestLocationId", "360");
        this.setUrlParams(urlParams);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        SessionsVerificationDTO sessionsVerificationDTO = new SessionsVerificationDTO(getHttpSuccessStatus(),
                null, null, null);
        sessionsVerificationDTO.requestSessionType("ADHOC");
        sessionsVerificationDTO.requestJudgeType("CHC");
        sessionsVerificationDTO.requestLocationID("360");
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                new GETSessionsPayloadValidationVerifier(),
                sessionsVerificationDTO);
    }

    @Test
    @DisplayName("Successfully validated response with mandatory header values - Query Param : requestSessionType=ADHOC, requestStartDate=2020-12-01T10:00:00Z , requestStartDate=2020-12-09T10:00:00Z")
    public void test_successful_response_with_a_mandatory_header_between_start_and_end_dates_and_duration() throws Exception {

        Map<String, String> urlParams = Map.of(
                "requestSessionType", "ADHOC",
                "requestStartDate", "2020-12-01T10:00:00Z",
                "requestDuration", "360",
                "requestEndDate", "2020-12-09T10:00:00Z");
        this.setUrlParams(urlParams);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        SessionsVerificationDTO sessionsVerificationDTO = new SessionsVerificationDTO(getHttpSuccessStatus(), null, null, null);
        sessionsVerificationDTO.requestSessionType("ADHOC");
        sessionsVerificationDTO.requestDuration("360");
        sessionsVerificationDTO.requestStartDate("2020-12-01T10:00:00Z");
        sessionsVerificationDTO.requestEndDate("2020-12-09T10:00:00Z");
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                new GETSessionsPayloadValidationVerifier(),
                sessionsVerificationDTO);
    }
}
