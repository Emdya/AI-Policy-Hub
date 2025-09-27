package com.example.demo;

import com.example.demo.data.ArticleRepository;
import com.example.demo.domain.Article;
import com.example.demo.domain.Rating;
import com.example.demo.service.RatingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final ArticleRepository articles;
    private final RatingService ratingService;

    public RatingController(ArticleRepository articles, RatingService ratingService) {
        this.articles = articles; this.ratingService = ratingService;
    }

    @PostMapping("/classify/{articleId}")
    public Rating classify(@PathVariable Long articleId) {
        Article a = articles.findById(articleId).orElseThrow();
        return ratingService.classifyAndPersist(a);
    }
}
