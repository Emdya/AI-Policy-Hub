package com.example.demo.ingest;

import java.time.Instant;

public record FeedEntry(String title, String url, Instant publishedAt, String outlet) {}
