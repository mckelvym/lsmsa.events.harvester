package lsmsa.events.parser.impl;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.Optional;
import lsmsa.events.config.ScraperConfiguration;
import lsmsa.events.domain.EventItem;
import lsmsa.events.parser.EventParser;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses and extracts event fields.
 */
public final class EventParserImpl implements EventParser {

    private static final Logger LOG = LoggerFactory.getLogger(EventParserImpl.class);
    private static final String MISSING = "MISSING";

    private final ScraperConfiguration config;
    private final DateExtractor dateExtractor;
    private final DescriptionExtractor descriptionExtractor;
    private final ImageExtractor imageExtractor;
    private final TitleExtractor titleExtractor;

    /**
     * Constructs an EventParserImpl with all required extractors.
     *
     * @param config the scraper configuration
     */
    public EventParserImpl(final ScraperConfiguration config) {
        this.config = requireNonNull(config, "config must not be null");
        this.titleExtractor = new TitleExtractor();
        this.dateExtractor = new DateExtractor();
        this.descriptionExtractor = new DescriptionExtractor();
        this.imageExtractor = new ImageExtractor();
    }

    @Override
    public Optional<EventItem> parseEvent(final Element eventElement) {
        requireNonNull(eventElement, "eventElement must not be null");
        String title = titleExtractor.extractTitle(eventElement);
        String link = titleExtractor.extractLink(eventElement, config.getEventLinkBaseUrl());
        String id = titleExtractor.extractIdFromLink(link);
        LocalDate eventDateStart = dateExtractor.extractDate(eventElement);
        String description = descriptionExtractor.extract(eventElement);
        String imageUrl = imageExtractor.extractImageUrl(eventElement);

        // Validate required fields
        if (id.isEmpty() || title.isEmpty() || link.isEmpty() || eventDateStart == null) {
            LOG.warn("Skipping item due to missing required fields: "
                    + "id={}, title={}, link={}, date={}",
                id.isEmpty() ? MISSING : id,
                title.isEmpty() ? MISSING
                    : title.substring(0, Math.min(30, title.length())),
                link.isEmpty() ? MISSING : link,
                eventDateStart == null ? MISSING : eventDateStart);
            return Optional.empty();
        }

        return Optional.of(new EventItem(id, title, link, description, eventDateStart, null,
            imageUrl, null));
    }
}
