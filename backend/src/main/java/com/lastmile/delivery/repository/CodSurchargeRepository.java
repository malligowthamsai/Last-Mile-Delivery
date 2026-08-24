package com.lastmile.delivery.repository;

import com.lastmile.delivery.entity.CodSurcharge;
import com.lastmile.delivery.entity.OrderType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CodSurchargeRepository extends JpaRepository<CodSurcharge, String> {
    Optional<CodSurcharge> findByOrderType(OrderType orderType);
}
