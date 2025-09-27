package com.example.demo.service;

import com.example.demo.data.RatingRepository;
import com.example.demo.domain.Article;
import com.example.demo.domain.Rating;
import com.example.demo.nlp.ClassificationEngine;
import org.springframework.stereotype.Service;

@Service
public class RatingServiceImpl implements RatingService {

    private final ClassificationEngine engine;
    private final RatingRepository ratings;

    public RatingServiceImpl(ClassificationEngine engine, RatingRepository ratings) {
        this.engine = engine; this.ratings = ratings;
    }

    @Override
    public Rating classifyAndPersist(Article article) {
        var res = engine.classify(article.getContent());
        Rating r = new Rating();
        r.setArticle(article);
        r.setStance(res.stance());
        r.setScore(res.score());
        r.setConfidence(res.confidence());
        r.setEvidenceJson(res.evidence().toString());
        return ratings.save(r);
    }
}
