package com.kmaleon.repository;

import com.kmaleon.model.TransferRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransferRequestRepository extends JpaRepository<TransferRequest, UUID> {
    List<TransferRequest> findByStatusOrderByCreatedAtDesc(String status);
    List<TransferRequest> findAllByOrderByCreatedAtDesc();
}
