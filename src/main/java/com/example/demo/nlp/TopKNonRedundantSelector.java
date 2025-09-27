package com.example.demo.nlp;

import java.util.*;
import java.util.stream.Collectors;

public class TopKNonRedundantSelector implements EvidenceSelector {

    private static double jaccard(String a, String b) {
        Set<String> sa = Arrays.stream(a.toLowerCase().split("\\W+")).filter(s->s.length()>2).collect(Collectors.toSet());
        Set<String> sb = Arrays.stream(b.toLowerCase().split("\\W+")).filter(s->s.length()>2).collect(Collectors.toSet());
        int inter = (int) sa.stream().filter(sb::contains).count();
        int union = sa.size() + sb.size() - inter;
        return union == 0 ? 0.0 : (double) inter / union;
    }

    @Override
    public List<String> select(List<ScoredSentence> scored, int k) {
        List<String> out = new ArrayList<>();
        for (ScoredSentence s : scored) {
            if (out.size() >= k) break;
            boolean redundant = out.stream().anyMatch(prev -> jaccard(prev, s.text()) >= 0.8);
            if (!redundant && Math.abs(s.score()) > 0) out.add(s.text());
        }
        return out;
    }
}
