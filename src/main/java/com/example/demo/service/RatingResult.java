package com.example.demo.service;

import com.example.demo.domain.Stance;
import java.util.List;

public record RatingResult(Stance stance, int score, double confidence, List<String> evidence) {}
