package com.safepath.service;

import com.safepath.dto.StreetSegmentRiskDto;
import java.util.List;
import java.util.Optional;

public interface SafetyRecommendationService {

    Optional<StreetSegmentRiskDto> getRiskBySegment(String unitid);

    List<StreetSegmentRiskDto> getRisksByBoundingBox(
        double west,
        double south,
        double east,
        double north
    );
}

