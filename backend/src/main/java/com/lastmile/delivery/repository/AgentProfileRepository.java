package com.lastmile.delivery.repository;

import com.lastmile.delivery.entity.AgentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AgentProfileRepository extends JpaRepository<AgentProfile, String> {
    Optional<AgentProfile> findByUserId(String userId);
    Optional<AgentProfile> findFirstByZoneIdAndIsAvailableTrue(String zoneId);
    Optional<AgentProfile> findFirstByIsAvailableTrue();
}
