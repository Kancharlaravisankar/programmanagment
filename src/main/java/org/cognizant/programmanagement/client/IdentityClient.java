package org.cognizant.programmanagement.client;

import org.cognizant.programmanagement.dto.request.UserDTO;
import org.cognizant.programmanagement.dto.request.CitizenDTO; // 1. Add this import
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "USERCITIZENMANAGEMENT", configuration = FeignClientInterceptor.class)
public interface IdentityClient {

    @PostMapping("/api/logs/CreateLog")
    void saveAuditLog(@RequestBody Map<String, Object> logData);

    @GetMapping("/api/users/getUserById/{id}")
    UserDTO getUserById(@PathVariable("id") int id);

    @GetMapping("/api/users/getAllUsers")
    List<UserDTO> allUsers();

    // 2. Add this method to resolve the Service error
    @GetMapping("/api/citizens/getCitizenById/{id}")
    CitizenDTO getCitizenById(@PathVariable("id") int id);


}