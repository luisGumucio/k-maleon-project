package com.kmaleon.repository;

import com.kmaleon.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {

    List<Location> findByActiveTrue();

    List<Location> findByTypeAndActiveTrue(String type);
}
