package com.rev.user_service.mapper;

import com.rev.user_service.dto.request.CreateUserRequest;
import com.rev.user_service.dto.request.UpdateUserRequest;
import com.rev.user_service.dto.response.EmployeeDirectoryResponse;
import com.rev.user_service.dto.response.UserResponse;
import com.rev.user_service.entity.User;

public class UserMapper {

    public static User toEntity(CreateUserRequest request) {

        return User.builder()
                .employeeId(request.getEmployeeId())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(request.getPassword())
                .phone(request.getPhone())
                .department(request.getDepartment())
                .designation(request.getDesignation())
                .managerId(request.getManagerId())
                .role(request.getRole())
                .active(true)
                .build();
    }

    public static UserResponse toUserResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .employeeId(user.getEmployeeId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .department(user.getDepartment())
                .designation(user.getDesignation())
                .managerId(user.getManagerId())
                .role(user.getRole())
                .active(user.getActive())
                .build();
    }

    public static EmployeeDirectoryResponse toEmployeeDirectoryResponse(User user) {

        return EmployeeDirectoryResponse.builder()
                .id(user.getId())
                .employeeId(user.getEmployeeId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .department(user.getDepartment())
                .designation(user.getDesignation())
                .build();
    }

    public static void updateUserFromRequest(UpdateUserRequest request, User user) {

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment());
        }

        if (request.getDesignation() != null) {
            user.setDesignation(request.getDesignation());
        }

        if (request.getManagerId() != null) {
            user.setManagerId(request.getManagerId());
        }
    }
}