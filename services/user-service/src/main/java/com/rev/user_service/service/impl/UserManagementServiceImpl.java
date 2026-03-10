package com.rev.user_service.service.impl;

import com.rev.user_service.dto.request.CreateUserRequest;
import com.rev.user_service.dto.request.SearchUserRequest;
import com.rev.user_service.dto.request.UpdateUserRequest;
import com.rev.user_service.dto.response.EmployeeDirectoryResponse;
import com.rev.user_service.dto.response.UserResponse;
import com.rev.user_service.entity.User;
import com.rev.user_service.exception.BadRequestException;
import com.rev.user_service.exception.ResourceNotFoundException;
import com.rev.user_service.mapper.UserMapper;
import com.rev.user_service.repository.UserRepository;
import com.rev.user_service.security.PasswordEncoderUtil;
import com.rev.user_service.service.UserManagementService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoderUtil passwordEncoder;
    private final com.rev.user_service.client.LeaveServiceClient leaveServiceClient;
    private final com.rev.user_service.client.EmployeeServiceClient employeeServiceClient;
    private final com.rev.user_service.client.ReportingServiceClient reportingServiceClient;

    public UserManagementServiceImpl(UserRepository userRepository,
                                     PasswordEncoderUtil passwordEncoder,
                                     com.rev.user_service.client.LeaveServiceClient leaveServiceClient,
                                     com.rev.user_service.client.EmployeeServiceClient employeeServiceClient,
                                     com.rev.user_service.client.ReportingServiceClient reportingServiceClient) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.leaveServiceClient = leaveServiceClient;
        this.employeeServiceClient = employeeServiceClient;
        this.reportingServiceClient = reportingServiceClient;
    }

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

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        if (request.getEmployeeId() == null || request.getEmployeeId().isBlank()) {
            List<User> allUsers = userRepository.findAll();
            long maxNumeric = 0;
            for (User u : allUsers) {
                String eid = u.getEmployeeId();
                if (eid != null && eid.startsWith("EMP")) {
                    try {
                        long val = Long.parseLong(eid.substring(3));
                        if (val > maxNumeric) maxNumeric = val;
                    } catch (Exception e) {}
                }
            }
            request.setEmployeeId("EMP" + String.format("%03d", maxNumeric + 1));
        } else if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new BadRequestException("Employee ID already exists");
        }

        User user = UserMapper.toEntity(request);
        
        if (user.getDepartmentId() != null) {
            try {
                Map<String, Object> dept = employeeServiceClient.getDepartmentById(user.getDepartmentId());
                if (dept != null && dept.containsKey("name")) {
                    user.setDepartment(dept.get("name").toString());
                }
            } catch (Exception e) {}
        }
        
        if (user.getDesignationId() != null) {
            try {
                Map<String, Object> desig = employeeServiceClient.getDesignationById(user.getDesignationId());
                if (desig != null && desig.containsKey("title")) {
                    user.setDesignation(desig.get("title").toString());
                }
            } catch (Exception e) {}
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);

        User savedUser = userRepository.save(user);
        logActivity(savedUser.getId(), "USER_CREATED", "User account created with role " + savedUser.getRole());
        
        try {
            // Casual Leave
            Map<String, Object> b1 = new HashMap<>();
            b1.put("userId", savedUser.getId());
            b1.put("leaveTypeId", 1L);
            b1.put("totalQuota", 10);
            leaveServiceClient.assignBalance(b1);
            
            // Sick Leave
            Map<String, Object> b2 = new HashMap<>();
            b2.put("userId", savedUser.getId());
            b2.put("leaveTypeId", 2L);
            b2.put("totalQuota", 15);
            leaveServiceClient.assignBalance(b2);
            
            // Paid Leave
            Map<String, Object> b3 = new HashMap<>();
            b3.put("userId", savedUser.getId());
            b3.put("leaveTypeId", 3L);
            b3.put("totalQuota", 12);
            leaveServiceClient.assignBalance(b3);
        } catch (Exception e) {
            System.err.println("Failed to assign default leaves: " + e.getMessage());
        }

        return UserMapper.toUserResponse(savedUser);
    }

    @Override
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserMapper.updateUserFromRequest(request, user);
        
        if (user.getDepartmentId() != null) {
            try {
                Map<String, Object> dept = employeeServiceClient.getDepartmentById(user.getDepartmentId());
                if (dept != null && dept.containsKey("name")) {
                    user.setDepartment(dept.get("name").toString());
                }
            } catch (Exception e) {}
        }
        
        if (user.getDesignationId() != null) {
            try {
                Map<String, Object> desig = employeeServiceClient.getDesignationById(user.getDesignationId());
                if (desig != null && desig.containsKey("title")) {
                    user.setDesignation(desig.get("title").toString());
                }
            } catch (Exception e) {}
        }

        User updatedUser = userRepository.save(user);
        logActivity(updatedUser.getId(), "PROFILE_UPDATED", "User updated their profile details");

        return UserMapper.toUserResponse(updatedUser);
    }

    @Override
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getDepartment() == null && user.getDepartmentId() != null) {
            try {
                Map<String, Object> dept = employeeServiceClient.getDepartmentById(user.getDepartmentId());
                if (dept != null && dept.containsKey("name")) {
                    user.setDepartment(dept.get("name").toString());
                }
            } catch (Exception e) {}
        }
        
        if (user.getDesignation() == null && user.getDesignationId() != null) {
            try {
                Map<String, Object> desig = employeeServiceClient.getDesignationById(user.getDesignationId());
                if (desig != null && desig.containsKey("title")) {
                    user.setDesignation(desig.get("title").toString());
                }
            } catch (Exception e) {}
        }

        User manager = null;
        if (user.getManagerId() != null) {
            manager = userRepository.findById(user.getManagerId()).orElse(null);
        }

        return UserMapper.toUserResponseEnriched(user, manager);
    }

    @Override
    public List<EmployeeDirectoryResponse> searchUsers(SearchUserRequest request) {
        List<User> users = userRepository.findAll();
        Map<Long, String> departments = fetchDepartments();
        Map<Long, String> designations = fetchDesignations();
        Map<Long, String> userNames = fetchUserNames(users);

        return users.stream()
                .filter(user -> {
                    boolean matchesName = true;
                    boolean matchesDepartment = true;
                    boolean matchesDesignation = true;

                    if (request.getName() != null && !request.getName().isBlank()) {
                        String fullName = (user.getFirstName() == null ? "" : user.getFirstName()) + " " +
                                         (user.getLastName() == null ? "" : user.getLastName());
                        matchesName = fullName.toLowerCase().contains(request.getName().toLowerCase());
                    }

                    if (request.getDepartment() != null && !request.getDepartment().isBlank()) {
                        String deptName = (user.getDepartment() != null) ? user.getDepartment() : departments.get(user.getDepartmentId());
                        matchesDepartment = deptName != null && deptName.toLowerCase().contains(request.getDepartment().toLowerCase());
                    }

                    if (request.getDesignation() != null && !request.getDesignation().isBlank()) {
                        String desigName = (user.getDesignation() != null) ? user.getDesignation() : designations.get(user.getDesignationId());
                        matchesDesignation = desigName != null && desigName.toLowerCase().contains(request.getDesignation().toLowerCase());
                    }

                    return matchesName && matchesDepartment && matchesDesignation;
                })
                .map(u -> {
                    if (u.getDepartment() == null && u.getDepartmentId() != null) {
                        u.setDepartment(departments.get(u.getDepartmentId()));
                    }
                    if (u.getDesignation() == null && u.getDesignationId() != null) {
                        u.setDesignation(designations.get(u.getDesignationId()));
                    }
                    String managerName = u.getManagerId() != null ? userNames.get(u.getManagerId()) : "None";
                    return UserMapper.toEmployeeDirectoryResponse(u, managerName);
                })
                .collect(Collectors.toList());
    }

    @Override
    public void deactivateUser(Long userId) {
        userRepository.findById(userId).ifPresent(u -> {
            u.setActive(false);
            userRepository.save(u);
        });
    }

    @Override
    public void reactivateUser(Long userId) {
        userRepository.findById(userId).ifPresent(u -> {
            u.setActive(true);
            userRepository.save(u);
        });
    }

    @Override
    public UserResponse getManager(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getManagerId() == null) throw new ResourceNotFoundException("Manager not assigned");
        User manager = userRepository.findById(user.getManagerId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
        return UserMapper.toUserResponse(manager);
    }

    @Override
    public List<EmployeeDirectoryResponse> getTeamMembers(Long managerId) {
        List<User> users = userRepository.findByManagerId(managerId);
        Map<Long, String> departments = fetchDepartments();
        Map<Long, String> designations = fetchDesignations();
        Map<Long, String> userNames = fetchUserNames(userRepository.findAll());
        return users.stream().map(u -> {
            if (u.getDepartment() == null && u.getDepartmentId() != null) u.setDepartment(departments.get(u.getDepartmentId()));
            if (u.getDesignation() == null && u.getDesignationId() != null) u.setDesignation(designations.get(u.getDesignationId()));
            String managerName = u.getManagerId() != null ? userNames.get(u.getManagerId()) : "None";
            return UserMapper.toEmployeeDirectoryResponse(u, managerName);
        }).collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDirectoryResponse> getEmployeeDirectory() {
        List<User> users = userRepository.findAll();
        Map<Long, String> departments = fetchDepartments();
        Map<Long, String> designations = fetchDesignations();
        Map<Long, String> userNames = fetchUserNames(users);
        return users.stream().map(u -> {
            if (u.getDepartment() == null && u.getDepartmentId() != null) u.setDepartment(departments.get(u.getDepartmentId()));
            if (u.getDesignation() == null && u.getDesignationId() != null) u.setDesignation(designations.get(u.getDesignationId()));
            String managerName = u.getManagerId() != null ? userNames.get(u.getManagerId()) : "None";
            return UserMapper.toEmployeeDirectoryResponse(u, managerName);
        }).collect(Collectors.toList());
    }

    @Override
    public long countByDepartmentId(Long departmentId) { return userRepository.countByDepartmentId(departmentId); }

    @Override
    public long countByDesignationId(Long designationId) { return userRepository.countByDesignationId(designationId); }

    @Override
    public UserResponse getMyProfile(Long userId) { return getUserById(userId); }

    @Override
    public UserResponse updateMyProfile(Long userId, UpdateUserRequest request) { return updateUser(userId, request); }

    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) throw new BadRequestException("Current password is incorrect");
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logActivity(userId, "PASSWORD_CHANGED", "User changed their password");
    }

    @Override
    public UserResponse assignManager(Long userId, Long managerId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (managerId != null) userRepository.findById(managerId).orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
        user.setManagerId(managerId);
        User savedUser = userRepository.save(user);
        User manager = managerId != null ? userRepository.findById(managerId).orElse(null) : null;
        return UserMapper.toUserResponseEnriched(savedUser, manager);
    }

    @Override
    public List<EmployeeDirectoryResponse> getUsersByDepartment(Long departmentId) {
        return getEmployeeDirectory().stream().filter(u -> u.getDepartmentId() != null && u.getDepartmentId().equals(departmentId)).toList();
    }

    @Override
    public List<EmployeeDirectoryResponse> getUsersByManager(Long managerId) { return getTeamMembers(managerId); }

    @Override
    public List<EmployeeDirectoryResponse> filterUsers(Long departmentId, Long designationId, Boolean active, String role) {
        return getEmployeeDirectory().stream().filter(u -> (departmentId == null || departmentId.equals(u.getDepartmentId())) &&
                (designationId == null || designationId.equals(u.getDesignationId())) &&
                (active == null || active.equals(u.isActive())) &&
                (role == null || role.equals(u.getRole()))).toList();
    }

    private Map<Long, String> fetchDepartments() {
        try { return employeeServiceClient.getAllDepartments().stream().collect(Collectors.toMap(d -> Long.valueOf(d.get("id").toString()), d -> d.get("name").toString(), (a, b) -> a)); }
        catch (Exception e) { return Map.of(); }
    }

    private Map<Long, String> fetchDesignations() {
        try { return employeeServiceClient.getAllDesignations().stream().collect(Collectors.toMap(d -> Long.valueOf(d.get("id").toString()), d -> d.get("title").toString(), (a, b) -> a)); }
        catch (Exception e) { return Map.of(); }
    }

    private Map<Long, String> fetchUserNames(List<User> users) {
        return users.stream().collect(Collectors.toMap(User::getId, u -> (u.getFirstName() != null ? u.getFirstName() : "") + (u.getLastName() != null ? " " + u.getLastName() : ""), (a, b) -> a));
    }
}