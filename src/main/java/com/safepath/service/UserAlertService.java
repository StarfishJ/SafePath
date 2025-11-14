package com.safepath.service;

import com.safepath.model.UserAlert;

import java.util.List;
import java.util.Optional;

public interface UserAlertService {
    UserAlert create(UserAlert alert);

    List<UserAlert> findByUserId(Integer userId);

    Optional<UserAlert> findById(Integer id);
}
