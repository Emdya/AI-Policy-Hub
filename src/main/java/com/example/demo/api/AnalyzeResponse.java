package com.example.demo.api;

import com.example.demo.domain.Stance;
import java.util.List;

public record AnalyzeResponse(
        boolean ok,
        String message,            // error or info
        String title,
        String outlet,
        Stance stance,
        Integer score,
        Double confidence,
        List<String> evidence
) { }
