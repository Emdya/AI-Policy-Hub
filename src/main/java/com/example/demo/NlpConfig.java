package com.example.demo;

import com.example.demo.nlp.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NlpConfig {

    @Bean SentenceSplitter splitter() {
        try { return new OpenNlpSentenceSplitter(); }
        catch (Exception e) { return new RegexSentenceSplitter(); } // fallback
    }

    @Bean StanceScorer scorer() { return new HeuristicStanceScorer(); }

    @Bean EvidenceSelector selector() { return new TopKNonRedundantSelector(); }

    @Bean ClassificationEngine classifier(SentenceSplitter s, StanceScorer sc, EvidenceSelector sel) {
        return new HeuristicClassifier(s, sc, sel);
    }
}
