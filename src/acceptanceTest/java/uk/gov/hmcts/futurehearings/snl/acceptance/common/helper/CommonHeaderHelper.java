package uk.gov.hmcts.futurehearings.snl.acceptance.common.helper;

import static uk.gov.hmcts.futurehearings.snl.acceptance.common.header.dto.factory.PayloadHeaderDTOFactory.buildStandardBusinessHeaderPart;
import static uk.gov.hmcts.futurehearings.snl.acceptance.common.header.dto.factory.PayloadHeaderDTOFactory.buildStandardSystemHeaderPart;
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

    private static final String SNL_DESTINATION_SYSTEM = "SNL";
    private static final String DESTINATION_SYSTEM = SNL_DESTINATION_SYSTEM;

    public static final Map<String, String> createCompletePayloadHeader() {

        return buildHeaderWithValues(MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_JSON_VALUE,
                "no-cache",
                null,
                "2012-03-19T07:22:00Z",
                "2012-03-19T07:22:00Z",
                "CFT",
                DESTINATION_SYSTEM,
                UUID.randomUUID().toString()
        );
    }

    public static final Map<String, String> createStandardPayloadHeader() {

        return buildHeaderWithValues(MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_JSON_VALUE,
                "2012-03-19T07:22:00Z",
                "2012-03-19T07:22:00Z",
                "CFT",
                DESTINATION_SYSTEM,
                UUID.randomUUID().toString()
        );
    }

    public static final Headers createStandardPayloadHeaderWithDuplicateValues(Map<String, String> duplicateHeaderValues) {

        return buildHeaderWithDoubleValues(MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_JSON_VALUE,
                "no-cache",
                null,
                "2012-03-19T07:22:00Z",
                "2012-03-19T07:22:00Z",
                "CFT",
                DESTINATION_SYSTEM,
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

    public static final Map<String, String> createHeaderWithCorruptedHeaderKey(final List<String> headersToBeTruncated) {

        return buildHeaderWithValuesWithKeysTruncated(MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_JSON_VALUE,
                "2012-03-19T07:22:00Z",
                "2012-03-19T07:22:00Z",
                "CFT",
                DESTINATION_SYSTEM,
                UUID.randomUUID().toString(),
                headersToBeTruncated
        );
    }

    public static final Map<String, String> createHeaderWithRemovedHeaderKey(final List<String> headersToBeRemoved) {

        return buildHeaderWithValuesWithKeysTruncated(MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_JSON_VALUE,
                "2012-03-19T07:22:00Z",
                "2012-03-19T07:22:00Z",
                "CFT",
                DESTINATION_SYSTEM,
                UUID.randomUUID().toString(),
                headersToBeRemoved
        );
    }

    public static Map<String, String> createHeaderWithSourceSystemValue(final String sourceSystem) {

        return buildHeaderWithValues(MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_JSON_VALUE,
                "2012-03-19T07:22:00Z",
                "2012-03-19T07:22:00Z",
                sourceSystem,
                DESTINATION_SYSTEM,
                UUID.randomUUID().toString()
        );
    }

    public static Map<String, String> createHeaderWithDestinationSystemValue(final String destinationSystem) {

        return buildHeaderWithValues(MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_JSON_VALUE,
                "2012-03-19T07:22:00Z",
                "2012-03-19T07:22:00Z",
                "CFT",
                destinationSystem,
                UUID.randomUUID().toString()
        );
    }

    public static Map<String, String> createHeaderWithRequestCreatedAtSystemValue(final String requestCreatedAt) {

        return buildHeaderWithValues(MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_JSON_VALUE,
                requestCreatedAt,
                "2012-03-19T07:22:00Z",
                "CFT",
                DESTINATION_SYSTEM,
                UUID.randomUUID().toString()
        );
    }

    public static Map<String, String> createHeaderWithRequestProcessedAtSystemValue(final String requestProcessedAt) {

        return buildHeaderWithValues(MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_JSON_VALUE,
                "2012-03-19T07:22:00Z",
                requestProcessedAt,
                "CFT",
                DESTINATION_SYSTEM,
                UUID.randomUUID().toString()
        );
    }

    public static Map<String, String> createHeaderWithTransactionIdHMCTSAtSystemValue(final String transactionIdHMCTS) {

        return buildHeaderWithValues(MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_JSON_VALUE,
                "2012-03-19T07:22:00Z",
                "2012-03-19T07:22:00Z",
                "CFT",
                DESTINATION_SYSTEM,
                transactionIdHMCTS
        );
    }

    public static Map<String, String> createHeaderWithAcceptTypeAtSystemValue(final String acceptType) {

        return buildHeaderWithValues(MediaType.APPLICATION_JSON_VALUE,
                acceptType,
                "2012-03-19T07:22:00Z",
                "2012-03-19T07:22:00Z",
                "CFT",
                DESTINATION_SYSTEM,
                UUID.randomUUID().toString()
        );
    }

    public static final Map<String, String> createHeaderWithDeprecatedHeaderValue(final String deprecatedHeaderKey,
                                                                                  final String deprecatedHeaderVal
    ) {
        //Set invalid value for specific header key
        final String acceptType = deprecatedHeaderKey.equalsIgnoreCase("X-Accept") ? MediaType.APPLICATION_PDF_VALUE : MediaType.APPLICATION_JSON_VALUE;
        final String sourceSystem = deprecatedHeaderKey.equalsIgnoreCase("X-Source-System") ? "S&L" : "CFT";
        final String destinationSystem = deprecatedHeaderKey.equalsIgnoreCase("X-Destination-System") ? "CFT" : DESTINATION_SYSTEM;
        final String requestCreatedAt = deprecatedHeaderKey.equalsIgnoreCase("X-Request-Created-At") ? "2002-10-02T15:00:00*05Z" : "2012-03-19T07:22:00Z";
        final String requestProcessedAt = deprecatedHeaderKey.equalsIgnoreCase("X-Request-Processed-At") ? "2002-10-02T15:00:00*05Z" : "2012-03-19T07:22:00Z";

        Map<String, String> headers = convertToMapWithMandatoryHeaders(buildStandardSystemHeaderPart(
                MediaType.APPLICATION_JSON_VALUE,
                acceptType,
                null,
                null,
                null),
                buildStandardBusinessHeaderPart(requestCreatedAt,
                        requestProcessedAt,
                        sourceSystem,
                        destinationSystem,
                        UUID.randomUUID().toString()));
        headers.put(deprecatedHeaderKey, deprecatedHeaderVal);
        return Collections.unmodifiableMap(headers);
    }

    private static Map<String, String> buildHeaderWithValues(final String contentType,
                                                             final String acceptType,
                                                             final String requestCreatedDate,
                                                             final String requestProcessedAt,
                                                             final String sourceSystem,
                                                             final String destinationSystem,
                                                             final String transactionIdHMCTS) {
        return Collections.unmodifiableMap(convertToMapWithMandatoryHeaders(buildStandardSystemHeaderPart(
                contentType,
                acceptType,
                null,
                null,
                null),
                buildStandardBusinessHeaderPart(requestCreatedDate,
                        requestProcessedAt,
                        sourceSystem,
                        destinationSystem,
                        transactionIdHMCTS)));
    }

    private static Map<String, String> buildHeaderWithValues(final String contentType,
                                                             final String acceptType,
                                                             final String cacheControl,
                                                             final String contentEncoding,
                                                             final String requestCreatedDate,
                                                             final String requestProcessedAt,
                                                             final String sourceSystem,
                                                             final String destinationSystem,
                                                             final String transactionIdHMCTS) {
        return Collections.unmodifiableMap(convertToMapWithAllHeaders(buildStandardSystemHeaderPart(
                contentType,
                acceptType,
                null,
                contentEncoding,
                cacheControl),
                buildStandardBusinessHeaderPart(requestCreatedDate,
                        requestProcessedAt,
                        sourceSystem,
                        destinationSystem,
                        transactionIdHMCTS)));
    }

    private static Headers buildHeaderWithDoubleValues(final String contentType,
                                                       final String acceptType,
                                                       final String cacheControl,
                                                       final String contentEncoding,
                                                       final String requestCreatedDate,
                                                       final String requestProcessedAt,
                                                       final String sourceSystem,
                                                       final String destinationSystem,
                                                       final String transactionIdHMCTS,
                                                       final Map<String, String> extraHeaderValue) {
        return convertToRestAssuredHeaderRequiredHeaders(buildStandardSystemHeaderPart(
                contentType,
                acceptType,
                null,
                contentEncoding,
                cacheControl),
                buildStandardBusinessHeaderPart(requestCreatedDate,
                        requestProcessedAt,
                        sourceSystem,
                        destinationSystem,
                        transactionIdHMCTS), extraHeaderValue);
    }

    private static Map<String, String> buildHeaderWithValuesWithKeysTruncated(final String contentType,
                                                                              final String acceptType,
                                                                              final String requestCreatedDate,
                                                                              final String requestProcessedAt,
                                                                              final String sourceSystem,
                                                                              final String destinationSystem,
                                                                              final String transactionIdHMCTS,
                                                                              List<String> headersToTruncate) {
        return Collections.unmodifiableMap(convertToMapAfterTruncatingHeaderKey(buildStandardSystemHeaderPart(
                contentType,
                acceptType,
                null,
                null,
                null),
                buildStandardBusinessHeaderPart(requestCreatedDate,
                        requestProcessedAt,
                        sourceSystem,
                        destinationSystem,
                        transactionIdHMCTS), headersToTruncate));

    }

    private static Map<String, String> buildHeaderWithValuesWithKeysRemoved(final String contentType,
                                                                            final String acceptType,
                                                                            final String requestCreatedDate,
                                                                            final String requestProcessedAt,
                                                                            final String sourceSystem,
                                                                            final String destinationSystem,
                                                                            final String transactionIdHMCTS,
                                                                            List<String> headersToBeRemoved) {
        return Collections.unmodifiableMap(convertToMapAfterHeadersRemoved(buildStandardSystemHeaderPart(
                contentType,
                acceptType,
                null,
                null,
                null),
                buildStandardBusinessHeaderPart(requestCreatedDate,
                        requestProcessedAt,
                        sourceSystem,
                        destinationSystem,
                        transactionIdHMCTS), headersToBeRemoved));

    }

}
