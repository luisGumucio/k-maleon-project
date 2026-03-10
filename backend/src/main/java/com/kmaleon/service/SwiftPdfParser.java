package com.kmaleon.service;

import com.kmaleon.dto.SwiftMetadataResponse;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SwiftPdfParser {

    private static final Logger log = LoggerFactory.getLogger(SwiftPdfParser.class);

    private static final DateTimeFormatter SWIFT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MMM,yy", Locale.ENGLISH);
    private static final DateTimeFormatter ISO_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public SwiftMetadataResponse parse(InputStream pdfStream) {
        SwiftMetadataResponse result = new SwiftMetadataResponse();
        String text;

        try (PDDocument doc = Loader.loadPDF(pdfStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            text = stripper.getText(doc);
        } catch (IOException e) {
            log.warn("Failed to extract text from PDF: {}", e.getMessage());
            return result;
        }

        result.setMessageId(extract(text, "Message Identification\\s+(\\S+)"));
        result.setUetr(extract(text, "UETR\\s+([a-f0-9\\-]{36})"));
        result.setSettlementDate(parseSettlementDate(text));
        result.setChargeBearer(parseChargeBearer(text));
        result.setRemittance(extract(text, "Unstructured\\s+(.+)"));

        parseDebtorSection(text, result);
        parseCreditorSection(text, result);

        return result;
    }

    // --- Settlement date ---

    private String parseSettlementDate(String text) {
        // Matches: "Interbank Settlement Date    02.Mar,26"
        String raw = extract(text, "Interbank Settlement Date\\s+(\\d{2}\\.\\w+,\\d{2})");
        if (raw == null) return null;
        try {
            LocalDate date = LocalDate.parse(raw, SWIFT_DATE_FORMAT);
            return date.format(ISO_DATE_FORMAT);
        } catch (Exception e) {
            log.warn("Could not parse settlement date: {}", raw);
            return null;
        }
    }

    // --- Charge bearer: extract only the code before " -" ---

    private String parseChargeBearer(String text) {
        String raw = extract(text, "Charge Bearer\\s+(\\S+(?:\\s+-\\s+.+)?)");
        if (raw == null) return null;
        int dashIdx = raw.indexOf(" -");
        return dashIdx > 0 ? raw.substring(0, dashIdx).trim() : raw.trim();
    }

    // --- Debtor section ---
    // The PDF has multiple BICFI occurrences. We isolate the debtor section
    // by cutting between "Debtor Agent" and "Creditor Agent".

    private void parseDebtorSection(String text, SwiftMetadataResponse result) {
        String section = extractSection(text, "Debtor Agent", "Creditor Agent");
        if (section == null) return;

        result.setDebtorBic(extract(section, "BICFI\\s+(\\S+)"));
        result.setDebtorBank(extractLineAfterBicfi(section));

        // Debtor account: look between "Debtor Account" and "Debtor Agent"
        // Skip separator lines (only underscores) and sub-headers like "Identification", "Other"
        String accountSection = extractSection(text, "Debtor Account", "Debtor Agent");
        if (accountSection != null) {
            result.setDebtorAccount(extractAccountId(accountSection));
        }
    }

    // --- Creditor section ---
    // Isolated between "Creditor Agent" and "Instruction For Next Agent"

    private void parseCreditorSection(String text, SwiftMetadataResponse result) {
        String agentSection = extractSection(text, "Creditor Agent", "Creditor\n");
        if (agentSection == null) {
            agentSection = extractSection(text, "Creditor Agent", "Creditor");
        }
        if (agentSection != null) {
            result.setCreditorBic(extract(agentSection, "BICFI\\s+(\\S+)"));
            result.setCreditorBank(extractLineAfterBicfi(agentSection));
        }

        result.setCreditorName(extract(text, "Creditor\\s*\\nName\\s+(.+)"));
        if (result.getCreditorName() == null) {
            result.setCreditorName(extract(text, "(?m)^Name\\s{2,}(.+)$",
                    indexOf(text, "Creditor\n", indexOf(text, "Creditor Agent", 0))));
        }

        String accountSection = extractSection(text, "Creditor Account", "Instruction For Next Agent");
        if (accountSection != null) {
            result.setCreditorAccount(extractAccountId(accountSection));
        }
    }

    // --- Helpers ---

    private String extract(String text, String regex) {
        return extract(text, regex, 0);
    }

    private String extract(String text, String regex, int fromIndex) {
        if (text == null || fromIndex < 0) return null;
        String searchIn = fromIndex > 0 && fromIndex < text.length()
                ? text.substring(fromIndex)
                : text;
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(searchIn);
        if (m.find()) {
            return m.group(1).trim();
        }
        return null;
    }

    private String extractSection(String text, String startMarker, String endMarker) {
        int start = text.indexOf(startMarker);
        if (start < 0) return null;
        start += startMarker.length();
        int end = text.indexOf(endMarker, start);
        if (end < 0) return text.substring(start);
        return text.substring(start, end);
    }

    private int indexOf(String text, String marker, int from) {
        if (from < 0) return -1;
        return text.indexOf(marker, from);
    }

    // Extracts the account number from a section that looks like:
    // Identification\n____\nOther\n____\nIdentification  90993132187
    // The actual number appears after "Identification" label followed by the value on the same line,
    // but only if it's NOT just underscores.
    private String extractAccountId(String section) {
        // Try inline: "Identification  <number>" where number contains digits
        Pattern inline = Pattern.compile("(?m)^Identification\\s+(\\d[\\w]+)$", Pattern.CASE_INSENSITIVE);
        Matcher m = inline.matcher(section);
        if (m.find()) {
            return m.group(1).trim();
        }
        // Fallback: any line that is purely alphanumeric (digits/letters, no underscores, no spaces)
        // and appears after "Other" keyword — this is the raw account number
        String otherSection = extractSection(section, "Other\n", null);
        if (otherSection != null) {
            for (String line : otherSection.split("\\n")) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && trimmed.matches("[\\w\\-]+") && !trimmed.contains("_")) {
                    return trimmed;
                }
            }
        }
        return null;
    }

    private String extractLineAfterBicfi(String section) {
        // The bank name is the first non-empty line after the BICFI line
        Pattern p = Pattern.compile("BICFI\\s+\\S+\\s*\\n(.+)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(section);
        if (m.find()) {
            return m.group(1).trim();
        }
        return null;
    }
}
