package com.lastmile.delivery.controller;

import com.lastmile.delivery.dto.AreaRequest;
import com.lastmile.delivery.entity.Area;
import com.lastmile.delivery.service.AreaService;
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
@RequestMapping("/api/areas")
public class AreaController {

    @Autowired
    private AreaService areaService;

    @GetMapping
    public ResponseEntity<List<Area>> listAreas(@RequestParam(required = false) String zoneId) {
        return ResponseEntity.ok(areaService.listAreas(zoneId));
    }

    @GetMapping("/lookup/{pincode}")
    public ResponseEntity<Area> lookupPincode(@PathVariable String pincode) {
        return ResponseEntity.ok(areaService.lookupByPincode(pincode));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Area> createArea(@Valid @RequestBody AreaRequest request) {
        Area created = areaService.createArea(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Area> updateArea(@PathVariable String id, @RequestBody AreaRequest request) {
        Area updated = areaService.updateArea(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteArea(@PathVariable String id) {
        areaService.deleteArea(id);
        return ResponseEntity.ok(Collections.singletonMap("message", "Area deleted"));
    }
}
