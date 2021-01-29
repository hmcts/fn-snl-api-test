package uk.gov.hmcts.futurehearings.snl.acceptance.resources;

import static io.restassured.config.EncoderConfig.encoderConfig;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.TestingUtils.replaceCharacterSequence;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.security.OAuthTokenGenerator.generateOAuthToken;

import uk.gov.hmcts.futurehearings.snl.Application;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.TestingUtils;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.test.SNLCommonHeaderTest;
import uk.gov.hmcts.futurehearings.snl.acceptance.common.test.SNLCommonPayloadTest;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest(classes = {Application.class})
@ActiveProfiles("acceptance")
public abstract class ResourcesPayloadValidationTest extends SNLCommonPayloadTest {

    @Value("${targetInstance}")
    private String targetInstance;

    @Value("${targetSubscriptionKey}")
    private String targetSubscriptionKey;

    @Value("${token_apiURL}")
    private String token_apiURL;

    @Value("${token_username}")
    private String token_username;

    @Value("${token_password}")
    private String token_password;

    @Value("${expired_access_token}")
    private String expired_access_token;

    @BeforeAll
    public void initialiseValues() throws Exception {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
        this.setApiSubscriptionKey("pointless");
        RestAssured.config = RestAssured.config()
                .encoderConfig(encoderConfig().defaultContentCharset("UTF-8").appendDefaultContentCharsetToContentTypeIfUndefined(true));
        this.setInputFileDirectory("resources");
        String authorizationToken = generateOAuthToken(token_apiURL,
                token_username,
                token_password,
                HttpStatus.OK);
        this.setAuthorizationToken(authorizationToken);
    }

    final void generatePayloadWithRandomHMCTSID(final String templatePath) throws IOException {
        final String randomID = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + templatePath + getInputPayloadFileName()), randomID));
    }

    final String generatePayloadWithRandomHMCTSID(int maxLength, final String templatePath) throws IOException {
        final String randomID = UUID.randomUUID().toString().substring(0, maxLength);
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + templatePath + getInputPayloadFileName()), randomID));
        return randomID;
    }

    final void generatePayloadWithHMCTSID(final String randomID, final String templatePath) throws IOException {
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + templatePath + getInputPayloadFileName()), randomID));
    }

    final void generatePayloadWithHMCTSIDAndField(final String randomID, final String formatValue, final String templatePath) throws IOException {
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + templatePath + getInputPayloadFileName()), randomID, formatValue));
    }

    final void generatePayloadWithRandomHMCTSIDAndField(final String formatValue, final String templatePath) throws IOException {
        final String randomID = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        this.setInputBodyPayload(String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + templatePath + getInputPayloadFileName()), randomID, formatValue));
    }

    final void generateLocationPayloadWithRandomHMCTSIDAndFieldTokenReplace(final String token, final String value, final String templatePath) throws IOException {
        final int randomId = new Random().nextInt(99999999);
        String formattedString = String.format(TestingUtils.readFileContents(String.format(INPUT_TEMPLATE_FILE_PATH,
                getInputFileDirectory()) + templatePath + getInputPayloadFileName()), randomId, value);
        this.setInputBodyPayload(replaceCharacterSequence(token, value, formattedString));
    }
}
