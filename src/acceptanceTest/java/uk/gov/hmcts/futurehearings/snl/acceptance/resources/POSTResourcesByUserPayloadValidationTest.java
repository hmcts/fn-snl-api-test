package uk.gov.hmcts.futurehearings.snl.acceptance.resources;

import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createCompletePayloadHeader;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.helper.CommonHeaderHelper.createStandardPayloadHeader;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.test.SNLCommonHeaderTest.INPUT_FILE_PATH;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.test.SNLCommonPayloadTest.INPUT_TEMPLATE_FILE_PATH;

import uk.gov.hmcts.futurehearings.snl.Application;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.TestingUtils;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.CommonDelegate;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.delegate.dto.DelegateDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.dto.SNLVerificationDTO;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.error.SNLCommonErrorVerifier;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.verify.success.SNLCommonSuccessVerifier;

import java.io.IOException;
import java.util.UUID;

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
        generateResourcesByUserPayloadWithRandomHMCTSId();
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
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
        generateResourcesByUserPayloadWithRandomHMCTSId();
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "PersonHMCTSID Negative tests")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "Invalid_Source_System, C_FEFC2424-32A6-4B3A-BD97-023296C7F76DC_FEFC2424-32A6-4B3A-BD97-023296C7F76DC_FEFC2424-32A6-4B3A-BD97-023296C7F76D"}, nullValues = "NIL")
    public void test_negative_response_with_mandatory_elements_payload(final String personHMCTSIDKey, final String personHMCTSIDValue) throws Exception {

        this.setInputPayloadFileName("resources-by-username-mandatory-person-id.json");
        generateResourcesByUserPayloadWithHMCTSID(personHMCTSIDValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
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
        generateResourcesByUserPayloadWithRandomHMCTSIDAndField(personFirstNameValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.userRequest.details.personFirstName: may only be 80 characters long]", null));
    }

    @ParameterizedTest(name = "PersonFirstName Positive Tests Scenario : {0}")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "max_length_first_name,123TEETING!£$^&*(_ajkfhbabgk+_)(*%876dfk123TEETING!£$^&*(_ajkfhbabgk+_)(*%876dfk"}, nullValues = "NIL")
    public void test_positive_response_for_person_first_name_with_mandatory_elements_payload(final String personFirstNameKey, final String personFirstNameValue) throws Exception {

        this.setInputPayloadFileName("resources-by-username-mandatory-person-first-name.json");
        generateResourcesByUserPayloadWithRandomHMCTSIDAndField(personFirstNameValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
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
        generateResourcesByUserPayloadWithRandomHMCTSIDAndField(personLastNameValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1004", "[$.userRequest.details.personLastName: may only be 30 characters long]", null));
    }

    @ParameterizedTest(name = "PersonLastName Positive Tests Scenario : {0}")
    @CsvSource(value = {"Empty Space,''", "Single Space,' '", "max_length_first_name,123TEETING!£$^&*(_ajkfhbabgk+_"}, nullValues = "NIL")
    public void test_positive_response_for_person_last_name_with_mandatory_elements_payload(final String personLastNameKey,
                                                                                            final String personLastNameValue) throws Exception {

        this.setInputPayloadFileName("resources-by-username-mandatory-person-first-name.json");
        generateResourcesByUserPayloadWithRandomHMCTSIDAndField(personLastNameValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "PersonRegistry Positive Tests Scenario : {0}")
    @CsvSource(value = {"KNT,KNT", "TV,TV"}, nullValues = "NIL")
    //TODO - Confirm that only these 2 values are valid LOV Values for this field.
    public void test_positive_response_for_person_registry_with_mandatory_elements_payload(final String personRegistryKey,
                                                                                           final String personRegistryValue) throws Exception {

        this.setInputPayloadFileName("resources-by-username-mandatory-person-registry.json");
        generateResourcesByUserPayloadWithRandomHMCTSIDAndField(personRegistryValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
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
        generateResourcesByUserPayloadWithRandomHMCTSIDAndField(personRegistryValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlErrorVerifier(),
                new SNLVerificationDTO(HttpStatus.BAD_REQUEST, "1000",
                        "'" + personRegistryValue + "' is not a valid value for field 'personRegistry'", null));
    }

    @ParameterizedTest(name = "PersonRegistry Positive Tests Scenario : {0}")
    @CsvSource(value = {"Mr,Mr","Miss,Miss","TRY,TRY"}, nullValues = "NIL")
    public void test_positive_response_for_person_salutation_with_mandatory_elements_payload(final String personSalutationKey,
                                                                                           final String personSalutationValue) throws Exception {

        this.setInputPayloadFileName("resources-by-username-mandatory-person-salutation.json");
        generateResourcesByUserPayloadWithRandomHMCTSIDAndField(personSalutationValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    @ParameterizedTest(name = "PersonRegistry Positive Tests Scenario : {0}")
    @CsvSource(value = {"Mr,Mr","Miss,Miss","TRY,TRY"}, nullValues = "NIL")
    public void test_negative_response_for_person_salutation_with_mandatory_elements_payload(final String personSalutationKey,
                                                                                             final String personSalutationValue) throws Exception {

        this.setInputPayloadFileName("resources-by-username-mandatory-person-salutation.json");
        generateResourcesByUserPayloadWithRandomHMCTSIDAndField(personSalutationValue);
        DelegateDTO delegateDTO = buildDelegateDTO(getRelativeURL(),
                createStandardPayloadHeader(getApiSubscriptionKey()), getHttpMethod(), getHttpSuccessStatus());
        log.debug("The value of the Delegate Payload : " + delegateDTO.inputPayload());
        commonDelegate.test_expected_response_for_supplied_header(
                delegateDTO,
                getSnlSuccessVerifier(),
                new SNLVerificationDTO(getHttpSuccessStatus(), null, null, null));
    }

    private void generateResourcesByUserPayloadWithRandomHMCTSId() throws IOException {
        final String randomID = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + "/" + getInputPayloadFileName()), randomID));
    }

    private void generateResourcesByUserPayloadWithHMCTSID(final String randomID) throws IOException {
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + "/" + getInputPayloadFileName()), randomID));
    }

    private void generateResourcesByUserPayloadWithRandomHMCTSIDAndField(final String formatValue) throws IOException {
        final String randomID = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + "/" + getInputPayloadFileName()), randomID, formatValue));
    }
}


