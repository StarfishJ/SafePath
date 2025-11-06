package com.safepath.service;

import com.safepath.model.AppUser;

import java.util.Optional;

public interface AppUserService {
    AppUser createUser(AppUser user);

    Optional<AppUser> findById(Integer id);

    Optional<AppUser> findByEmail(String email);
}
