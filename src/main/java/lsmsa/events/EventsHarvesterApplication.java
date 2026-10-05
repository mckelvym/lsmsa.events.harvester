package lsmsa.events;

import java.util.List;
import java.util.Set;
import lsmsa.events.config.ScraperConfiguration;
import lsmsa.events.config.impl.ScraperConfigurationImpl;
import lsmsa.events.domain.EventItem;
import lsmsa.events.feed.RssFeedManager;
import lsmsa.events.feed.RssFeedManagerImpl;
import lsmsa.events.parser.EventParser;
import lsmsa.events.parser.impl.EventParserImpl;
import lsmsa.events.scraper.EventScraper;
import lsmsa.events.scraper.impl.EventScraperImpl;
import lsmsa.events.webdriver.ChromeDriverManager;
import lsmsa.events.webdriver.PageLoader;
import lsmsa.events.webdriver.WebDriverManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Main application for scraping and generating RSS feed.
 */
public final class EventsHarvesterApplication {

    private static final String DEFAULT_OUTPUT_FILE = "events.xml";
    private static final Logger LOG = LoggerFactory.getLogger(EventsHarvesterApplication.class);

    private EventsHarvesterApplication() {
        // utility
    }

    private static void configureLogging() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    /**
     * Main entry point for the application.
     *
     * @param args command line arguments (optional: output file path)
     */
    public static void main(String[] args) {
        configureLogging();

        String outputFile = args.length > 0 ? args[0] : DEFAULT_OUTPUT_FILE;

        LOG.info("Starting LSMSA News Harvester");
        LOG.info("Output file: {}", outputFile);

        try {
            new EventsHarvesterApplication().run(outputFile);
            LOG.info("Harvesting completed successfully");
        } catch (Exception e) {
            LOG.error("Application failed with error", e);
            System.exit(1);
        }
    }

    /**
     * Runs the complete harvesting workflow.
     *
     * @param outputFile path to the output RSS file
     * @throws Exception if any step fails
     */
    public void run(String outputFile)
        throws Exception {
        // Initialize configuration and dependencies
        ScraperConfiguration config = new ScraperConfigurationImpl();
        RssFeedManager feedManager = new RssFeedManagerImpl(config);

        // Phase 1: Load existing feed
        LOG.info("Loading existing feed");
        Set<String> existingGuids = feedManager.loadExistingGuids(outputFile);
        LOG.info("Found {} existing events", existingGuids.size());

        // Phase 2: Scrape news
        LOG.info("Starting news scraping");
        List<EventItem> newItems = scrapeNews(config, existingGuids);
        LOG.info("Scraped {} new news items", newItems.size());

        // Phase 3: Generate RSS feed
        LOG.info("Generating RSS feed");
        feedManager.generateFeed(
            outputFile,
            newItems,
            outputFile
        );
        LOG.info("RSS feed generated successfully");
    }

    /**
     * Scrapes news items using dependency injection.
     *
     * @param config        the scraper configuration
     * @param existingGuids set of existing GUIDs to avoid duplicates
     * @return list of newly scraped news items
     */
    private List<EventItem> scrapeNews(ScraperConfiguration config, Set<String> existingGuids) {
        // Initialize dependencies with dependency injection
        try (WebDriverManager driverManager = new ChromeDriverManager(config)) {
            EventParser parser = new EventParserImpl(config);
            PageLoader pageLoader = new PageLoader(
                driverManager.getDriver(),
                config.getPageLoadTimeout()
            );
            EventScraper scraper = new EventScraperImpl(config, pageLoader, parser);
            return scraper.scrapeEvents(existingGuids);
        }
    }
}
