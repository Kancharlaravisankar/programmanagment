package org.cognizant.programmanagement.service;

import org.cognizant.programmanagement.client.IdentityClient;
import org.cognizant.programmanagement.entity.RecoveryProgram;
import org.cognizant.programmanagement.entity.Resource;
import org.cognizant.programmanagement.dao.RecoveryProgramRepository;
import org.cognizant.programmanagement.dao.ResourceRepository;
import org.cognizant.programmanagement.Enum.ResourceStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResourceService {

    @Autowired
    private ResourceRepository resourceRepo;

    @Autowired
    private RecoveryProgramRepository programRepo;

    @Autowired
    private IdentityClient identityClient;

    @Transactional
    public void addResource(int programId, Resource resource, int managerId) {
        RecoveryProgram program = programRepo.findById(programId)
                .orElseThrow(() -> new RuntimeException("Program not found with ID: " + programId));

        resource.setRecoveryProgram(program);
        resource.setStatus(ResourceStatus.ALLOCATED);
        Resource saved = resourceRepo.save(resource);

        // Audit Log for Addition
        sendAuditLog(managerId, "CREATE", "RESOURCE_TABLE",
                "Added " + saved.getQuantity() + " " + saved.getUnit() + " of " + saved.getName());
    }

    @Transactional
    public Resource consumeResource(int resourceId, double amount, String receiverName, int managerId) {
        Resource res = resourceRepo.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        if (res.getQuantity() < amount) {
            throw new RuntimeException("Insufficient quantity available!");
        }

        res.setQuantity(res.getQuantity() - amount);

        // Update History
        String currentHistory = (res.getReceivedBy() == null) ? "" : res.getReceivedBy();
        String newEntry = receiverName + " (" + amount + " " + res.getUnit() + ")";
        res.setReceivedBy(currentHistory.isEmpty() ? newEntry : currentHistory + " | " + newEntry);

        // Update Status
        if (res.getQuantity() == 0) {
            res.setStatus(ResourceStatus.CONSUMED);
        } else {
            res.setStatus(ResourceStatus.INUSE);
        }

        Resource updatedResource = resourceRepo.save(res);

        // Audit Log for Consumption
        sendAuditLog(managerId, "CONSUME", "RESOURCE_TABLE",
                "Allocated " + amount + " " + res.getUnit() + " of " + res.getName() + " to " + receiverName);

        return updatedResource;
    }

    private void sendAuditLog(int userId, String action, String resource, String details) {
        Map<String, Object> log = new HashMap<>();
        log.put("userId", userId);
        log.put("action", action);
        log.put("resource", resource);
        log.put("details", details);

        try {
            identityClient.saveAuditLog(log);
        } catch (Exception e) {
            // Log failure locally so the main transaction completes
            System.err.println("External Audit Log Failed: " + e.getMessage());
        }
    }

    public List<Resource> getAllResources() {
        return resourceRepo.findAll();
    }
}