package com.safepath.controller;

import com.safepath.dto.StreetSegmentRiskDto;
import com.safepath.service.SafetyRecommendationService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/safety/segments")
public class SafetyRecommendationController {

    private final SafetyRecommendationService service;

    public SafetyRecommendationController(SafetyRecommendationService service) {
        this.service = service;
    }

    @GetMapping("/{unitid}")
    public ResponseEntity<StreetSegmentRiskDto> getSegment(@PathVariable String unitid) {
        return service.getRiskBySegment(unitid)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<StreetSegmentRiskDto>> findWithinBounds(
        @RequestParam double west,
        @RequestParam double south,
        @RequestParam double east,
        @RequestParam double north
    ) {
        List<StreetSegmentRiskDto> list = service.getRisksByBoundingBox(west, south, east, north);
        return ResponseEntity.ok(list);
    }
}

