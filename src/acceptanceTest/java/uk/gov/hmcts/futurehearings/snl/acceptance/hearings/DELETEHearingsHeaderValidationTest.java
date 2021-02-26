package uk.gov.hmcts.futurehearings.snl.acceptance.hearings;

import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createCompletePayloadHeader;

import uk.gov.hmcts.futurehearings.snl.Application;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.RestClientTemplate;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.TestingUtils;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.CommonDelegate;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.dto.DelegateDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.dto.SNLVerificationDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.error.SNLCommonErrorVerifier;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.success.SNLCommonSuccessVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
@SelectClasses(DELETEHearingsHeaderValidationTest.class)
@IncludeTags("Post")
class DELETEHearingsHeaderValidationTest extends HearingsHeaderValidationTest {


    @Qualifier("CommonDelegate")
    @Autowired(required = true)
    private CommonDelegate commonDelegate;

    @Value("${hearingsApi_idRootContext}")
    private String hearingsApi_idRootContext;

    @Value("${hearingsApiRootContext}")
    private String hearingsApiRootContext;

    private Integer hearingId;

    @BeforeAll
    public void initialiseValues() throws Exception {

        super.initialiseValues();
        this.setHttpMethod(HttpMethod.DELETE);
        this.setInputPayloadFileName("hearing-request-template.json");
        this.setHttpSuccessStatus(HttpStatus.ACCEPTED);
        this.setSnlSuccessVerifier(new SNLCommonSuccessVerifier());
        this.setSnlErrorVerifier(new SNLCommonErrorVerifier());
        this.hearingId = makePostHearingAndFetchHearingId();
        this.hearingsApi_idRootContext = String.format(hearingsApi_idRootContext, hearingId);
        this.setRelativeURL(hearingsApi_idRootContext);
        this.setRelativeURLForNotFound(this.getRelativeURL().replace("hearings", "hearing"));
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_FILE_PATH, getInputFileDirectory())
                + "/" + getInputPayloadFileName()), hearingId));
    }

    @Test
    @DisplayName("Successfully validated response with an empty payload")
    @Override
    public void test_successful_response_for_empty_json_body() throws Exception {
        this.setSnlVerificationDTO(new SNLVerificationDTO(HttpStatus.BAD_REQUEST,
                "1004", "[$.hearingRequest: is missing but it is required]", null));
        super.test_successful_response_for_empty_json_body();
    }

    private Integer makePostHearingAndFetchHearingId() throws Exception {
        int randomId = new Random().nextInt(99999999);

        DelegateDTO delegateDTO = DelegateDTO.builder()
                .targetSubscriptionKey(getApiSubscriptionKey()).authorizationToken(getAuthorizationToken())
                .targetURL(hearingsApiRootContext)
                .inputPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_FILE_PATH, getInputFileDirectory()) +
                        "/" + getInputPayloadFileName()), randomId))
                .standardHeaderMap(createCompletePayloadHeader())
                .headers(null)
                .params(getUrlParams())
                .httpMethod(HttpMethod.POST)
                .status(HttpStatus.ACCEPTED)
                .build();
        Response response = RestClientTemplate.shouldExecute(convertHeaderMapToRestAssuredHeaders(delegateDTO.standardHeaderMap()),
                delegateDTO.authorizationToken(),
                delegateDTO.inputPayload(), delegateDTO.targetURL(),
                delegateDTO.params(), delegateDTO.status(), delegateDTO.httpMethod());
        log.debug("POST Response : " + response.getBody().asString());
        return randomId;
    }

    private static final Headers convertHeaderMapToRestAssuredHeaders(final Map<String, String> headerMap) {

        List<Header> listOfHeaders = new ArrayList<>();
        headerMap.forEach((key, value) -> {
            Header header = new Header(key, value);
            listOfHeaders.add(header);
        });
        Headers headers = new Headers(listOfHeaders);
        return headers;
    }

    //This test is for a Standard Header but a Payload for Non JSON Type is to be tested.
    //Confirmed by Product Owner that this should be a Success Scenario.
    /*@Test
    @DisplayName("Successfully validated response with an xml payload")
    @Disabled("Initial Setup")
    void test_successful_response_for_test_xml_body() throws Exception {

        commonDelegate.test_expected_response_for_supplied_header(getApiSubscriptionKey(),
                getAuthorizationToken(),
                getRelativeURL(), "sample-xml-payload.xml",
                createStandardPayloadHeader(getApiSubscriptionKey()),
                null,
                getUrlParams(),
                getHttpMethod(),
                this.getHttpSucessStatus(),
                getHmiSuccessVerifier(),
                new SNLDTO(HttpStatus.OK,null,null,null));
    }*/

    /*@ParameterizedTest(name = "Source System Header invalid values - Param : {0} --> {1}")
    @CsvSource(value = {"Null_Value, NIL", "Empty_Space,''", "Invalid_Source_System, SNL", "Invalid_Source_System, CfT","Invalid_Source_System, Anybody", "Invalid_Source_System, S&amp;L"}, nullValues = "NIL")
    void test_source_system_invalid_values(String sourceSystemKey, String sourceSystemVal) throws Exception {
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createHeaderWithSourceSystemValue(getApiSubscriptionKey(), sourceSystemVal), HttpStatus.BAD_REQUEST);
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, null, null, null));
    }*/
}
