package com.safepath.repository;

import com.safepath.model.OffenseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OffenseTypeRepository extends JpaRepository<OffenseType, String> {
}
