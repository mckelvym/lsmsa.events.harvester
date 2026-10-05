package lsmsa.events.webdriver;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads web pages using Selenium and parses them with JSoup.
 * Handles JavaScript-rendered content by waiting for elements to appear.
 */
public record PageLoader(WebDriver driver, Duration timeout) {

    private static final Logger LOG = LoggerFactory.getLogger(PageLoader.class);

    /**
     * Creates a new PageLoader.
     *
     * @param driver  the WebDriver to use
     * @param timeout the page load timeout
     */
    public PageLoader(final WebDriver driver, final Duration timeout) {
        this.driver = requireNonNull(driver);
        this.timeout = requireNonNull(timeout);
    }

    /**
     * Loads a URL and returns a JSoup document after waiting for a specific selector.
     *
     * @param url      the URL to load
     * @param selector CSS selector to wait for before parsing
     * @return JSoup document of the loaded page
     */
    public Document loadPage(String url, String selector) {
        requireNonNull(url, "url must not be null");
        requireNonNull(selector, "selector must not be null");
        LOG.info("Loading page: {}", url);
        driver.get(url);

        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(selector)));

        String pageSource = driver.getPageSource();
        return Jsoup.parse(requireNonNull(pageSource));
    }
}
