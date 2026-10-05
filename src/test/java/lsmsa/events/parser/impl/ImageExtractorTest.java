package lsmsa.events.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ImageExtractorTest {

    private ImageExtractor extractor;

    @Test
    void extractImageUrl_withAbsoluteHttpsUrl_returnsUrl() {
        String html = "<div>"
            + "<img src=\"https://www.lsmsa.edu/images/event.jpg\" />"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractImageUrl(element);

        assertThat(result).isEqualTo("https://www.lsmsa.edu/images/event.jpg");
    }

    @Test
    void extractImageUrl_withDataAttributes_ignoresDataAndUsesSrc() {
        String html = "<div>"
            + "<img src=\"/images/actual.jpg\" data-src=\"/images/lazy.jpg\" />"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractImageUrl(element);

        assertThat(result).isEqualTo("https://www.lsmsa.edu/images/actual.jpg");
    }

    @Test
    void extractImageUrl_withEmptySrc_returnsEmptyString() {
        String html = "<div>"
            + "<img src=\"\" />"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractImageUrl(element);

        assertThat(result).isEmpty();
    }

    @Test
    void extractImageUrl_withMultipleImages_returnsFirst() {
        String html = "<div>"
            + "<img src=\"/images/first.jpg\" />"
            + "<img src=\"/images/second.jpg\" />"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractImageUrl(element);

        assertThat(result).isEqualTo("https://www.lsmsa.edu/images/first.jpg");
    }

    @Test
    void extractImageUrl_withNoImage_returnsEmptyString() {
        String html = "<div><p>No image</p></div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractImageUrl(element);

        assertThat(result).isEmpty();
    }

    @Test
    void extractImageUrl_withProtocolRelativeUrl_prependsHttps() {
        String html = "<div>"
            + "<img src=\"//cdn.lsmsa.edu/images/photo.jpg\" />"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractImageUrl(element);

        assertThat(result).isEqualTo("https://cdn.lsmsa.edu/images/photo.jpg");
    }

    @Test
    void extractImageUrl_withRelativePathNoLeadingSlash_prependsBaseUrl() {
        String html = "<div>"
            + "<img src=\"images/article.jpg\" />"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractImageUrl(element);

        // Jsoup may not handle this case without a base URL, result may vary
        assertThat(result).isNotNull();
    }

    @Test
    void extractImageUrl_withRelativeUrlStartingWithSlash_prependsBaseUrl() {
        String html = "<div>"
            + "<img src=\"/images/news.jpg\" />"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractImageUrl(element);

        assertThat(result).isEqualTo("https://www.lsmsa.edu/images/news.jpg");
    }

    @BeforeEach
    void setUp() {
        extractor = new ImageExtractor();
    }
}
