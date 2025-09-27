package com.example.demo.data;

import com.example.demo.domain.Rating;
import com.example.demo.domain.Stance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    boolean existsByArticle_Id(Long articleId);
    List<Rating> findByStance(Stance stance);
    Optional<Rating> findTopByArticle_IdOrderByCreatedAtDesc(Long articleId);
}
