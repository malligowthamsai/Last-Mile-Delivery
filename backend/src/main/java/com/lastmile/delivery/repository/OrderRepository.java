package com.lastmile.delivery.repository;

import com.lastmile.delivery.entity.Order;
import com.lastmile.delivery.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {
    
    List<Order> findByCustomerIdOrderByCreatedAtDesc(String customerId);
    
    List<Order> findByAgentIdOrderByCreatedAtDesc(String agentId);

    @Query("SELECT o FROM Order o WHERE " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:agentId IS NULL OR o.agent.id = :agentId) AND " +
           "(:customerId IS NULL OR o.customer.id = :customerId) AND " +
           "(:zoneId IS NULL OR o.pickupZone.id = :zoneId OR o.dropZone.id = :zoneId) " +
           "ORDER BY o.createdAt DESC")
    List<Order> filterOrders(
        @Param("status") OrderStatus status,
        @Param("agentId") String agentId,
        @Param("customerId") String customerId,
        @Param("zoneId") String zoneId
    );
}
