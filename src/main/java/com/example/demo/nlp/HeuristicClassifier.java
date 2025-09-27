package com.example.demo.nlp;

public class HeuristicClassifier extends AbstractClassifier {
    public HeuristicClassifier(SentenceSplitter s, StanceScorer sc, EvidenceSelector sel) {
        super(s, sc, sel);
    }
}
