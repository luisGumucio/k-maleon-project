package com.kmaleon.service;

import com.kmaleon.dto.SwiftMetadataResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class SwiftPdfParserTest {

    private SwiftPdfParser parser;

    @BeforeEach
    void setUp() {
        parser = new SwiftPdfParser();
    }

    @Test
    void parse_withValidSwiftPdf_returnsAllFields() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("swift-sample.pdf")) {
            Objects.requireNonNull(is, "swift-sample.pdf not found in test resources");

            SwiftMetadataResponse result = parser.parse(is);

            assertThat(result.getMessageId()).isEqualTo("22591270001");
            assertThat(result.getUetr()).isEqualTo("9b6df95f-ce82-4871-b067-3e4d6d1b1ff1");
            assertThat(result.getSettlementDate()).isEqualTo("2026-03-02");
            assertThat(result.getDebtorBic()).isEqualTo("BKSACLRMXXX");
            assertThat(result.getDebtorBank()).isEqualToIgnoringCase("SCOTIABANK CHILE");
            assertThat(result.getDebtorAccount()).isEqualTo("90993132187");
            assertThat(result.getCreditorBic()).isEqualTo("CHASHKHHXXX");
            assertThat(result.getCreditorName()).isEqualToIgnoringCase("YIWU XUANSHI E-COMERCE FIRM");
            assertThat(result.getCreditorAccount()).isEqualTo("63003673626");
            assertThat(result.getRemittance()).isEqualToIgnoringCase("PAGO DE MERCADERIA");
            assertThat(result.getChargeBearer()).isEqualTo("DEBT");
        }
    }

    @Test
    void parse_withEmptyStream_returnsAllNulls() {
        // Minimal valid PDF-like content that PDFBox can handle gracefully
        SwiftMetadataResponse result = parser.parse(new ByteArrayInputStream(new byte[0]));

        assertThat(result.getMessageId()).isNull();
        assertThat(result.getUetr()).isNull();
        assertThat(result.getSettlementDate()).isNull();
        assertThat(result.getDebtorBic()).isNull();
        assertThat(result.getCreditorBic()).isNull();
        assertThat(result.getRemittance()).isNull();
        assertThat(result.getChargeBearer()).isNull();
    }

    @Test
    void parse_withValidSwiftPdf_chargeBearerIsCodeOnly() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("swift-sample.pdf")) {
            Objects.requireNonNull(is, "swift-sample.pdf not found in test resources");

            SwiftMetadataResponse result = parser.parse(is);

            // Should be "DEBT", not "DEBT - Borne by Debtor"
            assertThat(result.getChargeBearer()).doesNotContain("-");
            assertThat(result.getChargeBearer()).isEqualTo("DEBT");
        }
    }
}
