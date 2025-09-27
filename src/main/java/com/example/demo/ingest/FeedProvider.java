package com.example.demo.ingest;

import java.util.List;

public interface FeedProvider {
    List<FeedEntry> fetchRecent(); // entries with title + url
}