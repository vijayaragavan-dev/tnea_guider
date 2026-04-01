package com.tneaguider.backend.controller.extra;

import com.tneaguider.backend.service.extra.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/review")
    public ResponseEntity<Map<String, Object>> getReview(@RequestParam String collegeName,
            @RequestParam(required = false) String district) {
        if (collegeName == null || collegeName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("college", "", "reviews", new Object[]{}));
        }
        Map<String, Object> result = reviewService.getMultiReviews(collegeName.trim(), district);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/why")
    public ResponseEntity<Map<String, String>> getWhy(@RequestParam String collegeName, @RequestParam(required = false) String cutoff, @RequestParam(required = false) String category) {
        if (collegeName == null || collegeName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("college", "", "reason", "College name is required"));
        }
        Map<String, String> result = reviewService.getWhyThisCollege(collegeName.trim(), cutoff, category);
        return ResponseEntity.ok(result);
    }
}
