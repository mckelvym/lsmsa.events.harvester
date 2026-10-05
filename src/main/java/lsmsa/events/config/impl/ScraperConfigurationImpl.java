package lsmsa.events.config.impl;

import static lsmsa.events.parser.impl.CssSelectors.PAGE_LOAD_SELECTOR;

import java.time.Duration;
import lsmsa.events.config.ScraperConfiguration;

/**
 * Configuration implementation for LSMSA news scraping.
 * Provides LSMSA-specific URLs and settings.
 */
public class ScraperConfigurationImpl implements ScraperConfiguration {

    private static final String BASE_URL = "https://www.lsmsa.edu";
    private static final String EVENTS_URL = "https://www.lsmsa.edu/news";
    private static final String FEED_DESCRIPTION =
        "Latest news from Louisiana School for Math, Science, and the Arts";
    private static final String FEED_TITLE = "LSMSA School News";
    private static final Duration PAGE_LOAD_TIMEOUT = Duration.ofSeconds(10);
    private static final int RETENTION_DAYS = 7;
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; LSMSANewsBot/1.0)";

    @Override
    public String getBaseUrl() {
        return BASE_URL;
    }

    @Override
    public String getEventLinkBaseUrl() {
        return getBaseUrl();
    }

    @Override
    public String getEventsUrl() {
        return EVENTS_URL;
    }

    @Override
    public String getFeedDescription() {
        return FEED_DESCRIPTION;
    }

    @Override
    public String getFeedLink() {
        return getEventsUrl();
    }

    @Override
    public String getFeedTitle() {
        return FEED_TITLE;
    }

    @Override
    public String getPageLoadSelector() {
        return PAGE_LOAD_SELECTOR;
    }

    @Override
    public Duration getPageLoadTimeout() {
        return PAGE_LOAD_TIMEOUT;
    }

    @Override
    public int getRetentionDays() {
        return RETENTION_DAYS;
    }

    @Override
    public String getUserAgent() {
        return USER_AGENT;
    }
}
