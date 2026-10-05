package lsmsa.events.feed;

import static java.util.Objects.requireNonNull;
import static lsmsa.events.feed.RssElementNames.CHANNEL;
import static lsmsa.events.feed.RssElementNames.DESCRIPTION;
import static lsmsa.events.feed.RssElementNames.ENCLOSURE;
import static lsmsa.events.feed.RssElementNames.ENCODING_UTF8;
import static lsmsa.events.feed.RssElementNames.EVENT_NAMESPACE_URI;
import static lsmsa.events.feed.RssElementNames.EV_ENDDATE;
import static lsmsa.events.feed.RssElementNames.EV_STARTDATE;
import static lsmsa.events.feed.RssElementNames.GUID;
import static lsmsa.events.feed.RssElementNames.IMAGE_JPEG_TYPE;
import static lsmsa.events.feed.RssElementNames.INDENT_AMOUNT;
import static lsmsa.events.feed.RssElementNames.IS_PERMALINK_ATTR;
import static lsmsa.events.feed.RssElementNames.ITEM;
import static lsmsa.events.feed.RssElementNames.LANGUAGE;
import static lsmsa.events.feed.RssElementNames.LANGUAGE_VALUE;
import static lsmsa.events.feed.RssElementNames.LAST_BUILD_DATE;
import static lsmsa.events.feed.RssElementNames.LINK;
import static lsmsa.events.feed.RssElementNames.PUB_DATE;
import static lsmsa.events.feed.RssElementNames.RSS;
import static lsmsa.events.feed.RssElementNames.RSS_VERSION;
import static lsmsa.events.feed.RssElementNames.TITLE;
import static lsmsa.events.feed.RssElementNames.TRUE_VALUE;
import static lsmsa.events.feed.RssElementNames.TYPE_ATTR;
import static lsmsa.events.feed.RssElementNames.URL_ATTR;
import static lsmsa.events.feed.RssElementNames.VERSION_ATTR;
import static lsmsa.events.feed.RssElementNames.XMLNS_EV_ATTR;
import static lsmsa.events.feed.RssElementNames.XSLT_INDENT_PROPERTY;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lsmsa.events.config.ScraperConfiguration;
import lsmsa.events.domain.EventItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implementation of RSS feed management.
 * Handles loading existing feeds and generating new ones with security protections.
 */
public class RssFeedManagerImpl implements RssFeedManager {

    // Anything outside XML 1.0's Char production: #x9 | #xA | #xD | [#x20-#xD7FF] |
    // [#xE000-#xFFFD] | [#x10000-#x10FFFF]
    private static final Pattern INVALID_XML_CHARACTERS = Pattern.compile(
        "[^\\x09\\x0A\\x0D\\x{20}-\\x{D7FF}\\x{E000}-\\x{FFFD}\\x{10000}-\\x{10FFFF}]");
    private static final Logger LOG = LoggerFactory.getLogger(RssFeedManagerImpl.class);
    private final ScraperConfiguration config;
    private final EventFilter eventFilter;
    private final XmlSecurityConfigurer securityConfigurer;

    /**
     * Creates a new RssFeedManagerImpl.
     *
     * @param config the scraper configuration
     * @throws NullPointerException if config is null
     */
    public RssFeedManagerImpl(final ScraperConfiguration config) {
        this.config = requireNonNull(config, "config must not be null");
        this.securityConfigurer = new XmlSecurityConfigurer();
        this.eventFilter = new EventFilter(config);
    }

