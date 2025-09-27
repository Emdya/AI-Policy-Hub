package com.example.demo.ingest;

public interface ArticleExtractor {
    ExtractedArticle extract(String url, String html);
}
