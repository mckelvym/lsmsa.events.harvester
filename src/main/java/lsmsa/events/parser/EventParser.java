package lsmsa.events.parser;

import java.util.Optional;
import lsmsa.events.domain.EventItem;
import org.jsoup.nodes.Element;

/**
 * Interface for parsing event information from HTML elements.
 */
public interface EventParser {

    /**
     * Parses an event item from an HTML eventElement.
     *
     * @param eventElement the HTML eventElement containing the news item
     * @return Optional containing the parsed EventItem, or empty if event should be skipped
     * @throws RuntimeException if parsing fails due to unexpected error
     */
    Optional<EventItem> parseEvent(Element eventElement);
}
