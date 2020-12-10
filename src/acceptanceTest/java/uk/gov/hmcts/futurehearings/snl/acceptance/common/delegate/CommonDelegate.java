package uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate;


import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.dto.DelegateDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.dto.SNLDto;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.SNLVerifier;

import java.io.IOException;
import java.util.Map;

import io.restassured.http.Headers;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

public interface CommonDelegate {


    public void test_expected_response_for_supplied_header(final DelegateDTO delegateDTO,
                                                           final SNLVerifier SNLVerifier,
                                                           final SNLDto snlDto) throws IOException;
}