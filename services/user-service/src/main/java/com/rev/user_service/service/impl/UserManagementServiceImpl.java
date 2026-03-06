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

import java.util.List;
//import java.util.stream.Collectors;

@Service
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
   // private final UserMapper userMapper;
    private final PasswordEncoderUtil passwordEncoder;

    public UserManagementServiceImpl(UserRepository userRepository,
                                     PasswordEncoderUtil passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse createUser(CreateUserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new BadRequestException("Employee ID already exists");
        }

        User user = UserMapper.toEntity(request);

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);

        User savedUser = userRepository.save(user);

        return UserMapper.toUserResponse(savedUser);
    }

    @Override
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserMapper.updateUserFromRequest(request, user);

        User updatedUser = userRepository.save(user);

        return UserMapper.toUserResponse(updatedUser);
    }

    @Override
    public UserResponse getUserById(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return UserMapper.toUserResponse(user);
    }

    @Override
    public List<EmployeeDirectoryResponse> searchUsers(SearchUserRequest request) {

        List<User> users = userRepository.findAll();

        return users.stream()
                .filter(user -> {

                    boolean matchesName = true;
                    boolean matchesDepartment = true;
                    boolean matchesDesignation = true;

                    if (request.getName() != null && !request.getName().isBlank()) {

                        String fullName =
                                (user.getFirstName() == null ? "" : user.getFirstName()) + " " +
                                        (user.getLastName() == null ? "" : user.getLastName());

                        matchesName = fullName
                                .toLowerCase()
                                .contains(request.getName().toLowerCase());
                    }

                    if (request.getDepartment() != null && !request.getDepartment().isBlank()) {

                        matchesDepartment =
                                user.getDepartment() != null &&
                                        user.getDepartment()
                                                .toLowerCase()
                                                .contains(request.getDepartment().toLowerCase());
                    }

                    if (request.getDesignation() != null && !request.getDesignation().isBlank()) {

                        matchesDesignation =
                                user.getDesignation() != null &&
                                        user.getDesignation()
                                                .toLowerCase()
                                                .contains(request.getDesignation().toLowerCase());
                    }

                    return matchesName && matchesDepartment && matchesDesignation;
                })
                .map(UserMapper::toEmployeeDirectoryResponse)
                .toList();
    }

    @Override
    public void deactivateUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setActive(false);

        userRepository.save(user);
    }

    @Override
    public void reactivateUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setActive(true);

        userRepository.save(user);
    }

    @Override
    public UserResponse getManager(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getManagerId() == null) {
            throw new ResourceNotFoundException("Manager not assigned");
        }

        User manager = userRepository.findById(user.getManagerId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        return UserMapper.toUserResponse(manager);
    }

    @Override
    public List<EmployeeDirectoryResponse> getTeamMembers(Long managerId) {

        List<User> users = userRepository.findByManagerId(managerId);

        return users.stream()
                .map(UserMapper::toEmployeeDirectoryResponse)
                .toList();
    }

    @Override
    public List<EmployeeDirectoryResponse> getEmployeeDirectory() {

        List<User> users = userRepository.findAll();

        return users.stream()
                .map(UserMapper::toEmployeeDirectoryResponse)
                .toList();
    }
}