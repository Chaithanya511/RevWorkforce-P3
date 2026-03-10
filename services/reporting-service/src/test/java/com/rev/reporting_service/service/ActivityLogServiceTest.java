package com.rev.reporting_service.service;

import com.rev.reporting_service.client.UserServiceClient;
import com.rev.reporting_service.dto.ActivityLogResponse;
import com.rev.reporting_service.entity.ActivityLog;
import com.rev.reporting_service.repository.ActivityLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ActivityLogServiceTest {

    @Mock
    private ActivityLogRepository repository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private ActivityLogService activityLogService;

    private ActivityLog log;

    @BeforeEach
    void setUp() {
        log = new ActivityLog();
        log.setId(1L);
        log.setUserId(10L);
        log.setAction("LOGIN");
        log.setDetails("User logged in");
    }

    @Test
    void logActivity_Success() {
        activityLogService.logActivity(10L, "LOGIN", "User logged in");
        verify(repository).save(any(ActivityLog.class));
    }

    @Test
    void getActivitiesByUser_Success() {
        when(repository.findByUserIdOrderByCreatedAtDesc(10L)).thenReturn(Arrays.asList(log));

        List<ActivityLogResponse> result = activityLogService.getActivitiesByUser(10L);

        assertFalse(result.isEmpty());
        assertEquals("LOGIN", result.get(0).getAction());
    }

    @Test
    void getAllActivities_Success() {
        when(repository.findAll()).thenReturn(Arrays.asList(log));
        when(userServiceClient.getEmployeeDirectory()).thenReturn(Arrays.asList());

        List<ActivityLogResponse> result = activityLogService.getAllActivities();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }
}
