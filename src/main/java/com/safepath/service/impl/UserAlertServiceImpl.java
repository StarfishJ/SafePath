package com.safepath.service.impl;

import com.safepath.model.AppUser;
import com.safepath.model.UserAlert;
import com.safepath.repository.AppUserRepository;
import com.safepath.repository.UserAlertRepository;
import com.safepath.service.UserAlertService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserAlertServiceImpl implements UserAlertService {

    private final UserAlertRepository repository;
    private final AppUserRepository userRepository;

    public UserAlertServiceImpl(UserAlertRepository repository, AppUserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Override
    public UserAlert create(UserAlert alert) {
        // attach user reference if exists
        Integer uid = alert.getUser() != null ? alert.getUser().getUserId() : null;
        if (uid != null) {
            AppUser u = userRepository.findById(uid).orElse(null);
            alert.setUser(u);
        }
        alert.setCreatedAt(LocalDateTime.now());
        alert.setUpdatedAt(LocalDateTime.now());
        return repository.save(alert);
    }

    @Override
    public List<UserAlert> findByUserId(Integer userId) {
        return repository.findByUser_UserId(userId);
    }

    @Override
    public Optional<UserAlert> findById(Integer id) {
        return repository.findById(id);
    }
}
