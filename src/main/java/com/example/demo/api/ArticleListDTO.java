// src/main/java/com/example/demo/api/ArticleListDTO.java
package com.example.demo.api;

import com.example.demo.domain.Stance;
import java.time.Instant;

public record ArticleListDTO(
        Long id, String title, String outlet, Instant publishedAt,
        Stance stance, Integer score, Double confidence, String url
) {}
