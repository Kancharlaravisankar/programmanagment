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
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // Extracting user info from SecurityContext
            Integer userId = (principal instanceof Integer) ? (Integer) principal : 1;

            logData.put("userId", userId);
            logData.put("action", action);
            logData.put("resource", "RECOVERY_MANAGER");
            logData.put("details", details);
            logData.put("timestamp", LocalDateTime.now().toString());
            logData.put("ipAddress", "127.0.0.1");

            identityClient.saveAuditLog(logData);
            System.out.println(">>> Audit Log Sent: " + action);

        } catch (Exception e) {
            System.err.println(">>> Audit Log Failed: " + e.getMessage());
        }
    }
}