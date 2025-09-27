package com.example.demo.nlp;
import ai.djl.inference.Predictor;
import ai.djl.huggingface.translator.ZeroShotClassificationTranslator;
import ai.djl.modality.Classifications;
import ai.djl.modality.nlp.translator.ZeroShotClassificationInput;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;

import com.example.demo.domain.Stance;
import com.example.demo.service.RatingResult;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Zero-shot NLI stance classifier using DJL + Hugging Face.
 * Model: typeform/distilroberta-base-mnli (fast + accurate on CPU).
 *
 * Requires Maven deps:
 *   ai.djl.pytorch:pytorch-engine:0.29.0
 *   ai.djl.huggingface:tokenizers:0.29.0
 *
 * Also requires a SentenceSplitter bean (OpenNLP or Regex).
 */
public class DjlNliClassifier implements ClassificationEngine, AutoCloseable {

    private final SentenceSplitter splitter;
    private final ZooModel<ZeroShotClassificationInput, Classifications> model;
    private final Predictor<ZeroShotClassificationInput, Classifications> predictor;

    // Hypotheses: rephrase if you like; order matters (we read back by index).
    private static final List<String> HYPOTHESES = List.of(
            "This text is supportive of AI adoption.",
            "This text is skeptical about AI due to risks.",
            "This text is neutral about AI."
    );

    public DjlNliClassifier(SentenceSplitter splitter) throws Exception {
        this.splitter = splitter;

        // Choose a strong-but-light NLI model; swap to facebook/bart-large-mnli if you want.
        Criteria<ZeroShotClassificationInput, Classifications> criteria = Criteria.builder()
                .setTypes(ZeroShotClassificationInput.class, Classifications.class)
                .optModelUrls("djl://ai.djl.huggingface.pytorch/typeform/distilroberta-base-mnli")
                .optTranslator(ZeroShotClassificationTranslator.builder().build())
                .build();

        this.model = criteria.loadModel();
        this.predictor = model.newPredictor();

        // Warmup (optional)
        try {
            predictor.predict(new ZeroShotClassificationInput(
                    "AI improves productivity.",
                    HYPOTHESES, "entailment"
            ));
        } catch (Exception ignore) { /* warmup errors can be ignored */ }
    }

    @Override
    public RatingResult classify(String fullText) {
        if (fullText == null || fullText.isBlank()) {
            return new RatingResult(Stance.UNCERTAIN, 50, 0.5, List.of());
        }

        // 1) Split & filter sentences to informative ones
        List<String> all = splitter.split(fullText);
        List<String> sents = all.stream()
                .map(String::trim)
                .filter(s -> s.length() >= 30 && s.split("\\s+").length >= 6) // basic quality filter
                .limit(160) // hard cap for latency
                .toList();

        if (sents.isEmpty()) {
            return new RatingResult(Stance.UNCERTAIN, 50, 0.5, List.of());
        }

        // 2) Score each sentence with NLI
        record SS(String sent, double sup, double skep, double neu) {}
        List<SS> scored = new ArrayList<>(sents.size());

        for (String s : sents) {
            var input = new ZeroShotClassificationInput(s, HYPOTHESES, "entailment");
            Classifications out = safePredict(input);
            // DJL returns probs aligned with HYPOTHESES order we passed.
            List<Double> p = out.getProbabilities();
            double sup  = p.get(0);
            double skep = p.get(1);
            double neu  = p.get(2);
            scored.add(new SS(s, sup, skep, neu));
        }

        // 3) Aggregate: focus on strongest K sentences to reduce noise
        int K = Math.min(10, scored.size());
        List<SS> top = scored.stream()
                .sorted(Comparator.comparingDouble(x -> -Math.max(x.sup, x.skep)))
                .limit(K)
                .toList();

        double supMean  = mean(top, t -> t.sup);
        double skepMean = mean(top, t -> t.skep);
        double neuMean  = mean(top, t -> t.neu);

        // 4) 0–100 doc score: supportive 100 ↔ skeptical 0
        double raw = supMean - skepMean;              // [-1..1]
        int score = (int) Math.round((raw + 1.0) * 50); // [0..100]

        Stance stance = score >= 65 ? Stance.SUPPORTIVE
                : score <= 35 ? Stance.SKEPTICAL
                : (neuMean > 0.45 ? Stance.NEUTRAL : Stance.UNCERTAIN);

        // 5) Evidence: pick top 3 sentences with largest |sup - skep|, dedup near-duplicates
        List<String> evidence = scored.stream()
                .sorted(Comparator.comparingDouble(x -> -Math.abs(x.sup - x.skep)))
                .map(x -> x.sent)
                .filter(distinctByKey(this::fingerprint))
                .limit(3)
                .collect(Collectors.toList());

        // 6) Confidence: margin + coverage (simple calibration-lite)
        double margin = Math.abs(supMean - skepMean);               // separation
        double coverage = (double) top.size() / Math.max(1, sents.size()); // how many strong sents
        double conf = clamp(0.5 + 0.4 * margin + 0.1 * coverage, 0.5, 0.95);

        return new RatingResult(stance, score, conf, evidence);
    }

    private Classifications safePredict(ZeroShotClassificationInput input) {
        try {
            return predictor.predict(input);
        } catch (Exception e) {
            // Fail-soft: zero probabilities (pushes towards UNCERTAIN)
            return new Classifications(HYPOTHESES, List.of(0.0, 0.0, 0.0));
        }
    }

    private static double mean(List<?> list, ToDouble< ?> proj) {
        double sum = 0;
        int n = 0;
        for (Object o : list) { sum += proj.getAsDouble(o); n++; }
        return n == 0 ? 0.0 : sum / n;
    }

    // Simple functional interface to avoid pulling in java.util.function.ToDoubleFunction with generics noise
    @FunctionalInterface private interface ToDouble<T> { double getAsDouble(T t); }

    private static <T> java.util.function.Predicate<T> distinctByKey(java.util.function.Function<? super T, ?> keyExtractor) {
        Set<Object> seen = Collections.synchronizedSet(new HashSet<>());
        return t -> seen.add(keyExtractor.apply(t));
    }

    private String fingerprint(String s) {
        // lightweight dedupe: lowercase words >=3 chars, sort
        String[] toks = s.toLowerCase().split("\\W+");
        return Arrays.stream(toks)
                .filter(w -> w.length() >= 3)
                .sorted()
                .limit(30)
                .collect(Collectors.joining(" "));
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    @Override
    public void close() {
        try { predictor.close(); } finally { model.close(); }
    }
}
