package uk.gov.hmcts.futurehearings.snl.acceptance.sessions;

import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createCompletePayloadHeader;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createStandardPayloadHeader;

import uk.gov.hmcts.futurehearings.snl.Application;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.TestingUtils;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.dto.DelegateDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.dto.SNLVerificationDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.sessions.dto.SessionsVerificationDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.error.SNLCommonErrorVerifier;
import uk.gov.hmcts.futurehearings.snl.acceptance.sessions.verify.GETSessionsPayloadValidationVerifier;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
        this.setSnlSuccessVerifier(new GETSessionsPayloadValidationVerifier());
        this.setSnlErrorVerifier(new SNLCommonErrorVerifier());
        this.setInputFileDirectory("sessions");
        this.setInputPayloadFileName("empty-json-payload.json");
        TestingUtils.readFileContents(String.format(INPUT_FILE_PATH, getInputFileDirectory()) +
                "/" + getInputPayloadFileName());
        Map<String, String> urlParams = Map.of("requestSessionType", "ADHOC");
        this.setUrlParams(urlParams);
    }

    @Test
    @DisplayName("Successfully validated response with all the header values - Query Param : requestSessionType=ADHOC")
    public void test_successful_response_with_a_complete_header() throws Exception {

        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createCompletePayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        SessionsVerificationDTO sessionsVerificationDTO = new SessionsVerificationDTO(getHttpSuccessStatus(), null, null, null);
        sessionsVerificationDTO.requestSessionType("ADHOC");
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                new GETSessionsPayloadValidationVerifier(), sessionsVerificationDTO);
    }

    @Test
    @DisplayName("Successfully validated response with all the header values - Query Param : requestSessionType=ADHOC")
    public void test_negative_response_with_a_mandatory_header() throws Exception {

        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createCompletePayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        SessionsVerificationDTO sessionsVerificationDTO = new SessionsVerificationDTO(getHttpSuccessStatus(), null, null, null);
        sessionsVerificationDTO.requestSessionType("ADHOC");
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                new GETSessionsPayloadValidationVerifier(), sessionsVerificationDTO);
    }

    @Test
    @DisplayName("Successfully validated response with mandatory header values - Query Param : requestSessionType=ADHOC")
    public void test_successful_response_with_a_mandatory_header() throws Exception {

        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        SessionsVerificationDTO sessionsVerificationDTO = new SessionsVerificationDTO(getHttpSuccessStatus(), null, null, null);
        sessionsVerificationDTO.requestSessionType("ADHOC");
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                new GETSessionsPayloadValidationVerifier(),
                sessionsVerificationDTO);
    }

    @Test
    @DisplayName("Successfully validated response with mandatory header values - Query Param : requestSessionType=ADHOC, requestDuration=360")
    public void test_successful_response_with_a_mandatory_header_and_request_duration() throws Exception {

        Map<String, String> urlParams = Map.of(
                "requestSessionType", "ADHOC",
                "requestDuration", "360");
        this.setUrlParams(urlParams);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        SessionsVerificationDTO sessionsVerificationDTO = new SessionsVerificationDTO(getHttpSuccessStatus(), null, null, null);
        sessionsVerificationDTO.requestSessionType("ADHOC");
        sessionsVerificationDTO.requestDuration("360");
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                new GETSessionsPayloadValidationVerifier(),
                sessionsVerificationDTO);
    }

    @Test
    @DisplayName("Successfully validated response with mandatory header values - Query Param : requestSessionType=ADHOC, requestJudgeType=DJCC")
    public void test_successful_response_with_a_mandatory_header_and_request_judge_type() throws Exception {

        Map<String, String> urlParams = Map.of(
                "requestSessionType", "ADHOC",
                "requestJudgeType", "DJCC");
        this.setUrlParams(urlParams);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        SessionsVerificationDTO sessionsVerificationDTO = new SessionsVerificationDTO(getHttpSuccessStatus(), null, null, null);
        sessionsVerificationDTO.requestSessionType("ADHOC");
        sessionsVerificationDTO.requestJudgeType("DJCC");
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                new GETSessionsPayloadValidationVerifier(),
                sessionsVerificationDTO);
    }

    @Test
    @DisplayName("Successfully validated response with mandatory header values - Query Param : requestSessionType=ADHOC, requestLocationId=301")
    //TODO - Only working for Room Id's and not for any Venue Id's....
    public void test_successful_response_with_a_mandatory_header_and_request_location_id() throws Exception {

        Map<String, String> urlParams = Map.of(
                "requestSessionType", "ADHOC",
                "requestLocationId", "301");
        this.setUrlParams(urlParams);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        SessionsVerificationDTO sessionsVerificationDTO = new SessionsVerificationDTO(getHttpSuccessStatus(), null, null, null);
        sessionsVerificationDTO.requestSessionType("ADHOC");
        sessionsVerificationDTO.requestLocationID("301");
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                new GETSessionsPayloadValidationVerifier(),
                sessionsVerificationDTO);
    }

    @Test
    @DisplayName("Successfully validated response with mandatory header values - Query Param : requestSessionType=ADHOC, requestStartDate=2020-12-09T10:00:00Z")
    public void test_successful_response_with_a_mandatory_header_and_request_start_date() throws Exception {

        Map<String, String> urlParams = Map.of(
                "requestSessionType", "ADHOC",
                "requestStartDate", "2020-12-9T10:00:00Z");
        this.setUrlParams(urlParams);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        SessionsVerificationDTO sessionsVerificationDTO = new SessionsVerificationDTO(getHttpSuccessStatus(), null, null, null);
        sessionsVerificationDTO.requestSessionType("ADHOC");
        sessionsVerificationDTO.requestStartDate("2020-12-09T10:00:00Z");
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                new GETSessionsPayloadValidationVerifier(),
                sessionsVerificationDTO);
    }

    @Test
    @DisplayName("Successfully validated response with mandatory header values - Query Param : requestSessionType=ADHOC, requestEndDate=2020-12-09T10:00:00Z")
    public void test_successful_response_with_a_mandatory_header_and_request_end_date() throws Exception {

        Map<String, String> urlParams = Map.of(
                "requestSessionType", "ADHOC",
                "requestEndDate", "2020-12-9T10:00:00Z");
        this.setUrlParams(urlParams);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        SessionsVerificationDTO sessionsVerificationDTO = new SessionsVerificationDTO(getHttpSuccessStatus(), null, null, null);
        sessionsVerificationDTO.requestSessionType("ADHOC");
        sessionsVerificationDTO.requestEndDate("2020-12-09T10:00:00Z");
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                new GETSessionsPayloadValidationVerifier(),
                sessionsVerificationDTO);
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
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        SessionsVerificationDTO sessionsVerificationDTO = new SessionsVerificationDTO(getHttpSuccessStatus(), null, null, null);
        sessionsVerificationDTO.requestSessionType("ADHOC");
        sessionsVerificationDTO.requestStartDate("2020-12-01T10:00:00Z");
        sessionsVerificationDTO.requestEndDate("2020-12-09T10:00:00Z");
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                new GETSessionsPayloadValidationVerifier(),
                sessionsVerificationDTO);
    }
}
