package com.kmaleon.controller;

import com.kmaleon.dto.LocationRequest;
import com.kmaleon.dto.LocationResponse;
import com.kmaleon.security.Roles;
import com.kmaleon.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    @PreAuthorize(Roles.INVENTORY_STAFF)
    public List<LocationResponse> findAll(@RequestParam(required = false) String type) {
        return locationService.findAll(type);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Roles.INVENTORY_MANAGERS)
    public LocationResponse create(@Valid @RequestBody LocationRequest request) {
        return locationService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize(Roles.INVENTORY_MANAGERS)
    public LocationResponse update(@PathVariable UUID id,
                                   @Valid @RequestBody LocationRequest request) {
        return locationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Roles.INVENTORY_MANAGERS)
    public void delete(@PathVariable UUID id) {
        locationService.delete(id);
    }
}
