package lsmsa.events.parser.impl;

import static java.util.Objects.requireNonNull;
import static lsmsa.events.parser.impl.CssSelectors.BRIEF_DESCRIPTION;
import static lsmsa.events.parser.impl.CssSelectors.DESCRIPTION_DIV;
import static lsmsa.events.parser.impl.CssSelectors.PARAGRAPH;
import static lsmsa.events.parser.impl.HtmlConstants.EMPTY;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts the description from a news item element.
 */
public final class DescriptionExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(DescriptionExtractor.class);

    /**
     * Extracts the description from a news item element.
     *
     * @param element the news item element
     * @return the description text, or empty string if not found
     */
    public String extract(Element element) {
        requireNonNull(element, "element must not be null");
        // Strategy 1: div.brief-description (most common pattern)
        Elements briefDesc = element.select(BRIEF_DESCRIPTION);
        if (!briefDesc.isEmpty()) {
            return requireNonNull(briefDesc.first()).text().trim();
        }

        // Strategy 2: any div with class containing "description"
        Elements descElements = element.select(DESCRIPTION_DIV);
        if (!descElements.isEmpty()) {
            return requireNonNull(descElements.first()).text().trim();
        }

        // Strategy 3: any paragraph
        Elements paragraphs = element.select(PARAGRAPH);
        if (!paragraphs.isEmpty()) {
            return requireNonNull(paragraphs.first()).text().trim();
        }

        LOG.warn("Could not extract description from element");
        return EMPTY;
    }
}
