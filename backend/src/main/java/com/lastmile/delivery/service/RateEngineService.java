package com.lastmile.delivery.service;

import com.lastmile.delivery.dto.OrderCalculateRequest;
import com.lastmile.delivery.dto.OrderCalculateResponse;
import com.lastmile.delivery.entity.Area;
import com.lastmile.delivery.entity.CodSurcharge;
import com.lastmile.delivery.entity.OrderType;
import com.lastmile.delivery.entity.RateCard;
import com.lastmile.delivery.entity.Zone;
import com.lastmile.delivery.exception.BadRequestException;
import com.lastmile.delivery.repository.CodSurchargeRepository;
import com.lastmile.delivery.repository.RateCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RateEngineService {

    @Autowired
    private ZoneDetectorService zoneDetectorService;

    @Autowired
    private RateCardRepository rateCardRepository;

    @Autowired
    private CodSurchargeRepository codSurchargeRepository;

    public OrderCalculateResponse calculateCharge(OrderCalculateRequest request) {
        Area pickupArea = zoneDetectorService.detectZoneByPincode(request.getPickupPincode());
        Area dropArea = zoneDetectorService.detectZoneByPincode(request.getDropPincode());

        Zone pickupZone = pickupArea.getZone();
        Zone dropZone = dropArea.getZone();

        boolean isIntraZone = pickupZone.getId().equals(dropZone.getId());
        double distanceKm = estimatePincodeDistance(request.getPickupPincode(), request.getDropPincode(), isIntraZone);

        // Volumetric weight = (L * B * H) / 5000
        double volumetricWeight = (request.getLength() * request.getBreadth() * request.getHeight()) / 5000.0;
        double billableWeight = Math.max(request.getActualWeight(), volumetricWeight);

        // Parse OrderType and PaymentType
        OrderType orderType;
        try {
            orderType = OrderType.valueOf(request.getOrderType().toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid orderType: " + request.getOrderType() + ". Must be B2B or B2C");
        }

        // COD Surcharge lookup
        double codSurcharge = 0.0;
        if ("COD".equalsIgnoreCase(request.getPaymentType())) {
            Optional<CodSurcharge> surchargeOpt = codSurchargeRepository.findByOrderType(orderType);
            if (surchargeOpt.isPresent()) {
                codSurcharge = surchargeOpt.get().getSurchargeFlat();
            }
        }

        // Zone rate card lookup
        Optional<RateCard> rateCardOpt = rateCardRepository.findByZoneFromIdAndZoneToIdAndOrderType(
                pickupZone.getId(), dropZone.getId(), orderType);

        // Tier A: STANDARD
        double standardRatePerKg = rateCardOpt.isPresent()
                ? rateCardOpt.get().getRatePerKg()
                : round(22.0 + (distanceKm * 0.35));
        double standardMinCharge = rateCardOpt.isPresent()
                ? rateCardOpt.get().getMinCharge()
                : round(45.0 + (distanceKm * 0.8));
        double standardBase = round(Math.max(billableWeight * standardRatePerKg, standardMinCharge));
        double standardTotal = round(standardBase + codSurcharge);

        // Tier B: EXPRESS
        double expressRatePerKg = round(Math.max(standardRatePerKg * 1.45, 35.0 + distanceKm * 0.5));
        double expressMinCharge = round(Math.max(standardMinCharge * 1.5, 75.0 + distanceKm * 1.2));
        double expressBase = round(Math.max(billableWeight * expressRatePerKg, expressMinCharge));
        double expressTotal = round(expressBase + codSurcharge);

        // Tier C: ECONOMY
        double economyRatePerKg = round(Math.max(15.0, standardRatePerKg * 0.75));
        double economyMinCharge = round(Math.max(30.0, standardMinCharge * 0.75));
        double economyBase = round(Math.max(billableWeight * economyRatePerKg, economyMinCharge));
        double economyTotal = round(economyBase + codSurcharge);

        List<OrderCalculateResponse.RateOption> options = new ArrayList<>();
        options.add(OrderCalculateResponse.RateOption.builder()
                .id("STANDARD")
                .title("Standard Zone Delivery")
                .subtitle("Standard ground shipping")
                .estimatedDelivery(isIntraZone ? "Next Day (24 hrs)" : "1–2 Business Days")
                .ratePerKg(standardRatePerKg)
                .minCharge(standardMinCharge)
                .baseCharge(standardBase)
                .codSurcharge(round(codSurcharge))
                .totalCharge(standardTotal)
                .pricingMethod(rateCardOpt.isPresent() ? "ZONE_RATE_CARD" : "DISTANCE_STANDARD")
                .build());

        options.add(OrderCalculateResponse.RateOption.builder()
                .id("EXPRESS")
                .title("Express Same-Day Dispatch")
                .subtitle("Priority direct routing")
                .estimatedDelivery(isIntraZone ? "Same Day (4–6 hrs)" : "Next Day Priority")
                .ratePerKg(expressRatePerKg)
                .minCharge(expressMinCharge)
                .baseCharge(expressBase)
                .codSurcharge(round(codSurcharge))
                .totalCharge(expressTotal)
                .pricingMethod("DISTANCE_EXPRESS")
                .build());

        options.add(OrderCalculateResponse.RateOption.builder()
                .id("ECONOMY")
                .title("Economy Surface Transport")
                .subtitle("Budget ground shipping")
                .estimatedDelivery(isIntraZone ? "2–3 Days" : "3–5 Business Days")
                .ratePerKg(economyRatePerKg)
                .minCharge(economyMinCharge)
                .baseCharge(economyBase)
                .codSurcharge(round(codSurcharge))
                .totalCharge(economyTotal)
                .pricingMethod("ECONOMY_SURFACE")
                .build());

        String targetRateType = (request.getRateType() != null && !request.getRateType().trim().isEmpty())
                ? request.getRateType().toUpperCase()
                : "STANDARD";

        OrderCalculateResponse.RateOption selected = options.stream()
                .filter(o -> o.getId().equalsIgnoreCase(targetRateType))
                .findFirst()
                .orElse(options.get(0));

        return OrderCalculateResponse.builder()
                .pickupZone(OrderCalculateResponse.ZoneInfo.builder()
                        .zoneId(pickupZone.getId())
                        .zoneName(pickupZone.getName())
                        .build())
                .dropZone(OrderCalculateResponse.ZoneInfo.builder()
                        .zoneId(dropZone.getId())
                        .zoneName(dropZone.getName())
                        .build())
                .isIntraZone(isIntraZone)
                .estimatedDistanceKm(round(distanceKm))
                .volumetricWeight(round3(volumetricWeight))
                .billableWeight(round3(billableWeight))
                .selectedRateType(selected.getId())
                .ratePerKg(selected.getRatePerKg())
                .minCharge(selected.getMinCharge())
                .baseCharge(selected.getBaseCharge())
                .codSurcharge(selected.getCodSurcharge())
                .totalCharge(selected.getTotalCharge())
                .pricingMethod(selected.getPricingMethod())
                .estimatedDelivery(selected.getEstimatedDelivery())
                .options(options)
                .build();
    }

    private double estimatePincodeDistance(String p1, String p2, boolean isIntraZone) {
        if (isIntraZone) {
            return 8.5;
        }
        try {
            int p1Num = Integer.parseInt(p1.replaceAll("\\D+", ""));
            int p2Num = Integer.parseInt(p2.replaceAll("\\D+", ""));
            return Math.max(15.0, Math.min(850.0, Math.abs(p1Num - p2Num) * 0.05 + 20.0));
        } catch (Exception e) {
            return 25.0;
        }
    }

    private double round(double val) {
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private double round3(double val) {
        return BigDecimal.valueOf(val).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }
}
