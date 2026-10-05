# LSMSA News Harvester

This Java application scrapes news items from the [Louisiana School for Math, Science, and the Arts](https://www.lsmsa.edu/news--events) and generates an RSS feed. It uses Selenium WebDriver with headless Chrome to handle JavaScript-rendered content and JSoup for HTML parsing. The application produces an incremental RSS feed that appends new news items to an existing feed file while filtering out old entries.

Feed exported to https://github.com/mckelvym/lsmsa.events.rss

## Build and Run

Build the application:

```bash
./gradlew build
```

Build the image:

```bash
source scripts/version.sh && ./gradlew jib -Djib.to.image=$IMAGE:$VERSION
```

Run with default output file (events.xml):

```bash
./gradlew run
```

Run with custom output file:

```bash
./gradlew run -Pargs='my-events.xml'
```

## Docker

The project uses Jib for containerization. Run with Docker:

```bash
./scripts/run.sh
```

This pulls and runs the latest Docker image from GitHub Container Registry.

## How It Works

The application follows a three-phase workflow:

1. Load Existing Feed - Reads the existing RSS file and extracts all GUIDs to avoid duplicates
2. Scrape News - Uses Selenium to load the news page, discovers news items, and parses each item for title, date, description, and image information
3. Generate RSS Feed - Creates a new RSS 2.0 XML document with new news items, imports existing items from the old feed, filters out items older than 7 days, and writes the result to the output file

The scraper uses a custom user agent (Mozilla/5.0 compatible; LSMSANewsBot/1.0) for identification.

## Architecture

The application uses a modular SOLID design with clear separation of concerns:

- Domain layer: EventItem for news data
- Config layer: Site-specific configuration
- WebDriver layer: Chrome automation and page loading
- Scraper layer: News discovery
- Parser layer: Multi-strategy field extraction
- Feed layer: XXE-protected RSS generation

## Configuration

Event retention period: 7 days
Page load timeout: 10 seconds
User agent: Mozilla/5.0 (compatible; LSMSANewsBot/1.0)
Target URL: https://www.lsmsa.edu/news--events

## Output

The generated RSS feed includes:

- News title
- News description
- News link (also used as GUID)
- News image (as enclosure, when available)
- Publication date (RFC 1123 format with GMT timezone)

Dates are converted to RFC 1123 format for RSS compatibility.
