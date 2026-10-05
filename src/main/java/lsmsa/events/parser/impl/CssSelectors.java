package lsmsa.events.parser.impl;

/**
 * Constants for CSS selectors used in HTML parsing.
 *
 * <p>This class centralizes all CSS selector strings used throughout the
 * eventParser implementation to avoid magic strings and improve maintainability.
 */
public final class CssSelectors {

    // EventScraperImpl selectors
    public static final String NEWS_ITEMS = "div.content.newsarchivelist li.group";

    // TitleExtractor selectors
    public static final String H4_STYLE_LINK = "h4.h4-style a";
    public static final String H4 = "h4";

    // DateExtractor selectors
    public static final String TIME_LABEL = "time span.label";
    public static final String TIME = "time";

    // ImageExtractor selectors
    public static final String IMG = "img";

    // DescriptionExtractor selectors
    public static final String BRIEF_DESCRIPTION = "div.brief-description";
    public static final String DESCRIPTION_DIV = "div[class*=description]";
    public static final String PARAGRAPH = "p";

    // Page loading selectors
    public static final String PAGE_LOAD_SELECTOR = "div.content.newsarchivelist";

    private CssSelectors() {
        // Utility class - prevent instantiation
    }
}
