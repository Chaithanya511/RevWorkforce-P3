package com.rev.leave_service.repository;

import com.rev.leave_service.entity.Leave;
import com.rev.leave_service.entity.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRepository extends JpaRepository<Leave, Long> {
    List<Leave> findByUserId(Long userId);
    List<Leave> findByManagerId(Long managerId);
    List<Leave> findByUserIdAndStatus(Long userId, LeaveStatus status);
}
