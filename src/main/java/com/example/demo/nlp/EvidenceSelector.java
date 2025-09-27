package com.example.demo.nlp;
import java.util.List;
public interface EvidenceSelector { List<String> select(List<ScoredSentence> scored, int k); }
