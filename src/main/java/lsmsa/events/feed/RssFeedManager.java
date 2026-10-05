package lsmsa.events.feed;

import java.util.List;
import java.util.Set;
import lsmsa.events.domain.EventItem;

/**
 * Interface for managing RSS feed operations.
 */
public interface RssFeedManager {

    /**
     * Generates an RSS feed with new and existing items.
     *
     * @param feedFilePath     path to write the feed file
     * @param newItems         list of new news items to add
     * @param existingFeedPath path to existing feed (to preserve old items)
     * @throws Exception if generation fails
     */
    void generateFeed(String feedFilePath, List<EventItem> newItems,
                      String existingFeedPath)
        throws Exception;

    /**
     * Loads existing GUIDs from an RSS feed file.
     *
     * @param feedFilePath path to the existing feed file
     * @return set of existing GUIDs (news item URLs)
     * @throws Exception if loading fails
     */
    Set<String> loadExistingGuids(String feedFilePath)
        throws Exception;
}
