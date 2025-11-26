package com.safepath.service.impl;

import com.safepath.dto.StreetSegmentRiskDto;
import com.safepath.model.StreetSegment;
import com.safepath.model.StreetSegmentRisk;
import com.safepath.repository.StreetSegmentRiskRepository;
import com.safepath.service.SafetyRecommendationService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class SafetyRecommendationServiceImpl implements SafetyRecommendationService {

    private final StreetSegmentRiskRepository repository;

    public SafetyRecommendationServiceImpl(StreetSegmentRiskRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<StreetSegmentRiskDto> getRiskBySegment(String unitid) {
        Assert.hasText(unitid, "unitid must not be blank");
        return repository.findByUnitidWithSegment(unitid).map(this::toDto);
    }

    @Override
    public List<StreetSegmentRiskDto> getRisksByBoundingBox(
        double west,
        double south,
        double east,
        double north
    ) {
        Assert.isTrue(east >= west, "east must be greater than or equal to west");
        Assert.isTrue(north >= south, "north must be greater than or equal to south");
        return repository.findWithinBounds(west, south, east, north).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    private StreetSegmentRiskDto toDto(StreetSegmentRisk risk) {
        StreetSegmentRiskDto dto = new StreetSegmentRiskDto();
        dto.setUnitid(risk.getUnitid());
        dto.setRiskLabel(risk.getRiskLabel());
        dto.setRiskScore(risk.getRiskScore());
        dto.setClusterId(risk.getClusterId());
        dto.setIncidentDensity(risk.getIncidentDensity());
        dto.setNightFraction(risk.getNightFraction());
        dto.setLast90dIncidents(risk.getLast90dIncidents());
        dto.setModelVersion(risk.getModelVersion());
        dto.setOverrideReason(risk.getOverrideReason());
        dto.setUpdatedAt(risk.getUpdatedAt());

        StreetSegment segment = risk.getStreetSegment();
        if (segment != null) {
            dto.setStreetName(segment.getOnstreet());
            dto.setLatitude(segment.getGisMidY());
            dto.setLongitude(segment.getGisMidX());
        }
        return dto;
    }
}

