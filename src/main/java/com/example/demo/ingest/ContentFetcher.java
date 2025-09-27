package com.example.demo.ingest;

public interface ContentFetcher {
    FetchResult fetch(String url) throws Exception;
}
