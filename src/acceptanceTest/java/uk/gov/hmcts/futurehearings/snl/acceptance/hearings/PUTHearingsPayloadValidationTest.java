package uk.gov.hmcts.futurehearings.snl.acceptance.hearings;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
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
import uk.gov.hmcts.futurehearings.snl.Application;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.RestClientTemplate;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.TestingUtils;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.CommonDelegate;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.dto.DelegateDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.dto.SNLVerificationDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.error.SNLCommonErrorVerifier;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.success.SNLCommonSuccessVerifier;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

import static uk.gov.hmcts.futurehearings.snl.acceptance.common.TestingUtils.generateStringForGivenLength;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.TestingUtils.replaceCharacterSequence;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createCompletePayloadHeader;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createStandardPayloadHeader;

@Slf4j
@SpringBootTest(classes = {Application.class})
@ActiveProfiles("acceptance")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SelectClasses(PUTHearingsPayloadValidationTest.class)
@IncludeTags("Put")
class PUTHearingsPayloadValidationTest extends HearingsHeaderValidationTest {
    //private static final String INPUT_FILE_PATH = "uk/gov/hmcts/futurehearings/snl/acceptance/%s/input";

    @Qualifier("CommonDelegate")
    @Autowired(required = true)
    private CommonDelegate commonDelegate;

    @Value("${hearingsApi_idRootContext}")
    private String hearingsApi_idRootContext;

    @Value("${hearingsApiRootContext}")
    private String hearingsApiRootContext;

    public String caseListingRequestId = null;
    public String caseHMCTSId = null;

