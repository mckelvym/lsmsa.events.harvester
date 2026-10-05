package lsmsa.events.parser.impl;

import static java.util.Objects.requireNonNull;
import static lsmsa.events.parser.impl.CssSelectors.TIME;
import static lsmsa.events.parser.impl.CssSelectors.TIME_LABEL;
import static lsmsa.events.parser.impl.HtmlConstants.DATETIME_ATTR;
import static lsmsa.events.parser.impl.HtmlConstants.EMPTY;

import java.time.LocalDate;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts the publication date from an event item element.
 */
public final class DateExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(DateExtractor.class);

    private final DateParser dateParser;

    /**
     * Constructs a DateExtractor with a default DateParser.
     */
    public DateExtractor() {
        this.dateParser = new DateParser();
    }

    /**
     * Extracts and parses the date from a news item element.
     *
     * @param eventElement the news item element
     * @return the parsed LocalDate, or null if not found or unparseable
     */
    public LocalDate extractDate(Element eventElement) {
        String dateStr = extractDateString(eventElement);
        if (dateStr.isEmpty()) {
            return null;
        }
        return dateParser.parse(dateStr);
    }

    /**
     * Extracts the raw date string from a news item element.
     *
     * @param eventElement the news item element
     * @return the date string, or empty string if not found
     */
    public String extractDateString(Element eventElement) {
        // Strategy 1: time > span.label (most common pattern)
        Elements timeLabels = eventElement.select(TIME_LABEL);
        if (!timeLabels.isEmpty()) {
            return requireNonNull(timeLabels.first()).text().trim();
        }

        // Strategy 2: any time element text content
        Elements timeElements = eventElement.select(TIME);
        if (!timeElements.isEmpty()) {
            Element timeEl = requireNonNull(timeElements.first());
            String text = timeEl.text().trim();
            if (!text.isEmpty()) {
                return text;
            }
            // Strategy 3: datetime attribute (fallback when text is empty)
            if (timeEl.hasAttr(DATETIME_ATTR)) {
                return timeEl.attr(DATETIME_ATTR);
            }
            return text; // empty string
        }

        LOG.warn("Could not extract date from element");
        return EMPTY;
    }
}
