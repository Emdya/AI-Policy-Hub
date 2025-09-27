package com.example.demo.nlp;
import java.util.Arrays; import java.util.List;
public class RegexSentenceSplitter implements SentenceSplitter {
    @Override public List<String> split(String text) {
        if (text == null || text.isBlank()) return List.of();
        return Arrays.stream(text.split("(?<=[.!?])\\s+")).toList();
    }
}
