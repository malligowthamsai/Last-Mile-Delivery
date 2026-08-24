package com.lastmile.delivery.service;

import com.lastmile.delivery.dto.AreaRequest;
import com.lastmile.delivery.entity.Area;
import com.lastmile.delivery.entity.Zone;
import com.lastmile.delivery.exception.BadRequestException;
import com.lastmile.delivery.exception.ResourceNotFoundException;
import com.lastmile.delivery.repository.AreaRepository;
import com.lastmile.delivery.repository.ZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AreaService {

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    public List<Area> listAreas(String zoneId) {
        if (zoneId != null && !zoneId.trim().isEmpty()) {
            return areaRepository.findByZoneId(zoneId.trim());
        }
        return areaRepository.findAll();
    }

    public Area lookupByPincode(String pincode) {
        return areaRepository.findByPincode(pincode.trim())
                .orElseThrow(() -> new ResourceNotFoundException("No zone found for pincode " + pincode.trim()));
    }

    public Area createArea(AreaRequest request) {
        if (areaRepository.findByPincode(request.getPincode().trim()).isPresent()) {
            throw new BadRequestException("Pincode already mapped");
        }

        Zone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new BadRequestException("Zone not found"));

        Area area = Area.builder()
                .name(request.getName().trim())
                .pincode(request.getPincode().trim())
                .zone(zone)
                .build();

        return areaRepository.save(area);
    }

    public Area updateArea(String id, AreaRequest request) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Area not found"));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            area.setName(request.getName().trim());
        }

        if (request.getPincode() != null && !request.getPincode().trim().isEmpty()) {
            String newPin = request.getPincode().trim();
            if (!newPin.equals(area.getPincode()) && areaRepository.findByPincode(newPin).isPresent()) {
                throw new BadRequestException("Pincode already mapped");
            }
            area.setPincode(newPin);
        }

        if (request.getZoneId() != null && !request.getZoneId().trim().isEmpty()) {
            Zone zone = zoneRepository.findById(request.getZoneId().trim())
                    .orElseThrow(() -> new BadRequestException("Zone not found"));
            area.setZone(zone);
        }

        return areaRepository.save(area);
    }

    public void deleteArea(String id) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Area not found"));
        areaRepository.delete(area);
    }
}
