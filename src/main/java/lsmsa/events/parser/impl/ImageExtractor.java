package lsmsa.events.parser.impl;

import static lsmsa.events.parser.impl.CssSelectors.IMG;
import static lsmsa.events.parser.impl.HtmlConstants.EMPTY;
import static lsmsa.events.parser.impl.HtmlConstants.SRC_ATTR;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Extracts the image URL from a news item element.
 */
public final class ImageExtractor {

    /**
     * Extracts the image URL from a news item element.
     *
     * @param element the news item element
     * @return the full image URL, or empty string if not found
     */
    public String extractImageUrl(Element element) {
        // Strategy 1: img element
        Elements images = element.select(IMG);
        if (!images.isEmpty()) {
            Element img = images.first();
            if (img == null) {
                return EMPTY;
            }
            String src = img.attr(SRC_ATTR);

            // Make URL absolute if it's relative
            if (src.startsWith("//")) {
                src = "https:" + src;
            } else if (src.startsWith("/")) {
                src = "https://www.lsmsa.edu" + src;
            }

            return src;
        }

        return EMPTY;
    }
}
