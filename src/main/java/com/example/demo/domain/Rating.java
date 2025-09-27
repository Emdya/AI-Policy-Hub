package com.example.demo.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ratings", indexes = {
        @Index(name = "idx_ratings_stance", columnList = "stance")
})
public class Rating {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_rating_article"))
    private Article article;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Stance stance;

    // 0–100 stance score
    private int score;

    // 0.0–1.0 confidence
    private double confidence;

    // store 2–4 evidence sentences as JSON text for now
    @Lob
    @Column(name = "evidence_json")
    private String evidenceJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    // --- getters/setters ---
    public Long getId() { return id; }
    public Article getArticle() { return article; }
    public void setArticle(Article article) { this.article = article; }
    public Stance getStance() { return stance; }
    public void setStance(Stance stance) { this.stance = stance; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public String getEvidenceJson() { return evidenceJson; }
    public void setEvidenceJson(String evidenceJson) { this.evidenceJson = evidenceJson; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
