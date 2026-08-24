package com.lastmile.delivery.controller;

import com.lastmile.delivery.dto.ZoneRequest;
import com.lastmile.delivery.entity.Zone;
import com.lastmile.delivery.service.ZoneService;
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
@RequestMapping("/api/zones")
public class ZoneController {

    @Autowired
    private ZoneService zoneService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllZones() {
        return ResponseEntity.ok(zoneService.getAllZones());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getZoneById(@PathVariable String id) {
        return ResponseEntity.ok(zoneService.getZoneById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Zone> createZone(@Valid @RequestBody ZoneRequest request) {
        Zone created = zoneService.createZone(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Zone> updateZone(@PathVariable String id, @Valid @RequestBody ZoneRequest request) {
        Zone updated = zoneService.updateZone(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteZone(@PathVariable String id) {
        zoneService.deleteZone(id);
        return ResponseEntity.ok(Collections.singletonMap("message", "Zone deleted"));
    }
}
