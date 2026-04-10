package org.cognizant.programmanagement.client;

import org.cognizant.programmanagement.config.FeignClientInterceptor;
import org.cognizant.programmanagement.dto.request.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "USERCITIZENMANAGEMENT", configuration = FeignClientInterceptor.class)
public interface IdentityClient {

    @PostMapping("/api/logs/CreateLog")
    void saveAuditLog(@RequestBody Map<String, Object> logData);

    @GetMapping("api/users/getAllUsers")
    List<UserDTO> allUsers();
}