package uk.gov.hmcts.futurehearings.snl.acceptance.hearings;

import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createStandardPayloadHeader;

import org.junit.jupiter.params.provider.CsvFileSource;
import uk.gov.hmcts.futurehearings.snl.Application;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.TestingUtils;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.CommonDelegate;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.dto.DelegateDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.dto.SNLVerificationDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.error.SNLCommonErrorVerifier;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.success.SNLCommonSuccessVerifier;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
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

@Slf4j
@SpringBootTest(classes = {Application.class})
@ActiveProfiles("acceptance")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SelectClasses(POSTHearingsPayloadValidationTest.class)
@IncludeTags("Post")
public class POSTHearingsPayloadValidationTest extends HearingsPayloadValidationTest {

    @Qualifier("CommonDelegate")
    @Autowired(required = true)
    private CommonDelegate commonDelegate;

    @Value("${hearingsApiRootContext}")
    private String hearingsApiRootContext;

    @BeforeAll
    public void initialiseValues() throws Exception {
        super.initialiseValues();
        this.setRelativeURL(hearingsApiRootContext);
        this.setHttpMethod(HttpMethod.POST);
        this.setInputPayloadFileName("hearing-request-mandatory-case-id-hmcts-template.json");
        this.setHttpSuccessStatus(HttpStatus.ACCEPTED);
        this.setRelativeURLForNotFound(this.getRelativeURL().replace("hearings", "hearing"));
        this.setSnlSuccessVerifier(new SNLCommonSuccessVerifier());
        this.setSnlErrorVerifier(new SNLCommonErrorVerifier());
        this.setInputBodyPayload(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH, getInputFileDirectory()) +
                "/" + getInputPayloadFileName()));
    }

    @Test
    @DisplayName("Successfully validated response for case id hmcts tests")
    public void test_successful_response_with_case_id_mandatory_elements_payload() throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-case-id-hmcts-template.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTS();
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "caseIDHMCTS Negative tests")
    @CsvSource(value = {"Empty Space,''", "Invalid_Source_System,C_FEFC2424-32A6-4B3A-BD97-023296C7F76DC_FEFC2424-32A6-4B3A-BD97-023296C7F76DC_FEFC2424-32A6-4B3A-BD97-023296C7F76D"}, nullValues = "NIL")
    @DisplayName("Negative validated response for case id hmcts tests")
    //TODO -  Check caseIDHMCTS length max and min
    //TODO - Does not check for blank Spaces - Defect to be raised. Data - "Single Space,' '",.
    public void test_negative_response_with_case_id_hmcts_mandatory_elements_payload(final String personHMCTSIDKey, final String personHMCTSIDValue) throws Exception {
        final String errorMessage = personHMCTSIDKey.equalsIgnoreCase("Empty Space") ? "[$.hearingRequest._case.caseIdHMCTS: must be at least 1 characters long]" : "[$.hearingRequest._case.caseIdHMCTS: may only be 30 characters long]";
        this.setInputPayloadFileName("hearing-request-mandatory-case-id-hmcts-template.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTS(personHMCTSIDValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", errorMessage, null));
    }

    @Test
    @DisplayName("Successfully validated response for case listing request id tests")
    public void test_successful_response_with_case_listing_request_id_mandatory_elements_payload() throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-case-listing-request-id.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndField(UUID.randomUUID().toString().substring(0, 9));
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "caseListingRequestId Negative tests")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "Invalid case listing requested id, C_FEFC2424-32A6-4B3A-BD97-023296C7F76DC_FEFC2424-32A6-4B3A-BD97-023296C7F76DC_FEFC2424-32A6-4B3A-BD97-023296C7F76D"}, nullValues = "NIL")
    @DisplayName("Negative validated response for case listing requested Id tests")
    public void test_negative_response_with_case_listing_request_id_mandatory_elements_payload() throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-case-listing-request-id.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndField(UUID.randomUUID().toString().substring(0, 9));
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @Test
    @DisplayName("Successfully validated response for case title tests")
    public void test_successful_response_with_case_title_mandatory_elements_payload() throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-case-title.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndField("Title of the Case");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "caseTitle Negative tests")
    @CsvSource(value = {"Invalid Case Title, C"}, nullValues = "NIL")
    @DisplayName("Negative validated response for case title tests")
    //TODO - Raised Defect created by Venkata(MCGIRRSD-1683) around blank case titles. - Data "Empty Space,''", "Single Space,' '",
    public void test_negative_response_with_case_title_mandatory_elements_payload(final String caseTitleKey, String caseTitleValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-case-title.json");
        caseTitleValue = caseTitleKey.equals("Invalid Case Title") ? createString(501, caseTitleValue.charAt(0)) : caseTitleValue;
        SNLVerificationDTO snlVerificationDTO = caseTitleKey.equals("Invalid Case Title")
                ? new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.hearingRequest._case.caseTitle: may only be 500 characters long]", null)
                : new SNLVerificationDTO(HttpStatus.BAD_REQUEST, null, null, null);
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndField(caseTitleValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                snlVerificationDTO);
    }

    @ParameterizedTest(name = "Jurisdiction Positive tests")
    @CsvSource(value = {"Valid LOV Value, FAM", "Valid LOV Value, CIV"}, nullValues = "NIL")
    @DisplayName("Successfully response for a payload with the Case Jurisdiction")
    public void test_successful_response_with_case_jurisdiction_mandatory_elements_payload(final String caseJurisdictionKey, String caseJurisdictionValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-case-jurisdiction.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndField(caseJurisdictionValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "Jurisdiction Negative tests")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "Invalid Case Jurisdiction, VIC"}, nullValues = "NIL")
    @DisplayName("Negative validated response for Jurisdiction tests")
    public void test_negative_response_with_case_jurisdiction_mandatory_elements_payload(final String caseJurisdictionKey, String caseJurisdictionValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-case-jurisdiction.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndField(caseJurisdictionValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", "'" + caseJurisdictionValue + "' is not a valid value for field 'caseJurisdiction'", null));
    }

    @ParameterizedTest(name = "caseCourt Positive tests")
    @CsvFileSource(resources = "/uk/gov/hmcts/futurehearings/snl/acceptance/hearings/data/case-court-test-positive-values.csv", numLinesToSkip = 1)
    @DisplayName("Successfully response for a payload with the Case Court")
    public void test_successful_response_with_case_court_mandatory_elements_payload(final String caseCourtKey, String caseCourtValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-case-court.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndField(caseCourtValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "caseCourt Negative tests")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "NIL,NIL", "Invalid Case Court, 0", "Invalid Case Court, 11", "Invalid Case Court, 4.5"}, nullValues = "NIL")
    @DisplayName("Negative validated response for caseCourt tests")
    public void test_negative_response_with_case_court_mandatory_elements_payload(final String caseCourtKey, final String caseCourtValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-case-court.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndField(caseCourtValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", "'" + caseCourtValue + "' is not a valid value for field 'caseCourt'", null));
    }

    @ParameterizedTest(name = "case registered positive tests")
    @CsvSource(value = {"Valid Time,2015-12-11T19:28:30.45Z"}, nullValues = "NIL")
    @DisplayName("Successfully response for a payload with the Case registered")
    //TODO - The Time formats for payload is pending confirmation from McGirr
    public void test_successful_response_with_case_registered_mandatory_elements_payload(final String caseRegisteredKey,
                                                                                         String caseRegisteredValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-case-registered.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndField(caseRegisteredValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "case registered Negative tests")
    @CsvFileSource(resources = "/uk/gov/hmcts/futurehearings/snl/acceptance/common/data/case-registered-test-negative-values.csv", numLinesToSkip = 1)
    @DisplayName("Negative response for a payload with the Case registered")

    public void test_negative_response_with_case_registered_mandatory_elements_payload(final String caseRegisteredKey, String caseRegisteredValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-case-registered.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndField(caseRegisteredValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("[$.hearingRequest._case.caseRegistered: {0} is an invalid date-time]", caseRegisteredValue);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", errorDesc, null));
    }

    @ParameterizedTest(name = "listing Court positive tests")
    @CsvFileSource(resources = "/uk/gov/hmcts/futurehearings/snl/acceptance/hearings/data/listing-court-test-positive-values.csv", numLinesToSkip = 1)
    @DisplayName("Successfully response for a payload with the listing Court")
    public void test_successful_response_with_listing_court_mandatory_elements_payload(final String listingCourtKey, String listingCourtValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-listing-court.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndField(listingCourtValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "listing Court Negative tests")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "NIL,NIL", "InValid Court,0", "Valid Court,299", "Valid Court,500", "Valid Court,-318"})
    @DisplayName("Negative response for a payload with the listing Court")
    public void test_negative_response_with_listing_court_mandatory_elements_payload(final String listingCourtKey, String listingCourtValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-listing-court.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndField(listingCourtValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("'{0}' is not a valid value for field 'listingCourt'", listingCourtValue);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1002", errorDesc, null));
    }

    @ParameterizedTest(name = "listing Priority positive tests")
    @CsvSource(value = {"Listing priority, CRIT", "Listing priority, HIGH","Listing priority, NORM"}, nullValues = "NIL")
    @DisplayName("Successfully response for a payload with the listing Priority")
    public void test_successful_response_with_listing_priority_mandatory_elements_payload(final String listingPriorityKey, String listingPriorityValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-listing-priority.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndField(listingPriorityValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "listing Priority negative tests")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "NIL,NIL","Listing priority, Critical", "Listing priority, High","Listing priority, Normal","Listing priority, ABC"}, nullValues = "NIL")
    @DisplayName("Negative response for a payload with the listing Priority")
    public void test_negative_response_with_listing_priority_mandatory_elements_payload(final String listingPriorityKey, String listingPriorityValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-listing-priority.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndField(listingPriorityValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("'{0}' is not a valid value for field 'listingPriority'", listingPriorityValue);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", errorDesc, null));
    }

    @ParameterizedTest(name = "listing Session Type positive tests")
    @CsvFileSource(resources = "/uk/gov/hmcts/futurehearings/snl/acceptance/hearings/data/listing-session-type-test-positive-values.csv", numLinesToSkip = 1)
    @DisplayName("Successfully response for a payload with the listing session type")
    public void test_successful_response_with_listing_session_type_mandatory_elements_payload(final String listingSessionTypeKey, String listingSessionTypeValue,String listingTypeValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-listing-session-type.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndTwoField(listingSessionTypeValue,listingTypeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "listing Session Type negative tests")
    @CsvFileSource(resources = "/uk/gov/hmcts/futurehearings/snl/acceptance/hearings/data/listing-session-type-test-negative-values.csv", numLinesToSkip = 1)
    @DisplayName("Negative response for a payload with the listing session type")
    public void test_negative_response_with_listing_session_type_mandatory_elements_payload(final String listingSessionTypeKey, String listingSessionTypeValue,String listingTypeValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-listing-session-type.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndTwoField(listingSessionTypeValue,listingTypeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("'{0}' is not a valid value for field 'listingType'",listingTypeValue);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, errorDesc, null, null));
    }

    @ParameterizedTest(name = "Case Sub Type non mandatory positive tests")
    @CsvFileSource(resources = "/uk/gov/hmcts/futurehearings/snl/acceptance/hearings/data/case-sub-type-test-positive-values.csv", numLinesToSkip = 1)
    @DisplayName("Successfully response for a payload with the case sub type")
    public void test_successful_response_with_case_sub_type_non_mandatory_elements_payload(final String caseSubTypeKey, String caseSubTypeValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-case-subtype.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndField(caseSubTypeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "Case Sub Type non mandatory negative tests")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "NIL,NIL","case subtype min, 0", "case subtype max,94","case subtype, abc"}, nullValues = "NIL")
    @DisplayName("Negative response for a payload with the case sub type")
    public void test_negative_response_with_case_sub_type_non_mandatory_elements_payload(final String caseSubTypeKey, String caseSubTypeValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-case-subtype.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndField(caseSubTypeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("'{0}' is not a valid value for field 'caseSubType'",caseSubTypeValue);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", errorDesc, null));
    }

    /* work-in-progress hearing-request-non-mandatory-case-subtype.json  */

    @ParameterizedTest(name = "Case comments non mandatory positive tests")
    @CsvFileSource(resources = "/uk/gov/hmcts/futurehearings/snl/acceptance/hearings/data/case-comments-test-positive-values.csv", numLinesToSkip = 1)
    @DisplayName("Successfully response for a payload with the case comments")
    public void test_successful_response_with_case_comments_non_mandatory_elements_payload(final String caseCommentsKey, String caseCommentsValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-case-comments.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndField(caseCommentsValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "Case comments non mandatory negative tests")
    @CsvFileSource(resources = "/uk/gov/hmcts/futurehearings/snl/acceptance/hearings/data/case-comments-test-negative-values.csv", numLinesToSkip = 1)
    @DisplayName("Negative response for a payload with the case comments")
    public void test_negative_response_with_case_comments_non_mandatory_elements_payload(final String caseCommentsKey, String caseCommentsValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-case-comments.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndField(caseCommentsValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("[$.hearingRequest._{0}: may only be 5000 characters long]", caseCommentsValue);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", errorDesc, null));
    }

    private void generateResourcesByUserPayloadWithRandomCaseIdHMCTS() throws IOException {
        final String randomID = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + "/" + getInputPayloadFileName()), randomID.substring(0, 29)));
    }

    private void generateResourcesByUserPayloadWithRandomCaseIdHMCTS(final String randomID) throws IOException {
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + "/" + getInputPayloadFileName()), randomID));
    }

    private void generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndField(final String formatValue) throws IOException {
        final String randomID = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + "/" + getInputPayloadFileName()), randomID.substring(0, 29), formatValue));
    }
    private void generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndTwoField(final String formatValue1, final String formatValue2) throws IOException {
        final String randomID = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + "/" + getInputPayloadFileName()), randomID.substring(0, 29), formatValue1,formatValue2));
    }
    public static String createString(int length, char value) {
        char[] charArray = new char[length];
        Arrays.fill(charArray, value);
        return new String(charArray);
    }
}


