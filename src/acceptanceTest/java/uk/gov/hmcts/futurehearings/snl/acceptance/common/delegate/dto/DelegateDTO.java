package uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.dto;

import uk.gov.hmcts.futurehearings.snl.acceptance.common.dto.SNLDto;

import java.util.Map;

import io.restassured.http.Headers;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@Setter(AccessLevel.PUBLIC)
@Getter(AccessLevel.PUBLIC)
@Builder
@Accessors(fluent = true)
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class DelegateDTO implements SNLDto {

    final String targetSubscriptionKey;
    final String authorizationToken;
    final String targetURL;
    final String inputPayload;
    final Map<String, String> standardHeaderMap;
    final Headers headers;
    final Map<String, String> params;
    final HttpMethod httpMethod;
    final HttpStatus status;
}
