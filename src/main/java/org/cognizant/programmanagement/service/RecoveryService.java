package org.cognizant.programmanagement.service;

import org.cognizant.programmanagement.entity.RecoveryProgram;
import org.cognizant.programmanagement.dao.RecoveryProgramRepository;
import org.cognizant.programmanagement.client.IdentityClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecoveryService {

    @Autowired
    private RecoveryProgramRepository programRepository;

    @Autowired
    private IdentityClient identityClient;

    public void createProgram(RecoveryProgram program) {
        programRepository.save(program);
        logAction("CREATE_PROGRAM", "Created: " + program.getTitle());
    }

    public List<RecoveryProgram> getAllPrograms() {
        List<RecoveryProgram> programs = programRepository.findAll();
        logAction("VIEW_ALL", "Retrieved " + programs.size() + " programs");
        return programs;
    }

    public RecoveryProgram getProgramById(int id) {
        RecoveryProgram program = programRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Program not found"));
        logAction("VIEW_BY_ID", "Viewed ID: " + id);
        return program;
    }

    private void logAction(String action, String details) {
        try {
            Map<String, Object> logData = new HashMap<>();

            // 1. Get the current logged-in user's ID
            // For testing: if security context is empty, we use a default ID (e.g., 1)
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Integer userId = (principal instanceof Integer) ? (Integer) principal : 1;

            logData.put("userId", userId);
            logData.put("action", action);
            logData.put("resource", "RECOVERY_MANAGER");
            logData.put("details", details);
            logData.put("timestamp", LocalDateTime.now().toString());
            logData.put("ipAddress", "127.0.0.1");

            identityClient.saveAuditLog(logData);
            System.out.println(">>> Audit Log successfully sent to UserCitizenManagement");

        } catch (Exception e) {
            System.err.println(">>> Audit Log Failed: " + e.getMessage());
            // This prints the detailed error in your IntelliJ console
            e.printStackTrace();
        }
    }
}