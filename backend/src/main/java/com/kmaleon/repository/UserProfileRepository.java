package com.kmaleon.repository;

import com.kmaleon.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    List<UserProfile> findByRoleIn(Set<String> roles);
}
