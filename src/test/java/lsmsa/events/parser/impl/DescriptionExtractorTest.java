package lsmsa.events.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DescriptionExtractorTest {

    private DescriptionExtractor extractor;

    @Test
    void extractDescription_withBrief_returnsText() {
        String html = "<div>"
            + "<div class=\"brief-description\">LSMSA hosts annual science fair</div>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("LSMSA hosts annual science fair");
    }

    @Test
    void extractDescription_withClass_returnsText() {
        String html = "<div>"
            + "<div class=\"event-description\">Full event details here</div>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("Full event details here");
    }

    @Test
    void extractDescription_withEmptyBrief_returnsEmptyString() {
        String html = "<div>"
            + "<div class=\"brief-description\"></div>"
            + "<p>Fallback paragraph</p>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEmpty();
    }

    @Test
    void extractDescription_withMultipleStrategies_prefersBrief() {
        String html = "<div>"
            + "<div class=\"brief-description\">Brief text</div>"
            + "<div class=\"description\">Description text</div>"
            + "<p>Paragraph text</p>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("Brief text");
    }

    @Test
    void extractDescription_withNoBriefOr_fallsBackToParagraph() {
        String html = "<div>"
            + "<p>Paragraph text</p>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("Paragraph text");
    }

    @Test
    void extractDescription_withNoBrief_fallsBackTo() {
        String html = "<div>"
            + "<div class=\"description\">Description text</div>"
            + "<p>Paragraph text</p>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("Description text");
    }

    @Test
    void extract_withMultipleParagraphs_returnsFirst() {
        String html = "<div>"
            + "<p>First paragraph</p>"
            + "<p>Second paragraph</p>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("First paragraph");
    }

    @Test
    void extract_withNoMatchingElements_returnsEmptyString() {
        String html = "<div><span>Some content</span></div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEmpty();
    }

    @Test
    void extract_withParagraph_returnsText() {
        String html = "<div>"
            + "<p>News article content</p>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("News article content");
    }

    @Test
    void extract_withWhitespace_returnsTrimmedText() {
        String html = "<div>"
            + "<div class=\"brief-description\">  Event information  </div>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extract(element);

        assertThat(result).isEqualTo("Event information");
    }

    @BeforeEach
    void setUp() {
        extractor = new DescriptionExtractor();
    }
}
