package com.example.demo.nlp;
import com.example.demo.service.RatingResult;
public interface ClassificationEngine {
    RatingResult classify(String fullText);
}
