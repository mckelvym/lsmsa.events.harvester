package lsmsa.events.parser.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TitleExtractorTest {

    private TitleExtractor extractor;

    @Test
    void extractLink_withRelativeHref_returnsCanonicalUrlWithOnlyPk() {
        String html = "<div><h4 class=\"h4-style\">"
            + "<a href=\"/page/news-detail?pk=1687475&amp;fromId=331760\">News</a></h4></div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractLink(element, "https://www.lsmsa.edu");

        assertThat(result).isEqualTo("https://www.lsmsa.edu/page/news-detail?pk=1687475");
    }

    @Test
    void extractLink_withDifferentFromIds_returnsSameUrl() {
        String fromHome = "<div><h4 class=\"h4-style\"><a href=\"https://www.lsmsa.edu/page/"
            + "news-detail?pk=1687475&amp;nc=20751&amp;fromId=331753\">News</a></h4></div>";
        String fromNews = "<div><h4 class=\"h4-style\"><a href=\"/page/"
            + "news-detail?pk=1687475&amp;fromId=331760\">News</a></h4></div>";

        assertThat(extractor.extractLink(Jsoup.parse(fromHome).body(), "https://www.lsmsa.edu"))
            .isEqualTo(extractor.extractLink(Jsoup.parse(fromNews).body(),
                "https://www.lsmsa.edu"));
    }

    @Test
    void extractLink_withoutPk_returnsUrlUnchanged() {
        String html = "<div><h4 class=\"h4-style\"><a href=\"/page/other\">Other</a></h4></div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractLink(element, "https://www.lsmsa.edu");

        assertThat(result).isEqualTo("https://www.lsmsa.edu/page/other");
    }

    @Test
    void extractTitle_withBlankH4StyleLink_returnsEmptyString() {
        String html = "<div>"
            + "<h4 class=\"h4-style\"><a>   </a></h4>"
            + "<h4>Other Title</h4>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractTitle(element);

        assertThat(result).isEmpty();
    }

    @Test
    void extractTitle_withBlankH4_returnsEmptyString() {
        String html = "<div>"
            + "<h4>   </h4>"
            + "<h3>H3 Title</h3>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractTitle(element);

        assertThat(result).isEmpty();
    }

    @Test
    void extractTitle_withBlankHeadings_returnsEmptyString() {
        String html = "<div>"
            + "<h4 class=\"h4-style\"><a>   </a></h4>"
            + "<h4>   </h4>"
            + "<h1></h1>"
            + "<h2>  </h2>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractTitle(element);

        assertThat(result).isEmpty();
    }

    @Test
    void extractTitle_withH1_returnsTitle() {
        String html = "<div><h1>Heading</h1></div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractTitle(element);

        assertThat(result).isEqualTo("Heading");
    }

    @Test
    void extractTitle_withH2_returnsTitle() {
        String html = "<div><h2>Article</h2></div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractTitle(element);

        assertThat(result).isEqualTo("Article");
    }

    @Test
    void extractTitle_withH3_returnsTitle() {
        String html = "<div><h3>Event</h3></div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractTitle(element);

        assertThat(result).isEqualTo("Event");
    }

    @Test
    void extractTitle_withH4NoClass_returnsTitle() {
        String html = "<div><h4>News Title</h4></div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractTitle(element);

        assertThat(result).isEqualTo("News Title");
    }

    @Test
    void extractTitle_withH4Priority_ignoresH3() {
        String html = "<div>"
            + "<h4>H4 Title</h4>"
            + "<h3>H3 Title</h3>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractTitle(element);

        assertThat(result).isEqualTo("H4 Title");
    }

    @Test
    void extractTitle_withH4StyleLinkWhitespace_returnsTrimmedTitle() {
        String html = "<div><h4 class=\"h4-style\"><a>  Event Title  </a></h4></div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractTitle(element);

        assertThat(result).isEqualTo("Event Title");
    }

    @Test
    void extractTitle_withH4StyleLink_returnsTitle() {
        String html = "<div><h4 class=\"h4-style\"><a>News Article</a></h4></div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractTitle(element);

        assertThat(result).isEqualTo("News Article");
    }

    @Test
    void extractTitle_withH4StylePriority_ignoresH3() {
        String html = "<div>"
            + "<h4 class=\"h4-style\"><a>H4 Title</a></h4>"
            + "<h3>H3 Title</h3>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractTitle(element);

        assertThat(result).isEqualTo("H4 Title");
    }

    @Test
    void extractTitle_withH5_returnsTitle() {
        String html = "<div><h5>Small Heading</h5></div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractTitle(element);

        assertThat(result).isEqualTo("Small Heading");
    }

    @Test
    void extractTitle_withH6_returnsTitle() {
        String html = "<div><h6>Tiny Heading</h6></div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractTitle(element);

        assertThat(result).isEqualTo("Tiny Heading");
    }

    @Test
    void extractTitle_withMultipleHeadings_returnsFirstOne() {
        String html = "<div>"
            + "<h3>First Title</h3>"
            + "<h3>Second Title</h3>"
            + "</div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractTitle(element);

        assertThat(result).isEqualTo("First Title");
    }

    @Test
    void extractTitle_withNestedElements_returnsText() {
        String html = "<div><h4 class=\"h4-style\"><a>News <span>Article</span></a></h4></div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractTitle(element);

        assertThat(result).isEqualTo("News Article");
    }

    @Test
    void extractTitle_withNoHeadings_returnsEmptyString() {
        String html = "<div><p>Some content</p><span>More content</span></div>";
        Element element = Jsoup.parse(html).body();

        String result = extractor.extractTitle(element);

        assertThat(result).isEmpty();
    }

    @BeforeEach
    void setUp() {
        extractor = new TitleExtractor();
    }
}
