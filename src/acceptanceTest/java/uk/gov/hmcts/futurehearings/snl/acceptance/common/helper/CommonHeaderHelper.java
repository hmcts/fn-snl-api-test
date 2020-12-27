package uk.gov.hmcts.futurehearings.snl.acceptance.common.helper;

import static uk.gov.hmcts.futurehearings.snl.acceptance.common.header.dto.factory.PayloadHeaderDTOFactory.buildStandardBusinessHeaderPart;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.header.dto.factory.PayloadHeaderDTOFactory.buildStandardSytemHeaderPart;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.header.dto.factory.PayloadHeaderDTOFactory.convertToMapAfterHeadersRemoved;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.header.dto.factory.PayloadHeaderDTOFactory.convertToMapAfterTruncatingHeaderKey;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.header.dto.factory.PayloadHeaderDTOFactory.convertToMapWithAllHeaders;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.header.dto.factory.PayloadHeaderDTOFactory.convertToMapWithMandatoryHeaders;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.header.dto.factory.PayloadHeaderDTOFactory.convertToRestAssuredHeaderRequiredHeaders;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.restassured.http.Headers;
import org.springframework.http.MediaType;

public class CommonHeaderHelper {

    private static final String SNL_DESTINATION_SYSTEM = "S&L";
    private static final String DESTINATION_SYSTEM = SNL_DESTINATION_SYSTEM;
    public static final String CHARSET_UTF_8 = "; charset=UTF-8";

    public static final Map<String, String> createCompletePayloadHeader(final String subscriptionKey) {

        return buildHeaderWithValues(MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_JSON_VALUE + ";version=1.2",
                subscriptionKey,
                "no-cache",
                null,
                "2012-03-19T07:22:00Z",
                "2012-03-19T07:22:00Z",
                "CFT",
                DESTINATION_SYSTEM,
                "text",
                UUID.randomUUID().toString()
        );
    }

    public static final Map<String, String> createStandardPayloadHeader(final String subscriptionKey) {

        return buildHeaderWithValues(MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_JSON_VALUE + ";version=1.2",
                subscriptionKey,
                "2012-03-19T07:22:00Z",
                "2012-03-19T07:22:00Z",
                "CFT",
                DESTINATION_SYSTEM,
                "text",
                UUID.randomUUID().toString()
        );
    }

    public static final Headers createStandardPayloadHeaderWithDuplicateValues(final String subscriptionKey,
                                                                               Map<String, String> duplicateHeaderValues) {

        return buildHeaderWithDoubleValues(MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_JSON_VALUE + ";version=1.2",
                subscriptionKey,
                "no-cache",
                null,
                "2012-03-19T07:22:00Z",
                "2012-03-19T07:22:00Z",
                "CFT",
                DESTINATION_SYSTEM,
                "text",
                UUID.randomUUID().toString(),
                duplicateHeaderValues
        );
    }

