package com.example.demo.nlp;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class OpenNlpSentenceSplitter implements SentenceSplitter {
    private final SentenceDetectorME detector;

    public OpenNlpSentenceSplitter() {
        try (InputStream in = getClass().getResourceAsStream("/models/en-sent.bin")) {
            detector = new SentenceDetectorME(new SentenceModel(in));
        } catch (Exception e) {
            throw new IllegalStateException("OpenNLP model not found (models/en-sent.bin)", e);
        }
    }

    @Override public List<String> split(String text) {
        if (text == null || text.isBlank()) return List.of();
        return Arrays.asList(detector.sentDetect(text));
    }
}
