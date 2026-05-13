package com.greenchain.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.greenchain.backend.model.AuditLog;
import com.greenchain.backend.model.AuditLog.Action;
import com.greenchain.backend.model.AuditLog.EntityType;
import com.greenchain.backend.model.Supplier;
import com.greenchain.backend.model.Shipment;
import com.greenchain.backend.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    private final ObjectMapper objectMapper;

    public AuditLogService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Transactional
    public AuditLog logCreate(String username, EntityType entityType, Long entityId, Object entity, String ipAddress) {
        try {
            String newValue = objectMapper.writeValueAsString(entity);
            AuditLog log = new AuditLog(username, Action.CREATE, entityType, entityId, null, newValue,
                    String.format("Created %s with ID %d", entityType.name().toLowerCase(), entityId));
            log.setIpAddress(ipAddress);
            return auditLogRepository.save(log);
        } catch (Exception e) {
            AuditLog log = new AuditLog(username, Action.CREATE, entityType, entityId,
                    String.format("Created %s with ID %d", entityType.name().toLowerCase(), entityId));
            log.setIpAddress(ipAddress);
            return auditLogRepository.save(log);
        }
    }

    @Transactional
    public AuditLog logUpdate(String username, EntityType entityType, Long entityId,
                              Object oldEntity, Object newEntity, String ipAddress) {
        try {
            String oldValue = objectMapper.writeValueAsString(oldEntity);
            String newValue = objectMapper.writeValueAsString(newEntity);
            String description = generateUpdateDescription(entityType, oldEntity, newEntity);
            AuditLog log = new AuditLog(username, Action.UPDATE, entityType, entityId, oldValue, newValue, description);
            log.setIpAddress(ipAddress);
            return auditLogRepository.save(log);
        } catch (Exception e) {
            AuditLog log = new AuditLog(username, Action.UPDATE, entityType, entityId,
                    String.format("Updated %s with ID %d", entityType.name().toLowerCase(), entityId));
            log.setIpAddress(ipAddress);
            return auditLogRepository.save(log);
        }
    }

    @Transactional
    public AuditLog logDelete(String username, EntityType entityType, Long entityId, Object entity, String ipAddress) {
        try {
            String oldValue = objectMapper.writeValueAsString(entity);
            AuditLog log = new AuditLog(username, Action.DELETE, entityType, entityId, oldValue, null,
                    String.format("Deleted %s with ID %d", entityType.name().toLowerCase(), entityId));
            log.setIpAddress(ipAddress);
            return auditLogRepository.save(log);
        } catch (Exception e) {
            AuditLog log = new AuditLog(username, Action.DELETE, entityType, entityId,
                    String.format("Deleted %s with ID %d", entityType.name().toLowerCase(), entityId));
            log.setIpAddress(ipAddress);
            return auditLogRepository.save(log);
        }
    }

    @Transactional
    public AuditLog logExport(String username, EntityType entityType, String description, String ipAddress) {
        AuditLog log = new AuditLog(username, Action.EXPORT, entityType, null);
        log.setDescription(String.format("Exported %s report", entityType.name().toLowerCase()));
        log.setIpAddress(ipAddress);
        return auditLogRepository.save(log);
    }

    private String generateUpdateDescription(EntityType entityType, Object oldEntity, Object newEntity) {
        if (entityType == EntityType.SUPPLIER && oldEntity instanceof Supplier && newEntity instanceof Supplier) {
            Supplier oldSupplier = (Supplier) oldEntity;
            Supplier newSupplier = (Supplier) newEntity;
            StringBuilder changes = new StringBuilder("Updated supplier: ");
            if (!equalsSafe(oldSupplier.getName(), newSupplier.getName())) {
                changes.append(String.format("name '%s' -> '%s', ", oldSupplier.getName(), newSupplier.getName()));
            }
            if (!equalsSafe(oldSupplier.getCountry(), newSupplier.getCountry())) {
                changes.append(String.format("country '%s' -> '%s', ", oldSupplier.getCountry(), newSupplier.getCountry()));
            }
            if (!equalsSafe(oldSupplier.getHasEnvironmentalCertification(), newSupplier.getHasEnvironmentalCertification())) {
                changes.append(String.format("certification %s -> %s, ",
                        oldSupplier.getHasEnvironmentalCertification(), newSupplier.getHasEnvironmentalCertification()));
            }
            if (!equalsSafe(oldSupplier.getEmissionFactorPerUnit(), newSupplier.getEmissionFactorPerUnit())) {
                changes.append(String.format("emission factor %s -> %s, ",
                        oldSupplier.getEmissionFactorPerUnit(), newSupplier.getEmissionFactorPerUnit()));
            }
            if (!equalsSafe(oldSupplier.getContactEmail(), newSupplier.getContactEmail())) {
                changes.append(String.format("email '%s' -> '%s', ", oldSupplier.getContactEmail(), newSupplier.getContactEmail()));
            }
            return changes.length() > 18 ? changes.substring(0, changes.length() - 2) : "Updated supplier";
        }
        return String.format("Updated %s with ID", entityType.name().toLowerCase());
    }

    private boolean equalsSafe(Object a, Object b) {
        return (a == null && b == null) || (a != null && a.equals(b));
    }

    public Page<AuditLog> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable);
    }

    public Page<AuditLog> getLogsByUsername(String username, Pageable pageable) {
        return auditLogRepository.findByUsernameOrderByTimestampDesc(username, pageable);
    }

    public Page<AuditLog> getLogsByEntityType(String entityType, Pageable pageable) {
        return auditLogRepository.findByEntityTypeOrderByTimestampDesc(entityType, pageable);
    }

    public Page<AuditLog> getLogsByAction(String action, Pageable pageable) {
        return auditLogRepository.findByActionOrderByTimestampDesc(action, pageable);
    }

    public Page<AuditLog> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByDateRange(startDate, endDate, pageable);
    }

    public Page<AuditLog> getLogsByFilters(String username, String entityType, String action,
                                            LocalDateTime startDate, LocalDateTime endDate,
                                            Pageable pageable) {
        return auditLogRepository.findByFilters(username, entityType, action, startDate, endDate, pageable);
    }

    public List<AuditLog> getLogsForEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
    }

    public Optional<AuditLog> getLogById(Long id) {
        return auditLogRepository.findById(id);
    }

    public long getTotalLogCount() {
        return auditLogRepository.count();
    }

    public long getLogCountByUsername(String username) {
        return auditLogRepository.countByUsername(username);
    }

    public long getLogCountByEntityType(String entityType) {
        return auditLogRepository.countByEntityType(entityType);
    }
}
