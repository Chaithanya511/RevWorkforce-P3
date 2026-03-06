package com.rev.user_service.dto.response;

import com.rev.user_service.entity.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;

    private String employeeId;

    private String email;

    private String firstName;

    private String lastName;

    private String phone;

    private String department;

    private String designation;

    private Long managerId;

    private Role role;

    private Boolean active;

}