package org.cognizant.programmanagement.service;

import org.cognizant.programmanagement.Enum.ResourceStatus;
import org.cognizant.programmanagement.client.IdentityClient;
import org.cognizant.programmanagement.dao.RecoveryProgramRepository;
import org.cognizant.programmanagement.dao.ResourceRepository;
import org.cognizant.programmanagement.dto.request.ResourceRequestDTO;
import org.cognizant.programmanagement.dto.response.ResourceResponseDTO;
import org.cognizant.programmanagement.entity.RecoveryProgram;
import org.cognizant.programmanagement.entity.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ResourceService {

    @Autowired
    private ResourceRepository resourceRepo;

    @Autowired
    private RecoveryProgramRepository programRepo;

    @Autowired
    private IdentityClient identityClient;

    @Transactional
    public ResourceResponseDTO addResource(ResourceRequestDTO dto, int managerId) {
        System.out.println(">>> START: addResource for Program ID: " + dto.getProgramId());

        RecoveryProgram program = programRepo.findById(dto.getProgramId())
                .orElseThrow(() -> new RuntimeException("Program not found with ID: " + dto.getProgramId()));

        Resource resource = toEntity(dto);
        resource.setRecoveryProgram(program);
        resource.setStatus(ResourceStatus.ALLOCATED);

        // Commit to DB immediately
        Resource saved = resourceRepo.saveAndFlush(resource);
        System.out.println(">>> SUCCESS: Resource saved to Database.");

        // Attempt Audit Log
        sendAuditLog(managerId, "CREATE", "RESOURCE_TABLE",
                "Added " + saved.getQuantity() + " " + saved.getUnit() + " of " + saved.getName());

        return toResponseDTO(saved);
    }

    @Transactional
    public ResourceResponseDTO consumeResource(int resourceId, double amount, String receiverName, int managerId) {
        System.out.println(">>> START: consumeResource. Resource ID: " + resourceId + ", Manager ID: " + managerId);

        Resource res = resourceRepo.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + resourceId));

        // Check availability
        if (res.getQuantity() < amount) {
            System.err.println(">>> FAILED: Insufficient quantity. Requested: " + amount + ", Available: " + res.getQuantity());
            throw new RuntimeException("Insufficient quantity available!");
        }

        // Update quantity
        res.setQuantity(res.getQuantity() - amount);

        // Update history (null-safe)
        String currentHistory = (res.getReceivedBy() == null) ? "" : res.getReceivedBy();
        String newEntry = receiverName + " (" + amount + " " + res.getUnit() + ")";
        res.setReceivedBy(currentHistory.isEmpty() ? newEntry : currentHistory + " | " + newEntry);

        // Update status
        res.setStatus(res.getQuantity() <= 0 ? ResourceStatus.CONSUMED : ResourceStatus.INUSE);

        // Save immediately to ensure local DB is updated even if external call fails
        Resource updatedResource = resourceRepo.saveAndFlush(res);
        System.out.println(">>> SUCCESS: Local Resource updated. New Quantity: " + updatedResource.getQuantity());

        // Attempt Audit Log (Isolated from main transaction)
        try {
            String logMsg = "Allocated " + amount + " " + res.getUnit() + " of " + res.getName() + " to " + receiverName;
            System.out.println(">>> INFO: Sending Audit Log for CONSUME...");
            sendAuditLog(managerId, "CONSUME", "RESOURCE_TABLE", logMsg);
        } catch (Exception e) {
            System.err.println(">>> WARNING: Audit Log failed for CONSUME, but resource was successfully updated: " + e.getMessage());
        }

        return toResponseDTO(updatedResource);
    }

    public List<ResourceResponseDTO> getAllResources() {
        return resourceRepo.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // --- Private Helper Methods ---

    private ResourceResponseDTO toResponseDTO(Resource entity) {
        ResourceResponseDTO dto = new ResourceResponseDTO();
        dto.setResourceId(entity.getResourceId());
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setQuantity(entity.getQuantity());
        dto.setUnit(entity.getUnit());
        dto.setStatus(entity.getStatus());
        dto.setReceivedBy(entity.getReceivedBy());

        if (entity.getRecoveryProgram() != null) {
            dto.setProgramId(entity.getRecoveryProgram().getProgramId());
        }
        return dto;
    }

    private Resource toEntity(ResourceRequestDTO dto) {
        Resource entity = new Resource();
        entity.setName(dto.getName());
        entity.setType(dto.getType());
        entity.setQuantity(dto.getQuantity());
        entity.setUnit(dto.getUnit());
        entity.setReceivedBy("");
        return entity;
    }

    private void sendAuditLog(int userId, String action, String resource, String details) {
        Map<String, Object> log = new HashMap<>();
        log.put("userId", userId);
        log.put("action", action);
        log.put("resource", resource);
        log.put("details", details);

        // These fields are often mandatory in your User/Identity microservice
        log.put("timestamp", java.time.LocalDateTime.now().toString());
        log.put("ipAddress", "127.0.0.1");

        try {
            identityClient.saveAuditLog(log);
            System.out.println(">>> AUDIT LOG SUCCESS: " + action);
        } catch (Exception e) {
            // Log the specific error (likely 401 Unauthorized or 500 Internal Error)
            System.err.println(">>> AUDIT LOG EXTERNAL FAILURE: " + e.getMessage());
            // We throw the exception here so the calling method's try-catch can handle it
            throw e;
        }
    }
}