package com.rev.leave_service.mapper;

import com.rev.leave_service.dto.response.LeaveBalanceResponse;
import com.rev.leave_service.dto.response.LeaveResponse;
import com.rev.leave_service.entity.Leave;
import com.rev.leave_service.entity.LeaveBalance;
import com.rev.leave_service.entity.LeaveType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LeaveMapper {

    public LeaveResponse toLeaveResponse(Leave leave, Map<Long, String> leaveTypeNames, Map<Long, String> userNames) {
        LeaveResponse response = new LeaveResponse();
        response.setId(leave.getId());
        response.setUserId(leave.getUserId());
        response.setEmployeeName(userNames.getOrDefault(leave.getUserId(), "Unknown"));
        response.setLeaveTypeId(leave.getLeaveTypeId());
        response.setLeaveType(leaveTypeNames.getOrDefault(leave.getLeaveTypeId(), "Unknown"));
        response.setStartDate(leave.getStartDate());
        response.setEndDate(leave.getEndDate());
        if (leave.getStartDate() != null && leave.getEndDate() != null) {
            long d = java.time.temporal.ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1;
            response.setDays(d);
            response.setNumberOfDays(d);
        }
        response.setReason(leave.getReason());
        response.setStatus(leave.getStatus() != null ? leave.getStatus().name() : "PENDING");
        response.setManagerComment(leave.getManagerComment());
        response.setManagerId(leave.getManagerId());
        if (leave.getCreatedAt() != null) {
            String formattedDate = leave.getCreatedAt().toString();
            response.setCreatedAt(formattedDate);
            response.setAppliedDate(formattedDate);
        }
        return response;
    }

    public LeaveBalanceResponse toBalanceResponse(LeaveBalance balance, Map<Long, LeaveType> leaveTypeMap, Map<Long, String> userNames) {
        LeaveBalanceResponse response = new LeaveBalanceResponse();
        response.setId(balance.getId());
        response.setUserId(balance.getUserId());
        String name = userNames.getOrDefault(balance.getUserId(), "Unknown");
        response.setEmployeeName(name);
        response.setUserName(name);
        response.setLeaveTypeId(balance.getLeaveTypeId());
        LeaveType type = leaveTypeMap.get(balance.getLeaveTypeId());
        response.setLeaveTypeName(type != null ? type.getName() : "Unknown");
        response.setTotalQuota(balance.getTotalDays());
        response.setUsed(balance.getUsedDays());
        response.setRemaining(balance.getRemainingDays());
        return response;
    }
}
