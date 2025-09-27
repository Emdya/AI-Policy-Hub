package com.example.demo.nlp;
import java.util.List;
public interface StanceScorer {
    List<ScoredSentence> score(List<String> sentences);
}
