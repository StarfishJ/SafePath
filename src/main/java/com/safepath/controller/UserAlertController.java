package com.safepath.controller;

import com.safepath.dto.CreateUserAlertRequest;
import com.safepath.dto.UserAlertDto;
import com.safepath.model.AppUser;
import com.safepath.model.UserAlert;
import com.safepath.service.UserAlertService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/user-alerts")
public class UserAlertController {

    private final UserAlertService service;

    public UserAlertController(UserAlertService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UserAlertDto> create(@Valid @RequestBody CreateUserAlertRequest req) {
        UserAlert a = new UserAlert();
        AppUser u = new AppUser();
        u.setUserId(req.getUserId());
        a.setUser(u);
        a.setRadiusM(req.getRadiusM());
        a.setCenterLat(req.getCenterLat());
        a.setCenterLon(req.getCenterLon());
        a.setCrimeTypeFilter(req.getCrimeTypeFilter());
        a.setActiveFlag(req.getActiveFlag());

        UserAlert saved = service.create(a);
        return ResponseEntity.created(URI.create("/api/user-alerts/" + saved.getAlertId())).body(toDto(saved));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserAlertDto>> listByUser(@PathVariable Integer userId) {
        List<UserAlert> list = service.findByUserId(userId);
        List<UserAlertDto> dtoList = list.stream().map(this::toDto).toList();
        return ResponseEntity.ok(dtoList);
    }

    private UserAlertDto toDto(UserAlert a) {
        if (a == null) return null;
        UserAlertDto d = new UserAlertDto();
        d.setAlertId(a.getAlertId());
        d.setUserId(a.getUser() != null ? a.getUser().getUserId() : null);
        d.setRadiusM(a.getRadiusM());
        d.setCenterLat(a.getCenterLat());
        d.setCenterLon(a.getCenterLon());
        d.setCrimeTypeFilter(a.getCrimeTypeFilter());
        d.setActiveFlag(a.getActiveFlag());
        d.setCreatedAt(a.getCreatedAt());
        d.setUpdatedAt(a.getUpdatedAt());
        return d;
    }
}
