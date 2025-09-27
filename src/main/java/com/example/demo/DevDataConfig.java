package com.example.demo;

import com.example.demo.data.ArticleRepository;
import com.example.demo.data.RatingRepository;
import com.example.demo.domain.Article;
import com.example.demo.domain.Rating;
import com.example.demo.domain.Stance;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;

@Configuration
public class DevDataConfig {

    @Bean
    CommandLineRunner seedData(ArticleRepository articles, RatingRepository ratings) {
        return args -> {
            if (articles.count() == 0) {
                Article a = new Article();
                a.setUrl("https://example.com/ai-jobs");
                a.setTitle("AI hiring boom vs job risks");
                a.setOutlet("Wired");
                a.setAuthor("Jane D");
                a.setPublishedAt(Instant.now());
                a.setContent("Experts warn of potential job losses... but some see productivity gains.");
                a = articles.save(a);

                Rating r = new Rating();
                r.setArticle(a);
                r.setStance(Stance.SKEPTICAL);
                r.setScore(35);
                r.setConfidence(0.72);
                r.setEvidenceJson("[\"job losses in routine roles\",\"regulators consider stricter rules\"]");
                ratings.save(r);
            }
        };
    }
}
