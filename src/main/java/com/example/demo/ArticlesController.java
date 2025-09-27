package com.example.demo;

import com.example.demo.api.ArticleListDTO;
import com.example.demo.data.ArticleRepository;
import com.example.demo.data.RatingRepository;
import com.example.demo.domain.Rating;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ArticlesController {

    private final ArticleRepository articles;
    private final RatingRepository ratings;

    public ArticlesController(ArticleRepository articles, RatingRepository ratings) {
        this.articles = articles;
        this.ratings = ratings;
    }

    @GetMapping("/api/articles")
    public List<ArticleListDTO> list() {
        return articles.findAll().stream().map(a -> {
            // use repo method to grab the latest rating for this article
            Rating latest = ratings.findTopByArticle_IdOrderByCreatedAtDesc(a.getId()).orElse(null);

            return new ArticleListDTO(
                    a.getId(),
                    a.getTitle(),
                    a.getOutlet(),
                    a.getPublishedAt(),
                    latest != null ? latest.getStance() : null,
                    latest != null ? latest.getScore() : null,
                    latest != null ? latest.getConfidence() : null,
                    a.getUrl()
            );
        }).toList();
    }
}
