package uk.gov.hmcts.futurehearings.snl.acceptance.common.test;

import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.CommonDelegate;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.dto.DelegateDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.dto.SNLVerificationDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.error.SNLErrorVerifier;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.success.SNLSuccessVerifier;

import java.io.IOException;
import java.util.Map;

import io.restassured.http.Headers;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@Slf4j
@Setter
@Getter
public abstract class SNLCommonTest {

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

    public static final String INPUT_FILE_PATH = "uk/gov/hmcts/futurehearings/snl/acceptance/%s/input";
    public static final String INPUT_TEMPLATE_FILE_PATH = "uk/gov/hmcts/futurehearings/snl/acceptance/%s/input/template";

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