    @BeforeAll
    public void initialiseValues() throws Exception {
        super.initialiseValues();
        this.setInputPayloadFileName("create-hearing-request-complete.json");
        int result[] = makePostHearingAndFetchRandomIdAndCaseListingId();
        this.caseListingRequestId = String.valueOf(result[0]);
        this.caseHMCTSId = String.valueOf(result[1]);
        this.hearingsApi_idRootContext = String.format(hearingsApi_idRootContext, caseListingRequestId);
        this.setRelativeURL(hearingsApi_idRootContext);
        this.setHttpMethod(HttpMethod.PUT);
        this.setHttpSuccessStatus(HttpStatus.ACCEPTED);
        this.setRelativeURLForNotFound(this.getRelativeURL().replace("hearings", "hearing"));
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_FILE_PATH, getInputFileDirectory())
                + "/" + getInputPayloadFileName()), caseListingRequestId));
        this.setSnlSuccessVerifier(new SNLCommonSuccessVerifier());
        this.setSnlErrorVerifier(new SNLCommonErrorVerifier());
    }

    @Test
    @DisplayName("Successfully validated response for a payload with all the mandatory required fields")
    public void test_successful_response_with_all_mandatory_elements_payload() throws Exception {
        this.setInputPayloadFileName("update-hearing-request-complete.json");
        generatePayloadWithRandomHMCTSID("/put/",caseHMCTSId);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }


    @DisplayName("Successful Update CaseTitle Positive Tests for Single Field")
    @ParameterizedTest(name = "Update Case Title Positive Tests for Single Field")
    @CsvSource(value = {"Update case title 500 chars,t", "Update case title 499 chars,t"}, nullValues = "NIL")
    public void test_successful_response_by_updating_mandatory_case_title_elements_payload(final String caseTitleTemplateKey,
                                                                                           final String caseTitleTemplateValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-case-title-template.json");
        if (caseTitleTemplateKey.trim().equals("Update case title 500 chars")) {
            generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, generateStringForGivenLength(500, caseTitleTemplateValue));
        } else {
            generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, generateStringForGivenLength(499, caseTitleTemplateValue));
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));

    }

    @DisplayName("caseTitle update negative tests for single field")
    @ParameterizedTest(name = "Update Case tilte Nagative Tests for Single Field")
    @CsvSource(value = {"CaseTitle501Chars,t"}, nullValues = "NIL")
    // TODO: we need to test with "Empty Space,''", "Single Space,' '"
    //TODO - Raised Defect created by Venkata (MCGIRRSD-1683) around blank case titles. - Data "Empty Space,''", "Single Space,' '",
    public void test_Negative_response_by_updating_invalid_case_title_elements_payload(final String caseTitleTemplateKey,
                                                                                       final String caseTitleTemplateValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-case-title-template.json");
        if (caseTitleTemplateKey.trim().equals("CaseTitle501Chars")) {
            generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, generateStringForGivenLength(501, caseTitleTemplateValue));
        } else {
            generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, caseTitleTemplateValue);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        SNLVerificationDTO snlVerificationDTO = caseTitleTemplateKey.equals("CaseTitle501Chars")
                ? new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.hearingRequest._case.caseTitle: may only be 500 characters long]", null)
                : new SNLVerificationDTO(HttpStatus.BAD_REQUEST, null, null, null);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                snlVerificationDTO);

    }

    @DisplayName("Successful case jurisdiction update positive tests for single field")
    @ParameterizedTest(name = "Successful update case jurisdiction positive Tests for Single Field")
    @CsvSource(value = {"caseJurisdiction,FAM", "caseJurisdiction,CIV"}, nullValues = "NIL")
    public void test_successful_response_by_updating_mandatory_case_jurisdiction_elements_payload(final String caseJurisdictionTemplateKey,
                                                                                                  final String caseJurisdictionTemplateValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-case-jurisdiction-template.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, caseJurisdictionTemplateValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));

    }

    @DisplayName("Update case jurisdiction Negative Tests for Single Field")
    @ParameterizedTest(name = "Update case jurisdiction negative tests for single field")
    @CsvSource(value = {"caseJurisdiction,''", "caseJurisdiction,' '", "caseJurisdiction, VICKY"}, nullValues = "NIL")
    public void test_negative_response_by_updating_invalid_case_jurisdiction_elements_payload(final String caseJurisdictionKey, String caseJurisdictionValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-case-jurisdiction-template.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, caseJurisdictionValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        SNLVerificationDTO snlVerificationDTO = null;
        switch (caseJurisdictionKey) {
            case "caseJurisdiction":
                switch (caseJurisdictionValue) {
                    case "":
                    case " ":
                        snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", "' ' is not a valid value for field 'caseJurisdiction'", null);
                        break;
                    default:
                        snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.hearingRequest._case.caseJurisdiction: may only be 4 characters long]", null);
                        break;
                }
                break;
            default:
                break;
        }
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                snlVerificationDTO);

    }

    @ParameterizedTest(name = "Successful Case Court update Positive Tests for Single Field")
    @CsvFileSource(resources = "/uk/gov/hmcts/futurehearings/snl/acceptance/hearings/data/case-court-test-positive-values.csv", numLinesToSkip = 1)
    @DisplayName("Successful Update case Case Court Positive Tests for Single Field")
    public void test_successful_response_with_case_court_mandatory_elements_payload(final String caseCourtKey, String caseCourtValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-case-court-template.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, caseCourtValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "Update Negative caseCourt  tests")
    @CsvSource(value = {"caseCourt,''", "caseCourt,' '", "caseCourt, 0", "caseCourt, 11", "caseCourt, 4.5", "caseCourt, 467544446"}, nullValues = "NIL")
    @DisplayName("Update Negative validated response for caseCourt field tests")
    public void test_negative_response_by_updating_invalid_case_court_elements_payload(final String caseCourtKey, final String caseCourtValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-case-court-template.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, caseCourtValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        SNLVerificationDTO snlVerificationDTO = null;
        switch (caseCourtKey) {
            case "caseCourt":
                switch (caseCourtValue) {
                    case "":
                    case " ":
                    case "0":
                    case "11": // check this response is not as expected
                    case "4.5":
                        snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", "'" + caseCourtValue + "' is not a valid value for field 'caseCourt'", null);
                        break;
                    default:
                        snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.hearingRequest._case.caseCourt: may only be 8 characters long]", null);
                        break;
                }
                break;
            default:
                break;
        }
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                snlVerificationDTO);
    }


    @ParameterizedTest(name = "Update case registered field positive tests")
    @CsvSource(value = {"update year, 2021-01-13T20:20:39+00:00", //Can we updated this field ?
            "update month, 2020-02-13T20:20:39+00:00",//Can we updated this field ?
            "update date, 2020-01-14T20:20:39+00:00",//Can we updated this field ?
            "update hour, 2020-01-13T21:20:39+00:00",
            "update minute, 2020-01-14T20:30:39+00:00",
            "update second, 2020-01-13T20:20:49+00:00",
            "update Misecond, 2020-01-13T20:20:39+20:00",}, nullValues = "NIL") //you can't update
    @DisplayName("Successful Update case Case registered Positive Tests for Single Field")
    //TODO - The Time formats for payload is pending confirmation from McGirr
    public void test_successful_response_with_case_registered_mandatory_elements_payload(final String caseRegisteredKey,
                                                                                         String caseRegisteredValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-case-registered-template.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, caseRegisteredValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "Update case registered field Negative tests")
    @CsvFileSource(resources = "/uk/gov/hmcts/futurehearings/snl/acceptance/common/data/case-registered-test-negative-values.csv", numLinesToSkip = 1)
    //Check three tests failed with time formats : 2015-12-11T09:28:30 ,2015-12-11T09:28:30.45, 2015-10-11T10:28:35Z
    @DisplayName("Update Negative validated response for Case registered field")
    public void test_negative_response_with_case_registered_mandatory_elements_payload(final String caseRegisteredKey, String caseRegisteredValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-case-registered-template.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, caseRegisteredValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("[$.hearingRequest._case.caseRegistered: {0} is an invalid date-time]", caseRegisteredValue);
        SNLVerificationDTO snlVerificationDTO = null;
        switch (caseRegisteredValue) {
            case "":
            case " ":
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "'" + caseRegisteredValue + "' is not a valid value for field 'caseCourt'", null);
                break;
            default:
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "'" + caseRegisteredValue + "' is not a valid value for field 'caseCourt'", null);
                break;
        }
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                snlVerificationDTO);
    }

    @ParameterizedTest(name = "Update listing Court positive tests")
    @CsvFileSource(resources = "/uk/gov/hmcts/futurehearings/snl/acceptance/hearings/data/listing-court-test-positive-values.csv", numLinesToSkip = 1)
    @DisplayName("Update Successful response for a payload with the listing Court")
    public void test_successful_response_by_updating_listing_court_mandatory_elements_payload(final String listingCourtKey, String listingCourtValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-listing-court-template.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, listingCourtValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "Update listing Court Negative tests")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "NIL,NIL", "InValid Court,0", "Valid Court,299", "Valid Court,500", "Valid Court,-318"})
    @DisplayName("Update Negative response for a payload with the listing Court")
    public void test_negative_response_by_updating_listing_court_mandatory_elements_payload(final String listingCourtKey, String listingCourtValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-listing-court-template.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, listingCourtValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("'{0}' is not a valid value for field 'listingCourt'", listingCourtValue);
        SNLVerificationDTO snlVerificationDTO = null;
        switch (listingCourtValue) {
            case "":
            case " ":
            case "NIL":
            case "0":
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1002", "'" + listingCourtValue + "' is not a valid value for field 'listingCourt'", null);
                break;
            default:
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1002", "'" + listingCourtValue + "' is not a valid value for field 'listingCourt'", null);
                break;
        }
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                snlVerificationDTO);
    }

    @ParameterizedTest(name = "Update listing Priority positive tests")
    @CsvSource(value = {"Update Listing priority, CRIT", "Listing priority, HIGH", "Listing priority, NORM"}, nullValues = "NIL")
    @DisplayName("Update Successful response for a payload with the listing Priority")
    public void test_successful_response_by_updating_listing_priority_mandatory_elements_payload(final String listingPriorityKey, String listingPriorityValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-listing-priority-template.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, listingPriorityValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "Update listing Priority negative tests")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "NIL,NIL", "Listing priority, Critical", "Listing priority, High", "Listing priority, Normal", "Listing priority, ABC"}, nullValues = "NIL")
    @DisplayName("Update Negative response for a payload with the listing Priority")
    public void test_negative_response_by_updating_listing_priority_mandatory_elements_payload(final String listingPriorityKey, String listingPriorityValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-listing-priority-template.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, listingPriorityValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("'{0}' is not a valid value for field 'listingPriority'", listingPriorityValue);
        SNLVerificationDTO snlVerificationDTO = null;
        switch (listingPriorityValue) {
            case "":
            case " ":
            case "NIL":
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", "'" + listingPriorityValue + "' is not a valid value for field 'listingPriority'", null);
                break;
            default:
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", "'" + listingPriorityValue + "' is not a valid value for field 'listingPriority'", null);
                break;
        }
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                snlVerificationDTO);
    }

    @ParameterizedTest(name = "Update listing Session Type positive tests")
    @CsvFileSource(resources = "/uk/gov/hmcts/futurehearings/snl/acceptance/hearings/data/listing-session-type-test-positive-values.csv", numLinesToSkip = 1)
    @DisplayName("Update Successful response for a payload with the listing session type")
    public void test_successful_response_by_updating_listing_session_type_mandatory_elements_payload(final String listingSessionTypeKey, String listingSessionTypeValue, String listingTypeValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-listing-session-type-template.json");
        generatePayloadUpdateWithThreeField("/put/", caseHMCTSId, listingSessionTypeValue, listingTypeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "Update listing Session Type negative tests")
    @CsvFileSource(resources = "/uk/gov/hmcts/futurehearings/snl/acceptance/hearings/data/listing-session-type-test-negative-values.csv", numLinesToSkip = 1)
    @DisplayName("Update Negative response for a payload with the listing session type")
    public void test_negative_response_by_updating_listing_session_type_mandatory_elements_payload(final String listingSessionTypeKey, String listingSessionTypeValue, String listingTypeValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-listing-session-type-template.json");
        generatePayloadUpdateWithThreeField("/put/", caseHMCTSId, listingSessionTypeValue, listingTypeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("'{0}' is not a valid value for field 'listingType'", listingTypeValue);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, errorDesc, null, null));
    }

    @ParameterizedTest(name = "Update listingType positive tests")
    @CsvSource(value = {"listingType, DECNIS"}, nullValues = "NIL")
    @DisplayName("Update Successful response for a payload with the listingType")
    public void test_successful_response_by_updating_listing_type_mandatory_elements_payload(final String listingTypeKey, String listingTypeValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-listing-type-template.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, listingTypeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "Update listingType negative tests")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "NIL,NIL", "listingType, ABC"}, nullValues = "NIL")
    @DisplayName("Update Negative response for a payload with the listingType ")
    public void test_negative_response_by_updating_listing_type_mandatory_elements_payload(final String listingTypeKey, String listingTypeValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-listing-type-template.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, listingTypeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("'{0}' is not a valid value for field 'listingType'", listingTypeValue);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", errorDesc, null));
    }

    @ParameterizedTest(name = "Update Case Sub Type non mandatory negative tests")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "NIL,NIL", "case subtype min, 0", "case subtype max,94", "case subtype, abc"}, nullValues = "NIL")
    @DisplayName("Update Negative response for a payload with the case sub type")
    public void test_negative_response_by_updating_case_sub_type_complete_elements_payload(final String caseSubTypeKey, String caseSubTypeValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-case-sub-type-template.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, caseSubTypeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("'{0}' is not a valid value for field 'caseSubType'", caseSubTypeValue);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", errorDesc, null));
    }

    @ParameterizedTest(name = "Update Case comments non mandatory positive tests")
    @CsvSource(value = {"Valid entityLastName 5000,t"}, nullValues = "NIL")
    @DisplayName("Update Successful response for a payload with the case comments")
    public void test_successful_response_with_case_comments_complete_elements_payload(final String caseCommentsKey, String caseCommentsValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-case-comments-template.json");
        if (caseCommentsKey.trim().equals("Valid entityLastName 5000")) {
            generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, generateStringForGivenLength(5000, caseCommentsValue));
        } else {
            generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, caseCommentsValue);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "Update Case comments non mandatory negative tests")
    @CsvSource(value = {"InValid entityLastName 5001,t"}, nullValues = "NIL")
    @DisplayName("Update Negative response for a payload with the case comments")
    public void test_negative_response_by_updating_case_comments_complete_elements_payload(final String caseCommentsKey, String caseCommentsValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-case-comments-template.json");
        if (caseCommentsKey.trim().equals("InValid entityLastName 5001")) {
            generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, generateStringForGivenLength(5000, caseCommentsValue));
        } else {
            generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, caseCommentsValue);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("[$.hearingRequest._{0}: may only be 5000 characters long]", caseCommentsValue);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", errorDesc, null));
    }

    @ParameterizedTest(name = "Update Case Restricted Flag non mandatory positive tests")
    @CsvSource(value = {"Case Restricted Flag, true", "Case Restricted Flag, false"})
    //TODO - Negative testing of this scenario has to be done manually as Rest Assured is failing on the client side for incompatible datatypes against no string fields....
    @DisplayName("Update Successful response for a payload with the Case restricted flag")
    public void test_successful_response_by_updating_case_restricted_flag_complete_elements_payload(final String caseRestrictedFlagKey, String caseRestrictedFlagValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-case-restricted-flag-template.json");
        if (caseRestrictedFlagValue.trim().equals("true")) {
            generatePayloadWithFieldTokenReplace(caseHMCTSId, "\"caseRestrictedFlag\": 0", "\"caseRestrictedFlag\":" + true, "/put/", caseRestrictedFlagValue);
        } else {
            generatePayloadWithFieldTokenReplace(caseHMCTSId, "\"caseRestrictedFlag\": 0", "\"caseRestrictedFlag\":" + false, "/put/", caseRestrictedFlagValue);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "Update Case Interpreter Required Flag non mandatory positive tests")
    @CsvSource(value = {"Case Interpreter Required, true", "Case Interpreter Required, false"})
    //TODO - Negative testing of this scenario has to be done manually as Rest Assured is failing on the client side for incompatible datatypes against no string fields....
    @DisplayName("Update Successful response for a payload with the Case Interpreter Required")
    public void test_successful_response_by_updating_case_interpreter_required_flag_complete_elements_payload(final String caseInterpreterRequiredFlagKey, String caseInterpreterRequiredFlagValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-case-interpreter-restricted-falg-template.json");
        if (caseInterpreterRequiredFlagValue.trim().equals("true")) {
            generatePayloadWithFieldTokenReplace(caseHMCTSId, "\"caseInterpreterRequiredFlag\": 0", "\"caseInterpreterRequiredFlag\":" + true, "/put/", caseInterpreterRequiredFlagValue);
        } else {
            generatePayloadWithFieldTokenReplace(caseHMCTSId, "\"caseInterpreterRequiredFlag\": 0", "\"caseInterpreterRequiredFlag\":" + false, "/put/", caseInterpreterRequiredFlagValue);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "Update Case Additional Security Flag non mandatory positive tests")
    @CsvSource(value = {"Case Additional Security, true", "Case Additional Security, false"})
    //TODO - Negative testing of this scenario has to be done manually as Rest Assured is failing on the client side for incompatible datatypes against no string fields....
    @DisplayName("Update Successful response for a payload with the Case Additional Security flag")
    public void test_successful_response_by_updating_case_additional_security_flag_complete_elements_payload(final String caseAdditionalSecurityFlagKey, String caseAdditionalSecurityFlagValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-case-additional-security-flag-template.json");
        if (caseAdditionalSecurityFlagValue.trim().equals("true")) {
            generatePayloadWithFieldTokenReplace(caseHMCTSId, "\"caseAdditionalSecurityFlag\": 0", "\"caseAdditionalSecurityFlag\":" + true, "/put/", caseAdditionalSecurityFlagValue);
        } else {
            generatePayloadWithFieldTokenReplace(caseHMCTSId, "\"caseAdditionalSecurityFlag\": 0", "\"caseAdditionalSecurityFlag\":" + false, "/put/", caseAdditionalSecurityFlagValue);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "listingAutoCreateFlag non mandatory positive tests")
    @CsvSource(value = {"listingAutoCreateFlag, true", "listingAutoCreateFlag, false"})
    //TODO - Negative testing of this scenario has to be done manually as Rest Assured is failing on the client side for incompatible datatypes against no string fields....
    @DisplayName("Update Successful response for a payload with the listingAutoCreateFlag")
    public void test_successful_response_by_updating_listing_auto_create_flag_complete_elements_payload(final String listingAutoCreateFlagKey, String listingAutoCreateFlagValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-non-mandatory-listing-auto-create-flag.json");
        if (listingAutoCreateFlagValue.trim().equals("true")) {
            generatePayloadWithFieldTokenReplace(caseHMCTSId, "\"listingAutoCreateFlag\": 0", "\"listingAutoCreateFlag\":" + true, "/put/", listingAutoCreateFlagValue);
        } else {
            generatePayloadWithFieldTokenReplace(caseHMCTSId, "\"listingAutoCreateFlag\": 0", "\"listingAutoCreateFlag\":" + false, "/put/", listingAutoCreateFlagValue);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "Update listingStartDate non mandatory positive tests")
    @CsvSource(value = {"listingStartDate, 2015-12-11T19:28:35Z"}) // Can we update this filed ??
    @DisplayName("Update Successful response for a payload with the listingStartDate")
    public void test_successful_response_by_updating_listing_start_date_complete_elements_payload(final String listingStartDateKey, String listingStartDateValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-non-mandatory-listing-start-date.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, listingStartDateValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    /* 3 tests failed bug raised: MCGIRRSD-1987*/
    @ParameterizedTest(name = "Update listingStartDate non mandatory negative tests")
    @CsvFileSource(resources = "/uk/gov/hmcts/futurehearings/snl/acceptance/common/data/case-registered-test-negative-values.csv", numLinesToSkip = 1)
    @DisplayName("Update Negative response for a payload with the listingStartDate")
    public void test_negative_response_by_updating_listing_start_date_complete_elements_payload(final String listingStartDateKey, String listingStartDateValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-non-mandatory-listing-start-date.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, listingStartDateValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("[$.hearingRequest.listing.listingStartDate: {0} is an invalid date-time]", listingStartDateValue);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", null, null));
    }

    @ParameterizedTest(name = "Update listingEndDate non mandatory positive tests")
    @CsvSource(value = {"listingEndDate, 2015-11-10T09:28:35Z,2015-12-11T09:28:35Z", "listingEndDateStartDateEqual,2015-10-11T19:28:35Z,2015-10-11T19:28:35Z"})
    @DisplayName("Update successful response for a payload with the listingEndDate")
    public void test_successful_response_by_updating_listing_end_date_complete_elements_payload(final String listingEndDateKey, String listingEndDateValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-non-mandatory-listing-end-date.json");
        //Checking startDate<=endDate
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, listingEndDateValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    /* 3 tests failed bug raised: MCGIRRSD-1987*/
    @ParameterizedTest(name = "Update listingEndDate non mandatory negative tests")
    @CsvFileSource(resources = "/uk/gov/hmcts/futurehearings/snl/acceptance/common/data/case-registered-test-negative-values.csv", numLinesToSkip = 1)
    @DisplayName("Update Negative response for a payload with the listingEndDate")
    public void test_negative_response_by_updating_listing_end_date_complete_elements_payload(final String listingEndDateKey, String listingEndDateValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-non-mandatory-listing-end-date.json");
        //Checking startDate<=endDate
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, listingEndDateValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("[$.hearingRequest.listing.listingStartDate: {0} is an invalid date-time]", listingEndDateValue);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", null, null));
    }


    // No negative tests because we don't know the AC
    @ParameterizedTest(name = "update listingNumberOfAttendees non mandatory positive tests")
    @CsvSource(value = {"listingNumberOfAttendees, 1", "listingNumberOfAttendees, 10000"}, nullValues = "NIL")
    @DisplayName("update successfully response for a payload with the listingNumberOfAttendees")
    public void test_successful_response_by_updating_listing_Number_Attendees_elements_payload(final String listingNumberOfAttendeesKey, String listingNumberOfAttendeesValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-non-mandatory-listing-number-attendees-template.json");
        generatePayloadWithFieldTokenReplace(caseHMCTSId, "\"listingNumberAttendees\": 0", "\"listingNumberAttendees\":" + Integer.parseInt(listingNumberOfAttendeesValue), "/put/", listingNumberOfAttendeesValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "update listingCluster non mandatory positive tests")
    @CsvSource(value = {"listingCluster, TV", "entityClassCode, KNT"}, nullValues = "NIL")
    @DisplayName("Update successful response for a payload with the listingCluster")
    public void test_successful_response_by_updating_listing_cluster_payload(final String listingClusterKey, String listingClusterValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-non-mandatory-listing-cluster.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, listingClusterValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "update listingCluster non mandatory negative tests")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "NIL,NIL", "listingCluster, ABC"}, nullValues = "NIL")
    @DisplayName("Update Negative response for a payload with the listingCluster")
    public void test_negative_response_by_updating_listing_cluster_payload(final String listingClusterKey, String listingClusterValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-non-mandatory-listing-cluster.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, listingClusterValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("'{0}' is not a valid value for field 'listingCluster'", listingClusterValue);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", errorDesc, null));
    }


    @ParameterizedTest(name = "update listingDuration non mandatory positive tests")
    //Listing duration default 30min and there is no min limit.
    @CsvSource(value = {"listingDuration, 30", "listingDuration, 5000", "listingDuration, 29", "listingDuration, 1"}, nullValues = "NIL")
    @DisplayName("Update successfully response for a payload with the listingDuration")
    public void test_successful_response_by_updating_listing_Duration_elements_payload(final String listingDurationsKey, String listingDurationValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-non-mandatory-listing-duration-template.json");
        generatePayloadWithFieldTokenReplace(caseHMCTSId, "\"listingDuration\": 0", "\"listingDuration\":" + Integer.parseInt(listingDurationValue), "/put/", listingDurationValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "update entityHmiId non mandatory positive tests")
    //EntityId we need to check the Acceptance criteria
    @CsvSource(value = {"entityHmiId, 1", "entityHmiId, 5000", "entityHmiId, 29"}, nullValues = "NIL")
    @DisplayName("Update successfully response for a payload with the entityHmiId")
    public void test_successful_response_by_updating_entity_hmi_id_payload(final String entityHmiIdKey, String entityHmiIdValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-non-mandatory-entity-id-template.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, entityHmiIdValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    //Doubt chek the excel sheet
    @ParameterizedTest(name = "update entityTypeCode non mandatory positive tests")
    //Test Failed: When entity type code as ORG   "errorDesc": "entityTypeCode/entityClassCode ORG/PERSON",
    @CsvSource(value = {"entityTypeCode, IND", "entityTypeCode, ORG"}, nullValues = "NIL")
    @DisplayName("Update successfully response for a payload with the entityTypeCodeValue")
    public void test_successful_response_by_updating_entity_type_code_payload(final String entityTypeCodeKey, String entityTypeCodeValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-non-mandatory-entity-type-coded-template.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, entityTypeCodeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "update entityTypeCode non mandatory negative tests")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "NIL,NIL", "entityTypeCode, ABC"}, nullValues = "NIL")
    @DisplayName("Update Negative response for a payload with the entityTypeCode")
    public void test_negative_response_by_updating_entity_type_code_payload(final String entityTypeCodeKey, String entityTypeCodeValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-non-mandatory-entity-type-coded-template.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, entityTypeCodeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("'{0}' is not a valid value for field 'entityTypeCode'", entityTypeCodeValue);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", errorDesc, null));
    }

    //tests failing we need to check the combinations
    @ParameterizedTest(name = "update entityRoleCode non mandatory positive tests")
    //REP and WIT not working Is there any combinations
    @CsvSource(value = {"entityRoleCode, DEF", "entityRoleCode, APL", "entityRoleCode, REP", "entityRoleCode, WIT"}, nullValues = "NIL")
    @DisplayName("Update successfully response for a payload with the entityRoleCode")
    public void test_successful_response_by_updating_entity_role_code_payload(final String entityRoleCodeKey, String entityRoleCodeValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-non-mandatory-entity-role-template.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, entityRoleCodeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "update entityRoleCode non mandatory negative tests")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "NIL,NIL", "entityRoleCode, ABC"}, nullValues = "NIL")
    @DisplayName("Update Negative response for a payload with the entityRoleCode")
    public void test_negative_response_by_updating_entity_role_code_payload(final String entityRoleCodeKey, String entityRoleCodeValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-non-mandatory-entity-role-template.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, entityRoleCodeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("'{0}' is not a valid value for field 'entityRoleCode'", entityRoleCodeValue);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", errorDesc, null));
    }

    @ParameterizedTest(name = "update entityClassCode non mandatory positive tests")
    //Test failing :  "errorDesc" : "entityTypeCode/entityClassCode IND/ORG"
    @CsvSource(value = {"entityClassCode, PERSON", "entityClassCode, ORG"}, nullValues = "NIL")
    @DisplayName("Update successfully response for a payload with the entityClassCode")
    public void test_successful_response_by_updating_entity_class_code_payload(final String entityClassCodeKey, String entityClassCodeValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-non-mandatory-entity-class-code-template.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, entityClassCodeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "update entityClassCode non mandatory negative tests")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "NIL,NIL", "entityClassCode, ABC"}, nullValues = "NIL")
    @DisplayName("Update Negative response for a payload with the entityClassCode")
    public void test_negative_response_by_updating_entity_class_code_payload(final String entityClassCodeKey, String entityClassCodeValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-non-mandatory-entity-class-code-template.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, entityClassCodeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("'{0}' is not a valid value for field 'entityClassCode'", entityClassCodeValue);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", errorDesc, null));
    }

    @ParameterizedTest(name = "update entityTitle non mandatory positive tests")
    //Test passed with Mrsssssss and InvalidTitle
    @CsvSource(value = {"entityTitle, Mr", "entityTitle, Mrs", "entityTitle, Mrsssssss", "entityTitle, InvalidTitle",}, nullValues = "NIL")
    @DisplayName("Update successfully response for a payload with the entityTitle")
    public void test_successful_response_by_updating_entity_title_payload(final String entityTitleKey, String entityTitleValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-non-mandatory-entity-title.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, entityTitleValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "update entityTitle non mandatory negative tests")
    //This test is passing because it is not checking max length, without value it is accepting (Not mandatory)
    //expected: <400> but was: <202>
    @CsvSource(value = {"Invalid entityTitle  16 chars,t"}, nullValues = "NIL")
    @DisplayName("Update Negative response for a payload with the entityTitle")
    public void test_negative_response_by_updating_entity_title_payload(final String entityTitleKey, String entityTitleValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-non-mandatory-entity-title.json");
        if (entityTitleKey.trim().equals("Invalid entityTitle  16 chars")) {
            generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, generateStringForGivenLength(16, entityTitleValue));
        } else {
            generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, entityTitleValue);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("'{0}' is not a valid value for field 'entityTitle'", entityTitleValue);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", errorDesc, null));
    }

    @ParameterizedTest(name = "update entityFirstName non mandatory positive tests")
    @CsvSource(value = {"entityFirstName, testFirstName updated"}, nullValues = "NIL")
    @DisplayName("Update successfully response for a payload with the entityFirstName")
    public void test_successful_response_by_updating_entity_first_name_payload(final String entityFirstNameKey, String entityFirstNameValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-non-mandatory-entity-first-name.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, entityFirstNameValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "update entityFirstName non mandatory negative tests")
    @CsvSource(value = {"Invalid entityFirstName 101,t"}, nullValues = "NIL")
    @DisplayName("Update Negative response for a payload with the entityTitle")
    public void test_negative_response_by_updating_entity_first_name_payload(final String entityFirstNameKey, String entityFirstNameValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-non-mandatory-entity-first-name.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, entityFirstNameValue);
        if (entityFirstNameKey.trim().equals("Invalid entityFirstName 101")) {
            this.setInputPayloadFileName("update-hearing-request-non-mandatory-entity-first-name.json");
            generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, generateStringForGivenLength(101, entityFirstNameValue));
        } else {
            generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, entityFirstNameValue);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("'{0}' is not a valid value for field 'entityTitle'", entityFirstNameValue);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", errorDesc, null));
    }

    @ParameterizedTest(name = "update entityLastName non mandatory positive tests")
    @CsvSource(value = {"entityLastName, testFirstName updated"}, nullValues = "NIL")
    @DisplayName("Update successfully response for a payload with the entityLastName")
    public void test_successful_response_by_updating_entity_last_name_payload(final String entityLastNameKey, String entityLastNameValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-non-mandatory-entity-last-name.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, entityLastNameValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "update entityLastName non mandatory negative tests")
    //This test is failing because it is not checking max length
    @CsvSource(value = {"Invalid entityLastName 731,t"}, nullValues = "NIL")
    @DisplayName("Update Negative response for a payload with the entityLastName")
    public void test_negative_response_by_updating_entity_last_name_payload(final String entityLastNameKey, String entityLastNameValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-non-mandatory-entity-last-name.json");
        if (entityLastNameKey.trim().equals("Invalid entityLastName 731")) {
            generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId,
                    generateStringForGivenLength(731, entityLastNameValue));
        } else {
            generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, entityLastNameValue);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("'{0}' is not a valid value for field 'entityTitle'", entityLastNameValue);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", errorDesc, null));
    }

    //Should check the combinations
    @ParameterizedTest(name = "Update entityCompanyName non mandatory positive tests")
    @CsvSource(value = {"entityCompanyName, entityCompanyName"}, nullValues = "NIL")
    @DisplayName("Update successfully response for a payload with the entityCompanyName")
    public void test_successful_response_by_updating_entity_company_name_payload(final String entityCompanyNameKey, String entityCompanyNameValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-non-mandatory-entity-company-name.json");
        generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, entityCompanyNameValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "entityCompanyName non mandatory negative tests")
    @CsvSource(value = {"Invalid entityCompanyName 2001,t"}, nullValues = "NIL")
    @DisplayName("Negative response for a payload with the entityCompanyName")
    public void test_negative_response_with_entity_company_name_payload(final String entityCompanyNameKey, String entityCompanyNameValue) throws Exception {
        this.setInputPayloadFileName("update-hearing-request-non-mandatory-entity-company-name.json");
        if (entityCompanyNameKey.trim().equals("Invalid entityCompanyName 2001")) {
            generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId,
                    generateStringForGivenLength(2001, entityCompanyNameValue));
        } else {
            generatePayloadWithRandomCaseIdHMCTS("/put/", caseHMCTSId, entityCompanyNameValue);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("'{0}' is not a valid value for field 'entityCompanyName'", entityCompanyNameValue);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", errorDesc, null));
    }

    private int[] makePostHearingAndFetchRandomIdAndCaseListingId() throws Exception {
        int randomId = new Random().nextInt(99999999);
        int caseListingRequestId = new Random().nextInt(8888);
        DelegateDTO delegateDTO = DelegateDTO.builder()
                .targetSubscriptionKey(getApiSubscriptionKey()).authorizationToken(getAuthorizationToken())
                .targetURL(hearingsApiRootContext)
                .inputPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_FILE_PATH, getInputFileDirectory()) +
                        "/template/put/" + getInputPayloadFileName()), randomId, caseListingRequestId))
                .standardHeaderMap(createCompletePayloadHeader())
                .headers(null)
                .params(getUrlParams())
                .httpMethod(HttpMethod.POST)
                .status(HttpStatus.ACCEPTED)
                .build();
        Response response = RestClientTemplate.shouldExecute(convertHeaderMapToRestAssuredHeaders(delegateDTO.standardHeaderMap()),
                delegateDTO.authorizationToken(),
                delegateDTO.inputPayload(), delegateDTO.targetURL(),
                delegateDTO.params(), delegateDTO.status(), delegateDTO.httpMethod());
        log.debug("POST Response : " + response.getBody().asString());
        return new int[]{randomId, caseListingRequestId};
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

    private void generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(final String templatePath, final String formatValue) throws IOException {
        final int randomId = new Random().nextInt(99999999);
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + templatePath + getInputPayloadFileName()), randomId, formatValue));
    }

    private void generatePayloadWithRandomHMCTSID(final String templatePath, final String formatValue) throws IOException {
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + templatePath + getInputPayloadFileName()),formatValue));
    }


    final void generatePayloadWithUpdatedField(final String formatKey, final String formatValue, final String templatePath) throws IOException {
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + templatePath + getInputPayloadFileName()), formatKey, formatValue));
    }

    private void generatePayloadUpdateWithThreeField(final String templatePath, final String formatValue1, final String formatValue2, final String formatValue3) throws IOException {
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + templatePath + getInputPayloadFileName()), formatValue1, formatValue2, formatValue3));
    }

    private void generatePayloadWithFieldValueFormat(final String formatValue, final String templatePath) throws IOException {
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + templatePath + getInputPayloadFileName()), formatValue));
    }

    private void generateHearingsPayloadWithRandomCaseIdHMCTS() throws IOException {
        final String randomID = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        System.out.println("this is the random id" + randomID);
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + "/put/" + getInputPayloadFileName()), randomID.substring(0, 29)));
    }

    private void generatePayloadWithRandomCaseIdHMCTS(final String templatePath, final String formatValue1, final String formatValue2) throws IOException {
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + templatePath + getInputPayloadFileName()), formatValue1, formatValue2));
    }

    private void generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace(final String token, final String value) throws IOException {
        final int randomId = new Random().nextInt(99999999);
        String formattedString = String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + "/" + getInputPayloadFileName()), randomId, value);
        this.setInputBodyPayload(replaceCharacterSequence(token, value, formattedString));
    }

    private void generatePayloadWithFieldTokenReplace(final String formatValue1, final String token, final String value, final String templatePath, final String formatValue2) throws IOException {
        String formattedString = String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + templatePath + getInputPayloadFileName()), formatValue1, token, value, formatValue2);
        this.setInputBodyPayload(replaceCharacterSequence(token, value, formattedString));
    }

}
