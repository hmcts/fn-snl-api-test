package uk.gov.hmcts.futurehearings.snl.acceptance.resources;

import static uk.gov.hmcts.futurehearings.snl.acceptance.common.test.SNLCommonPayloadTest.INPUT_TEMPLATE_FILE_PATH;

import uk.gov.hmcts.futurehearings.snl.Application;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.TestingUtils;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.CommonDelegate;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.dto.SNLVerificationDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.error.SNLCommonErrorVerifier;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.success.SNLCommonSuccessVerifier;

import java.io.IOException;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
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
@IncludeTags("Post")
class POSTResourcesByUserHeaderValidationTest extends ResourcesHeaderValidationTest {

    @Qualifier("CommonDelegate")
    @Autowired(required = true)
    private CommonDelegate commonDelegate;

    @Value("${resourcesByUserRootContext}")
    private String resourcesByUserRootContext;

    @BeforeAll
    public void initialiseValues() throws Exception {
        super.initialiseValues();
        this.setRelativeURL(resourcesByUserRootContext);
        this.setHttpMethod(HttpMethod.POST);
        this.setInputPayloadFileName("resources-by-user-mandatory-ui-based.json");
        this.setHttpSuccessStatus(HttpStatus.CREATED);
        this.setRelativeURLForNotFound(this.getRelativeURL().replace("resources", "resource"));
        this.setSnlSuccessVerifier(new SNLCommonSuccessVerifier());
        this.setSnlErrorVerifier(new SNLCommonErrorVerifier());
        this.setInputBodyPayload(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + "/user/post/" + getInputPayloadFileName()));
    }

    @Test
    @DisplayName("Successfully validated response with all the header values")
    @Override
    public void test_successful_response_with_a_complete_header() throws Exception {

        generateResourcesByUserPayloadWithRandomHMCTSId();
        super.test_successful_response_with_a_complete_header();
    }

    @Test
    @DisplayName("Successfully validated response with mandatory header values")
    @Override
    public void test_successful_response_with_a_mandatory_header() throws Exception {
        generateResourcesByUserPayloadWithRandomHMCTSId();
        super.test_successful_response_with_a_mandatory_header();
    }


    @Test
    @DisplayName("Successfully validated response with an empty payload")
    @Override
    public void test_successful_response_for_empty_json_body() throws Exception {
        this.setSnlVerificationDTO(new SNLVerificationDTO(HttpStatus.BAD_REQUEST,
                "1004", "[$.userRequest: is missing but it is required]", null));
        super.test_successful_response_for_empty_json_body();
    }

    @ParameterizedTest(name = "Request Created At System Header With Valid Date Format - Param : {0} --> {1}")
    @CsvSource({"Valid_Date_Format, 2012-03-19T07:22:00Z",
            "Valid_Date_Format, 2002-10-02T15:00:00-10:00",
            "Valid_Date_Format, 2002-10-02T15:00:00+05:00",
            "Valid_Date,2099-10-02T15:00:00Z"})
    @Override
    public void test_request_created_at_with_valid_values(final String requestCreatedAtKey, final String requestCreatedAtVal) throws Exception {

        generateResourcesByUserPayloadWithRandomHMCTSId();
        super.test_request_created_at_with_valid_values(requestCreatedAtKey, requestCreatedAtVal);

    }


    @ParameterizedTest(name = "Request Processed At System Header With Valid Date Format - Param : {0} --> {1}")
    @CsvSource({"Valid_Date_Format,2002-10-02T10:00:00-05:00",
            "Valid_Date_Format,2002-10-02T15:00:00Z",
            "Valid_Date,2099-10-02T15:00:00Z"
    })
    //@Disabled("TODO - Enable the following tests after MCGIRRSD-1745 and MCGIRRSD-1776")
    @Override
    public void test_request_processed_at_with_valid_values(String requestProcessedAtKey, String requestProcessedAtVal) throws Exception {
        generateResourcesByUserPayloadWithRandomHMCTSId();
        super.test_request_processed_at_with_valid_values(requestProcessedAtKey, requestProcessedAtVal);
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

   /* @ParameterizedTest(name = "Source System Header invalid values - Param : {0} --> {1}")
    @CsvSource(value = {"Null_Value, NIL", "Empty_Space,''", "Invalid_Source_System, SNL", "Invalid_Source_System, CfT","Invalid_Source_System, Anybody", "Invalid_Source_System, S&amp;L"}, nullValues = "NIL")
    void test_source_system_invalid_values(String sourceSystemKey, String sourceSystemVal) throws Exception {
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createHeaderWithSourceSystemValue(getApiSubscriptionKey(), sourceSystemVal), HttpStatus.BAD_REQUEST);
        commonDelegate.test_expected_response_for_supplied_header(delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, null, null, null));
    }*/
}
