package com.example.demo.ingest.http;

import com.example.demo.ingest.ContentFetcher;
import com.example.demo.ingest.FetchResult;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

@Component
public class JsoupHttpFetcher implements ContentFetcher {
    @Override
    public FetchResult fetch(String url) throws Exception {
        Connection.Response resp = Jsoup.connect(url)
                .userAgent("AI-Stance-News/1.0 (+https://localhost)")
                .timeout(8000)
                .followRedirects(true)
                .ignoreContentType(true)
                .execute();
        String mime = resp.contentType() == null ? "" : resp.contentType();
        return new FetchResult(resp.statusCode(), mime, resp.body());
    }
}
