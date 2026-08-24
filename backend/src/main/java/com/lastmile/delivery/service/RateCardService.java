package com.lastmile.delivery.service;

import com.lastmile.delivery.dto.RateCardRequest;
import com.lastmile.delivery.entity.OrderType;
import com.lastmile.delivery.entity.RateCard;
import com.lastmile.delivery.entity.Zone;
import com.lastmile.delivery.exception.BadRequestException;
import com.lastmile.delivery.exception.ResourceNotFoundException;
import com.lastmile.delivery.repository.RateCardRepository;
import com.lastmile.delivery.repository.ZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RateCardService {

    @Autowired
    private RateCardRepository rateCardRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    public List<RateCard> getAllRateCards() {
        return rateCardRepository.findAll();
    }

    public RateCard createRateCard(RateCardRequest request) {
        OrderType orderType;
        try {
            orderType = OrderType.valueOf(request.getOrderType().toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("orderType must be B2B or B2C");
        }

        if (rateCardRepository.findByZoneFromIdAndZoneToIdAndOrderType(
                request.getZoneFromId(), request.getZoneToId(), orderType).isPresent()) {
            throw new BadRequestException("Rate card for this zone pair and order type already exists");
        }

        Zone zoneFrom = zoneRepository.findById(request.getZoneFromId())
                .orElseThrow(() -> new BadRequestException("zoneFrom not found"));
        Zone zoneTo = zoneRepository.findById(request.getZoneToId())
                .orElseThrow(() -> new BadRequestException("zoneTo not found"));

        RateCard card = RateCard.builder()
                .zoneFrom(zoneFrom)
                .zoneTo(zoneTo)
                .orderType(orderType)
                .ratePerKg(request.getRatePerKg())
                .minCharge(request.getMinCharge() != null ? request.getMinCharge() : 0.0)
                .build();

        return rateCardRepository.save(card);
    }

    public RateCard updateRateCard(String id, RateCardRequest request) {
        RateCard card = rateCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rate card not found"));

        if (request.getRatePerKg() != null) {
            card.setRatePerKg(request.getRatePerKg());
        }
        if (request.getMinCharge() != null) {
            card.setMinCharge(request.getMinCharge());
        }

        return rateCardRepository.save(card);
    }

    public void deleteRateCard(String id) {
        RateCard card = rateCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rate card not found"));
        rateCardRepository.delete(card);
    }
}
