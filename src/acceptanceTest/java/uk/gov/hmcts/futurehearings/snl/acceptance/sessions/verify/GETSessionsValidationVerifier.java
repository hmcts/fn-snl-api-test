package uk.gov.hmcts.futurehearings.snl.acceptance.sessions.verify;

import static org.junit.jupiter.api.Assertions.assertTrue;

import uk.gov.hmcts.futurehearings.snl.acceptance.common.dto.SNLDto;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.success.SNLSuccessVerifier;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GETSessionsValidationVerifier implements SNLSuccessVerifier {

    @Override
    public void verify(SNLDto snlDto, Response response) {
        log.debug(response.getBody().asString());
        assertTrue(response.getBody().jsonPath().getMap("$").size() > 0);
    }
}
