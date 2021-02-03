package uk.gov.hmcts.futurehearings.snl.acceptance.resources;

import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createStandardPayloadHeader;

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
        generatePayloadWithHMCTSID("hmctsqa@hmcts.net", "/user/post/");
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
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "Not An Email id,testing", "Invalid_Source_System, C_FEFC2424-32A6-4B3A-BD97-023296C7F76DC_FEFC2424-32A6-4B3A-BD97-023296C7F76DC_FEFC2424-32A6-4B3A-BD97-023296C7F76D"}, nullValues = "NIL")
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
    @CsvSource(value = {"Valid LOV Value,100", "Valid LOV Value,152"}, nullValues = "NIL")
    //TODO - Raise a Defect as "Valid LOV Value,33,34 onwards" is failing the Test.
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
        if ( personRoleIdKey.trim().equals("Null Value")) {
            snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.userRequest.details.personRoleId: may only be 3 characters long]", null);
        } else {
            snlVerificationDTO = new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", "'"+personRoleIdValue+"' is not a valid value for field 'personRoleId'", null);
        }
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                snlVerificationDTO);
    }

    //Getting the LOV Values from the MCGirr Spreadsheet - Hearing LOV's Locations Section
    @ParameterizedTest(name = "PersonVenueID Positive Tests Scenario : {0} - {1}")
    @CsvSource(value = {"Valid LOV Value,300", "Valid LOV Value,350", "Valid LOV Value,386"}, nullValues = "NIL")
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
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000", "'"+personVenueIdValue+"' is not a valid value for field 'personVenueId'", null));
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
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.userRequest.details.personActiveDate: "+personActiveDateValue+" is an invalid date]", null));
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
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.userRequest.details.personInactiveDate: "+personInactiveDateValue+" is an invalid date]", null));
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
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.userRequest.details."+userSchemaElement+": is missing but it is required]", null));
    }
}


