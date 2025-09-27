package com.example.demo.nlp;

import java.util.*;
import java.util.stream.Collectors;

public class HeuristicStanceScorer implements StanceScorer {

    private static final Map<String,Integer> POS = Map.ofEntries(
            Map.entry("boosts productivity", +2),
            Map.entry("accelerates innovation", +2),
            Map.entry("efficiency gains", +2),
            Map.entry("improves accuracy", +2),
            Map.entry("adoption", +1), Map.entry("growth", +1), Map.entry("breakthrough", +2)
    );

    private static final Map<String,Integer> NEG = Map.ofEntries(
            Map.entry("job losses", -2),
            Map.entry("misinformation", -2),
            Map.entry("bias", -2),
            Map.entry("harm", -2),
            Map.entry("risk", -2), Map.entry("safety concern", -2),
            Map.entry("ban", -2), Map.entry("pause", -2),
            Map.entry("privacy violation", -2), Map.entry("regulatory crackdown", -2)
    );

    @Override
    public List<ScoredSentence> score(List<String> sentences) {
        List<ScoredSentence> out = new ArrayList<>();
        for (String s : sentences) {
            String t = s.toLowerCase(Locale.ROOT);
            int score = 0;
            for (var e : POS.entrySet()) if (t.contains(e.getKey())) score += e.getValue();
            for (var e : NEG.entrySet()) if (t.contains(e.getKey())) score += e.getValue();
            out.add(new ScoredSentence(s, score));
        }
        // keep strongest first
        return out.stream()
                .sorted((a,b) -> Integer.compare(Math.abs(b.score()), Math.abs(a.score())))
                .collect(Collectors.toList());
    }
}
