package com.lastmile.delivery.service;

import com.lastmile.delivery.entity.Area;
import com.lastmile.delivery.exception.BadRequestException;
import com.lastmile.delivery.repository.AreaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ZoneDetectorService {

    @Autowired
    private AreaRepository areaRepository;

    public Area detectZoneByPincode(String pincode) {
        if (pincode == null || pincode.trim().isEmpty()) {
            throw new BadRequestException("Pincode is required");
        }
        return areaRepository.findByPincode(pincode.trim())
                .orElseThrow(() -> new BadRequestException(
                        "No zone found for pincode \"" + pincode.trim() + "\". Please ensure it is configured in admin."));
    }
}
