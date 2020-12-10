package uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate;

import static uk.gov.hmcts.futurehearings.snl.acceptance.common.RestClientTemplate.shouldExecute;

import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.dto.DelegateDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.dto.SNLDto;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.SNLVerifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component("CommonDelegate")
public class CommonDelegateImpl implements CommonDelegate {

    public void test_expected_response_for_supplied_header(final DelegateDTO delegateDTO,
                                                           final SNLVerifier SNLVerifier,
                                                           final SNLDto snlDto) throws IOException {

        log.debug("The value of the target header (Header Map) : " + delegateDTO.standardHeaderMap());
        log.debug("The value of the target header (Wiremock Header) :" + delegateDTO.standardHeaderMap());
        Headers standardRestAssuredHeaders = null;
        if (Objects.nonNull(delegateDTO.standardHeaderMap()) && delegateDTO.standardHeaderMap().size() > 0) {
            standardRestAssuredHeaders = convertHeaderMapToRestAssuredHeaders(delegateDTO.standardHeaderMap());
        } else {
            standardRestAssuredHeaders = delegateDTO.headers();
        }
        handleRestCall(delegateDTO.targetURL(), delegateDTO.inputPayload(), standardRestAssuredHeaders,
                delegateDTO.authorizationToken(), delegateDTO.params(), delegateDTO.httpMethod(),
                delegateDTO.status(), SNLVerifier, snlDto);
    }

    private void handleRestCall(final String targetURL,
                                final String inputPayload,
                                final Headers headers,
                                final String authorizationToken,
                                final Map<String, String> params,
                                final HttpMethod httpMethod,
                                final HttpStatus status,
                                final SNLVerifier SNLVerifier,
                                final SNLDto snlDto) throws IOException {

        switch (httpMethod) {
            case POST:
            case PUT:
            case DELETE:
                SNLVerifier.verify(snlDto,
                        performRESTCall(targetURL, headers, authorizationToken, params, httpMethod, status, inputPayload));
                break;
            case GET:
                SNLVerifier.verify(snlDto, performRESTCall(targetURL, headers, authorizationToken, params, httpMethod, status, inputPayload));
                break;
            case OPTIONS:
                performRESTCall(targetURL, headers, authorizationToken, params, httpMethod, status, inputPayload);
        }
    }

    private Response performRESTCall(final String targetURL,
                                     final Headers headers,
                                     final String authorizationToken,
                                     final Map<String, String> params,
                                     final HttpMethod httpMethod,
                                     final HttpStatus status,
                                     final String inputPayload) {
        return shouldExecute(
                headers,
                authorizationToken,
                inputPayload,
                targetURL,
                params,
                status,
                httpMethod);
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
}
