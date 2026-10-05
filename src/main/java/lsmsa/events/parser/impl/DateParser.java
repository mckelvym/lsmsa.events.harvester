package lsmsa.events.parser.impl;

import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized date parsing utility
 */
public final class DateParser {

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
        DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("M/d/yyyy", Locale.US),
        DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US),
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.RFC_1123_DATE_TIME
    );
    private static final Logger LOG = LoggerFactory.getLogger(DateParser.class);

    /**
     * Parses a date string to LocalDate using multiple format strategies.
     *
     * @param dateStr the date string to parse
     * @return the parsed LocalDate, or null if parsing fails
     */
    public LocalDate parse(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }

        String trimmed = dateStr.trim();
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                TemporalAccessor parsed = formatter.parseBest(
                    trimmed,
                    LocalDate::from,
                    LocalDate::from
                );
                return LocalDate.from(parsed);
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }

        LOG.warn("Could not parse date '{}' with any known format", dateStr);
        return null;
    }

    /**
     * Parses a date string, inferring year if missing.
     * If the resulting date is more than 2 months in the past, assumes next year.
     *
     * @param dateStr the date string to parse (may lack year)
     * @return the parsed LocalDate, or null if parsing fails
     */
    public LocalDate parseWithYearInference(String dateStr) {
        // First try standard parsing
        LocalDate date = parse(dateStr);
        if (date != null) {
            return date;
        }

        // If standard parsing fails, try adding current year
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }

        int currentYear = Year.now().getValue();
        String dateWithYear = dateStr.trim() + "/" + currentYear;

        // Try M/d/yyyy format with appended year
        try {
            date = LocalDate.parse(dateWithYear,
                DateTimeFormatter.ofPattern("M/d/yyyy", Locale.US));
        } catch (DateTimeParseException e) {
            // Try other formats if needed
            return null;
        }

        // If date is more than 2 months in the past, assume next year
        if (date.isBefore(LocalDate.now().minusMonths(2))) {
            date = date.plusYears(1);
        }

        return date;
    }
}
