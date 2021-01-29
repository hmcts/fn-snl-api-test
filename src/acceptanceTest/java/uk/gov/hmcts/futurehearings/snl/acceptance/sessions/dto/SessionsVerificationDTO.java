package uk.gov.hmcts.futurehearings.snl.acceptance.sessions.dto;

import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.dto.SNLVerificationDTO;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;

@Setter(AccessLevel.PUBLIC)
@Getter(AccessLevel.PUBLIC)
@Accessors(fluent = true)
@ToString
public class SessionsVerificationDTO extends SNLVerificationDTO {

    private String requestJudgeType = null;
    private String requestSessionType = null;
    private String requestDuration = null;
    private String requestLocationID = null;
    private String requestStartDate = null;
    private String requestEndDate = null;

    public SessionsVerificationDTO(final HttpStatus httpStatus,
                                   final String errorCode,
                                   final String errorDescription,
                                   final String errorLinkID) {

        super(httpStatus,
                errorCode,
                errorDescription,
                errorLinkID);
    }

    public void setRequestJudgeType (final String requestJudgeType) {
        this.requestJudgeType = requestJudgeType;
    }

    public void setRequestSessionType(final String requestSessionType) {
        this.requestSessionType = requestSessionType;
    }

    public void setRequestDuration (final String requestDuration) {
        this.requestDuration = requestDuration;
    }

    public void setRequestLocationID (final String requestLocationID) {
        this.requestLocationID = requestLocationID;
    }

    public void setRequestStartDate (final String requestStartDate) {
        this.requestStartDate = requestStartDate;
    }

    public void setRequestEndDate (final String requestEndDate) {
        this.requestEndDate = requestEndDate;
    }
}
