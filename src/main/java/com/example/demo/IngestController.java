package com.example.demo;

import com.example.demo.service.IngestReport;
import com.example.demo.service.IngestService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class IngestController {
    private final IngestService ingest;

    public IngestController(IngestService ingest) {
        this.ingest = ingest;
    }

    // Accepts either an empty body (RSS mode) OR a JSON array of URLs (direct mode)
    @PostMapping(
            value = "/ingest",
            consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.ALL_VALUE },
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public IngestReport ingest(@RequestBody(required = false) List<String> urls) {
        return ingest.ingest(urls == null ? List.of() : urls);
    }
}
