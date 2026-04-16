package org.cognizant.programmanagement.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignClientInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        // Retrieve the current request attributes
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();

            // Extract the Authorization header (JWT or Basic token)
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null) {
                // Attach it to the outgoing Feign request
                template.header("Authorization", authHeader);
            }
        }
    }
}