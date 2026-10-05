package lsmsa.events.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for DateExtractor.
 * Tests multi-strategy date extraction from time elements.
 */
class DateExtractorTest {

    private DateExtractor extractor;

    @Test
    void extractDateString_withAllStrategies_prefersTimeLabelSpan() {
        String html = """
            <div>
                <time datetime="2025-12-15">
                    <span class="label">December 15, 2025</span>
                    Generic time text
                </time>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractDateString(element);

        assertThat(result).isEqualTo("December 15, 2025");
    }

    // Tests for time > span.label strategy (most common)

    @Test
    void extractDateString_withBlankTimeElement_returnsEmptyString() {
        String html = """
            <div>
                <time>   </time>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractDateString(element);

        assertThat(result).isEmpty();
    }

    @Test
    void extractDateString_withBlankTimeLabelSpan_returnsEmptyString() {
        // The extractor finds the blank label span and returns it trimmed
        String html = """
            <div>
                <time>
                    <span class="label">   </span>
                    February 10, 2026
                </time>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractDateString(element);

        assertThat(result).isEmpty();
    }

    @Test
    void extractDateString_withComplexNestedStructure_findsCorrectly() {
        String html = """
            <div class="news-item">
                <div class="date-container">
                    <time class="published">
                        <span class="label">December 15, 2025</span>
                        <span class="time">10:00 AM</span>
                    </time>
                </div>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractDateString(element);

        assertThat(result).isEqualTo("December 15, 2025");
    }

    // Tests for time element strategy (fallback)

    @Test
    void extractDateString_withDateFormats_extractsCorrectly() {
        String[] dateFormats = {
            "Dec 15, 2025",
            "December 15, 2025",
            "12/15/2025",
            "2025-12-15",
            "15 December 2025"
        };

        for (String dateFormat : dateFormats) {
            String html = String.format("""
                <div>
                    <time>
                        <span class="label">%s</span>
                    </time>
                </div>
                """, dateFormat);
            Element element = Jsoup.parse(html).body();

            String result = extractor.extractDateString(element);

            assertThat(result).as("Date format: " + dateFormat)
                .isEqualTo(dateFormat);
        }
    }

    @Test
    void extractDateString_withDatetimeAttributeEmptyText_usesAttribute() {
        String html = """
            <div>
                <time datetime="2025-12-15"></time>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractDateString(element);

        // Falls back to datetime attribute when text is empty
        assertThat(result).isEqualTo("2025-12-15");
    }

    // Tests for datetime attribute strategy (third fallback)

    @Test
    void extractDateString_withDatetimeAttribute_returnsTextContent() {
        String html = """
            <div>
                <time datetime="2025-12-15">Event Date</time>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractDateString(element);

        assertThat(result).isEqualTo("Event Date");
    }

    @Test
    void extractDateString_withEmptyTimeElement_returnsEmptyString() {
        String html = """
            <div>
                <time></time>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractDateString(element);

        assertThat(result).isEmpty();
    }

    @Test
    void extractDateString_withEmptyTimeLabelSpan_returnsEmptyString() {
        // The extractor finds the empty label span and returns it
        // It doesn't fall back to time text
        String html = """
            <div>
                <time>
                    <span class="label"></span>
                    February 10, 2026
                </time>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractDateString(element);

        assertThat(result).isEmpty();
    }

    // Tests for strategy priority

    @Test
    void extractDateString_withMultipleTimeElements_usesFirst() {
        String html = """
            <div>
                <time>December 15, 2025</time>
                <time>January 20, 2026</time>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractDateString(element);

        assertThat(result).isEqualTo("December 15, 2025");
    }

    @Test
    void extractDateString_withMultipleTimeLabelSpans_usesFirst() {
        String html = """
            <div>
                <time>
                    <span class="label">December 15, 2025</span>
                </time>
                <time>
                    <span class="label">January 20, 2026</span>
                </time>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractDateString(element);

        assertThat(result).isEqualTo("December 15, 2025");
    }

    // Tests for edge cases and failures

    @Test
    void extractDateString_withNestedTimeElement_findsCorrectly() {
        String html = """
            <div>
                <div class="outer">
                    <time>
                        <span class="label">March 10, 2026</span>
                    </time>
                </div>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractDateString(element);

        assertThat(result).isEqualTo("March 10, 2026");
    }

    @Test
    void extractDateString_withNoTimeElement_returnsEmptyString() {
        String html = "<div><p>No time element</p></div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractDateString(element);

        assertThat(result).isEmpty();
    }

    @Test
    void extractDateString_withOtherSpansInTime_selectsLabelSpan() {
        String html = """
            <div>
                <time>
                    <span class="other">Other info</span>
                    <span class="label">December 15, 2025</span>
                </time>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractDateString(element);

        assertThat(result).isEqualTo("December 15, 2025");
    }

    @Test
    void extractDateString_withTimeAndDatetime_prefersTimeText() {
        String html = """
            <div>
                <time datetime="2025-12-15">January 20, 2026</time>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractDateString(element);

        assertThat(result).isEqualTo("January 20, 2026");
    }

    @Test
    void extractDateString_withTimeElementOnly_returnsDate() {
        String html = """
            <div>
                <time>March 10, 2026</time>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractDateString(element);

        assertThat(result).isEqualTo("March 10, 2026");
    }

    @Test
    void extractDateString_withTimeElementWhitespace_trimsCorrectly() {
        String html = """
            <div>
                <time>  April 5, 2026  </time>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractDateString(element);

        assertThat(result).isEqualTo("April 5, 2026");
    }

    @Test
    void extractDateString_withTimeLabelSpanWhitespace_trimsCorrectly() {
        String html = """
            <div>
                <time>
                    <span class="label">  Jan 20, 2026  </span>
                </time>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractDateString(element);

        assertThat(result).isEqualTo("Jan 20, 2026");
    }

    @Test
    void extractDateString_withTimeLabelSpan_returnsDate() {
        String html = """
            <div>
                <time>
                    <span class="label">December 15, 2025</span>
                </time>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractDateString(element);

        assertThat(result).isEqualTo("December 15, 2025");
    }

    @Test
    void extractDate_withDatetimeAttributeEmptyText_parsesAttribute() {
        String html = """
            <div>
                <time datetime="2025-12-15"></time>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractDate(element);

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @Test
    void extractDate_withNoTimeElement_returnsNull() {
        String html = "<div><p>No time element</p></div>";
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractDate(element);

        assertThat(result).isNull();
    }

    @Test
    void extractDate_withTimeLabelSpan_returnsLocalDate() {
        String html = """
            <div>
                <time>
                    <span class="label">December 15, 2025</span>
                </time>
            </div>
            """;
        Element element = Jsoup.parse(html).body();

        LocalDate result = extractor.extractDate(element);

        assertThat(result).isEqualTo(LocalDate.of(2025, 12, 15));
    }

    @BeforeEach
    void setUp() {
        extractor = new DateExtractor();
    }
}
