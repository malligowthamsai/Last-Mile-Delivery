package com.lastmile.delivery.repository;

import com.lastmile.delivery.entity.TrackingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrackingHistoryRepository extends JpaRepository<TrackingHistory, String> {
    List<TrackingHistory> findByOrderIdOrderByTimestampAsc(String orderId);
}
