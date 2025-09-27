package com.example.demo.ingest.rome;

import com.example.demo.ingest.FeedEntry;
import com.example.demo.ingest.FeedProvider;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class RomeFeedProvider implements FeedProvider {

    // Start with a couple AI-related feeds
    private static final List<String> FEEDS = List.of(
            "https://www.technologyreview.com/feed/",
            "https://www.theverge.com/rss/index.xml"
    );

    @Override
    public List<FeedEntry> fetchRecent() {
        List<FeedEntry> out = new ArrayList<>();
        for (String f : FEEDS) {
            try (XmlReader reader = new XmlReader(new URL(f))) {
                SyndFeed feed = new SyndFeedInput().build(reader);
                String outlet = feed.getTitle();
                for (SyndEntry e : feed.getEntries()) {
                    Instant published = e.getPublishedDate() != null
                            ? e.getPublishedDate().toInstant() : null;
                    out.add(new FeedEntry(e.getTitle(), e.getLink(), published, outlet));
                }
            } catch (Exception ignore) { /* skip bad feeds */ }
        }
        return out;
    }
}
