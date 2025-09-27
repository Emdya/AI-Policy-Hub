package com.example.demo.ingest.extract;

import com.example.demo.ingest.ArticleExtractor;
import com.example.demo.ingest.ExtractedArticle;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class GenericArticleExtractor implements ArticleExtractor {
    @Override
    public ExtractedArticle extract(String url, String html) {
        Document doc = Jsoup.parse(html, url);

        String title = doc.title();
        String outlet = doc.select("meta[property=og:site_name]").attr("content");
        if (outlet == null || outlet.isBlank()) outlet = doc.location();

        String author = doc.select("meta[name=author]").attr("content");
        if (author.isBlank()) author = doc.select("[rel=author]").text();

        // naive: collect all paragraphs within article-ish containers, else all <p>
        Elements main = doc.select("article p, .article p, .post p, .StoryBodyCompanionColumn p, p");
        String content = main.stream()
                .map(e -> e.text().trim())
                .filter(s -> s.length() > 0)
                .distinct()
                .collect(Collectors.joining("\n"));

        return new ExtractedArticle(title, outlet, author, content);
    }
}

