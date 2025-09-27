package com.example.demo;

import com.example.demo.api.*;
import com.example.demo.data.ArticleRepository;
import com.example.demo.domain.Article;
import com.example.demo.ingest.*;
import com.example.demo.nlp.ClassificationEngine;
import com.example.demo.service.RatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api")
public class AnalyzeController {

    private final ContentFetcher fetcher;
    private final ArticleExtractor extractor;
    private final ClassificationEngine classifier;
    private final ArticleRepository articles;
    private final RatingService ratingService;

    // simple “AI mention” regex (expand as needed)
    private static final Pattern AI_PATTERN = Pattern.compile(
            "\\bai\\b|artificial intelligence|machine learning|gen(?:erative)? ai|large language model|llm|chatgpt|openai|anthropic|stability ai|midjourney",
            Pattern.CASE_INSENSITIVE
    );

    public AnalyzeController(ContentFetcher fetcher,
                             ArticleExtractor extractor,
                             ClassificationEngine classifier,
                             ArticleRepository articles,
                             RatingService ratingService) {
        this.fetcher = fetcher;
        this.extractor = extractor;
        this.classifier = classifier;
        this.articles = articles;
        this.ratingService = ratingService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalyzeResponse> analyze(@RequestBody AnalyzeRequest req) {
        try {
            if (req == null || req.url() == null || !req.url().startsWith("http"))
                return ResponseEntity.badRequest().body(new AnalyzeResponse(false, "Invalid URL", null,null,null,null,null,null));

            // fetch page
            FetchResult res = fetcher.fetch(req.url());
            if (res.status() >= 300 || res.html() == null || res.html().isBlank())
                return ResponseEntity.ok(new AnalyzeResponse(false, "Failed to fetch the page", null,null,null,null,null,null));

            // extract content
            ExtractedArticle ea = extractor.extract(req.url(), res.html());
            String title = (ea.title() == null || ea.title().isBlank()) ? req.url() : ea.title();
            String body  = ea.content() == null ? "" : ea.content();

            // quick guard: detect AI mentions
            String haystack = (title + "\n" + body);
            if (!AI_PATTERN.matcher(haystack).find())
                return ResponseEntity.ok(new AnalyzeResponse(false, "Did not detect any mention of AI in article", title, ea.outlet(), null, null, null, null));

            // length check (avoid junk pages)
            if (body.length() < 400)
                return ResponseEntity.ok(new AnalyzeResponse(false, "Article content is too short to score", title, ea.outlet(), null, null, null, null));

            // classify
            var result = classifier.classify(body);

            // optionally persist as a normal Article + Rating (checkbox in UI)
            if (req.persist()) {
                var existing = articles.findByUrl(req.url()).orElse(null);
                Article a = existing != null ? existing : new Article();
                a.setUrl(req.url());
                a.setTitle(title);
                a.setOutlet(ea.outlet());
                a.setAuthor(ea.author());
                a.setPublishedAt(existing != null ? existing.getPublishedAt() : Instant.now());
                a.setContent(body);
                a = articles.save(a);
                ratingService.classifyAndPersist(a); // uses same pipeline, saves Rating
            }

            return ResponseEntity.ok(
                    new AnalyzeResponse(true, "Scored",
                            title, ea.outlet(),
                            result.stance(), result.score(), result.confidence(), result.evidence()
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.ok(new AnalyzeResponse(false, "Unexpected error: " + e.getMessage(), null,null,null,null,null,null));
        }
    }
}
