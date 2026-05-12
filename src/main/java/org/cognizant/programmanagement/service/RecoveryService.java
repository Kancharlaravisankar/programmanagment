package org.cognizant.programmanagement.service;

import org.cognizant.programmanagement.Enum.RecoveryStatus;
import org.cognizant.programmanagement.client.IdentityClient;
import org.cognizant.programmanagement.dao.RecoveryProgramRepository;
import org.cognizant.programmanagement.dto.request.RecoveryProgramRequestDTO;
import org.cognizant.programmanagement.dto.response.RecoveryProgramResponseDTO;
import org.cognizant.programmanagement.dto.response.ResourceResponseDTO;
import org.cognizant.programmanagement.entity.RecoveryProgram;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecoveryService {

    @Autowired
    private RecoveryProgramRepository programRepository;

    @Autowired
    private IdentityClient identityClient;

    public RecoveryProgramResponseDTO createProgram(RecoveryProgramRequestDTO dto) {
        // 1. Convert Request DTO to Entity
        RecoveryProgram entity = toEntity(dto);

        // 2. Save to Database
        RecoveryProgram savedEntity = programRepository.save(entity);

        // 3. Log the action
        logAction("CREATE_PROGRAM", "Created Program: " + savedEntity.getTitle());

        // 4. Return the Response DTO
        return toResponseDTO(savedEntity);
    }

    public List<RecoveryProgramResponseDTO> getAllPrograms() {
        List<RecoveryProgram> entities = programRepository.findAll();
        logAction("VIEW_ALL", "Retrieved " + entities.size() + " programs");

        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public RecoveryProgramResponseDTO getProgramById(int id) {
        RecoveryProgram entity = programRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Program not found with ID: " + id));

        logAction("VIEW_BY_ID", "Viewed Program ID: " + id);
        return toResponseDTO(entity);
    }
    public RecoveryProgramResponseDTO updateProgramStatus(int id, String statusStr) {
        // 1. Find the existing program
        RecoveryProgram entity = programRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Program not found with ID: " + id));

        // 2. Convert String to Enum (Case-insensitive)
        try {
            RecoveryStatus newStatus = RecoveryStatus.valueOf(statusStr.toUpperCase());
            entity.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status value: " + statusStr);
        }

        // 3. Save the updated entity
        RecoveryProgram updatedEntity = programRepository.save(entity);

        // 4. Log the action in the Audit Log
        logAction("UPDATE_STATUS", "Changed Program ID " + id + " status to " + statusStr);

        // 5. Return the updated DTO
        return toResponseDTO(updatedEntity);
    }

    // --- Private Helper Methods for Mapping ---

    private RecoveryProgramResponseDTO toResponseDTO(RecoveryProgram entity) {
        RecoveryProgramResponseDTO res = new RecoveryProgramResponseDTO();
        res.setProgramId(entity.getProgramId());
        res.setTitle(entity.getTitle());
        res.setDescription(entity.getDescription());
        res.setStartDate(entity.getStartDate());
        res.setEndDate(entity.getEndDate());
        res.setBudget(entity.getBudget());
        res.setStatus(entity.getStatus());

        if (entity.getResources() != null) {
            res.setResources(entity.getResources().stream().map(resEntity -> {
                ResourceResponseDTO rDto = new ResourceResponseDTO();
                rDto.setResourceId(resEntity.getResourceId());
                rDto.setName(resEntity.getName());
                rDto.setType(resEntity.getType());
                rDto.setQuantity(resEntity.getQuantity());
                rDto.setUnit(resEntity.getUnit());
                rDto.setStatus(resEntity.getStatus());
                rDto.setReceivedBy(resEntity.getReceivedBy());
                return rDto;
            }).collect(Collectors.toList()));
        }
        return res;
    }

    private RecoveryProgram toEntity(RecoveryProgramRequestDTO dto) {
        RecoveryProgram entity = new RecoveryProgram();
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setBudget(dto.getBudget());
        // Defaulting status for new programs
        entity.setStatus(RecoveryStatus.PLANNED);
        return entity;
    }

    private void logAction(String action, String details) {
        try {
            Map<String, Object> logData = new HashMap<>();

            // 1. Get the current authentication
            var auth = SecurityContextHolder.getContext().getAuthentication();

            // 2. Logic to get User ID (Verify how your JWT stores the ID)
            // If your JWT filter puts the ID in the principal, use it.
            // Otherwise, you might need to cast 'auth.getPrincipal()' to your User object.
            Long userId = 1L; // Use Long (1L) instead of Integer to match standard JPA IDs

            if (auth != null && auth.getPrincipal() instanceof Long) {
                userId = (Long) auth.getPrincipal();
            }

            // 3. Populate Map with keys EXACTLY matching AuditLogRequestDTO
            logData.put("userId", userId);
            logData.put("action", action);
            logData.put("resource", "RECOVERY_MANAGER");
            logData.put("details", details);

            // 4. Use ISO_DATE_TIME format (Standard for @Valid and JSON)
            logData.put("timestamp", LocalDateTime.now().toString());

            logData.put("ipAddress", "127.0.0.1"); // In production, get this from HttpServletRequest

            identityClient.saveAuditLog(logData);
            System.out.println(">>> Audit Log Sent successfully: " + action);

        } catch (Exception e) {
            // This will now print the full error to help you debug
            System.err.println(">>> Audit Log Failed: " + e.getLocalizedMessage());
            e.printStackTrace();
        }

    }
}