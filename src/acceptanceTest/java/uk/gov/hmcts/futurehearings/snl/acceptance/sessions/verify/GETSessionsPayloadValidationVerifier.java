package uk.gov.hmcts.futurehearings.snl.acceptance.sessions.verify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import uk.gov.hmcts.futurehearings.snl.acceptance.common.dto.SNLDto;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.success.SNLSuccessVerifier;
import uk.gov.hmcts.futurehearings.snl.acceptance.sessions.dto.SessionsVerificationDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GETSessionsPayloadValidationVerifier implements SNLSuccessVerifier {

    @Override
    public void verify(SNLDto snlDTO, Response response) {

        final String responseBody = response.getBody().asString();
        log.debug("The value of the Response Body : " + responseBody);
        SessionsVerificationDTO sessionsVerificationDTO = null;
        if (snlDTO instanceof SessionsVerificationDTO) {
            sessionsVerificationDTO = (SessionsVerificationDTO) snlDTO;
        }
        final DocumentContext context = JsonPath.parse(responseBody);
        if (sessionsVerificationDTO.requestSessionType() != null
                && !sessionsVerificationDTO.requestSessionType().trim().equals("")
                && sessionsVerificationDTO.requestDuration() == null
                && sessionsVerificationDTO.requestLocationID() == null
                && sessionsVerificationDTO.requestJudgeType() == null
                && sessionsVerificationDTO.requestStartDate() == null
                && sessionsVerificationDTO.requestEndDate() == null) {

            final String requestSessionType = sessionsVerificationDTO.requestSessionType().trim();
            assertEquals(sessionsVerificationDTO.httpStatus().value(), response.statusCode());
            final List<String> sessionTypeList = context.read("$.sessionsResponse.sessions[*].sessionType");
            assertTrue(sessionTypeList.stream().allMatch(s -> {
                return s.equals(requestSessionType);
            }));

        } else if (sessionsVerificationDTO.requestDuration() != null
                && sessionsVerificationDTO.requestDuration().trim().equals("")
                && sessionsVerificationDTO.requestSessionType() != null
                && !sessionsVerificationDTO.requestSessionType().trim().equals("")) {

            final int requestDuration = Integer.parseInt(sessionsVerificationDTO.requestDuration());
            final List<Integer> sessionsList = context.read("$.sessionsResponse.sessions[*].sessionDuration");
            assertTrue(sessionsList.stream().allMatch(stringObjectMap -> {
                return stringObjectMap == requestDuration;
            }));
            final List<String> sessionTypeList = context.read(
                    "$.sessionsResponse.sessions[*].sessionType");

            assertTrue(sessionTypeList.stream().allMatch(s -> {
                return s.equals("ADHOC");
            }));

        } else if (sessionsVerificationDTO.requestJudgeType() != null
                && sessionsVerificationDTO.requestJudgeType().trim().equals("")
                && sessionsVerificationDTO.requestSessionType() != null
                && !sessionsVerificationDTO.requestSessionType().trim().equals("")) {

            final List<String> sessionsList = context.read("$.sessionsResponse.sessions[*].sessionJudges[*].sessionJudgeType");
            final String requestJudgeType = sessionsVerificationDTO.requestJudgeType();
            assertTrue(sessionsList.stream().allMatch(stringObjectMap -> {
                return stringObjectMap.equals(requestJudgeType);
            }));
            final List<String> sessionTypeList = context.read(
                    "$.sessionsResponse.sessions[*].sessionType");

            assertTrue(sessionTypeList.stream().allMatch(s -> {
                return s.equals("ADHOC");
            }));
        } else if (sessionsVerificationDTO.requestLocationID() != null
                && sessionsVerificationDTO.requestLocationID().trim().equals("")
                && sessionsVerificationDTO.requestSessionType() != null
                && !sessionsVerificationDTO.requestSessionType().trim().equals("")) {

            final List<Integer> sessionsList = context.read("$.sessionsResponse.sessions[*].sessionRoomId");
            assertTrue(sessionsList.stream().allMatch(stringObjectMap -> {
                return stringObjectMap == 301;
            }));
            final List<String> sessionTypeList = context.read(
                    "$.sessionsResponse.sessions[*].sessionType");

            assertTrue(sessionTypeList.stream().allMatch(s -> {
                return s.equals("ADHOC");
            }));
        } else if (sessionsVerificationDTO.requestStartDate() != null
                && !sessionsVerificationDTO.requestStartDate().trim().equals("")
                && sessionsVerificationDTO.requestEndDate() == null
                && sessionsVerificationDTO.requestSessionType() != null
                && !sessionsVerificationDTO.requestSessionType().trim().equals("")) {

            final LocalDateTime dateTimeInput = LocalDateTime.parse(sessionsVerificationDTO.requestStartDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
            log.debug("The value of the given date" + dateTimeInput);
            //log.debug("The value of the given date seconds" + dateTimeInput.getSecond());
            final List<String> sessionsList = context.read("$.sessionsResponse.sessions[*].sessionStartTime");
            assertTrue(sessionsList.stream().allMatch(s -> {
                final LocalDateTime dateTime = LocalDateTime.parse(s,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
                log.debug("The date Time in the list" + dateTime);
                return dateTime.isAfter(dateTimeInput) || dateTime.isEqual(dateTimeInput);
            }));
            final List<String> sessionTypeList = context.read(
                    "$.sessionsResponse.sessions[*].sessionType");

            assertTrue(sessionTypeList.stream().allMatch(s -> {
                return s.equals("ADHOC");
            }));
        } else if (sessionsVerificationDTO.requestEndDate() != null
                && !sessionsVerificationDTO.requestEndDate().trim().equals("")
                && sessionsVerificationDTO.requestStartDate() == null
                && sessionsVerificationDTO.requestSessionType() != null
                && !sessionsVerificationDTO.requestSessionType().trim().equals("")) {

            final LocalDateTime dateTimeInput = LocalDateTime.parse(sessionsVerificationDTO.requestEndDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
            log.debug("The value of the given date" + dateTimeInput);
            log.debug("The value of the given date seconds" + dateTimeInput.getSecond());
            final List<String> sessionsList = context.read("$.sessionsResponse.sessions[*].sessionEndTime");
            assertTrue(sessionsList.stream().allMatch(s -> {
                final LocalDateTime dateTime = LocalDateTime.parse(s,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
                return dateTime.isBefore(dateTimeInput) || dateTime.isEqual(dateTimeInput);
            }));
            final List<String> sessionTypeList = context.read(
                    "$.sessionsResponse.sessions[*].sessionType");

            assertTrue(sessionTypeList.stream().allMatch(s -> {
                return s.equals("ADHOC");
            }));
        } else if (sessionsVerificationDTO.requestStartDate() != null
                && !sessionsVerificationDTO.requestStartDate().trim().equals("")
                && sessionsVerificationDTO.requestEndDate() != null
                && !sessionsVerificationDTO.requestEndDate().trim().equals("")
                && sessionsVerificationDTO.requestSessionType() != null
                && !sessionsVerificationDTO.requestSessionType().trim().equals("")) {

            final LocalDateTime dateTimeStartDateInput = LocalDateTime.parse(sessionsVerificationDTO.requestStartDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
            final LocalDateTime dateTimeEndDateInput = LocalDateTime.parse(sessionsVerificationDTO.requestEndDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
            log.debug("The value of the given date" + dateTimeEndDateInput);
            log.debug("The value of the given date seconds" + dateTimeEndDateInput.getSecond());
            final List<String> sessionsList = context.read("$.sessionsResponse.sessions[*].sessionStartTime");
            assertTrue(sessionsList.stream().allMatch(s -> {
                final LocalDateTime dateTime = LocalDateTime.parse(s,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
                return (dateTime.isAfter(dateTimeStartDateInput) && dateTime.isBefore(dateTimeEndDateInput)
                        || (dateTime.isEqual(dateTimeStartDateInput) || dateTime.isEqual(dateTimeEndDateInput)));
            }));
            final List<String> sessionTypeList = context.read(
                    "$.sessionsResponse.sessions[*].sessionType");

            assertTrue(sessionTypeList.stream().allMatch(s -> {
                return s.equals("ADHOC");
            }));
        } else if (sessionsVerificationDTO.requestSessionType() != null
                && !sessionsVerificationDTO.requestSessionType().trim().equals("")
                && sessionsVerificationDTO.requestJudgeType() != null
                && !sessionsVerificationDTO.requestJudgeType().trim().equals("")
                && sessionsVerificationDTO.requestLocationID() != null
                && !sessionsVerificationDTO.requestLocationID().trim().equals("")) {

            final String requestJudgeType = sessionsVerificationDTO.requestJudgeType();
            final String requestSessionType = sessionsVerificationDTO.requestSessionType();
            final String requestLocationID = sessionsVerificationDTO.requestLocationID();

            final List<String> sessionsJudgeTypeList = context.read("$.sessionsResponse.sessions[*].sessionJudgeType");
            assertTrue(sessionsJudgeTypeList.stream().allMatch(s -> {
                return s.equals(requestJudgeType);
            }));

            final List<String> sessionsLocationIdList = context.read("$.sessionsResponse.sessions[*].sessionRoomId");
            assertTrue(sessionsJudgeTypeList.stream().allMatch(s -> {
                return s.equals(requestLocationID);
            }));

            final List<String> sessionTypeList = context.read(
                    "$.sessionsResponse.sessions[*].sessionType");

            assertTrue(sessionTypeList.stream().allMatch(s -> {
                return s.equals(requestSessionType);
            }));
        } else if (sessionsVerificationDTO.requestStartDate() != null
                && !sessionsVerificationDTO.requestStartDate().trim().equals("")
                && sessionsVerificationDTO.requestEndDate() != null
                && !sessionsVerificationDTO.requestEndDate().trim().equals("")
                && sessionsVerificationDTO.requestSessionType() != null
                && !sessionsVerificationDTO.requestSessionType().trim().equals("")
                && sessionsVerificationDTO.requestDuration() != null
                && !sessionsVerificationDTO.requestDuration().trim().equals("")) {

            final int requestDuration = Integer.parseInt(sessionsVerificationDTO.requestDuration());
            final List<Integer> sessionsDurationList = context.read("$.sessionsResponse.sessions[*].sessionDuration");
            assertTrue(sessionsDurationList.stream().allMatch(stringObjectMap -> {
                return stringObjectMap == requestDuration;
            }));

            final LocalDateTime dateTimeStartDateInput = LocalDateTime.parse(sessionsVerificationDTO.requestStartDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
            final LocalDateTime dateTimeEndDateInput = LocalDateTime.parse(sessionsVerificationDTO.requestEndDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
            log.debug("The value of the given date" + dateTimeEndDateInput);
            log.debug("The value of the given date seconds" + dateTimeEndDateInput.getSecond());
            final List<String> sessionsList = context.read("$.sessionsResponse.sessions[*].sessionStartTime");
            assertTrue(sessionsList.stream().allMatch(s -> {
                final LocalDateTime dateTime = LocalDateTime.parse(s,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
                return (dateTime.isAfter(dateTimeStartDateInput) && dateTime.isBefore(dateTimeEndDateInput)
                        || (dateTime.isEqual(dateTimeStartDateInput) || dateTime.isEqual(dateTimeEndDateInput)));
            }));
            final List<String> sessionTypeList = context.read(
                    "$.sessionsResponse.sessions[*].sessionType");

            assertTrue(sessionTypeList.stream().allMatch(s -> {
                return s.equals("ADHOC");
            }));
        }

    }
}
