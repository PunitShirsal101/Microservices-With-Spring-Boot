package com.spunit.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommend")
public class RecommendationController {
    @PostMapping
    public ResponseEntity<Map<String, Object>> getRecommendations(@RequestBody Map<String, Object> payload) {
        // Placeholder: Integrate with Python ML service
        String userId = (String) payload.get("user_id");
        List<String> recommendations = List.of("content1", "content2", "content3");
        return ResponseEntity.ok(Map.of("user_id", userId, "recommendations", recommendations));
    }
}
