package com.rev.user_service.controller;

import com.rev.user_service.dto.request.CreateUserRequest;
import com.rev.user_service.dto.request.SearchUserRequest;
import com.rev.user_service.dto.request.UpdateUserRequest;
import com.rev.user_service.dto.response.EmployeeDirectoryResponse;
import com.rev.user_service.dto.response.UserResponse;
import com.rev.user_service.service.UserManagementService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserManagementController {

    private final UserManagementService userService;

    public UserManagementController(UserManagementService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}/manager")
    public ResponseEntity<UserResponse> getManager(@PathVariable Long id) {

        return ResponseEntity.ok(userService.getManager(id));
    }

    @GetMapping("/directory")
    public ResponseEntity<List<EmployeeDirectoryResponse>> getEmployeeDirectory() {

        return ResponseEntity.ok(userService.getEmployeeDirectory());
    }

    @GetMapping("/manager/{managerId}/team")
    public ResponseEntity<List<EmployeeDirectoryResponse>> getTeamMembers(
            @PathVariable Long managerId) {

        return ResponseEntity.ok(userService.getTeamMembers(managerId));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request) {

        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {

        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping("/search")
    public ResponseEntity<List<EmployeeDirectoryResponse>> searchUsers(
            @RequestBody SearchUserRequest request) {

        return ResponseEntity.ok(userService.searchUsers(request));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {

        userService.deactivateUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivateUser(@PathVariable Long id) {

        userService.reactivateUser(id);
        return ResponseEntity.ok().build();
    }
}