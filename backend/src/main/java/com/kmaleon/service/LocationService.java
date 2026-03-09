package com.kmaleon.service;

import com.kmaleon.dto.LocationRequest;
import com.kmaleon.dto.LocationResponse;
import com.kmaleon.exception.ResourceNotFoundException;
import com.kmaleon.model.Location;
import com.kmaleon.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class LocationService {

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public List<LocationResponse> findAll(String type) {
        List<Location> locations;
        if (type != null && !type.isBlank()) {
            locations = locationRepository.findByTypeAndActiveTrue(type);
        } else {
            locations = locationRepository.findByActiveTrue();
        }
        return locations.stream().map(LocationResponse::from).toList();
    }

    @Transactional
    public LocationResponse create(LocationRequest request) {
        Location location = new Location();
        location.setName(request.getName());
        location.setType(request.getType());
        return LocationResponse.from(locationRepository.save(location));
    }

    @Transactional
    public LocationResponse update(UUID id, LocationRequest request) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + id));
        location.setName(request.getName());
        location.setType(request.getType());
        return LocationResponse.from(locationRepository.save(location));
    }

    @Transactional
    public void delete(UUID id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + id));
        location.setActive(false);
        locationRepository.save(location);
    }
}
