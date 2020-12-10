package uk.gov.hmcts.futurehearings.snl.acceptance.common.verify;

import uk.gov.hmcts.futurehearings.snl.acceptance.common.dto.SNLDto;

import io.restassured.response.Response;

public interface SNLVerifier {

    void verify(SNLDto snlDto, Response response);
}
