package com.greenchain.backend.controller;

import com.greenchain.backend.dto.PageResponse;
import com.greenchain.backend.model.AuditLog;
import com.greenchain.backend.model.User;
import com.greenchain.backend.repository.UserRepository;
import com.greenchain.backend.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private UserRepository userRepository;

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    @GetMapping
    public ResponseEntity<?> getAuditLogs(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Principal principal) {

        // Only admin can view all audit logs
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        if (currentUser == null || currentUser.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only administrators can view audit logs");
        }

        int pageNum = (page != null) ? Math.max(0, page) : 0;
        int pageSize = (size != null) ? Math.min(Math.max(1, size), MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;

        PageRequest pageRequest = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "timestamp"));

        Page<AuditLog> logs;
        LocalDateTime start = parseDateTime(startDate, true);
        LocalDateTime end = parseDateTime(endDate, false);

        if (username != null || entityType != null || action != null || start != null || end != null) {
            logs = auditLogService.getLogsByFilters(username, entityType, action, start, end, pageRequest);
        } else {
            logs = auditLogService.getAllLogs(pageRequest);
        }

        PageResponse<AuditLog> response = new PageResponse<>(
                logs.getContent(),
                logs.getNumber(),
                logs.getSize(),
                logs.getTotalElements()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLog> getAuditLogById(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        if (currentUser == null || currentUser.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return auditLogService.getLogById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<?> getLogsForEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        if (currentUser == null || currentUser.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(auditLogService.getLogsForEntity(entityType.toUpperCase(), entityId));
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<?> getLogsByUser(
            @PathVariable String username,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        if (currentUser == null || currentUser.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        int pageNum = (page != null) ? Math.max(0, page) : 0;
        int pageSize = (size != null) ? Math.min(Math.max(1, size), MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;

        PageRequest pageRequest = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLog> logs = auditLogService.getLogsByUsername(username, pageRequest);

        PageResponse<AuditLog> response = new PageResponse<>(
                logs.getContent(),
                logs.getNumber(),
                logs.getSize(),
                logs.getTotalElements()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getAuditStats(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
        if (currentUser == null || currentUser.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalLogs", auditLogService.getTotalLogCount());
        stats.put("supplierLogs", auditLogService.getLogCountByEntityType("SUPPLIER"));
        stats.put("shipmentLogs", auditLogService.getLogCountByEntityType("SHIPMENT"));
        stats.put("userLogs", auditLogService.getLogCountByEntityType("USER"));

        return ResponseEntity.ok(stats);
    }

    @PostMapping("/export")
    public ResponseEntity<?> logExport(
            @RequestBody Map<String, String> request,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String reportType = request.getOrDefault("reportType", "UNKNOWN");

        AuditLog log = auditLogService.logExport(
                principal.getName(),
                AuditLog.EntityType.valueOf(reportType.toUpperCase()),
                "Exported " + reportType + " report",
                null
        );

        return ResponseEntity.ok(log);
    }

    private LocalDateTime parseDateTime(String dateStr, boolean isStart) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime dt = LocalDateTime.parse(dateStr, formatter);
            if (isStart) {
                return dt;
            } else {
                // For end date, include the entire day
                return dt.plusDays(1).withHour(0).withMinute(0).withSecond(0);
            }
        } catch (Exception e) {
            return null;
        }
    }
}
