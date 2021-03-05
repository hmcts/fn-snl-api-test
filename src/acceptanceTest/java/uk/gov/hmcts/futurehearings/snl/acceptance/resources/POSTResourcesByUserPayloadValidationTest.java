package uk.gov.hmcts.futurehearings.snl.acceptance.resources;

import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createStandardPayloadHeader;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.provider.CsvFileSource;
import uk.gov.hmcts.futurehearings.snl.Application;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.TestingUtils;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.CommonDelegate;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.dto.DelegateDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.dto.SNLVerificationDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.error.SNLCommonErrorVerifier;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.success.SNLCommonSuccessVerifier;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
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

import java.io.IOException;
import java.util.*;

@Slf4j
@SpringBootTest(classes = {Application.class})
@ActiveProfiles("acceptance")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SelectClasses(POSTResourcesByUserHeaderValidationTest.class)
@IncludeTags("Post")
//@Disabled("Disabled the test as test users were getting created in the UAT environment")
public class POSTResourcesByUserPayloadValidationTest extends ResourcesPayloadValidationTest {

    @Qualifier("CommonDelegate")
    @Autowired(required = true)
    private CommonDelegate commonDelegate;

    @Value("${resourcesByUserRootContext}")
    private String resourcesByUserRootContext;

    @BeforeAll
    public void initialiseValues() throws Exception {
        super.initialiseValues();
        this.setRelativeURL(resourcesByUserRootContext);
        this.setHttpMethod(HttpMethod.POST);
        this.setInputPayloadFileName("resources-by-username-complete.json");
        this.setHttpSuccessStatus(HttpStatus.CREATED);
        this.setRelativeURLForNotFound(this.getRelativeURL().replace("resources", "resource"));
        this.setSnlSuccessVerifier(new SNLCommonSuccessVerifier());
        this.setSnlErrorVerifier(new SNLCommonErrorVerifier());
        this.setInputBodyPayload(TestingUtils.readFileContents(String.format(INPUT_FILE_PATH, getInputFileDirectory()) +
                "/" + getInputPayloadFileName()));
    }

    @Test
    @DisplayName("Successfully validated response for a payload with all the mandatory required fields")
    public void test_successful_response_with_mandatory_elements_payload() throws Exception {

        this.setInputPayloadFileName("resources-by-username-mandatory-person-id.json");
        generatePayloadWithHMCTSID("@hmcts.net", "/user/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @Test
    @DisplayName("Successfully validated response for a payload with all the complete payload fields")
    public void test_successful_response_with_complete_elements_payload() throws Exception {
        this.setInputPayloadFileName("resources-by-username-complete.json");
        generatePayloadWithRandomHMCTSID("/user/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "Negative Extra Field in Payload Scenario : {0}")
    @CsvSource(value = {"Mr,Mr", "Miss,Miss", "TRY,TRY"}, nullValues = "NIL")
    public void test_negative_response_for_extra_element_in_payload(final String personSalutationKey,
                                                                    final String personSalutationValue) throws Exception {

        this.setInputPayloadFileName("resources-by-username-mandatory-person-salutation.json");
        generatePayloadWithRandomHMCTSIDAndField(personSalutationValue, "/user/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.userRequest.details.personSalutation: is not defined in the schema and the schema does not allow additional properties]", null));
    }

    @ParameterizedTest(name = "PersonHMCTSID Negative tests")
    @CsvSource(value = {"Empty Space,''",
            "Single Space,' '",
            "Invalid email,xxxtest@gmail",
            "Invalid email1,xxxtest@gmail.",
            "Invalid email1,xxxtest@gmail.c",
            "Invalid email1,xtest@xxxcom",
            "Invalid email1,testxxx.com",
            "Invalid email1,testxxxcom",
            "Not An Email id,testing",
            "Invalid_Source_System, C_FEFC2424-32A6-4B3A-BD97-023296C7F76DC_FEFC2424-32A6-4B3A-BD97-023296C7F76DC_FEFC2424-32A6-4B3A-BD97-023296C7F76D"}, nullValues = "NIL")
    @Disabled("TODO Defect MCGIRRSD-2193 - Switched this test off as Empty and Blanks are not allowed....")
    public void test_negative_response_with_mandatory_elements_payload(final String personHMCTSIDKey, final String personHMCTSIDValue) throws Exception {

        this.setInputPayloadFileName("resources-by-username-mandatory-person-id.json");
        generatePayloadWithHMCTSID(personHMCTSIDValue, "/user/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        SNLVerificationDTO snlVerificationDTO = null;
        switch (personHMCTSIDValue) {
            case "":
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.userRequest.details.personIdHMCTS: must be at least 1 characters long]", null);
                break;
            case " ":
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1001", "A User resource with 'personIdHMCTS' = ' ' already exists", null);
                break;
            default:
                snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.userRequest.details.personIdHMCTS: may only be 100 characters long]", null);
                break;

        }
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                snlVerificationDTO);
    }

    @ParameterizedTest(name = "PersonFirstName Negative Tests Scenario : {0}")
    @CsvSource(value = {"greater_than_max_length_person_first_name, C_FEFC2424-32A6-4B3A-BD97-023296C7F76DC_FEFC2424-32A6-4B3A-BD97-023296C7F76DC_FEFC2424-32A6-4B3A-BD97-023296C7F76D"}, nullValues = "NIL")
    public void test_negative_response_for_person_first_name_with_mandatory_elements_payload(final String personFirstNameKey, final String personFirstNameValue) throws Exception {

        this.setInputPayloadFileName("resources-by-username-mandatory-person-first-name.json");
        generatePayloadWithRandomHMCTSIDAndField(personFirstNameValue, "/user/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.userRequest.details.personFirstName: may only be 80 characters long]", null));
    }

