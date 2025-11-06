package com.safepath.repository;

import com.safepath.model.RealtimeIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RealtimeIncidentRepository extends JpaRepository<RealtimeIncident, String> {
}
