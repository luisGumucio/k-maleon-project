package com.kmaleon.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmaleon.model.AuditLog;
import com.kmaleon.repository.AuditLogRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    public void log(String action, String entityName, UUID entityId, Object payload) {
        AuditLog entry = new AuditLog();
        entry.setAction(action);
        entry.setEntityName(entityName);
        entry.setEntityId(entityId);
        entry.setPayload(toJson(payload));
        auditLogRepository.save(entry);
    }

    public List<AuditLog> findAll(String action, String entityName, OffsetDateTime from, OffsetDateTime to) {
        return auditLogRepository.findAll(buildSpec(action, entityName, from, to));
    }

    private Specification<AuditLog> buildSpec(String action, String entityName, OffsetDateTime from, OffsetDateTime to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (action != null) predicates.add(cb.equal(root.get("action"), action));
            if (entityName != null) predicates.add(cb.equal(root.get("entityName"), entityName));
            if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            if (to != null) predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
