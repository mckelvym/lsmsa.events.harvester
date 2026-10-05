package lsmsa.events.scraper.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lsmsa.events.config.ScraperConfiguration;
import lsmsa.events.config.impl.ScraperConfigurationImpl;
import lsmsa.events.domain.EventItem;
import lsmsa.events.parser.EventParser;
import lsmsa.events.webdriver.PageLoader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventScraperImplTest {

    private static final String LINK_A = "https://www.lsmsa.edu/page/news-detail?pk=1";
    private static final String LINK_B = "https://www.lsmsa.edu/page/news-detail?pk=2";

    private PageLoader pageLoader;
    private EventScraperImpl scraper;

    private static String eventItem(final String href) {
        return "<div class=\"content newsarchivelist\"><ul><li class=\"group in-view\">"
            + "<h4 class=\"h4-style\">"
            + "<a href=\"" + href + "\">Event</a></h4></li></ul></div>";
    }

    private static Optional<EventItem> toEvent(final Element element) {
        final String link = element.selectFirst("a").attr("abs:href");
        return Optional.of(new EventItem(link, "Event", link, null,
            LocalDate.of(2026, 1, 1), null, null, null));
    }

    @BeforeEach
    void setUp() {
        final ScraperConfiguration config = new ScraperConfigurationImpl();
        pageLoader = mock(PageLoader.class);
        final EventParser eventParser = mock(EventParser.class);
        when(eventParser.parseEvent(any(Element.class)))
            .thenAnswer(invocation -> toEvent(invocation.getArgument(0)));
        scraper = new EventScraperImpl(config, pageLoader, eventParser);
    }

    @Test
    void scrapeEventsSkipsExistingGuids() {
        when(pageLoader.loadPage(anyString(), anyString())).thenReturn(Jsoup.parse(
            "<html><body>" + eventItem(LINK_A) + eventItem(LINK_B) + "</body></html>",
            "https://www.lsmsa.edu/news"));

        final List<EventItem> events = scraper.scrapeEvents(Set.of(LINK_A));

        assertThat(events).extracting(EventItem::guid).containsExactly(LINK_B);
    }

    @Test
    void scrapeEventsDeduplicatesRepeatedLinks() {
        when(pageLoader.loadPage(anyString(), anyString())).thenReturn(Jsoup.parse(
            "<html><body>" + eventItem(LINK_A) + eventItem(LINK_A) + "</body></html>",
            "https://www.lsmsa.edu/news"));

        final List<EventItem> events = scraper.scrapeEvents(Set.of());

        assertThat(events).extracting(EventItem::guid).containsExactly(LINK_A);
    }

    @Test
    void scrapeEventsReturnsEmptyListWhenPageLoadFails() {
        when(pageLoader.loadPage(anyString(), anyString()))
            .thenThrow(new IllegalStateException("boom"));

        assertThat(scraper.scrapeEvents(Set.of())).isEmpty();
    }
}
