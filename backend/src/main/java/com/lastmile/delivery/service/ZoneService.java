package com.lastmile.delivery.service;

import com.lastmile.delivery.dto.ZoneRequest;
import com.lastmile.delivery.entity.Area;
import com.lastmile.delivery.entity.Zone;
import com.lastmile.delivery.exception.BadRequestException;
import com.lastmile.delivery.exception.ResourceNotFoundException;
import com.lastmile.delivery.repository.AreaRepository;
import com.lastmile.delivery.repository.ZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ZoneService {

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private AreaRepository areaRepository;

    public List<Map<String, Object>> getAllZones() {
        List<Zone> zones = zoneRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Zone zone : zones) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", zone.getId());
            map.put("name", zone.getName());
            map.put("createdAt", zone.getCreatedAt());
            map.put("areas", areaRepository.findByZoneId(zone.getId()));
            result.add(map);
        }
        return result;
    }

    public Map<String, Object> getZoneById(String id) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found"));

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", zone.getId());
        map.put("name", zone.getName());
        map.put("createdAt", zone.getCreatedAt());
        map.put("areas", areaRepository.findByZoneId(zone.getId()));
        return map;
    }

    public Zone createZone(ZoneRequest request) {
        if (zoneRepository.findByName(request.getName().trim()).isPresent()) {
            throw new BadRequestException("Zone name already exists");
        }

        Zone zone = Zone.builder()
                .name(request.getName().trim())
                .build();
        return zoneRepository.save(zone);
    }

    public Zone updateZone(String id, ZoneRequest request) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found"));

        zone.setName(request.getName().trim());
        return zoneRepository.save(zone);
    }

    public void deleteZone(String id) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found"));
        zoneRepository.delete(zone);
    }
}
