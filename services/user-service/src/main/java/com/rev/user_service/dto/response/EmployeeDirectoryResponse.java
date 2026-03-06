package com.rev.user_service.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDirectoryResponse {

    private Long id;

    private String employeeId;

    private String firstName;

    private String lastName;

    private String department;

    private String designation;

}