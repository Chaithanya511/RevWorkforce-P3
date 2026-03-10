package com.rev.reporting_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.Map;

@FeignClient(name = "user-service", contextId = "userServiceClient")
public interface UserServiceClient {

    @GetMapping("/api/users/directory")
    List<Map<String, Object>> getEmployeeDirectory();
}