    public static final Map<String, String> createHeaderWithAllValuesNull() {

        return buildHeaderWithValues(MediaType.APPLICATION_JSON_VALUE,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static final Map<String, String> createHeaderWithAllValuesEmpty() {

        return buildHeaderWithValues(MediaType.APPLICATION_JSON_VALUE,
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                ""
        );
    }

    public static final Map<String, String> createHeaderWithCorruptedHeaderKey(final String subscriptionKey,
                                                                               final List<String> headersToBeTruncated) {

        return buildHeaderWithValuesWithKeysTruncated(MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_JSON_VALUE + ";version=1.2",
                subscriptionKey,
                "2012-03-19T07:22:00Z",
                "2012-03-19T07:22:00Z",
                "CFT",
                DESTINATION_SYSTEM,
                "text",
                UUID.randomUUID().toString(),
                headersToBeTruncated
        );
    }

    public static final Map<String, String> createHeaderWithRemovedHeaderKey(final String subscriptionKey,
                                                                             final List<String> headersToBeRemoved) {

        return buildHeaderWithValuesWithKeysTruncated(MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_JSON_VALUE + ";version=1.2",
                subscriptionKey,
                "2012-03-19T07:22:00Z",
                "2012-03-19T07:22:00Z",
                "CFT",
                DESTINATION_SYSTEM,
                "text",
                UUID.randomUUID().toString(),
                headersToBeRemoved
        );
    }

    public static Map<String, String> createHeaderWithSourceSystemValue(final String subscriptionKey,
                                                                        final String sourceSystem) {

        return buildHeaderWithValues(MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_JSON_VALUE + ";version=1.2",
                subscriptionKey,
                "2012-03-19T07:22:00Z",
                "2012-03-19T07:22:00Z",
                sourceSystem,
                DESTINATION_SYSTEM,
                "text",
                UUID.randomUUID().toString()
        );
    }

    public static Map<String, String> createHeaderWithDestinationSystemValue(final String subscriptionKey,
                                                                             final String destinationSystem) {

        return buildHeaderWithValues(MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_JSON_VALUE + ";version=1.2",
                subscriptionKey,
                "2012-03-19T07:22:00Z",
                "2012-03-19T07:22:00Z",
                "CFT",
                destinationSystem,
                "text",
                UUID.randomUUID().toString()
        );
    }

    public static Map<String, String> createHeaderWithRequestCreatedAtSystemValue(final String subscriptionKey,
                                                                                  final String requestCreatedAt) {

        return buildHeaderWithValues(MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_JSON_VALUE + ";version=1.2",
                subscriptionKey,
                requestCreatedAt,
                "2012-03-19T07:22:00Z",
                "CFT",
                DESTINATION_SYSTEM,
                "text",
                UUID.randomUUID().toString()
        );
    }

    public static Map<String, String> createHeaderWithRequestProcessedAtSystemValue(final String subscriptionKey,
                                                                                    final String requestProcessedAt) {

        return buildHeaderWithValues(MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_JSON_VALUE + ";version=1.2",
                subscriptionKey,
                "2012-03-19T07:22:00Z",
                requestProcessedAt,
                "CFT",
                DESTINATION_SYSTEM,
                "text",
                UUID.randomUUID().toString()
        );
    }

    public static Map<String, String> createHeaderWithTransactionIdHMCTSAtSystemValue(final String subscriptionKey,
                                                                                 final String transactionIdHMCTS) {

        return buildHeaderWithValues(MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_JSON_VALUE + ";version=1.2",
                subscriptionKey,
                "2012-03-19T07:22:00Z",
                "2012-03-19T07:22:00Z",
                "CFT",
                DESTINATION_SYSTEM,
                "text",
                transactionIdHMCTS
        );
    }

    public static Map<String, String> createHeaderWithAcceptTypeAtSystemValue(final String subscriptionKey,
                                                                              final String acceptType) {

        return buildHeaderWithValues(MediaType.APPLICATION_JSON_VALUE,
                acceptType,
                subscriptionKey,
                "2012-03-19T07:22:00Z",
                "2012-03-19T07:22:00Z",
                "CFT",
                DESTINATION_SYSTEM,
                "text",
                UUID.randomUUID().toString()
        );
    }

    public static final Map<String, String> createHeaderWithDeprecatedHeaderValue(final String subscriptionKey,
                                                                                  final String deprecatedHeaderKey,
                                                                                  final String deprecatedHeaderVal
    ) {
        //Set invalid value for specific header key
        final String acceptType = deprecatedHeaderKey.equalsIgnoreCase("X-Accept") ? MediaType.APPLICATION_PDF_VALUE : MediaType.APPLICATION_JSON_VALUE;
        final String sourceSystem = deprecatedHeaderKey.equalsIgnoreCase("X-Source-System") ? "S&L" : "CFT";
        final String destinationSystem = deprecatedHeaderKey.equalsIgnoreCase("X-Destination-System") ? "CFT" : DESTINATION_SYSTEM;
        final String requestType = deprecatedHeaderKey.equalsIgnoreCase("X-Request-Type") ? "Robbery" : "Assault";
        final String requestCreatedAt = deprecatedHeaderKey.equalsIgnoreCase("X-Request-Created-At") ? "2002-10-02T15:00:00*05Z" : "2012-03-19T07:22:00Z";
        final String requestProcessedAt = deprecatedHeaderKey.equalsIgnoreCase("X-Request-Processed-At") ? "2002-10-02T15:00:00*05Z" : "2012-03-19T07:22:00Z";

        Map<String, String> headers = convertToMapWithMandatoryHeaders(buildStandardSytemHeaderPart(
                MediaType.APPLICATION_JSON_VALUE,
                acceptType + "; version=1.2",
                null,
                null,
                subscriptionKey,
                null),
                buildStandardBusinessHeaderPart(requestCreatedAt,
                        requestProcessedAt,
                        sourceSystem,
                        destinationSystem,
                        requestType, UUID.randomUUID().toString()));
        headers.put(deprecatedHeaderKey, deprecatedHeaderVal);
        return Collections.unmodifiableMap(headers);
    }

    private static Map<String, String> buildHeaderWithValues(final String contentType,
                                                             final String acceptType,
                                                             final String subscriptionKey,
                                                             final String requestCreatedDate,
                                                             final String requestProcessedAt,
                                                             final String sourceSystem,
                                                             final String destinationSystem,
                                                             final String requestType,
                                                             final String transactionIdHMCTS) {
        return Collections.unmodifiableMap(convertToMapWithMandatoryHeaders(buildStandardSytemHeaderPart(
                contentType,
                acceptType,
                null,
                null,
                subscriptionKey,
                null),
                buildStandardBusinessHeaderPart(requestCreatedDate,
                        requestProcessedAt,
                        sourceSystem,
                        destinationSystem,
                        requestType, transactionIdHMCTS)));
    }

    private static Map<String, String> buildHeaderWithValues(final String contentType,
                                                             final String acceptType,
                                                             final String subscriptionKey,
                                                             final String cacheControl,
                                                             final String contentEncoding,
                                                             final String requestCreatedDate,
                                                             final String requestProcessedAt,
                                                             final String sourceSystem,
                                                             final String destinationSystem,
                                                             final String requestType,
                                                             final String transactionIdHMCTS) {
        return Collections.unmodifiableMap(convertToMapWithAllHeaders(buildStandardSytemHeaderPart(
                contentType,
                acceptType,
                null,
                contentEncoding,
                subscriptionKey,
                cacheControl),
                buildStandardBusinessHeaderPart(requestCreatedDate,
                        requestProcessedAt,
                        sourceSystem,
                        destinationSystem,
                        requestType, transactionIdHMCTS)));
    }

    private static Headers buildHeaderWithDoubleValues(final String contentType,
                                                       final String acceptType,
                                                       final String subscriptionKey,
                                                       final String cacheControl,
                                                       final String contentEncoding,
                                                       final String requestCreatedDate,
                                                       final String requestProcessedAt,
                                                       final String sourceSystem,
                                                       final String destinationSystem,
                                                       final String requestType,
                                                       final String transactionIdHMCTS,
                                                       final Map<String, String> extraHeaderValue) {
        return convertToRestAssuredHeaderRequiredHeaders(buildStandardSytemHeaderPart(
                contentType,
                acceptType,
                null,
                contentEncoding,
                subscriptionKey,
                cacheControl),
                buildStandardBusinessHeaderPart(requestCreatedDate,
                        requestProcessedAt,
                        sourceSystem,
                        destinationSystem,
                        requestType, transactionIdHMCTS), extraHeaderValue);
    }

    private static Map<String, String> buildHeaderWithValuesWithKeysTruncated(final String contentType,
                                                                              final String acceptType,
                                                                              final String subscriptionKey,
                                                                              final String requestCreatedDate,
                                                                              final String requestProcessedAt,
                                                                              final String sourceSystem,
                                                                              final String destinationSystem,
                                                                              final String requestType,
                                                                              final String transactionIdHMCTS,
                                                                              List<String> headersToTruncate) {
        return Collections.unmodifiableMap(convertToMapAfterTruncatingHeaderKey(buildStandardSytemHeaderPart(
                contentType,
                acceptType,
                null,
                null,
                subscriptionKey,
                null),
                buildStandardBusinessHeaderPart(requestCreatedDate,
                        requestProcessedAt,
                        sourceSystem,
                        destinationSystem,
                        requestType, transactionIdHMCTS), headersToTruncate));

    }

    private static Map<String, String> buildHeaderWithValuesWithKeysRemoved(final String contentType,
                                                                            final String acceptType,
                                                                            final String subscriptionKey,
                                                                            final String requestCreatedDate,
                                                                            final String requestProcessedAt,
                                                                            final String sourceSystem,
                                                                            final String destinationSystem,
                                                                            final String requestType,
                                                                            final String transactionIdHMCTS,
                                                                            List<String> headersToBeRemoved) {
        return Collections.unmodifiableMap(convertToMapAfterHeadersRemoved(buildStandardSytemHeaderPart(
                contentType,
                acceptType,
                null,
                null,
                subscriptionKey,
                null),
                buildStandardBusinessHeaderPart(requestCreatedDate,
                        requestProcessedAt,
                        sourceSystem,
                        destinationSystem,
                        requestType,transactionIdHMCTS), headersToBeRemoved));

    }

}
