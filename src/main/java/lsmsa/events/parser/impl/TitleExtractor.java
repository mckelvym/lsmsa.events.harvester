package lsmsa.events.parser.impl;

import static java.util.Objects.requireNonNull;
import static lsmsa.events.parser.impl.CssSelectors.H4;
import static lsmsa.events.parser.impl.CssSelectors.H4_STYLE_LINK;
import static lsmsa.events.parser.impl.HtmlConstants.EMPTY;
import static lsmsa.events.parser.impl.HtmlConstants.HREF_ATTR;
import static lsmsa.events.parser.impl.HtmlConstants.HTTP_PREFIX;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts the title from a news item element.
 * Uses multiple strategies to handle different HTML structures.
 */
public final class TitleExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(TitleExtractor.class);
    private static final String PK = "pk=";

    /**
     * Reduces a news-detail URL to its {@code pk} parameter.
     *
     * <p>The site appends {@code fromId} (and sometimes {@code nc}) parameters that vary by the
     * page the link appears on, which would otherwise change the item GUID.
     *
     * @param url the absolute news-detail URL
     * @return the URL with only the {@code pk} query parameter, or the input if it has none
     */
    private String canonicalizeLink(String url) {
        String id = extractIdFromLink(url);
        int queryStart = url.indexOf('?');
        if (id.isEmpty() || queryStart < 0) {
            return url;
        }
        return url.substring(0, queryStart + 1) + PK + id;
    }

    /**
     * Extracts the news item ID from the detail page URL.
     *
     * @param link the full URL to the detail page
     * @return the news item ID (pk parameter), or empty string if not found
     */
    public String extractIdFromLink(String link) {
        if (link.contains(PK)) {
            int pkStart = link.indexOf(PK) + 3;
            int pkEnd = link.indexOf("&", pkStart);
            return pkEnd > 0 ? link.substring(pkStart, pkEnd) : link.substring(pkStart);
        }

        LOG.warn("Could not extract ID from link: {}", link);
        return EMPTY;
    }

    /**
     * Extracts the link URL from a news item element.
     *
     * @param element the news item element
     * @param baseUrl the base URL for resolving relative links
     * @return the canonical full URL, or empty string if not found
     */
    public String extractLink(Element element, String baseUrl) {
        Elements h4Links = element.select(H4_STYLE_LINK);
        if (!h4Links.isEmpty()) {
            String href = requireNonNull(h4Links.first()).attr(HREF_ATTR);
            return canonicalizeLink(href.startsWith(HTTP_PREFIX) ? href : baseUrl + href);
        }

        LOG.warn("Could not extract link from element");
        return EMPTY;
    }

    /**
     * Extracts the title from a news item element.
     *
     * @param element the news item element
     * @return the title text, or empty string if not found
     */
    public String extractTitle(Element element) {
        // Strategy 1: h4 > a (most common pattern)
        Elements h4Links = element.select(H4_STYLE_LINK);
        if (!h4Links.isEmpty()) {
            return requireNonNull(h4Links.first()).text().trim();
        }

        // Strategy 2: any h4
        Elements h4Elements = element.select(H4);
        if (!h4Elements.isEmpty()) {
            return requireNonNull(h4Elements.first()).text().trim();
        }

        // Strategy 3: any heading
        for (int i = 1; i <= 6; i++) {
            Elements headings = element.select("h" + i);
            if (!headings.isEmpty()) {
                return requireNonNull(headings.first()).text().trim();
            }
        }

        LOG.warn("Could not extract title from element");
        return EMPTY;
    }
}
