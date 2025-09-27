package com.example.demo.domain;


import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "articles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_articles_url", columnNames = "url")
})
public class Article {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String url;

    @Column(nullable = false)
    private String title;

    private String outlet;
    private String author;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Lob
    private String content;

    // --- getters/setters ---
    public Long getId() { return id; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getOutlet() { return outlet; }
    public void setOutlet(String outlet) { this.outlet = outlet; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
