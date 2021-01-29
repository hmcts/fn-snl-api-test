package uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.dto;

import uk.gov.hmcts.futurehearings.snl.acceptance.common.dto.SNLDto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;

//@Setter(AccessLevel.PUBLIC)
//@Getter(AccessLevel.PUBLIC)
//@Accessors(fluent = true)
@ToString
@EqualsAndHashCode
public class SNLVerificationDTO implements SNLDto {

    public HttpStatus httpStatus;
    public String errorCode;
    public String errorDescription;
    public String errorLinkID;

    public SNLVerificationDTO() {
        super();
    }

    public SNLVerificationDTO(final HttpStatus httpStatus,
                              final String errorCode,
                              final String errorDescription,
                              final String errorLinkID) {

        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
        this.errorLinkID = errorLinkID;
    }

    public HttpStatus httpStatus() {
        return this.httpStatus;
    }

    public void setHttpStatus(final HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String errorCode() {
        return this.errorCode;
    }

    public void setErrorCode(final String errorCode) {
        this.errorCode = errorCode;
    }

    public String errorDescription() {
        return this.errorDescription;
    }

    public void setErrorDescription(final String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public String errorLinkID() {
        return this.errorLinkID;
    }

    public void setErrorLinkID(final String errorLinkID) {
        this.errorLinkID = errorLinkID;
    }
}
