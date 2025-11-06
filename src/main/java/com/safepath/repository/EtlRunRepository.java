package com.safepath.repository;

import com.safepath.model.EtlRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EtlRunRepository extends JpaRepository<EtlRun, Integer> {
}
