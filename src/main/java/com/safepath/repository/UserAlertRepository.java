package com.safepath.repository;

import com.safepath.model.UserAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAlertRepository extends JpaRepository<UserAlert, Integer> {
    List<UserAlert> findByUser_UserId(Integer userId);
}
