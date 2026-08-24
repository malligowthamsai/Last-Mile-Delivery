package com.lastmile.delivery.repository;

import com.lastmile.delivery.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface AreaRepository extends JpaRepository<Area, String> {
    Optional<Area> findByPincode(String pincode);
    List<Area> findByZoneId(String zoneId);
}
