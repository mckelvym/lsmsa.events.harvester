package lsmsa.events.config;

import java.time.Duration;

/**
 * Configuration interface for the news scraper.
 * Follows the Strategy pattern to allow different scraper configurations.
 */
public interface ScraperConfiguration {

    /**
     * Gets the base URL for the news page.
     *
     * @return the base URL
     */
    String getBaseUrl();

    /**
     * Gets the base URL for resolving relative event links.
     * This may differ from getBaseUrl() which is the main site URL.
     *
     * @return the base URL for event links
     */
    String getEventLinkBaseUrl();

    /**
     * Gets the events URL to scrape.
     *
     * @return the events URL
     */
    String getEventsUrl();

    /**
     * Gets the RSS feed description.
     *
     * @return the feed description
     */
    String getFeedDescription();

    /**
     * Gets the RSS feed link.
     *
     * @return the feed link URL
     */
    String getFeedLink();

    /**
     * Gets the RSS feed title.
     *
     * @return the feed title
     */
    String getFeedTitle();

    /**
     * Gets the CSS selector to wait for page load.
     *
     * @return CSS selector
     */
    String getPageLoadSelector();

    /**
     * Gets the timeout duration for page loads.
     *
     * @return the timeout duration
     */
    Duration getPageLoadTimeout();

    /**
     * Gets the number of days to keep news items.
     * News items older than this will be filtered out.
     *
     * @return number of days to retain
     */
    int getRetentionDays();

    /**
     * Gets the user agent string for HTTP requests.
     *
     * @return the user agent string
     */
    String getUserAgent();
}
