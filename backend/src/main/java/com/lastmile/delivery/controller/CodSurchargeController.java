package com.lastmile.delivery.controller;

import com.lastmile.delivery.dto.CodSurchargeRequest;
import com.lastmile.delivery.entity.CodSurcharge;
import com.lastmile.delivery.service.CodSurchargeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cod-surcharges")
public class CodSurchargeController {

    @Autowired
    private CodSurchargeService codSurchargeService;

    @GetMapping
    public ResponseEntity<List<CodSurcharge>> getAllSurcharges() {
        return ResponseEntity.ok(codSurchargeService.getAllSurcharges());
    }

    @PutMapping("/{orderType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CodSurcharge> upsertSurcharge(
            @PathVariable String orderType,
            @Valid @RequestBody CodSurchargeRequest request) {
        CodSurcharge surcharge = codSurchargeService.upsertSurcharge(orderType, request);
        return ResponseEntity.ok(surcharge);
    }
}
