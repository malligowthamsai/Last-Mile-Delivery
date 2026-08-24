package com.lastmile.delivery.controller;

import com.lastmile.delivery.dto.RateCardRequest;
import com.lastmile.delivery.entity.RateCard;
import com.lastmile.delivery.service.RateCardService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rate-cards")
public class RateCardController {

    @Autowired
    private RateCardService rateCardService;

    @GetMapping
    public ResponseEntity<List<RateCard>> getAllRateCards() {
        return ResponseEntity.ok(rateCardService.getAllRateCards());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RateCard> createRateCard(@Valid @RequestBody RateCardRequest request) {
        RateCard created = rateCardService.createRateCard(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RateCard> updateRateCard(@PathVariable String id, @RequestBody RateCardRequest request) {
        RateCard updated = rateCardService.updateRateCard(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteRateCard(@PathVariable String id) {
        rateCardService.deleteRateCard(id);
        return ResponseEntity.ok(Collections.singletonMap("message", "Rate card deleted"));
    }
}
