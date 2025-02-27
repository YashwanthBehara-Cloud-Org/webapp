package com.cloud.webapp.repository;
import com.cloud.webapp.model.HealthCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HealthCheckRepository extends JpaRepository<HealthCheck, Long> {

    Optional<HealthCheck> findTopByOrderByCheckIdDesc();
}