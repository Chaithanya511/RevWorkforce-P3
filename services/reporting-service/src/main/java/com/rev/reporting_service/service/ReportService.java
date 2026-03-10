package com.rev.reporting_service.service;

import com.rev.reporting_service.client.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final UserClient userClient;
    private final LeaveClient leaveClient;
    private final PerformanceClient performanceClient;
    private final EmployeeClient employeeClient;

    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        
        // Get counts from various services
        Object users = userClient.getAllUsers();
        Object departments = employeeClient.getAllDepartments();
        Object leaveTypes = leaveClient.getAllLeaveTypes();
        
        dashboard.put("totalUsers", users);
        dashboard.put("totalDepartments", departments);
        dashboard.put("leaveTypes", leaveTypes);
        dashboard.put("timestamp", System.currentTimeMillis());
        
        return dashboard;
    }

    public Map<String, Object> getLeaveReport(Long userId) {
        Map<String, Object> report = new HashMap<>();
        
        Object user = userClient.getUserById(userId);
        Object leaves = leaveClient.getUserLeaves(userId);
        Object balance = leaveClient.getUserBalance(userId);
        
        report.put("user", user);
        report.put("leaves", leaves);
        report.put("balance", balance);
        
        return report;
    }

    public Map<String, Object> getPerformanceReport(Long userId) {
        Map<String, Object> report = new HashMap<>();
        
        Object user = userClient.getUserById(userId);
        Object reviews = performanceClient.getUserReviews(userId);
        Object goals = performanceClient.getUserGoals(userId);
        
        report.put("user", user);
        report.put("reviews", reviews);
        report.put("goals", goals);
        
        return report;
    }

    public Map<String, Object> getEmployeeReport(Long userId) {
        Map<String, Object> report = new HashMap<>();
        
        Object user = userClient.getUserById(userId);
        Object leaves = leaveClient.getUserLeaves(userId);
        Object reviews = performanceClient.getUserReviews(userId);
        
        report.put("user", user);
        report.put("leaves", leaves);
        report.put("reviews", reviews);
        
        return report;
    }

    public Map<String, Object> getDepartmentReport(Long departmentId) {
        Map<String, Object> report = new HashMap<>();
        
        Object department = employeeClient.getDepartmentById(departmentId);
        
        report.put("department", department);
        report.put("timestamp", System.currentTimeMillis());
        
        return report;
    }
}
