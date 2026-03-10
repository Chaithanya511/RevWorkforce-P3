package com.rev.leave_service.controller;

import com.rev.leave_service.dto.response.LeaveBalanceResponse;
import com.rev.leave_service.dto.response.LeaveResponse;
import com.rev.leave_service.entity.*;
import com.rev.leave_service.mapper.LeaveMapper;
import com.rev.leave_service.repository.LeaveTypeRepository;
import com.rev.leave_service.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;
    private final LeaveMapper leaveMapper;
    private final LeaveTypeRepository leaveTypeRepository;
    private final com.rev.leave_service.client.UserServiceClient userServiceClient;

    // Helper to build userId -> fullName map
    private Map<Long, String> getUserNames() {
        try {
            return userServiceClient.getEmployeeDirectory().stream()
                .collect(Collectors.toMap(
                    u -> Long.valueOf(u.get("id").toString()),
                    u -> {
                        if (u.containsKey("name")) return u.get("name").toString();
                        String f = u.getOrDefault("firstName", "").toString();
                        String l = u.getOrDefault("lastName", "").toString();
                        return (f + " " + l).trim();
                    },
                    (existing, replacement) -> existing
                ));
        } catch (Exception e) {
            System.err.println("Error fetching user names: " + e.getMessage());
            return Map.of();
        }
    }

    // Helper to build leaveTypeId -> name map
    private Map<Long, String> getLeaveTypeNames() {
        return leaveTypeRepository.findAll().stream()
                .collect(Collectors.toMap(LeaveType::getId, LeaveType::getName));
    }

    // Helper to build leaveTypeId -> LeaveType map
    private Map<Long, LeaveType> getLeaveTypeMap() {
        return leaveTypeRepository.findAll().stream()
                .collect(Collectors.toMap(LeaveType::getId, lt -> lt));
    }

    @PostMapping("/apply")
    public ResponseEntity<LeaveResponse> applyLeave(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        Long leaveTypeId = Long.valueOf(request.get("leaveTypeId").toString());
        LocalDate startDate = LocalDate.parse(request.get("startDate").toString());
        LocalDate endDate = LocalDate.parse(request.get("endDate").toString());
        String reason = request.get("reason").toString();

        Leave leave = leaveService.applyLeave(userId, leaveTypeId, startDate, endDate, reason);
        return ResponseEntity.ok(leaveMapper.toLeaveResponse(leave, getLeaveTypeNames(), getUserNames()));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<LeaveResponse> approveLeave(@PathVariable("id") Long id, @RequestBody Map<String, Object> request) {
        Long managerId = Long.valueOf(request.get("managerId").toString());
        String comment = request.getOrDefault("comment", "").toString();

        Leave leave = leaveService.approveLeave(id, managerId, comment);
        return ResponseEntity.ok(leaveMapper.toLeaveResponse(leave, getLeaveTypeNames(), getUserNames()));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<LeaveResponse> rejectLeave(@PathVariable("id") Long id, @RequestBody Map<String, Object> request) {
        Long managerId = Long.valueOf(request.get("managerId").toString());
        String comment = request.getOrDefault("comment", "").toString();

        Leave leave = leaveService.rejectLeave(id, managerId, comment);
        return ResponseEntity.ok(leaveMapper.toLeaveResponse(leave, getLeaveTypeNames(), getUserNames()));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelLeave(@PathVariable("id") Long id, @RequestParam("userId") Long userId) {
        leaveService.cancelLeave(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LeaveResponse>> getMyLeaves(@PathVariable("userId") Long userId) {
        Map<Long, String> names = getLeaveTypeNames();
        Map<Long, String> userNames = getUserNames();
        List<LeaveResponse> result = leaveService.getMyLeaves(userId).stream()
                .map(l -> leaveMapper.toLeaveResponse(l, names, userNames))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/manager/{managerId}/team")
    public ResponseEntity<List<LeaveResponse>> getTeamLeaves(@PathVariable("managerId") Long managerId) {
        Map<Long, String> names = getLeaveTypeNames();
        Map<Long, String> userNames = getUserNames();
        List<LeaveResponse> result = leaveService.getTeamLeaves(managerId).stream()
                .map(l -> leaveMapper.toLeaveResponse(l, names, userNames))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/balance/{userId}")
    public ResponseEntity<List<LeaveBalanceResponse>> getMyBalance(@PathVariable("userId") Long userId) {
        Map<Long, LeaveType> typeMap = getLeaveTypeMap();
        Map<Long, String> userNames = getUserNames();
        List<LeaveBalanceResponse> result = leaveService.getMyBalance(userId).stream()
                .map(b -> leaveMapper.toBalanceResponse(b, typeMap, userNames))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/balance")
    public ResponseEntity<LeaveBalance> assignBalance(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        Long leaveTypeId = Long.valueOf(request.get("leaveTypeId").toString());

        if (request.containsKey("adjustment")) {
            int adjustment = Integer.parseInt(request.get("adjustment").toString());
            String reason = request.getOrDefault("reason", "Administrative adjustment").toString();
            return ResponseEntity.ok(leaveService.adjustLeaveBalance(userId, leaveTypeId, adjustment, reason));
        }

        int totalDays = Integer.parseInt(request.getOrDefault("totalDays",
                request.getOrDefault("totalQuota", "0")).toString());

        return ResponseEntity.ok(leaveService.assignLeaveBalance(userId, leaveTypeId, totalDays));
    }

    @PostMapping("/types")
    public ResponseEntity<LeaveType> createLeaveType(@RequestBody Map<String, Object> request) {
        String name = request.get("name").toString();
        int defaultQuota = Integer.parseInt(request.getOrDefault("defaultQuota",
                request.getOrDefault("maxDaysPerYear", "0")).toString());

        return ResponseEntity.ok(leaveService.createLeaveType(name, defaultQuota));
    }

    @GetMapping("/types")
    public ResponseEntity<List<LeaveType>> getAllLeaveTypes() {
        return ResponseEntity.ok(leaveService.getAllLeaveTypes());
    }

    @DeleteMapping("/types/{id}")
    public ResponseEntity<Void> deleteLeaveType(@PathVariable("id") Long id) {
        leaveService.deleteLeaveType(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/holidays")
    public ResponseEntity<Holiday> createHoliday(@RequestBody Map<String, Object> request) {
        LocalDate date = LocalDate.parse(request.get("holidayDate").toString());
        String name = request.get("name").toString();
        String description = request.getOrDefault("description", "").toString();

        return ResponseEntity.ok(leaveService.createHoliday(date, name, description));
    }

    @GetMapping("/holidays")
    public ResponseEntity<List<Holiday>> getAllHolidays() {
        return ResponseEntity.ok(leaveService.getAllHolidays());
    }

    @DeleteMapping("/holidays/{id}")
    public ResponseEntity<Void> deleteHoliday(@PathVariable("id") Long id) {
        leaveService.deleteHoliday(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<LeaveResponse>> getAllLeaves() {
        Map<Long, String> names = getLeaveTypeNames();
        Map<Long, String> userNames = getUserNames();
        List<LeaveResponse> result = leaveService.getAllLeaves().stream()
                .map(l -> leaveMapper.toLeaveResponse(l, names, userNames))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/all-balances")
    public ResponseEntity<List<LeaveBalanceResponse>> getAllLeaveBalances() {
        Map<Long, LeaveType> typeMap = getLeaveTypeMap();
        Map<Long, String> userNames = getUserNames();
        List<LeaveBalanceResponse> result = leaveService.getAllLeaveBalances().stream()
                .map(b -> leaveMapper.toBalanceResponse(b, typeMap, userNames))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/report/department/{departmentId}")
    public ResponseEntity<List<Map<String, Object>>> getDepartmentWiseReport(@PathVariable("departmentId") Long departmentId) {
        return ResponseEntity.ok(leaveService.getDepartmentWiseReport(departmentId));
    }

    @GetMapping("/report/employee/{employeeId}")
    public ResponseEntity<List<Map<String, Object>>> getEmployeeWiseReport(@PathVariable("employeeId") Long employeeId) {
        return ResponseEntity.ok(leaveService.getEmployeeWiseReport(employeeId));
    }
}
