package com.rev.reporting_service.service;

import com.rev.reporting_service.dto.ActivityLogResponse;
import com.rev.reporting_service.entity.ActivityLog;
import com.rev.reporting_service.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository repository;
    private final com.rev.reporting_service.client.UserServiceClient userServiceClient;

    public List<ActivityLogResponse> getAllActivities() {
        List<ActivityLog> logs = repository.findAll();
        Map<Long, Map<String, Object>> users = fetchUsers();

        return logs.stream()
                .map(log -> toResponse(log, users.get(log.getUserId())))
                .collect(Collectors.toList());
    }

    private Map<Long, Map<String, Object>> fetchUsers() {
        try {
            return userServiceClient.getEmployeeDirectory().stream()
                .collect(Collectors.toMap(
                    u -> Long.valueOf(u.get("id").toString()),
                    u -> u,
                    (existing, replacement) -> existing
                ));
        } catch (Exception e) {
            return java.util.Collections.emptyMap();
        }
    }

    public List<ActivityLogResponse> getActivitiesByUser(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void logActivity(Long userId, String action, String details) {
        ActivityLog log = new ActivityLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setDetails(details);
        repository.save(log);
    }

    private ActivityLogResponse toResponse(ActivityLog log, Map<String, Object> user) {
        ActivityLogResponse response = new ActivityLogResponse();
        response.setId(log.getId());
        response.setUserId(log.getUserId());
        response.setAction(log.getAction());
        response.setDetails(log.getDetails());
        response.setCreatedAt(log.getCreatedAt());
        
        if (user != null) {
            response.setUserName(user.get("name") != null ? user.get("name").toString() : "Unknown");
            response.setUserRole(user.get("role") != null ? user.get("role").toString() : "Unknown");
        } else {
            response.setUserName("Unknown User (ID: " + log.getUserId() + ")");
            response.setUserRole("Unknown");
        }
        
        return response;
    }

    private ActivityLogResponse toResponse(ActivityLog log) {
        return toResponse(log, null);
    }
}
