package com.example.demo.nlp;

import com.example.demo.domain.Stance;
import com.example.demo.service.RatingResult;

import java.util.List;

public abstract class AbstractClassifier implements ClassificationEngine {
    protected final SentenceSplitter splitter;
    protected final StanceScorer scorer;
    protected final EvidenceSelector selector;

    protected AbstractClassifier(SentenceSplitter s, StanceScorer sc, EvidenceSelector sel) {
        this.splitter = s; this.scorer = sc; this.selector = sel;
    }

    @Override
    public RatingResult classify(String text) {
        List<String> sents = splitter.split(text);
        var scored = scorer.score(sents);
        int sum = scored.stream().mapToInt(ScoredSentence::score).sum();
        int clamped = Math.max(-10, Math.min(10, sum));
        int score100 = Math.round((clamped + 10) * 5);   // [-10..10] -> [0..100]
        Stance stance = score100 >= 65 ? Stance.SUPPORTIVE
                : score100 <= 35 ? Stance.SKEPTICAL
                : Stance.NEUTRAL;
        double coverage = (double) scored.stream().filter(s -> s.score()!=0).count()
                / Math.max(1, sents.size());
        double confidence = Math.min(0.95, 0.5 + 0.5 * coverage);
        if (confidence < 0.55) stance = Stance.UNCERTAIN;
        var evidence = selector.select(scored, 3);
        return new RatingResult(stance, score100, confidence, evidence);
    }
}
