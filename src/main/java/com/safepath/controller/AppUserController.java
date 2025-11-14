package com.safepath.controller;

import com.safepath.dto.AppUserDto;
import com.safepath.dto.CreateUserRequest;
import com.safepath.model.AppUser;
import com.safepath.service.AppUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
public class AppUserController {

    private final AppUserService service;

    public AppUserController(AppUserService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AppUserDto> register(@Valid @RequestBody CreateUserRequest req) {
        AppUser u = new AppUser();
        u.setEmail(req.getEmail());
        u.setPasswordHash(req.getPassword());
        AppUser saved = service.createUser(u);
        AppUserDto dto = toDto(saved);
        return ResponseEntity.created(URI.create("/api/users/" + saved.getUserId())).body(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppUserDto> get(@PathVariable Integer id) {
        return service.findById(id).map(u -> ResponseEntity.ok(toDto(u))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    private AppUserDto toDto(AppUser u) {
        if (u == null) return null;
        AppUserDto d = new AppUserDto();
        d.setUserId(u.getUserId());
        d.setEmail(u.getEmail());
        d.setCreatedAt(u.getCreatedAt());
        return d;
    }
}
