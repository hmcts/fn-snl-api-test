package uk.gov.hmcts.futurehearings.snl.acceptance.hearings;

import static uk.gov.hmcts.futurehearings.snl.acceptance.common.TestingUtils.generateStringForGivenLength;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createStandardPayloadHeader;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.TestingUtils.replaceCharacterSequence;

import org.junit.jupiter.api.*;
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
import java.util.Random;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
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
                "/post/" + getInputPayloadFileName()));
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
    @CsvSource(value = {"Invalid_Source_System,C_FEFC2424-32A6-4B3A-BD97-023296C7F76DC_FEFC2424-32A6-4B3A-BD97-023296C7F76DC_FEFC2424-32A6-4B3A-BD97-023296C7F76D"}, nullValues = "NIL")
    @DisplayName("Negative validated response for case id hmcts tests")
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
    @CsvSource(value = {"Invalid case listing requested id, C_FEFC2424-32A6-4B3A-BD97-023296C7F76DC_FEFC2424-32A6-4B3A-BD97-023296C7F76DC_FEFC2424-32A6-4B3A-BD97-023296C7F76D"}, nullValues = "NIL")
    @DisplayName("Negative validated response for case listing requested Id tests")
    public void test_negative_response_with_case_listing_request_id_mandatory_elements_payload(final String caseListingRequestIDKey, String caseListingRequestIDValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-case-listing-request-id.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndField(caseListingRequestIDValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.hearingRequest._case.caseListingRequestId: may only be 10 characters long]", null));
    }

    @Test
    @DisplayName("Successfully validated response for case title tests")
    public void test_successful_response_with_case_title_mandatory_elements_payload() throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-case-title.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField("Title of the Case");
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
    public void test_negative_response_with_case_title_mandatory_elements_payload(final String caseTitleKey, String caseTitleValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-case-title.json");
        caseTitleValue = caseTitleKey.equals("Invalid Case Title") ? createString(501, caseTitleValue.charAt(0)) : caseTitleValue;
        SNLVerificationDTO snlVerificationDTO = caseTitleKey.equals("Invalid Case Title")
                ? new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.hearingRequest._case.caseTitle: may only be 500 characters long]", null)
                : new SNLVerificationDTO(HttpStatus.BAD_REQUEST, null, null, null);
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(caseTitleValue);
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
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(caseJurisdictionValue);
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
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(caseJurisdictionValue);
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
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(caseCourtValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "caseCourt Negative tests")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "NIL,NIL", "Invalid Case Court, 0", "Invalid Case Court, 12", "Invalid Case Court, 4.5"}, nullValues = "NIL")
    @DisplayName("Negative validated response for caseCourt tests")
    public void test_negative_response_with_case_court_mandatory_elements_payload(final String caseCourtKey, final String caseCourtValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-case-court.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(caseCourtValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", "'" + caseCourtValue + "' is not a valid value for field 'caseCourt'", null));
    }

    @ParameterizedTest(name = "case registered positive tests")
    @CsvSource(value = {"Valid Time,2015-12-11T19:28:30+00:00", "Valid Time,2015-12-11T19:28:30Z",}, nullValues = "NIL")
    @DisplayName("Successfully response for a payload with the Case registered")
    public void test_successful_response_with_case_registered_mandatory_elements_payload(final String caseRegisteredKey,
                                                                                         String caseRegisteredValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-case-registered.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(caseRegisteredValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "case registered Negative tests")
    //TODO: Raise a defect: Test failed with invalid date format 2015-12-11T09:28:30.45
    @CsvFileSource(resources = "/uk/gov/hmcts/futurehearings/snl/acceptance/common/data/negative-different-date-time-format-values.csv", numLinesToSkip = 1)
    @DisplayName("Negative response for a payload with the Case registered")
    public void test_negative_response_with_case_registered_mandatory_elements_payload(final String caseRegisteredKey, String caseRegisteredValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-case-registered.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(caseRegisteredValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("[$.hearingRequest._case.caseRegistered: {0} is an invalid date-time]", caseRegisteredValue);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", errorDesc, null));
    }

    @ParameterizedTest(name = "listing Court positive tests")
    @CsvFileSource(resources = "/uk/gov/hmcts/futurehearings/snl/acceptance/hearings/data/listing-court-test-positive-values.csv", numLinesToSkip = 1)
    @DisplayName("Successfully response for a payload with the listing Court")
    public void test_successful_response_with_listing_court_mandatory_elements_payload(final String listingCourtKey, String listingCourtValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-listing-court.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(listingCourtValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "listing Court Negative tests")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "NIL,NIL", "InValid Court,0", "InValid Court,299", "InValid Court,500", "InValid Court,-318"})
    @DisplayName("Negative response for a payload with the listing Court")
    public void test_negative_response_with_listing_court_mandatory_elements_payload(final String listingCourtKey, String listingCourtValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-listing-court.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(listingCourtValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = "'" + listingCourtValue + "'" + " is not a valid value for field 'listingCourt'";
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1002", errorDesc, null));
    }

    @ParameterizedTest(name = "listing Priority positive tests")
    @CsvSource(value = {"Listing priority, CRIT", "Listing priority, HIGH", "Listing priority, PEND", "Listing priority, NORM"}, nullValues = "NIL")
    @DisplayName("Successfully response for a payload with the listing Priority")
    public void test_successful_response_with_listing_priority_mandatory_elements_payload(final String listingPriorityKey, String listingPriorityValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-listing-priority.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(listingPriorityValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "listing Priority negative tests")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "NIL,NIL", "Listing priority, Critical", "Listing priority, High", "Listing priority, Normal", "Listing priority, ABC"}, nullValues = "NIL")
    @DisplayName("Negative response for a payload with the listing Priority")
    public void test_negative_response_with_listing_priority_mandatory_elements_payload(final String listingPriorityKey, String listingPriorityValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-listing-priority.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(listingPriorityValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = "'" + listingPriorityValue + "'" + " is not a valid value for field 'listingPriority'";
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", errorDesc, null));
    }

    @ParameterizedTest(name = "listing Session Type positive tests")
    //TODO: check the combinations again as ADHOC session type seems to have more listing types assigned to it
    @CsvFileSource(resources = "/uk/gov/hmcts/futurehearings/snl/acceptance/hearings/data/listing-session-type-test-positive-values.csv", numLinesToSkip = 1)
    @DisplayName("Successfully response for a payload with the listing session type")
    public void test_successful_response_with_listing_session_type_mandatory_elements_payload(final String listingSessionTypeKey, String listingSessionTypeValue, String listingTypeValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-listing-session-type.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndTwoField(listingSessionTypeValue, listingTypeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "listing Session Type negative tests")
    @CsvFileSource(resources = "/uk/gov/hmcts/futurehearings/snl/acceptance/hearings/data/listing-session-type-test-negative-values.csv", numLinesToSkip = 1)
    @DisplayName("Negative response for a payload with the listing session type")
    public void test_negative_response_with_listing_session_type_mandatory_elements_payload(final String listingSessionTypeKey, String listingSessionTypeValue, String listingTypeValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-listing-session-type.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndTwoField(listingSessionTypeValue, listingTypeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = "Invalid combination of values for fields 'listingSessionType' (" + listingSessionTypeValue + ") and 'listingType' (" + listingTypeValue + ")";
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1002", errorDesc, null));
    }

    //Depends on Session Type, listing type(Hearing type)
    //Combination test of listing type and session type covered in test_successful_response_with_listing_session_type_mandatory_elements_payload
    @ParameterizedTest(name = "listingType positive tests")
    @CsvSource(value = {"listingType, DECNIS"}, nullValues = "NIL")
    @DisplayName("Successfully response for a payload with the listingType")
    public void test_successful_response_with_listing_type_mandatory_elements_payload(final String listingTypeKey, String listingTypeValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-listing-type.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(listingTypeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "listingType negative tests")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "NIL,NIL", "listingType, ABC"}, nullValues = "NIL")
    @DisplayName("Negative response for a payload with the listingType ")
    public void test_negative_response_with_listing_type_mandatory_elements_payload(final String listingTypeKey, String listingTypeValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-mandatory-listing-type.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(listingTypeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = "'" + listingTypeValue + "'" + " is not a valid value for field 'listingType'";
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", errorDesc, null));
    }

    //TODO: awaiting resolution of MCGIRRSD-1491 to see if a combination test is required between jurisdiction,service,case type and case sub type
    @ParameterizedTest(name = "Case Sub Type non mandatory positive tests")
    @CsvFileSource(resources = "/uk/gov/hmcts/futurehearings/snl/acceptance/hearings/data/case-sub-type-test-positive-values.csv", numLinesToSkip = 1)
    @DisplayName("Successfully response for a payload with the case sub type")
    public void test_successful_response_with_case_sub_type_complete_elements_payload(final String caseSubTypeKey, String caseSubTypeValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-case-subtype.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(caseSubTypeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    //TODO: May need to be refactored based on the above combination tests
    @ParameterizedTest(name = "Case Sub Type non mandatory negative tests")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "NIL,NIL", "case subtype min, 0", "case subtype max,94", "case subtype, abc"}, nullValues = "NIL")
    @DisplayName("Negative response for a payload with the case sub type")
    public void test_negative_response_with_case_sub_type_complete_elements_payload(final String caseSubTypeKey, String caseSubTypeValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-case-subtype.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(caseSubTypeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = "'" + caseSubTypeValue + "'" + " is not a valid value for field 'caseSubType'";
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", errorDesc, null));
    }

    @ParameterizedTest(name = "Case comments non mandatory positive tests")
    @CsvSource(value = {"Valid entityLastName 5000,t"}, nullValues = "NIL")
    @DisplayName("Successfully response for a payload with the case comments")
    public void test_successful_response_with_case_comments_complete_elements_payload(final String caseCommentsKey, String caseCommentsValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-case-comments.json");
        if (caseCommentsKey.trim().equals("Valid entityLastName 5000")) {
            generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(
                    generateStringForGivenLength(5000, caseCommentsValue));
        } else {
            generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(caseCommentsValue);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "Case comments non mandatory negative tests")
    @CsvSource(value = {"InValid entityLastName 5001,t"}, nullValues = "NIL")
    @DisplayName("Negative response for a payload with the case comments")
    public void test_negative_response_with_case_comments_complete_elements_payload(final String caseCommentsKey, String caseCommentsValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-case-comments.json");
        if (caseCommentsKey.trim().equals("InValid entityLastName 5001")) {
            generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(
                    generateStringForGivenLength(5001, caseCommentsValue));
        } else {
            generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(caseCommentsValue);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.hearingRequest._case.caseComments: may only be 5000 characters long]", null));
    }

    @ParameterizedTest(name = "Case Restricted Flag non mandatory positive tests")
    @CsvSource(value = {"Case Restricted Flag, true", "Case Restricted Flag, false"})
    @DisplayName("Successfully response for a payload with the Case restricted flag")
    public void test_successful_response_with_case_restricted_flag_complete_elements_payload(final String caseRestrictedFlagKey, String caseRestrictedFlagValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-case-restricted-flag.json");
        if (caseRestrictedFlagValue.trim().equals("true")) {
            generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"caseRestrictedFlag\": 0", "\"caseRestrictedFlag\":" + true);
        } else {
            generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"caseRestrictedFlag\": 0", "\"caseRestrictedFlag\":" + false);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @Test
    @DisplayName("Negative response for a payload with invalid Case restricted flag")
    public void test_negative_response_with_invalid_case_restricted_flag_complete_elements_payload() throws IOException {
        this.setInputPayloadFileName("hearing-request-non-mandatory-case-restricted-flag.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTS();
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO, getSnlErrorVerifier(), new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004",
                        "[$.hearingRequest._case.caseRestrictedFlag: integer found, boolean expected]",
                        null));
    }

    @Test
    @DisplayName("Negative response for a payload with invalid Case interpreter restricted flag")
    public void test_negative_response_with_invalid_case_interpreter_restricted_flag_complete_elements_payload() throws IOException {
        this.setInputPayloadFileName("hearing-request-non-mandatory-case-interpreter-restricted-flag.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTS();
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO, getSnlErrorVerifier(), new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004",
                        "[$.hearingRequest._case.caseInterpreterRequiredFlag: integer found, boolean expected]",
                        null));
    }

    @Test
    @DisplayName("Negative response for a payload with invalid Case additional security flag")
    public void test_negative_response_with_invalid_case_additional_security_flag_complete_elements_payload() throws IOException {
        this.setInputPayloadFileName("hearing-request-non-mandatory-case-additional-security-flag.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTS();
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO, getSnlErrorVerifier(), new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004",
                        "[$.hearingRequest._case.caseAdditionalSecurityFlag: integer found, boolean expected]",
                        null));
    }

    @Test
    @DisplayName("Negative response for a payload with invalid listing auto create flag")
    public void test_negative_response_with_invalid_case_listing_auto_create_flag_complete_elements_payload() throws IOException {
        this.setInputPayloadFileName("hearing-request-non-mandatory-listing-auto-create-flag.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTS();
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO, getSnlErrorVerifier(), new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004",
                        "[$.hearingRequest.listing.listingAutoCreateFlag: integer found, boolean expected]",
                        null));
    }

    @ParameterizedTest(name = "Case Interpreter Required Flag non mandatory positive tests")
    @CsvSource(value = {"Case Interpreter Required, true", "Case Interpreter Required, false"})
    //TODO - Negative tests required
    @DisplayName("Successfully response for a payload with the Case Interpreter Required")
    public void test_successful_response_with_case_interpreter_required_flag_complete_elements_payload(final String caseInterpreterRequiredFlagKey, String caseInterpreterRequiredFlagValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-case-interpreter-restricted-flag.json");
        if (caseInterpreterRequiredFlagValue.trim().equals("true")) {
            generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"caseInterpreterRequiredFlag\": 0", "\"caseInterpreterRequiredFlag\":" + true);
        } else {
            generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"caseInterpreterRequiredFlag\": 0", "\"caseInterpreterRequiredFlag\":" + false);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "Case Additional Security Flag non mandatory positive tests")
    @CsvSource(value = {"Case Additional Security, true", "Case Additional Security, false"})
    //TODO - Negative tests required
    @DisplayName("Successfully response for a payload with the Case Additional Security flag")
    public void test_successful_response_with_case_additional_security_flag_complete_elements_payload(final String caseAdditionalSecurityFlagKey, String caseAdditionalSecurityFlagValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-case-additional-security-flag.json");
        if (caseAdditionalSecurityFlagValue.trim().equals("true")) {
            generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"caseAdditionalSecurityFlag\": 0", "\"caseAdditionalSecurityFlag\":" + true);
        } else {
            generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"caseAdditionalSecurityFlag\": 0", "\"caseAdditionalSecurityFlag\":" + false);
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
    //TODO - Negative tests required
    @DisplayName("Successfully response for a payload with the listingAutoCreateFlag")
    public void test_successful_response_with_listing_auto_create_flag_complete_elements_payload(final String listingAutoCreateFlagKey, String listingAutoCreateFlagValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-listing-auto-create-flag.json");
        if (listingAutoCreateFlagValue.trim().equals("true")) {
            generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"listingAutoCreateFlag\": 0", "\"listingAutoCreateFlag\":" + true);
        } else {
            generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"listingAutoCreateFlag\": 0", "\"listingAutoCreateFlag\":" + false);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "listingStartDate non mandatory positive tests")
    @CsvSource(value = {"listingStartDate, 2015-12-11T19:28:35+00:00", "listingStartDate, 2015-12-11T19:28:35Z"})
    @DisplayName("Successfully response for a payload with the listingStartDate")
    public void test_successful_response_with_listing_start_date_complete_elements_payload(final String listingStartDateKey, String listingStartDateValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-listing-start-date.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(listingStartDateValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "listingStartDate non mandatory negative tests")
    @CsvFileSource(resources = "/uk/gov/hmcts/futurehearings/snl/acceptance/common/data/negative-different-date-time-format-values.csv", numLinesToSkip = 1)
    @DisplayName("Negative response for a payload with the listingStartDate")
    public void test_negative_response_with_listing_start_date_complete_elements_payload(final String listingStartDateKey, String listingStartDateValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-listing-start-date.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(listingStartDateValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("[$.hearingRequest.listing.listingStartDate: {0} is an invalid date-time]", listingStartDateValue);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", errorDesc, null));
    }

    @ParameterizedTest(name = "listingEndDate non mandatory positive tests")
    @CsvSource(value = {"listingEndDate, 2015-11-10T09:28:35Z,2015-12-11T09:28:35Z", "listingEndDateStartDateEqual,2015-10-11T19:28:35Z,2015-10-11T19:28:35Z"})
    @DisplayName("Successfully response for a payload with the listingEndDate")
    public void test_successful_response_with_listing_end_date_complete_elements_payload(final String listingEndDateKey, String listingStartDateValue, String listingEndDateValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-listing-end-date.json");
        //Checking startDate<=endDate
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndTwoField(listingStartDateValue, listingEndDateValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    //TODO Defect has to be raised for the data of 2015-12-11T09:28:30.45
    @ParameterizedTest(name = "listingEndDate non mandatory negative tests")
    @CsvFileSource(resources = "/uk/gov/hmcts/futurehearings/snl/acceptance/common/data/negative-different-date-time-format-values.csv", numLinesToSkip = 1)
    @DisplayName("Negative response for a payload with the listingEndDate")
    public void test_negative_response_with_listing_end_date_complete_elements_payload(final String listingEndDateKey, String listingEndDateValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-listing-end-date.json");
        String startDate = "2015-09-11T19:29:35Z";
        //Bug: Checking startDate>endDate, currently it is passing and we need to check listing start time and end time
        if (listingEndDateValue.trim().equals("2015-10-11T10:28:35Z")) {
            generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndTwoField("2015-11-11T19:29:35Z", listingEndDateValue);
        } else {
            generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndTwoField(startDate, listingEndDateValue);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("[$.hearingRequest.listing.listingEndDate: {0} is an invalid date-time]", listingEndDateValue);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", errorDesc, null));
    }

    @ParameterizedTest(name = "listingNumberOfAttendees non mandatory positive tests")
    @CsvSource(value = {"listingNumberOfAttendees, 1", "listingNumberOfAttendees, 10000"}, nullValues = "NIL")
    @DisplayName("Successfully response for a payload with the listingNumberOfAttendees")
    public void test_successful_response_with_listing_Number_Attendees_elements_payload(final String listingNumberOfAttendeesKey, String listingNumberOfAttendeesValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-listing-number-attendees.json");
        generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"listingNumberAttendees\": 0", "\"listingNumberAttendees\":" + Integer.parseInt(listingNumberOfAttendeesValue));
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "listingNumberOfAttendees non mandatory Negative tests")
    @CsvSource(value = {"listingNumberOfAttendees, -1"}, nullValues = "NIL")
    @DisplayName("Negative response for a payload with the listingNumberOfAttendees")
    public void test_unsuccessful_response_with_listing_Number_Attendees_elements_payload(final String listingNumberOfAttendeesKey, String listingNumberOfAttendeesValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-listing-number-attendees.json");
        generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"listingNumberAttendees\": 0", "\"listingNumberAttendees\":" + Integer.parseInt(listingNumberOfAttendeesValue));
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.hearingRequest.listing.listingNumberAttendees: must have a minimum value of 0]", null));
    }


    @ParameterizedTest(name = "listingCluster non mandatory positive tests")
    @CsvSource(value = {"listingCluster, TV", "entityClassCode, KNT"}, nullValues = "NIL")
    @DisplayName("Successfully response for a payload with the listingCluster")
    public void test_successful_response_with_listing_cluster_payload(final String listingClusterKey, String listingClusterValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-listing-cluster.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(listingClusterValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "listingCluster non mandatory negative tests")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "NIL,NIL", "listingCluster, ABC"}, nullValues = "NIL")
    @DisplayName("Negative response for a payload with the listingCluster")
    public void test_negative_response_with_listing_cluster_payload(final String listingClusterKey, String listingClusterValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-listing-cluster.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(listingClusterValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = "'" + listingClusterValue + "'" + " is not a valid value for field 'listingCluster'";
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", errorDesc, null));
    }

    @ParameterizedTest(name = "listingDuration non mandatory positive tests")
    @CsvSource(value = {"listingDuration, 5", "listingDuration, 25", "listingDuration, 5000"}, nullValues = "NIL")
    @DisplayName("Successfully response for a payload with the listingDuration")
    public void test_successful_response_with_listing_Duration_elements_payload(final String listingDurationsKey, String listingDurationValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-listing-duration.json");
        generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"listingDuration\": 0", "\"listingDuration\":" + Integer.parseInt(listingDurationValue));
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "listingDuration non mandatory negative tests")
    @CsvSource(value = {"listingDuration, -50", "listingDuration, -1", "listingDuration, 0", "listingDuration, 29"}, nullValues = "NIL")
    @DisplayName("Negative response for a payload with the listingDuration")
    public void test_unsuccessful_response_with_listing_Duration_elements_payload(final String listingDurationsKey, String listingDurationValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-listing-duration.json");
        generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace("\"listingDuration\": 0", "\"listingDuration\":" + Integer.parseInt(listingDurationValue));
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDescription = null;
        if (listingDurationValue.equals("-1")) {
            errorDescription = "[$.hearingRequest.listing.listingDuration: must have a exclusive minimum value of 0, $.hearingRequest.listing.listingDuration: must be multiple of 5.0]";
        } else if (listingDurationValue.equals("29")) {
            errorDescription = "[$.hearingRequest.listing.listingDuration: must be multiple of 5.0]";
        } else {
            errorDescription = "[$.hearingRequest.listing.listingDuration: must have a exclusive minimum value of 0]";
        }
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", errorDescription, null));
    }

    @ParameterizedTest(name = "entityHmiId non mandatory positive tests")
    @CsvSource(value = {"entityHmiId, 1", "entityHmiId, 5000", "entityHmiId, 29", "entityHmiId, 15"}, nullValues = "NIL")
    @DisplayName("Successfully response for a payload with the entityHmiId")
    public void test_successful_response_with_entity_hmi_id_payload(final String entityHmiIdKey, String entityHmiIdValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-entity-hmi-id.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(entityHmiIdValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "entityHmiId non mandatory negative tests")
    @CsvSource(value = {"entityHmiId, 1234567890123456"}, nullValues = "NIL")
    @DisplayName("Unsuccessful response for a payload with invalid entityHmiId")
    public void test_unsuccessful_responses_with_entity_hmi_id_payload(final String entityHmiIdKey, String entityHmiIdValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-entity-hmi-id.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(entityHmiIdValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004",
                        "[$.hearingRequest.entities[0].entityHmiId: may only be 15 characters long]",
                        null));
    }

    @ParameterizedTest(name = "entityTypeCode non mandatory positive tests")
    @CsvSource(value = {"entityTypeCode, IND"}, nullValues = "NIL")
    @DisplayName("Successfully response for a payload with the entityTypeCodeValue")
    public void test_successful_response_with_entity_type_code_payload(final String entityTypeCodeKey, String entityTypeCodeValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-entity-type-code.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(entityTypeCodeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "entityTypeCode non mandatory negative tests")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "NIL,NIL", "entityTypeCode, ABC"}, nullValues = "NIL")
    @DisplayName("Negative response for a payload with the entityTypeCode")
    public void test_negative_response_with_entity_type_code_payload(final String entityTypeCodeKey, String entityTypeCodeValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-entity-type-code.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(entityTypeCodeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = "'" + entityTypeCodeValue + "'" + " is not a valid value for field 'entityTypeCode'";
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", errorDesc, null));
    }

    @ParameterizedTest(name = "entityRoleCode non mandatory positive tests")
    @CsvSource(value = {"entityRoleCode, OTH", // Claimant
            "entityRoleCode, RES", // Respondent
            "entityRoleCode, APL", // Appellant
            "entityRoleCode, APP", // Applicant
            "entityRoleCode, CHI", // CHILD
            "entityRoleCode, DEF", // Defendant
            "entityRoleCode, PET", // Petitioner
    }, nullValues = "NIL")
    @DisplayName("Successfully response for a payload with the entityRoleCode")
    public void test_successful_response_with_entity_role_code_payload(final String entityRoleCodeKey, String entityRoleCodeValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-entity-role-code.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(entityRoleCodeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "entityRoleCode non mandatory negative tests")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "NIL,NIL", "entityRoleCode, ABC"}, nullValues = "NIL")
    @DisplayName("Negative response for a payload with the entityRoleCode")
    public void test_negative_response_with_entity_role_code_payload(final String entityRoleCodeKey, String entityRoleCodeValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-entity-role-code.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(entityRoleCodeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = "'" + entityRoleCodeValue + "'" + " is not a valid value for field 'entityRoleCode'";
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", errorDesc, null));
    }


    @ParameterizedTest(name = "entityClassCode non mandatory positive tests")
    @CsvSource(value = {"entityClassCode, PERSON"}, nullValues = "NIL")
    @DisplayName("Successfully response for a payload with the entityClassCode")
    public void test_successful_response_with_entity_class_code_payload(final String entityClassCodeKey, String entityClassCodeValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-entity-class-code.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(entityClassCodeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "entityClassCode non mandatory negative tests")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "NIL,NIL", "entityClassCode, ABC"}, nullValues = "NIL")
    @DisplayName("Negative response for a payload with the entityClassCode")
    public void test_negative_response_with_entity_class_code_payload(final String entityClassCodeKey, String entityClassCodeValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-entity-class-code.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(entityClassCodeValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = "[$.hearingRequest.entities[0].entitySubType.entityClassCode: does not have a value in the enumeration [PERSON, ORG]";
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", errorDesc, null));
    }

    @ParameterizedTest(name = "entityTitle non mandatory positive tests")
    @CsvSource(value = {"entityTitle, Mr", "entityTitle, Mrs"}, nullValues = "NIL")
    @DisplayName("Successfully response for a payload with the entityTitle")
    public void test_successful_response_with_entity_title_payload(final String entityTitleKey, String entityTitleValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-entity-title.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(entityTitleValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "entityTitle non mandatory negative tests")
    @CsvSource(value = {"Invalid entityTitle  41 chars,t"}, nullValues = "NIL")
    @DisplayName("Negative response for a payload with the entityTitle")
    public void test_negative_response_with_entity_title_payload(final String entityTitleKey, String entityTitleValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-entity-title.json");
        if (entityTitleKey.trim().equals("Invalid entityTitle  41 chars")) {
            generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(
                    generateStringForGivenLength(41, entityTitleValue));
        } else {
            generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(entityTitleValue);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = "[$.hearingRequest.entities[0].entitySubType.entityTitle: may only be 40 characters long]";
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", errorDesc, null));
    }

    @ParameterizedTest(name = "entityFirstName non mandatory positive tests")
    @CsvSource(value = {"entityFirstName, testFirstName"}, nullValues = "NIL")
    @DisplayName("Successfully response for a payload with the entityFirstName")
    public void test_successful_response_with_entity_first_name_payload(final String entityFirstNameKey, String entityFirstNameValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-entity-first-name.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(entityFirstNameValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "entityFirstName non mandatory negative tests")
    @CsvSource(value = {"Invalid entityFirstName 101,t"}, nullValues = "NIL")
    @DisplayName("Negative response for a payload with the entityTitle")
    public void test_negative_response_with_entity_first_name_payload(final String entityFirstNameKey, String entityFirstNameValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-entity-first-name.json");
        if (entityFirstNameKey.trim().equals("Invalid entityFirstName 101")) {
            generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(
                    generateStringForGivenLength(101, entityFirstNameValue));
        } else {
            generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(entityFirstNameValue);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = "[$.hearingRequest.entities[0].entitySubType.entityFirstName: may only be 100 characters long]";
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", errorDesc, null));
    }

    @ParameterizedTest(name = "entityLastName non mandatory positive tests")
    @CsvSource(value = {"entityLastName, testLastName"}, nullValues = "NIL")
    @DisplayName("Successfully response for a payload with the entityLastName")
    public void test_successful_response_with_entity_last_name_payload(final String entityLastNameKey, String entityLastNameValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-entity-last-name.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(entityLastNameValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @Disabled
    //This test is failing because it is having clash with entityCompanyName: is missing but it is required, defect id = MCGIRRSD-2475
    @ParameterizedTest(name = "entityLastName non mandatory negative tests")
    @CsvSource(value = {"Invalid entityLastName 731,t"}, nullValues = "NIL")
    @DisplayName("Negative response for a payload with the entityLastName")
    public void test_negative_response_with_entity_last_name_payload(final String entityLastNameKey, String entityLastNameValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-entity-last-name.json");
        if (entityLastNameKey.trim().equals("Invalid entityLastName 731")) {
            generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(
                    generateStringForGivenLength(731, entityLastNameValue));
        } else {
            generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(entityLastNameValue);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("[$.hearingRequest.entities[0].entitySubType.'{0}': may only be 730 characters long", entityLastNameValue);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", errorDesc, null));
    }

    @ParameterizedTest(name = "entityCompanyName non mandatory positive tests")
    @CsvSource(value = {"entityCompanyName, entityCompanyName"}, nullValues = "NIL")
    @DisplayName("Successfully response for a payload with the entityCompanyName")
    public void test_successful_response_with_entity_company_name_payload(final String entityCompanyNameKey, String entityCompanyNameValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-entity-company-name.json");
        generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(entityCompanyNameValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @Disabled
    @ParameterizedTest(name = "entityCompanyName non mandatory negative tests")
    @CsvSource(value = {"Invalid entityCompanyName 2001,t"}, nullValues = "NIL")
    @DisplayName("Negative response for a payload with the entityCompanyName")
    public void test_negative_response_with_entity_company_name_payload(final String entityCompanyNameKey, String entityCompanyNameValue) throws Exception {
        this.setInputPayloadFileName("hearing-request-non-mandatory-entity-company-name.json");
        if (entityCompanyNameKey.trim().equals("Invalid entityCompanyName 2001")) {
            generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(
                    generateStringForGivenLength(2001, entityCompanyNameValue));
        } else {
            generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(entityCompanyNameValue);
        }
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        String errorDesc = MessageFormat.format("'{0}' is not a valid value for field 'entityCompanyName'", entityCompanyNameValue);
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", errorDesc, null));
    }


    private void generateResourcesByUserPayloadWithRandomCaseIdHMCTS() throws IOException {
        final String randomID = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        final String caseListingRequestID = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + "/post/" + getInputPayloadFileName()), randomID.substring(0, 29), caseListingRequestID.substring(0, 9)));
    }

    private void generateResourcesByUserPayloadWithRandomCaseIdHMCTS(final String randomID) throws IOException {
        final String caseListingRequestID = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + "/post/" + getInputPayloadFileName()), randomID, caseListingRequestID.substring(0, 9)));
    }

    private void generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndField(final String formatValue) throws IOException {
        final String randomID = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + "/post/" + getInputPayloadFileName()), randomID.substring(0, 29), formatValue));
    }


    private void generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndCaseListingRequestIDAndField(final String formatValue) throws IOException {
        final String randomID = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        final String caseListingRequestID = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + "/post/" + getInputPayloadFileName()), randomID.substring(0, 29), caseListingRequestID.substring(0, 9), formatValue));
    }


    private void generateResourcesByUserPayloadWithRandomCaseIdHMCTSAndTwoField(final String formatValue1, final String formatValue2) throws IOException {
        final String randomID = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        final String caseListingRequestID = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + "/post/" + getInputPayloadFileName()), randomID.substring(0, 29), caseListingRequestID.substring(0, 9), formatValue1, formatValue2));
    }

    public static String createString(int length, char value) {
        char[] charArray = new char[length];
        Arrays.fill(charArray, value);
        return new String(charArray);
    }

    private void generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace(final String token, final String value) throws IOException {
        final int randomId = new Random().nextInt(99999999);
        final String caseListingRequestID = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        String formattedString = String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + "/post/" + getInputPayloadFileName()), randomId, caseListingRequestID.substring(0, 9), value);
        this.setInputBodyPayload(replaceCharacterSequence(token, value, formattedString));
    }

    private void generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace(final String id, final String token, final String value) throws IOException {
        final int randomId = new Random().nextInt(99999999);
        final String caseListingRequestID = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        String formattedString = String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + "/post/" + getInputPayloadFileName()), randomId, id, caseListingRequestID.substring(0, 9), value);
        this.setInputBodyPayload(replaceCharacterSequence(token, value, formattedString));
    }

    private void generateLocationPayloadWithRandomHMCTSIDAndFieldValueFormat(final String formatValue) throws IOException {
        final int randomId = new Random().nextInt(99999999);
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + "/post/" + getInputPayloadFileName()), randomId, formatValue));
    }

}
