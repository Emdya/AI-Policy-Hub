package com.example.demo.service;

import com.example.demo.domain.Article;
import com.example.demo.domain.Rating;

public interface RatingService {
    Rating classifyAndPersist(Article article);
}
