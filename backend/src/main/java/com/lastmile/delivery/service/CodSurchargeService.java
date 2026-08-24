package com.lastmile.delivery.service;

import com.lastmile.delivery.dto.CodSurchargeRequest;
import com.lastmile.delivery.entity.CodSurcharge;
import com.lastmile.delivery.entity.OrderType;
import com.lastmile.delivery.exception.BadRequestException;
import com.lastmile.delivery.repository.CodSurchargeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CodSurchargeService {

    @Autowired
    private CodSurchargeRepository codSurchargeRepository;

    public List<CodSurcharge> getAllSurcharges() {
        return codSurchargeRepository.findAll();
    }

    public CodSurcharge upsertSurcharge(String orderTypeStr, CodSurchargeRequest request) {
        OrderType orderType;
        try {
            orderType = OrderType.valueOf(orderTypeStr.toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("orderType must be B2B or B2C");
        }

        CodSurcharge surcharge = codSurchargeRepository.findByOrderType(orderType)
                .orElse(CodSurcharge.builder().orderType(orderType).build());

        surcharge.setSurchargeFlat(request.getSurchargeFlat());
        return codSurchargeRepository.save(surcharge);
    }
}
