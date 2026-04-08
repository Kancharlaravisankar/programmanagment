package org.cognizant.programmanagement.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

@FeignClient(name = "USERCITIZENMANAGEMENT")
public interface IdentityClient {

    @PostMapping("/api/audit/log")
    void saveAuditLog(@RequestBody Map<String, Object> logData);
}