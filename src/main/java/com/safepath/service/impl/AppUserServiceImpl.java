package com.safepath.service.impl;

import com.safepath.model.AppUser;
import com.safepath.repository.AppUserRepository;
import com.safepath.service.AppUserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository repository;
    // use a lightweight SHA-256 hash to avoid adding new dependencies
    private String hashPassword(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes());
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public AppUserServiceImpl(AppUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public AppUser createUser(AppUser user) {
    // hash password before saving (SHA-256)
    user.setPasswordHash(hashPassword(user.getPasswordHash()));
        user.setCreatedAt(LocalDateTime.now());
        return repository.save(user);
    }

    @Override
    public Optional<AppUser> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public Optional<AppUser> findByEmail(String email) {
        return repository.findByEmail(email);
    }
}
