package com.rev.leave_service.service;

import com.rev.leave_service.client.NotificationServiceClient;
import com.rev.leave_service.client.UserServiceClient;
import com.rev.leave_service.entity.*;
import com.rev.leave_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveService {

    private final LeaveRepository leaveRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final HolidayRepository holidayRepository;
    private final UserServiceClient userServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final com.rev.leave_service.client.ReportingServiceClient reportingServiceClient;

    private void logActivity(Long userId, String action, String details) {
        try {
            Map<String, Object> log = new HashMap<>();
            log.put("userId", userId);
            log.put("action", action);
            log.put("details", details);
            reportingServiceClient.logActivity(log);
        } catch (Exception e) {
            System.err.println("Failed to log activity for user " + userId + ": " + e.getMessage());
        }
    }

    // Leave Application
    public Leave applyLeave(Long userId, Long leaveTypeId, LocalDate startDate, LocalDate endDate, String reason) {
        LeaveBalance balance = leaveBalanceRepository.findByUserIdAndLeaveTypeId(userId, leaveTypeId)
                .orElseThrow(() -> new RuntimeException("Leave balance not found"));
        
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (balance.getRemainingDays() < days) {
            throw new RuntimeException("Insufficient leave balance");
        }

        Leave leave = new Leave();
        leave.setUserId(userId);
        leave.setLeaveTypeId(leaveTypeId);
        leave.setStartDate(startDate);
        leave.setEndDate(endDate);
        leave.setReason(reason);
        leave.setStatus(LeaveStatus.PENDING);
        
        Leave savedLeave = leaveRepository.save(leave);

        // Notify Manager
        try {
            Map<String, Object> employeeInfo = userServiceClient.getUserById(userId);
            String employeeName = (employeeInfo != null && employeeInfo.get("name") != null) ? 
                    employeeInfo.get("name").toString() : "Employee ID: " + userId;
                    
            Map<String, Object> manager = userServiceClient.getManager(userId);
            if (manager != null && manager.containsKey("id")) {
                Long managerId = Long.valueOf(manager.get("id").toString());
                Map<String, Object> notification = new HashMap<>();
                notification.put("userId", managerId);
                notification.put("message", "New leave request from " + employeeName + " for " + startDate + " to " + endDate);
                notification.put("type", "LEAVE_APPLIED");
                notificationServiceClient.createNotification(notification);
            }
        } catch (Exception e) {
            System.err.println("Failed to notify manager for leave: " + e.getMessage());
        }

        // Notify Employee
        try {
            Map<String, Object> empNotification = new HashMap<>();
            empNotification.put("userId", userId);
            empNotification.put("message", "Your leave request for " + startDate + " to " + endDate + " has been submitted successfully");
            empNotification.put("type", "LEAVE_APPLIED_SUCCESS");
            notificationServiceClient.createNotification(empNotification);
        } catch (Exception e) {}
        
        logActivity(userId, "LEAVE_APPLIED", "Applied for leave type ID " + leaveTypeId + " from " + startDate + " to " + endDate);

        return savedLeave;
    }

    public Leave approveLeave(Long leaveId, Long managerId, String comment) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));
        
        leave.setStatus(LeaveStatus.APPROVED);
        leave.setManagerId(managerId);
        leave.setManagerComment(comment);
        
        LeaveBalance balance = leaveBalanceRepository.findByUserIdAndLeaveTypeId(leave.getUserId(), leave.getLeaveTypeId())
                .orElseThrow(() -> new RuntimeException("Leave balance not found"));
        
        long days = ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1;
        balance.setUsedDays(balance.getUsedDays() + (int) days);
        balance.setRemainingDays(balance.getTotalDays() - balance.getUsedDays());
        leaveBalanceRepository.save(balance);

        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("userId", leave.getUserId());
            notification.put("message", "Your leave request has been approved");
            notification.put("type", "LEAVE_APPROVED");
            notificationServiceClient.createNotification(notification);
        } catch (Exception e) {
            // Log but don't fail transaction
        }

        logActivity(leave.getUserId(), "LEAVE_APPROVED", "Leave request from " + leave.getStartDate() + " to " + leave.getEndDate() + " was approved");

        return leaveRepository.save(leave);
    }

    public Leave rejectLeave(Long leaveId, Long managerId, String comment) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));
        
        leave.setStatus(LeaveStatus.REJECTED);
        leave.setManagerId(managerId);
        leave.setManagerComment(comment);

        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("userId", leave.getUserId());
            notification.put("message", "Your leave request has been rejected");
            notification.put("type", "LEAVE_REJECTED");
            notificationServiceClient.createNotification(notification);
        } catch (Exception e) {
            // Log but don't fail transaction
        }

        logActivity(leave.getUserId(), "LEAVE_REJECTED", "Leave request from " + leave.getStartDate() + " to " + leave.getEndDate() + " was rejected");

        return leaveRepository.save(leave);
    }

    public void cancelLeave(Long leaveId, Long userId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));
        
        if (!leave.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Can only cancel pending leaves");
        }
        
        leave.setStatus(LeaveStatus.CANCELLED);
        leaveRepository.save(leave);
        
        // Notify Manager
        try {
            Map<String, Object> manager = userServiceClient.getManager(userId);
            if (manager != null && manager.containsKey("id")) {
                Long managerId = Long.valueOf(manager.get("id").toString());
                Map<String, Object> notification = new HashMap<>();
                notification.put("userId", managerId);
                notification.put("message", "A leave request for " + leave.getStartDate() + " to " + leave.getEndDate() + " has been cancelled");
                notification.put("type", "LEAVE_CANCELLED");
                notificationServiceClient.createNotification(notification);
            }
        } catch (Exception e) {
            System.err.println("Failed to notify manager for leave cancel: " + e.getMessage());
        }

        // Notify Employee
        try {
            Map<String, Object> empNotification = new HashMap<>();
            empNotification.put("userId", userId);
            empNotification.put("message", "Your leave request for " + leave.getStartDate() + " to " + leave.getEndDate() + " has been cancelled successfully");
            empNotification.put("type", "LEAVE_CANCELLED_SUCCESS");
            notificationServiceClient.createNotification(empNotification);
        } catch (Exception e) {
            System.err.println("Failed to notify employee for leave cancel: " + e.getMessage());
        }
        
        logActivity(userId, "LEAVE_CANCELLED", "Cancelled leave request for " + leave.getStartDate());
    }

    public List<Leave> getMyLeaves(Long userId) {
        return leaveRepository.findByUserId(userId);
    }

    public List<Leave> getTeamLeaves(Long managerId) {
        // First get team member IDs from User Service
        List<Map<String, Object>> teamMembers = userServiceClient.getTeamMembers(managerId);
        List<Long> memberIds = teamMembers.stream()
                .map(m -> Long.valueOf(m.get("id").toString()))
                .collect(Collectors.toList());
        
        // Get all leaves for these members
        List<Leave> teamLeaves = leaveRepository.findAll().stream()
                .filter(l -> memberIds.contains(l.getUserId()))
                .collect(Collectors.toList());
        
        // Also include leaves where managerId is explicitly set (approved/rejected)
        List<Leave> explicitlyAssigned = leaveRepository.findByManagerId(managerId);
        
        // Merge and unique
        Map<Long, Leave> uniqueLeaves = new HashMap<>();
        teamLeaves.forEach(l -> uniqueLeaves.put(l.getId(), l));
        explicitlyAssigned.forEach(l -> uniqueLeaves.put(l.getId(), l));
        
        return List.copyOf(uniqueLeaves.values());
    }

    // Leave Balance
    public List<LeaveBalance> getMyBalance(Long userId) {
        return leaveBalanceRepository.findByUserId(userId);
    }

    public LeaveBalance assignLeaveBalance(Long userId, Long leaveTypeId, int totalDays) {
        LeaveBalance balance = leaveBalanceRepository.findByUserIdAndLeaveTypeId(userId, leaveTypeId)
                .orElse(new LeaveBalance());
        
        balance.setUserId(userId);
        balance.setLeaveTypeId(leaveTypeId);
        balance.setTotalDays(totalDays);
        balance.setRemainingDays(totalDays - balance.getUsedDays());
        
        return leaveBalanceRepository.save(balance);
    }
    public LeaveBalance adjustLeaveBalance(Long userId, Long leaveTypeId, int adjustment, String reason) {
        LeaveBalance balance = leaveBalanceRepository.findByUserIdAndLeaveTypeId(userId, leaveTypeId)
                .orElseThrow(() -> new RuntimeException("Leave balance not found for this user and leave type"));

        balance.setTotalDays(balance.getTotalDays() + adjustment);
        balance.setRemainingDays(balance.getRemainingDays() + adjustment);

        logActivity(userId, "BALANCE_ADJUSTED", "Adjusted balance for leave type ID " + leaveTypeId + " by " + adjustment + " days. Reason: " + reason);
        
        return leaveBalanceRepository.save(balance);
    }

    // Leave Type
    public LeaveType createLeaveType(String name, int defaultQuota) {
        if (leaveTypeRepository.existsByName(name)) {
            throw new RuntimeException("Leave type already exists");
        }
        
        LeaveType leaveType = new LeaveType();
        leaveType.setName(name);
        leaveType.setDefaultQuota(defaultQuota);
        
        return leaveTypeRepository.save(leaveType);
    }

    public List<LeaveType> getAllLeaveTypes() {
        return leaveTypeRepository.findAll();
    }

    public void deleteLeaveType(Long id) {
        leaveTypeRepository.deleteById(id);
    }

    // Holiday
    public Holiday createHoliday(LocalDate date, String name, String description) {
        Holiday holiday = new Holiday();
        holiday.setHolidayDate(date);
        holiday.setName(name);
        holiday.setDescription(description);
        
        return holidayRepository.save(holiday);
    }

    public List<Holiday> getAllHolidays() {
        return holidayRepository.findAll();
    }

    public void deleteHoliday(Long id) {
        holidayRepository.deleteById(id);
    }

    public List<Leave> getAllLeaves() {
        return leaveRepository.findAll();
    }

    public List<LeaveBalance> getAllLeaveBalances() {
        return leaveBalanceRepository.findAll();
    }

    public List<Map<String, Object>> getDepartmentWiseReport(Long departmentId) {
        List<Leave> allLeaves = leaveRepository.findAll();
        Map<Long, Map<String, Object>> userMap = new HashMap<>(); // userId -> user info
        try {
            userServiceClient.getEmployeeDirectory().forEach(u -> {
                Long uid = Long.valueOf(u.get("id").toString());
                userMap.put(uid, u);
            });
        } catch (Exception e) {}

        Map<String, Map<String, Object>> deptStats = new HashMap<>(); // deptName -> stats

        for (Leave leave : allLeaves) {
            Map<String, Object> user = userMap.get(leave.getUserId());
            if (user == null) continue;
            
            String deptName = (user.get("departmentName") != null) ? user.get("departmentName").toString() : "Other";
            
            // If specific department requested, filter out others
            if (departmentId != null && departmentId > 0) {
                Long userDeptId = (user.get("departmentId") != null) ? Long.valueOf(user.get("departmentId").toString()) : -1L;
                if (!departmentId.equals(userDeptId)) continue;
            }

            deptStats.putIfAbsent(deptName, createEmptyStats(deptName));
            updateStats(deptStats.get(deptName), leave);
        }

        return new ArrayList<>(deptStats.values());
    }

    public List<Map<String, Object>> getEmployeeWiseReport(Long employeeId) {
        List<Leave> leaves = (employeeId == null || employeeId == 0) ? leaveRepository.findAll() : leaveRepository.findByUserId(employeeId);
        
        Map<Long, Map<String, Object>> userMap = new HashMap<>();
        try {
            userServiceClient.getEmployeeDirectory().forEach(u -> {
                Long uid = Long.valueOf(u.get("id").toString());
                userMap.put(uid, u);
            });
        } catch (Exception e) {}

        Map<Long, Map<String, Object>> empStats = new HashMap<>();

        for (Leave leave : leaves) {
            Long uid = leave.getUserId();
            if (!empStats.containsKey(uid)) {
                Map<String, Object> user = userMap.get(uid);
                String name = (user != null && user.get("name") != null) ? user.get("name").toString() : "User " + uid;
                String dept = (user != null && user.get("departmentName") != null) ? user.get("departmentName").toString() : "-";
                
                Map<String, Object> stats = createEmptyStats(name);
                stats.put("department", dept);
                empStats.put(uid, stats);
            }
            updateStats(empStats.get(uid), leave);
        }

        return new ArrayList<>(empStats.values());
    }

    private Map<String, Object> createEmptyStats(String name) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("name", name);
        stats.put("totalLeaves", 0);
        stats.put("approvedLeaves", 0);
        stats.put("pendingLeaves", 0);
        stats.put("rejectedLeaves", 0);
        stats.put("casualLeaves", 0);
        stats.put("sickLeaves", 0);
        stats.put("paidLeaves", 0);
        return stats;
    }

    private void updateStats(Map<String, Object> stats, Leave leave) {
        stats.put("totalLeaves", (int) stats.get("totalLeaves") + 1);
        
        String status = leave.getStatus().name();
        if ("APPROVED".equals(status)) stats.put("approvedLeaves", (int) stats.get("approvedLeaves") + 1);
        else if ("PENDING".equals(status)) stats.put("pendingLeaves", (int) stats.get("pendingLeaves") + 1);
        else if ("REJECTED".equals(status)) stats.put("rejectedLeaves", (int) stats.get("rejectedLeaves") + 1);
        
        if (leave.getLeaveTypeId() == 1) stats.put("casualLeaves", (int) stats.get("casualLeaves") + 1);
        else if (leave.getLeaveTypeId() == 2) stats.put("sickLeaves", (int) stats.get("sickLeaves") + 1);
        else if (leave.getLeaveTypeId() == 3) stats.put("paidLeaves", (int) stats.get("paidLeaves") + 1);
    }
}
