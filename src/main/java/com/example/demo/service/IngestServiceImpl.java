package com.example.demo.service;

import com.example.demo.data.ArticleRepository;
import com.example.demo.data.RatingRepository;
import com.example.demo.domain.Article;
import com.example.demo.ingest.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class IngestServiceImpl implements IngestService {
    private final FeedProvider feeds;
    private final ContentFetcher fetcher;
    private final ArticleExtractor extractor;
    private final ArticleRepository articles;
    private final RatingService ratingService;
    private final RatingRepository ratings;

    public IngestServiceImpl(
            FeedProvider feeds,
            ContentFetcher fetcher,
            ArticleExtractor extractor,
            ArticleRepository articles,
            RatingService ratingService,
            RatingRepository ratings
    ) {
        this.feeds = feeds;
        this.fetcher = fetcher;
        this.extractor = extractor;
        this.articles = articles;
        this.ratingService = ratingService;
        this.ratings = ratings;
    }

    @Override
    public IngestReport ingest(List<String> urlsOrEmpty) {
        // 1) decide input set: provided URLs OR RSS-derived URLs
        Set<String> urls = new HashSet<>();
        if (urlsOrEmpty != null && !urlsOrEmpty.isEmpty()) {
            urls.addAll(urlsOrEmpty);
        } else {
            feeds.fetchRecent().forEach(e -> urls.add(e.url()));
        }

        int fetched = 0, saved = 0;

        for (String url : urls) {
            try {
                // quick URL hygiene
                if (url == null || !url.startsWith("http")) continue;
                if (articles.findByUrl(url).isPresent()) continue; // de-dupe by URL

                // 2) fetch the page
                FetchResult res = fetcher.fetch(url);
                if (res.status() >= 300 || res.html() == null || res.html().isBlank()) continue;
                fetched++;

                // 3) extract article fields
                ExtractedArticle ea = extractor.extract(url, res.html());
                if (ea.content() == null || ea.content().length() < 400) continue; // skip very short pages

                // 4) save Article
                Article a = new Article();
                a.setUrl(url);
                a.setTitle((ea.title() == null || ea.title().isBlank()) ? url : ea.title());
                a.setOutlet(ea.outlet());
                a.setAuthor(ea.author());
                a.setPublishedAt(Instant.now()); // optionally from feed if available
                a.setContent(ea.content());
                a = articles.save(a);
                saved++;

                // 5) immediately classify (skip if already rated)
                if (!ratings.existsByArticle_Id(a.getId())) {
                    ratingService.classifyAndPersist(a);
                }
            } catch (Exception ignore) { /* skip bad URL, keep loop going */ }
        }
        return new IngestReport(urls.size(), fetched, saved);
    }
}
