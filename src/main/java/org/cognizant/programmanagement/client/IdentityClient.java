package org.cognizant.programmanagement.client;

import org.cognizant.programmanagement.config.FeignClientInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

@FeignClient(name = "USERCITIZENMANAGEMENT", configuration = FeignClientInterceptor.class)
public interface IdentityClient {

    @PostMapping("/api/logs/CreateLog")
    void saveAuditLog(@RequestBody Map<String, Object> logData);
}