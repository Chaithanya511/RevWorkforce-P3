package com.rev.reporting_service.controller;

import com.rev.reporting_service.dto.ActivityLogResponse;
import com.rev.reporting_service.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping
    public ResponseEntity<List<ActivityLogResponse>> getAllActivities() {
        return ResponseEntity.ok(activityLogService.getAllActivities());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<ActivityLogResponse>> getActivitiesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(activityLogService.getActivitiesByUser(userId));
    }

    @PostMapping
    public ResponseEntity<Void> logActivity(@RequestBody java.util.Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        String action = request.get("action").toString();
        String details = request.getOrDefault("details", "").toString();
        activityLogService.logActivity(userId, action, details);
        return ResponseEntity.ok().build();
    }
}