    /**
     * Adds channel metadata (title, description, link).
     *
     * @param doc     the XML document
     * @param channel the channel element
     */
    private void addChannelMetadata(Document doc, Element channel) {
        createElement(doc, channel, TITLE, config.getFeedTitle());
        createElement(doc, channel, LINK, config.getFeedLink());
        createElement(doc, channel, DESCRIPTION, config.getFeedDescription());
        createElement(doc, channel, LANGUAGE, LANGUAGE_VALUE);
        createElement(doc, channel, LAST_BUILD_DATE,
            ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
    }

    /**
     * Adds a description element wrapped in CDATA, omitting it when empty.
     *
     * @param doc         the XML document
     * @param item        the item element to add to
     * @param description the description HTML or text
     */
    private void addDescriptionElement(Document doc, Element item,
                                       String description) {
        if (description.isEmpty()) {
            return;
        }
        Element element = doc.createElement(DESCRIPTION);
        element.appendChild(doc.createCDATASection(description));
        item.appendChild(element);
    }

    /**
     * Adds the machine-readable event dates (RSS Event module) used for retention.
     *
     * @param doc   the XML document
     * @param item  the item element to add to
     * @param event the event whose dates to add
     */
    private void addEventDateElements(Document doc, Element item,
                                      EventItem event) {
        Element startDate = doc.createElement(EV_STARTDATE);
        startDate.setTextContent(event.eventDateStart().toString());
        item.appendChild(startDate);
        if (event.eventDateEnd() != null) {
            Element endDate = doc.createElement(EV_ENDDATE);
            endDate.setTextContent(event.eventDateEnd().toString());
            item.appendChild(endDate);
        }
    }

    /**
     * Adds a news item to the channel as an RSS item element.
     *
     * @param doc     the XML document
     * @param channel the channel element
     * @param event   the news item to add
     */
    private void addEventItem(Document doc, Element channel, EventItem event) {
        Element item = doc.createElement(ITEM);

        createElement(doc, item, TITLE, "%s (%s)".formatted(event.title(), event.eventDateStart()));
        createElement(doc, item, LINK, event.link());
        addDescriptionElement(doc, item, event.sanitizedDescription());
        addGuidElement(doc, item, event);
        addEventDateElements(doc, item, event);
        createElement(doc, item, PUB_DATE,
            ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));

        if (event.hasImage()) {
            Element enclosure = doc.createElement(ENCLOSURE);
            enclosure.setAttribute(URL_ATTR, event.imageUrl());
            enclosure.setAttribute(TYPE_ATTR, IMAGE_JPEG_TYPE);
            item.appendChild(enclosure);
        }

        channel.appendChild(item);
    }

    /**
     * Adds the item GUID, which is always the event URL and therefore a permalink.
     *
     * @param doc   the XML document
     * @param item  the item element to add to
     * @param event the event whose GUID to add
     */
    private void addGuidElement(Document doc, Element item, EventItem event) {
        Element guid = doc.createElement(GUID);
        guid.setAttribute(IS_PERMALINK_ATTR, TRUE_VALUE);
        guid.setTextContent(event.guid());
        item.appendChild(guid);
    }

    /**
     * Helper method to create and append a text element.
     *
     * @param doc     the XML document
     * @param parent  the parent element
     * @param tagName the tag name
     * @param content the text content
     */
    private void createElement(Document doc, Element parent, String tagName, String content) {
        Element element = doc.createElement(tagName);
        element.setTextContent(content);
        parent.appendChild(element);
    }

    @Override
    public void generateFeed(String feedFilePath, List<EventItem> newEvents,
                             String existingFeedPath)
        throws Exception {
        requireNonNull(feedFilePath, "feedFilePath must not be null");
        requireNonNull(newEvents, "newItems must not be null");
        requireNonNull(existingFeedPath, "existingFeedPath must not be null");

        LOG.info("Generating RSS feed at: {}", feedFilePath);

        DocumentBuilderFactory factory = securityConfigurer
            .createSecureDocumentBuilderFactory();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        // Create RSS structure
        Element rss = doc.createElement(RSS);
        rss.setAttribute(VERSION_ATTR, RSS_VERSION);
        rss.setAttribute(XMLNS_EV_ATTR, EVENT_NAMESPACE_URI);
        doc.appendChild(rss);

        Element channel = doc.createElement(CHANNEL);
        rss.appendChild(channel);

        addChannelMetadata(doc, channel);

        // Add new events (sorted by eventDateStart descending), skipping any past retention
        List<EventItem> sortedEvents = new ArrayList<>(newEvents);
        sortedEvents.sort(Comparator.comparing(EventItem::eventDateStart).reversed());

        for (EventItem event : sortedEvents) {
            if (eventFilter.shouldKeep(event)) {
                addEventItem(doc, channel, event);
            }
        }

        // Import existing items if feed exists
        importExistingEvents(doc, channel, new File(existingFeedPath));

        // Write to file
        writeXmlToFile(doc, feedFilePath);
        LOG.info("RSS feed generated successfully with {} items",
            channel.getElementsByTagName(ITEM).getLength());
    }

