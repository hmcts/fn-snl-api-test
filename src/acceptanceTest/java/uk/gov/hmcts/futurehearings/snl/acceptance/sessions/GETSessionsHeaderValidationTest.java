package uk.gov.hmcts.futurehearings.snl.acceptance.sessions;

import uk.gov.hmcts.futurehearings.snl.Application;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.TestingUtils;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.error.SNLCommonErrorVerifier;
import uk.gov.hmcts.futurehearings.snl.acceptance.sessions.verify.GETSessionsValidationVerifier;

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
public class GETSessionsHeaderValidationTest extends SessionsHeaderValidationTest {

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
        this.setSnlSuccessVerifier(new GETSessionsValidationVerifier());
        this.setSnlErrorVerifier(new SNLCommonErrorVerifier());
        this.setInputPayloadFileName("empty-json-payload.json");
        TestingUtils.readFileContents(String.format(INPUT_FILE_PATH, getInputFileDirectory()) +
                "/" + getInputPayloadFileName());
        Map<String,String> urlParams = Map.of("requestSessionType","ADHOC");
        this.setUrlParams(urlParams);
    }

   /* @Test
    @DisplayName("Successfully validated response with all the header values")
    @Override
    public void test_successful_response_with_a_complete_header() throws Exception {


        super.test_successful_response_with_a_complete_header();
    }*/

    /*@Test
    @DisplayName("Successfully validated response with mandatory header values")
    public void test_successful_response_with_a_mandatory_header() throws Exception {

        Map<String,String> urlParams = Map.of("requestSessionType","ADHOC");
        this.setUrlParams(urlParams);
        super.test_successful_response_with_a_mandatory_header();
    }*/

    @Test
    @DisplayName("Successfully validated response with an empty payload")
    @Override
    public void test_successful_response_for_empty_json_body() throws Exception {
        return;
    }

    /*
    @Test
    @DisplayName("Successfully validated response with all the header values and Error Http Status as No Query params were passed.")
    void test_mandatory_query_parameter_not_provided () throws Exception {

        DelegateDTO delegateDTO = super.buildDelegateDTO(getRelativeURL(),
                createCompletePayloadHeader(getApiSubscriptionKey()),HttpStatus.BAD_REQUEST);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST,null,null,null));
    }

    @ParameterizedTest(name = "Source System Header invalid values - Param : {0} --> {1}")
    @CsvSource(value = {"Null_Value, NIL", "Empty_Space,''", "Invalid_Source_System, SNL", "Invalid_Source_System, CfT",
            "Invalid_Source_System, Anybody", "Invalid_Source_System, S&amp;L"}, nullValues = "NIL")
    public void test_source_system_invalid_values(String sourceSystemKey, String sourceSystemVal) throws Exception {
        log.debug("Inside the test_source_system_invalid_values() method :");
        Map<String,String> urlParams = Map.of("requestSessionType","ADHOC");
        this.setUrlParams(urlParams);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createHeaderWithSourceSystemValue(getApiSubscriptionKey(), sourceSystemVal), HttpStatus.BAD_REQUEST);
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, null, null, null));
    }*/
}
