package com.lastmile.delivery.repository;

import com.lastmile.delivery.entity.OrderType;
import com.lastmile.delivery.entity.RateCard;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RateCardRepository extends JpaRepository<RateCard, String> {
    Optional<RateCard> findByZoneFromIdAndZoneToIdAndOrderType(String zoneFromId, String zoneToId, OrderType orderType);
}