    /**
     * Imports items from the existing feed, dropping those past the retention period.
     *
     * <p>Errors are logged rather than thrown so a scheduled run still publishes new events.
     *
     * @param doc              the new feed document
     * @param channel          the channel to append items to
     * @param existingFeedFile the existing feed file (may not exist)
     */
    private void importExistingEvents(Document doc, Element channel,
                                      File existingFeedFile) {
        if (!existingFeedFile.exists()) {
            return;
        }
        try {
            DocumentBuilder builder =
                securityConfigurer.createSecureDocumentBuilderFactory().newDocumentBuilder();
            NodeList items = builder.parse(existingFeedFile).getElementsByTagName(ITEM);
            int imported = 0;
            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                if (eventFilter.shouldKeep(item)) {
                    Node importedNode = doc.importNode(item, true);
                    removeWhitespaceNodes(importedNode);
                    channel.appendChild(importedNode);
                    imported++;
                }
            }
            LOG.info("Imported {} existing events, dropped {} past retention",
                imported, items.getLength() - imported);
        } catch (Exception e) {
            LOG.error("Failed to import existing events from {}: {}",
                existingFeedFile, e.getMessage(), e);
        }
    }

    @Override
    public Set<String> loadExistingGuids(String feedFilePath)
        throws Exception {
        requireNonNull(feedFilePath, "feedFilePath must not be null");
        Set<String> guids = new HashSet<>();
        File feedFile = new File(feedFilePath);

        if (!feedFile.exists()) {
            LOG.info("No existing feed file found at: {}", feedFilePath);
            return guids;
        }

        LOG.info("Loading existing GUIDs from: {}", feedFilePath);

        DocumentBuilderFactory factory = securityConfigurer
            .createSecureDocumentBuilderFactory();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(feedFile);

        NodeList guidNodes = doc.getElementsByTagName(GUID);
        for (int i = 0; i < guidNodes.getLength(); i++) {
            String guid = guidNodes.item(i).getTextContent();
            guids.add(guid);
        }

        LOG.info("Loaded {} existing GUIDs", guids.size());
        return guids;
    }

    /**
     * Removes characters that are not allowed in XML 1.0 from all text, CDATA and attributes.
     *
     * <p>Scraped text can contain control characters (e.g. U+0002). The serializer writes them
     * as character references such as {@code &#2;}, which no XML parser accepts, so the next
     * run could not read the feed.
     *
     * @param root the root node to clean
     */
    private void removeInvalidXmlCharacters(final Node root) {
        final Deque<Node> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            final Node current = stack.pop();
            final short type = current.getNodeType();
            if (type == Node.TEXT_NODE || type == Node.CDATA_SECTION_NODE) {
                stripInvalidXmlCharacters(current);
            }
            final NamedNodeMap attributes = current.getAttributes();
            if (attributes != null) {
                for (int i = 0; i < attributes.getLength(); i++) {
                    stripInvalidXmlCharacters(attributes.item(i));
                }
            }
            final NodeList children = current.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                stack.push(children.item(i));
            }
        }
    }

    /**
     * Removes whitespace-only text nodes from a DOM tree.
     *
     * <p>This is necessary to ensure proper indentation when writing XML
     * and prevents whitespace accumulation across multiple runs.
     *
     * @param node The root node to clean
     */
    private void removeWhitespaceNodes(final Node node) {
        final Deque<Node> stack = new ArrayDeque<>();
        stack.push(node);

        while (!stack.isEmpty()) {
            final Node current = stack.pop();
            final NodeList children = current.getChildNodes();

            for (int i = children.getLength() - 1; i >= 0; i--) {
                final Node child = children.item(i);
                if (child.getNodeType() == Node.TEXT_NODE) {
                    if (child.getTextContent().trim().isEmpty()) {
                        current.removeChild(child);
                    }
                } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                    stack.push(child);
                }
            }
        }
    }

    private void stripInvalidXmlCharacters(final Node node) {
        final String value = node.getNodeValue();
        final String cleaned = INVALID_XML_CHARACTERS.matcher(value).replaceAll("");
        if (!cleaned.equals(value)) {
            node.setNodeValue(cleaned);
        }
    }

    /**
     * Writes an XML document to a file with formatting.
     *
     * @param doc          the XML document
     * @param feedFilePath path to write the file
     * @throws TransformerException if XML cannot be written
     * @throws IOException          if file cannot be written
     */
    private void writeXmlToFile(Document doc, String feedFilePath)
        throws TransformerException, IOException {
        removeInvalidXmlCharacters(doc);
        TransformerFactory transformerFactory = securityConfigurer
            .createSecureTransformerFactory();
        Transformer transformer = transformerFactory.newTransformer();

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, ENCODING_UTF8);
        transformer.setOutputProperty(XSLT_INDENT_PROPERTY, INDENT_AMOUNT);

        DOMSource source = new DOMSource(doc);
        try (FileOutputStream output = new FileOutputStream(feedFilePath)) {
            StreamResult result = new StreamResult(output);
            transformer.transform(source, result);
        }

        LOG.info("XML written to: {}", feedFilePath);
    }
}
