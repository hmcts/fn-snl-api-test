package uk.gov.hmcts.futurehearings.snl.acceptance.hearings;

import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createStandardPayloadHeader;

import uk.gov.hmcts.futurehearings.snl.Application;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.CommonDelegate;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.dto.DelegateDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.dto.SNLVerificationDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.error.SNLCommonErrorVerifier;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.success.SNLCommonSuccessVerifier;

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
@SelectClasses(POSTHearingsValidationTest.class)
@IncludeTags("Post")
class POSTHearingsValidationTest extends HearingValidationTest {

    private static final String INPUT_FILE_PATH = "uk/gov/hmcts/futurehearings/snl/acceptance/%s/input";

    @Qualifier("CommonDelegate")
    @Autowired(required = true)
    private CommonDelegate commonDelegate;

    @Value("${hearingsApiRootContext}")
    private String hearingsApiRootContext;

    @BeforeAll
    public void initialiseValues() throws Exception {
        super.initialiseValues();
        this.setRelativeURL(hearingsApiRootContext);
        this.setHttpMethod(HttpMethod.POST);
        this.setInputPayloadFileName("hearing-request-standard.json");
        this.setHttpSucessStatus(HttpStatus.ACCEPTED);
        this.setRelativeURLForNotFound(this.getRelativeURL().replace("hearings","hearing"));
        this.setHmiSuccessVerifier(new SNLCommonSuccessVerifier());
        this.setHmiErrorVerifier(new SNLCommonErrorVerifier());
    }

    @Test
    @DisplayName("Successfully validated response with an empty payload")
    @Override
    public void test_successful_response_for_empty_json_body() throws Exception {
       this.setSnlVerificationDTO(new SNLVerificationDTO(HttpStatus.BAD_REQUEST,"1004","[$.hearingRequest: is missing but it is required]",null));
       super.test_successful_response_for_empty_json_body();
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
}