    @ParameterizedTest(name = "PersonFirstName Positive Tests Scenario : {0}")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "max_length_first_name,123TEETING!£$^&*(_ajkfhbabgk+_)(*%876dfk123TEETING!£$^&*(_ajkfhbabgk+_)(*%876dfk"}, nullValues = "NIL")
    @Disabled("TODO - Based on Defect MCGIRRSD-2193 mandatory values should not be taking in Blanks Empty and Nulls.")
    public void test_positive_response_for_person_first_name_with_mandatory_elements_payload(final String personFirstNameKey, final String personFirstNameValue) throws Exception {

        this.setInputPayloadFileName("resources-by-username-mandatory-person-first-name.json");
        generatePayloadWithRandomHMCTSIDAndField(personFirstNameValue, "/user/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "PersonLastName Negative Tests Scenario : {0}")
    @CsvSource(value = {"greater_than_max_length_person_last_name_length, C_FEFC2424-32A6-4B3A-BD97-023296"}, nullValues = "NIL")
    public void test_negative_response_for_person_last_name_with_mandatory_elements_payload(final String personFirstNameKey, final String personLastNameValue) throws Exception {

        this.setInputPayloadFileName("resources-by-username-mandatory-person-last-name.json");
        generatePayloadWithRandomHMCTSIDAndField(personLastNameValue, "/user/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.userRequest.details.personLastName: may only be 30 characters long]", null));
    }

    @ParameterizedTest(name = "PersonLastName Positive Tests Scenario : {0}")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "max_length_first_name,123TEETING!£$^&*(_ajkfhbabgk+_"}, nullValues = "NIL")
    @Disabled("TODO - Based on Defect MCGIRRSD-2193 mandatory values should not be taking in Blanks Empty and Nulls.")
    public void test_positive_response_for_person_last_name_with_mandatory_elements_payload(final String personLastNameKey,
                                                                                            final String personLastNameValue) throws Exception {

        this.setInputPayloadFileName("resources-by-username-mandatory-person-first-name.json");
        generatePayloadWithRandomHMCTSIDAndField(personLastNameValue, "/user/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "PersonRegistry Positive Tests Scenario : {0}")
    @CsvSource(value = {"Valid LOV Value,KNT", "Valid LOV Value,TV"}, nullValues = "NIL")
    //Confirmed with Ola that this are the only acceptable values that the Registry will accept.
    public void test_positive_response_for_person_registry_with_mandatory_elements_payload(final String personRegistryKey,
                                                                                           final String personRegistryValue) throws Exception {

        this.setInputPayloadFileName("resources-by-username-mandatory-person-registry.json");
        generatePayloadWithRandomHMCTSIDAndField(personRegistryValue, "/user/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "PersonRegistry Negative Tests Scenario : {0}")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "Invalid Single Char Capital,X", "Invalid Single Char Small,b", "Invalid Double Char,cc"}, nullValues = "NIL")
    public void test_negative_response_for_person_registry_with_mandatory_elements_payload(final String personRegistryKey,
                                                                                           final String personRegistryValue) throws Exception {

        this.setInputPayloadFileName("resources-by-username-mandatory-person-registry.json");
        generatePayloadWithRandomHMCTSIDAndField(personRegistryValue, "/user/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000",
                        "'" + personRegistryValue + "' is not a valid value for field 'personRegistry'", null));
    }

    //Getting the LOV Values from the MCGirr Spreadsheet - Other LOV's tab
    @ParameterizedTest(name = "PersonRoleID Positive Tests Scenario : {0} - {1}")
    @CsvSource(value = {"Valid LOV Value,033","Valid LOV Value,055","Valid LOV Value,078","Valid LOV Value,100", "Valid LOV Value,152"}, nullValues = "NIL")
    //TODO - Raised a Defect as "Valid LOV Value,33,34 onwards" is failing the Test. McGirrSD-2339
    public void test_positive_response_for_person_role_id_with_mandatory_elements_payload(final String personRoleIdKey,
                                                                                          final String personRoleIdValue) throws Exception {

        this.setInputPayloadFileName("resources-by-username-optional-person-role-id.json");
        generatePayloadWithRandomHMCTSIDAndField(personRoleIdValue, "/user/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "PersonRoleID Positive Tests Scenario : {0}")
    @CsvSource(value = {"Empty Space,''", "Null Value,NIL", "Invalid LOV Value,32", "Invalid LOV Value,153"}, nullValues = "NIL")
    public void test_negative_response_for_person_role_id_with_mandatory_elements_payload(final String personRoleIdKey,
                                                                                          final String personRoleIdValue) throws Exception {
        this.setInputPayloadFileName("resources-by-username-optional-person-role-id.json");
        generatePayloadWithRandomHMCTSIDAndField(personRoleIdValue, "/user/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        SNLVerificationDTO snlVerificationDTO = null;
        if (personRoleIdKey.trim().equals("Null Value")) {
            snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.userRequest.details.personRoleId: may only be 3 characters long]", null);
        } else {
            snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", "'" + personRoleIdValue + "' is not a valid value for field 'personRoleId'", null);
        }
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                snlVerificationDTO);
    }

    //Getting the LOV Values from the MCGirr Spreadsheet - Hearing LOV's Locations Section
    @ParameterizedTest(name = "PersonVenueID Positive Tests Scenario : {0} - {1}")
    @CsvSource(value = {"Valid LOV Value,300", "Valid LOV Value,350", "Valid LOV Value,358"}, nullValues = "NIL")
    //TODO - Clarify if the Source of the LOV's is Correct.
    public void test_positive_response_for_person_venue_id_with_mandatory_elements_payload(final String personVenueIdKey,
                                                                                           final String personVenueIdValue) throws Exception {

        this.setInputPayloadFileName("resources-by-username-optional-person-venue-id.json");
        generatePayloadWithRandomHMCTSIDAndField(personVenueIdValue, "/user/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "PersonVenueID Negative Tests Scenario : {0}")
    @CsvSource(value = {"Empty Space,''", "Null Value,NIL", "Invalid LOV Value,299", "Invalid LOV Value,387"}, nullValues = "NIL")
    //TODO - Clarify the Source of this LOV...At present we are using the Location Area of the Hearings LOV tab of the Spreadsheet....
    //TODO - Raise a Defect based on the Length of the Element on the Schema.
    public void test_negative_response_for_person_venue_id_with_mandatory_elements_payload(final String personVenueIdKey,
                                                                                           final String personVenueIdValue) throws Exception {

        this.setInputPayloadFileName("resources-by-username-optional-person-venue-id.json");
        generatePayloadWithRandomHMCTSIDAndField(personVenueIdValue, "/user/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        /*SNLVerificationDTO snlVerificationDTO = null;
        if ( personVenueIdKey.trim().equals("Null Value")) {
            snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.userRequest.details.personVenueId: may only be 3 characters long]", null);
        } else {
            snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", "'"+personVenueIdValue+"' is not a valid value for field 'personVenueId'", null);
        }*/
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", "'" + personVenueIdValue + "' is not a valid value for field 'personVenueId'", null));
    }

    @ParameterizedTest(name = "PersonActiveDate Positive Tests Scenario : {0} - {1}")
    @CsvSource(value = {"Valid Person Active Date Value,2002-10-02"}, nullValues = "NIL")
    public void test_positive_response_for_person_active_date_with_mandatory_elements_payload(final String personRoleIdKey,
                                                                                              final String personRoleIdValue) throws Exception {

        this.setInputPayloadFileName("resources-by-username-optional-person-active-date.json");
        generatePayloadWithRandomHMCTSIDAndField(personRoleIdValue, "/user/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "PersonActiveDate Negative Tests Scenario : {0} - {1}")
    @CsvSource(value = {"Invalid Person Active Date Value,NIL",
            "Invalid Person Active Date Format,2002/10/02",
            "Invalid Person Active Date Value,2002-02-31",
            "Invalid Person Active Date Value,2002-04-57"}, nullValues = "NIL")
    public void test_negative_response_for_person_active_date_with_mandatory_elements_payload(final String personActiveDateKey,
                                                                                              final String personActiveDateValue) throws Exception {

        this.setInputPayloadFileName("resources-by-username-optional-person-active-date.json");
        generatePayloadWithRandomHMCTSIDAndField(personActiveDateValue, "/user/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.userRequest.details.personActiveDate: " + personActiveDateValue + " is an invalid date]", null));
    }

    @ParameterizedTest(name = "PersonInactiveDate Positive Tests Scenario : {0} - {1}")
    @CsvSource(value = {"Valid Person Inactive Date Value, 2002-10-02"}, nullValues = "NIL")
    public void test_positive_response_for_person_inactive_date_with_mandatory_elements_payload(final String personInactiveDateKey,
                                                                                                final String personInactiveDateValue) throws Exception {

        this.setInputPayloadFileName("resources-by-username-optional-person-inactive-date.json");
        generatePayloadWithRandomHMCTSIDAndField(personInactiveDateValue, "/user/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "PersonInactiveDate Negative Tests Scenario : {0} - {1}")
    @CsvSource(value = {"Invalid Person Inactive Date Value,NIL",
            "Invalid Person Inactive Date Format,2002/10/02",
            "Invalid Person Inactive Date Value,2002-02-31",
            "Invalid Person Inactive Date Value,2002-04-57"}, nullValues = "NIL")
    public void test_negative_response_for_person_inactive_date_with_mandatory_elements_payload(final String personInactiveDateKey,
                                                                                                final String personInactiveDateValue) throws Exception {

        this.setInputPayloadFileName("resources-by-username-optional-person-inactive-date.json");
        generatePayloadWithRandomHMCTSIDAndField(personInactiveDateValue, "/user/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.userRequest.details.personInactiveDate: " + personInactiveDateValue + " is an invalid date]", null));
    }

    @ParameterizedTest(name = "Mandatory Fields not available Negative Tests Scenario : {0} - {1}")
    @CsvSource(value = {
            "Checking Payload without the Person Id HMCTS, resources-by-username-mandatory-without-person-id-hmcts.json,personIdHMCTS",
            "Checking Payload without the Person First Name, resources-by-username-mandatory-without-person-first-name.json,personFirstName",
            "Checking Payload without the Person Last Name, resources-by-username-mandatory-without-person-last-name.json,personLastName",
            "Checking Payload without the Person Registry, resources-by-username-mandatory-without-person-registry.json,personRegistry"
    }, nullValues = "NIL")
    public void test_negative_response_mandatory_elements_payload(final String userPayloadTestScenarioDescription,
                                                                  final String userPayloadTestScenarioFileName,
                                                                  final String userSchemaElement) throws Exception {
        this.setInputPayloadFileName(userPayloadTestScenarioFileName);
        generatePayloadWithRandomHMCTSID("/user/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.userRequest.details." + userSchemaElement + ": is missing but it is required]", null));
    }

    @ParameterizedTest(name = "Person contact phone positive scenario")
    @CsvSource(value = {"Valid phone number 19Chars,19CharactersPhoneNo", "Valid phone number 20Chars,20CharactersPhoneNos",
            "Valid phone number 0Chars,''"}, nullValues = "NIL")
    public void test_positive_response_for_person_contact_phone_with_mandatory_elements_payload(final String personPhoneKey,
                                                                                                String personContactValue) throws Exception {
        String generateTelePhoneNo = UUID.randomUUID().toString() + UUID.randomUUID().toString();

        this.setInputPayloadFileName("resources-by-user-optional-person-contact-phone.json");
               switch(personContactValue){
            case ("19CharactersPhoneNo"):
                personContactValue = generateTelePhoneNo.substring(0, 19);
                break;
            case ("20CharactersPhoneNos"):
                personContactValue = generateTelePhoneNo.substring(0, 20);
                break;
            case (""):
                personContactValue = generateTelePhoneNo.substring(0, 0);
                break;
        }
                  generatePayloadWithRandomHMCTSIDAndField(personContactValue, "/user/post/");
                  DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                  createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
                    log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
                        commonDelegate.test_expected_response_for_supplied_header(
                                    delegateDTO,
                                     getSnlSuccessVerifier(),
                                     new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "Person contact phone negative scenario")
    @CsvSource(value = {"Invalid phone number 21Chars,personContactValue"}, nullValues = "NIL")
    public void test_negative_response_for_person_contact_phone_with_mandatory_elements_payload(final String personPhoneKey,
                                                                                                String personContactValue) throws Exception {
        this.setInputPayloadFileName("resources-by-user-optional-person-contact-phone.json");
        String contactPhoneValue = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        personContactValue = contactPhoneValue.substring(0, 21);
        generatePayloadWithRandomHMCTSIDAndField(personContactValue, "/user/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.userRequest.details.personContactPhone: may only be 20 characters long]", null));
    }

    @ParameterizedTest(name = "personContactEmail positive scenario")
    @CsvSource(value = {"Valid email 100 Chars,100Chars", "Valid email 99Chars,99Chars",
            "Valid email 0Chars,0Chars"}, nullValues = "NIL")
    public void test_positive_response_for_person_contact_email_with_mandatory_elements_payload(final String personContactEmailKey,
                                                                                                String personEmailValue) throws Exception {

        this.setInputPayloadFileName("resources-by-user-optional-person-email.json");
        String contactEmail = UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString();

        switch(personEmailValue){
            case("100Chars"):
                personEmailValue = contactEmail.substring(0, 100);
                break;
            case("99Chars"):
                personEmailValue = contactEmail.substring(0, 99);
                break;
            case("0Chars"):
                personEmailValue = contactEmail.substring(0, 0);
                break;
        }

        generatePayloadWithRandomHMCTSIDAndField(personEmailValue, "/user/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "personContactEmail negative scenario")
    @CsvSource(value = {"InValid email 101 Chars,personEmailValue",
            "Empty Space,''",
            "Single Space,' '",
            "Invalid email,xxxtest@gmail",
            "Invalid email1,xxxtest@gmail.",
            "Invalid email1,xxxtest@gmail.c",
            "Invalid email1,xtest@xxxcom",
            "Invalid email1,testxxx.com",
            "Invalid email1,testxxxcom",
            "Not An Email id,testing"}, nullValues = "NIL")
    public void test_negative_response_for_person_contact_email_with_mandatory_elements_payload(final String personContactEmailKey,
                                                                                                String personEmailValue) throws Exception {

        this.setInputPayloadFileName("resources-by-user-optional-person-email.json");
        String contactEmail = UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString();
        personEmailValue = contactEmail.substring(0, 101);
        generatePayloadWithRandomHMCTSIDAndField(personEmailValue, "/user/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004",
                        "[$.userRequest.details.personContactEmail: may only be 100 characters long]",
                        null));

    }

    @ParameterizedTest(name = "Person Position From Date Positive Test Scenario")
    @CsvSource(value = {"Valid Person Position From Date,2002-10-02"})
    public void test_positive_response_for_person_position_from_date_with_mandatory_elements_payload(final String validPersonPositionFromDate,
                                                                                                     final String validPersonPositionFromDateValue) throws IOException {
        this.setInputPayloadFileName("resources-by-user-optional-person-position-from-date.json");
        generatePayloadWithRandomHMCTSIDAndField(validPersonPositionFromDateValue, "/user/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "Person Position From Date Negative Tests Scenario")
    @CsvSource(value = {"Invalid Person Position Date Value,NIL",
            "Invalid Person Inactive Date Format,2002/10/02",
            "Invalid Person Inactive Date Value,2002-02-31",
            "Invalid Person Inactive Date Value,2002-04-57"}, nullValues = "NIL")
    public void test_negative_person_position_from_date_with_mandatory_elements_payload(final String validPersonPositionFromDate,
                                                                                        final String inValidPersonPositionFromDateValue) throws IOException {
        this.setInputPayloadFileName("resources-by-user-optional-person-position-from-date.json");
        generatePayloadWithRandomHMCTSIDAndField(inValidPersonPositionFromDateValue, "/user/post/");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.userRequest.details.personPositionFrom: "+inValidPersonPositionFromDateValue+" is an invalid date]", null));
    }

    @ParameterizedTest(name = "personAuthorisedSessionTypes Positive Test Scenario")
    @CsvFileSource(resources="/uk/gov/hmcts/futurehearings/snl/acceptance/hearings/data/valid-person-authorised-session-types.csv", numLinesToSkip = 1)

    public void test_positive_response_for_person_authorised_sessionTypes_with_mandatory_elements_payload(final String personAuthorisedSessionTypesKey,
                                                                                                          String personAuthorisedSessionTypesValue) throws IOException {
        this.setInputPayloadFileName("resources-by-user-optional-person-authorised-session-type.json");
        System.out.println("Value from the csv file : "+ personAuthorisedSessionTypesValue);

        List<String> sessionTypes = new ArrayList<>();
        sessionTypes.add(personAuthorisedSessionTypesValue);
        ObjectMapper objectMapper = new ObjectMapper();
        String personAuthorisedSessionValue = objectMapper.writeValueAsString(sessionTypes);

        generatePayloadWithRandomHMCTSIDAndPersonField(personAuthorisedSessionValue, "/user/post/",
                "[\"personAuthorisedSessionTypes\"]");

        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());

        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "personAuthorisedSessionTypes Negative Test Scenario")
    @CsvSource(value = {"Invalid personAuthorisedSessionTypes, testPerson",
            "Invalid personAuthorisedSessionTypes_11Chars, testPersonA"})
    public void test_negative_response_for_person_authorised_sessionTypes_with_mandatory_elements_payload(final String personAuthorisedSessionTypesKey,
                                                                                                          String personAuthorisedSessionTypesValue) throws IOException {
        this.setInputPayloadFileName("resources-by-user-optional-person-authorised-session-type.json");

        List<String> sessionTypes = new ArrayList<>();
        sessionTypes.add(personAuthorisedSessionTypesValue);
        ObjectMapper objectMapper = new ObjectMapper();
        String personAuthorisedSessionValue = objectMapper.writeValueAsString(sessionTypes);

        generatePayloadWithRandomHMCTSIDAndPersonField(personAuthorisedSessionValue, "/user/post/",
                "[\"personAuthorisedSessionTypes\"]");

        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                    createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
            log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
            SNLVerificationDTO snlVerificationDTO = null;

            switch(personAuthorisedSessionTypesValue) {
                case ("testPerson"):
                    snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000",
                            "'" + personAuthorisedSessionTypesValue + "' is not a valid value for field 'personAuthorisedSessionTypes[0]'",
                            null);
                    break;
                case ("testPersonA"):
                    snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004",
                            "[$.userRequest.details.personAuthorisedSessionTypes[0]: may only be 10 characters long]",
                            null);
                    break;
            }
            commonDelegate.test_expected_response_for_supplied_header(
                    delegateDTO,
                    getSnlErrorVerifier(),
                    snlVerificationDTO);
        }

    //Positive test for PersonPositionTitle
    @Test
    @CsvSource(value = {"Valid PersonPositionTitleKey, PersonPositionTitleValue",})
    @Disabled
    //Raised defect MCGIRRSD-2339
    public void test_positive_response_for_Person_position_title_with_mandatory_elements_payload() throws IOException {
        this.setInputPayloadFileName("resources-by-user-optional-person-position-title.json");

        for (int positionTitle = 33; positionTitle < 37; positionTitle++) {
            generatePayloadWithRandomHMCTSIDAndField(String.valueOf(positionTitle), "/user/post/");
            DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                    createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
            log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
            commonDelegate.test_expected_response_for_supplied_header(
                    delegateDTO,
                    getSnlSuccessVerifier(),
                    new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
        }

        for (int positionTitle = 110; positionTitle < 114; positionTitle++) {
            generatePayloadWithRandomHMCTSIDAndField(String.valueOf(positionTitle), "/user/post/");
            DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                    createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
            log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
            commonDelegate.test_expected_response_for_supplied_header(
                    delegateDTO,
                    getSnlSuccessVerifier(),
                    new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
        }


    }

    //Negative scenario to verify Person position title
    @Test
    @CsvSource(value = {"Valid PersonPositionTitleKey, PersonPositionTitleValue",})
    //@Disabled
    //Raised defect MCGIRRSD-2339
    public void test_negative_response_for_Person_position_title_with_mandatory_elements_payload() throws IOException {
        this.setInputPayloadFileName("resources-by-user-optional-person-position-title.json");

        for (int positionTitle = 30; positionTitle < 32; positionTitle++) {
            generatePayloadWithRandomHMCTSIDAndField(String.valueOf(positionTitle), "/user/post/");
            DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                    createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
            log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
            commonDelegate.test_expected_response_for_supplied_header(
                    delegateDTO,
                    getSnlErrorVerifier(),
                    new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", "'"+positionTitle+"' is not a valid value for field 'personPositionTitle'", null));
        }

        for (int positionTitle = 153; positionTitle < 158; positionTitle++) {
            generatePayloadWithRandomHMCTSIDAndField(String.valueOf(positionTitle), "/user/post/");
            DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                    createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
            log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
            commonDelegate.test_expected_response_for_supplied_header(
                    delegateDTO,
                    getSnlErrorVerifier(),
                    new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", "'"+positionTitle+"' is not a valid value for field 'personPositionTitle'", null));
        }


    }

    @Override
    public String getApiSubscriptionKey() {
        return super.getApiSubscriptionKey();
    }

    @ParameterizedTest(name = "personJOHType Positive Test Scenario")
    @CsvSource(value = {"AC,Smith, 2009-10-12, 2010-10-12, 1",
            "AC,ca7612bd-0f63-45b4-bbd7-dabfe3274ba14ba163a8-2e46-42cb-8c25-07da0f69fc2f-8c25-07, 2009-10-12, 2010-10-12, 1",
    })

    public void test_positive_response_for_person_person_joh_types(String positionCode, String personJOHTypesValue,
                                                                   String appointmentDate, String retirementDate, int rank) throws IOException {

        this.setInputPayloadFileName("resources-by-user-person-JOH-types.json");

        List<Map<String, Object>> PersonJOHTypeArray = new ArrayList<>();
        Map<String, Object> personJOHTypeValue1 = new HashMap<>();


        personJOHTypeValue1.put("bodyPositionCode", positionCode);
        personJOHTypeValue1.put("lastName", personJOHTypesValue);
        personJOHTypeValue1.put("appointmentDate", appointmentDate);
        personJOHTypeValue1.put("retirementDate", retirementDate);
        personJOHTypeValue1.put("rank", rank);

        PersonJOHTypeArray.add(personJOHTypeValue1);
        ObjectMapper objectMapper = new ObjectMapper();
        String data = objectMapper.writeValueAsString(PersonJOHTypeArray);

        generatePayloadWithRandomHMCTSIDAndPersonField(data, "/user/post/", "[\"personJOHTypes\"]");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));

    }

    @ParameterizedTest(name = "personJOHType Negative Test Scenario")
    @Disabled //Last name field is mandatory but is able to create a record with status 201.
    @CsvSource(value = {"AC,Smith, 2009/10/12, 2010-10-12, 1",
            "AC,ca7612bd-0f63-45b4-bbd7-dabfe3274ba14ba163a8-2e46-42cb-8c25-07da0f69fc2f-8c25-087, 2009-10-12, 2010-10-12, 1",
            "AC,Smith, 2009-10-12, 2010/10/12, 1",
            "' ',Smith, 2009-10-12, 2010-10-12, 1",
            "'',Smith, 2009-10-12, 2010-10-12, 1",
            "AC,'', 2009-10-12, 2010-10-12, 1",
            "AC, Test, '', 2010-10-12, 1"
            })

    public void test_negative_response_for_person_person_joh_types(String positionCode, String personJOHTypesValue,
                                                                   String appointmentDate, String retirementDate, int rank) throws IOException {

        this.setInputPayloadFileName("resources-by-user-person-JOH-types.json");

        List<Map<String, Object>> PersonJOHTypeArray = new ArrayList<>();
        Map<String, Object> personJOHTypeValue1 = new HashMap<>();


        personJOHTypeValue1.put("bodyPositionCode", positionCode);
        personJOHTypeValue1.put("lastName", personJOHTypesValue);
        personJOHTypeValue1.put("appointmentDate", appointmentDate);
        personJOHTypeValue1.put("retirementDate", retirementDate);
        personJOHTypeValue1.put("rank", rank);

        PersonJOHTypeArray.add(personJOHTypeValue1);
        ObjectMapper objectMapper = new ObjectMapper();
        String data = objectMapper.writeValueAsString(PersonJOHTypeArray);

        generatePayloadWithRandomHMCTSIDAndPersonField(data, "/user/post/", "[\"personJOHTypes\"]");
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());

        if(appointmentDate.equals("2009/10/12")) {
            commonDelegate.test_expected_response_for_supplied_header(
                    delegateDTO,
                    getSnlErrorVerifier(),
                    new SNLVerificationDTO(HttpStatus.BAD_REQUEST,
                            "1004",
                            "[$.userRequest.details.personJOHTypes[0].appointmentDate: "+appointmentDate+" is an invalid date]",
                            null));
        }
        else if(personJOHTypesValue.equals("ca7612bd-0f63-45b4-bbd7-dabfe3274ba14ba163a8-2e46-42cb-8c25-07da0f69fc2f-8c25-087")){
            commonDelegate.test_expected_response_for_supplied_header(
                    delegateDTO,
                    getSnlErrorVerifier(),
                    new SNLVerificationDTO(HttpStatus.BAD_REQUEST,
                            "1004",
                            "[$.userRequest.details.personJOHTypes[0].lastName: may only be 80 characters long]",
                            null));
        }

        else if(retirementDate.equals("2010/10/12")){
            commonDelegate.test_expected_response_for_supplied_header(
                    delegateDTO,
                    getSnlErrorVerifier(),
                    new SNLVerificationDTO(HttpStatus.BAD_REQUEST,
                            "1004",
                            "[$.userRequest.details.personJOHTypes[0].retirementDate: "+retirementDate+" is an invalid date]",
                            null));
        }
        else if(positionCode.equals(" ")){
            commonDelegate.test_expected_response_for_supplied_header(
                    delegateDTO,
                    getSnlErrorVerifier(),
                    new SNLVerificationDTO(HttpStatus.BAD_REQUEST,
                            "1000",
                            "' ' is not a valid value for field 'personJOHTypes[0].bodyPositionCode'",
                            null));
        }
        else if(positionCode.equals("")){

            commonDelegate.test_expected_response_for_supplied_header(
                    delegateDTO,
                    getSnlErrorVerifier(),
                    new SNLVerificationDTO(HttpStatus.BAD_REQUEST,
                            "1000",
                            "'' is not a valid value for field 'personJOHTypes[0].bodyPositionCode'",
                            null));
        }
        else if(personJOHTypesValue.equals("")){
            commonDelegate.test_expected_response_for_supplied_header(
                    delegateDTO,
                    getSnlErrorVerifier(),
                    new SNLVerificationDTO(HttpStatus.BAD_REQUEST,
                            "400",
                            null,
                            null));
        }
        else if(appointmentDate.equals("")){
            commonDelegate.test_expected_response_for_supplied_header(
                    delegateDTO,
                    getSnlErrorVerifier(),
                    new SNLVerificationDTO(HttpStatus.BAD_REQUEST,
                            "1004",
                            "[$.userRequest.details.personJOHTypes[0].appointmentDate:  is an invalid date]",
                            null));

        }
    }
}


