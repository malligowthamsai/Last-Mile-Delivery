package com.lastmile.delivery.repository;

import com.lastmile.delivery.entity.RescheduleRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RescheduleRequestRepository extends JpaRepository<RescheduleRequest, String> {
    List<RescheduleRequest> findByOrderIdOrderByRequestedAtDesc(String orderId);
}
