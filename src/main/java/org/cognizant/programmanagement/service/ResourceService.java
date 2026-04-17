
package org.cognizant.programmanagement.service;

import org.cognizant.programmanagement.Enum.ResourceStatus;
import org.cognizant.programmanagement.Enum.Role; // Added Role Enum
import org.cognizant.programmanagement.client.IdentityClient;
import org.cognizant.programmanagement.dao.RecoveryProgramRepository;
import org.cognizant.programmanagement.dao.ResourceRepository;
import org.cognizant.programmanagement.dto.request.ResourceRequestDTO;
import org.cognizant.programmanagement.dto.request.UserDTO;
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
        // 1. Validate Manager Role
        validateManagerRole(managerId);

        RecoveryProgram program = programRepo.findById(dto.getProgramId())
                .orElseThrow(() -> new RuntimeException("Program not found with ID: " + dto.getProgramId()));

        Resource resource = toEntity(dto);
        resource.setRecoveryProgram(program);
        resource.setStatus(ResourceStatus.ALLOCATED);

        Resource saved = resourceRepo.saveAndFlush(resource);

        sendAuditLog(managerId, "CREATE", "RESOURCE_TABLE",
                "Added " + saved.getQuantity() + " " + saved.getUnit() + " of " + saved.getName());

        return toResponseDTO(saved);
    }

    @Transactional
    public ResourceResponseDTO consumeResource(int resourceId, double amount, String receiverName, int managerId) {
        // 1. Validate Manager Role
        validateManagerRole(managerId);

        Resource res = resourceRepo.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        if (res.getQuantity() < amount) {
            throw new RuntimeException("Insufficient quantity available!");
        }

        res.setQuantity(res.getQuantity() - amount);
        String currentHistory = (res.getReceivedBy() == null) ? "" : res.getReceivedBy();
        String newEntry = receiverName + " (" + amount + " " + res.getUnit() + ")";
        res.setReceivedBy(currentHistory.isEmpty() ? newEntry : currentHistory + " | " + newEntry);
        res.setStatus(res.getQuantity() <= 0 ? ResourceStatus.CONSUMED : ResourceStatus.INUSE);

        Resource updatedResource = resourceRepo.saveAndFlush(res);

        try {
            sendAuditLog(managerId, "CONSUME", "RESOURCE_TABLE", "Allocated " + amount + " to " + receiverName);
        } catch (Exception e) {
            System.err.println("Audit Log failed.");
        }

        return toResponseDTO(updatedResource);
    }

    private void validateManagerRole(int managerId) {
        try {
            List<UserDTO> userList = identityClient.allUsers();
            boolean isValid = userList != null && userList.stream()
                    .anyMatch(u -> u.getUserId() == managerId && u.getRole() == Role.MANAGER);

            if (!isValid) {
                throw new RuntimeException("Access Denied: User ID " + managerId + " is not an authorized Manager.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Manager verification failed: " + e.getMessage());
        }
    }

    public List<ResourceResponseDTO> getAllResources() {
        return resourceRepo.findAll().stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    private ResourceResponseDTO toResponseDTO(Resource entity) {
        ResourceResponseDTO dto = new ResourceResponseDTO();
        dto.setResourceId(entity.getResourceId());
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setQuantity(entity.getQuantity());
        dto.setUnit(entity.getUnit());
        dto.setStatus(entity.getStatus());
        dto.setReceivedBy(entity.getReceivedBy());
        if (entity.getRecoveryProgram() != null) dto.setProgramId(entity.getRecoveryProgram().getProgramId());
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
        log.put("timestamp", java.time.LocalDateTime.now().toString());
        log.put("ipAddress", "127.0.0.1");
        identityClient.saveAuditLog(log);
    }
}