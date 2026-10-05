package lsmsa.events.config.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ScraperConfigurationImpl.
 */
class ScraperConfigurationImplTest {

    private ScraperConfigurationImpl config;

    @BeforeEach
    void setUp() {
        config = new ScraperConfigurationImpl();
    }

    @Test
    void testGetBaseUrl() {
        assertThat(config.getBaseUrl())
            .isEqualTo("https://www.lsmsa.edu");
    }

    @Test
    void testGetEventLinkBaseUrl() {
        assertThat(config.getEventLinkBaseUrl())
            .isEqualTo("https://www.lsmsa.edu");
    }

    @Test
    void testGetEventsUrl() {
        assertThat(config.getEventsUrl())
            .isEqualTo("https://www.lsmsa.edu/news");
    }

    @Test
    void testGetFeedDescription() {
        assertThat(config.getFeedDescription())
            .isEqualTo("Latest news from Louisiana School for Math, Science, and the Arts");
    }

    @Test
    void testGetFeedLink() {
        assertThat(config.getFeedLink())
            .isEqualTo("https://www.lsmsa.edu/news");
    }

    @Test
    void testGetFeedTitle() {
        assertThat(config.getFeedTitle())
            .isEqualTo("LSMSA School News");
    }

    @Test
    void testGetPageLoadSelector() {
        assertThat(config.getPageLoadSelector())
            .isEqualTo("div.content.newsarchivelist");
    }

    @Test
    void testGetPageLoadTimeout() {
        assertThat(config.getPageLoadTimeout().toSecondsPart())
            .isEqualTo(10);
    }

    @Test
    void testGetRetentionDays() {
        assertThat(config.getRetentionDays())
            .isEqualTo(7);
    }

    @Test
    void testGetUserAgent() {
        assertThat(config.getUserAgent())
            .isEqualTo("Mozilla/5.0 (compatible; LSMSANewsBot/1.0)");
    }
}
