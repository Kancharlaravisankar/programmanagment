package org.cognizant.programmanagement.service;

import org.cognizant.programmanagement.Enum.ReportStatus;
import org.cognizant.programmanagement.client.IdentityClient;
import org.cognizant.programmanagement.dao.EmergencyRepository;
import org.cognizant.programmanagement.dto.request.EmergencyReportRequestDTO;
import org.cognizant.programmanagement.dto.request.CitizenDTO;
import org.cognizant.programmanagement.dto.response.EmergencyReportDetailsResponseDTO;
import org.cognizant.programmanagement.dto.response.EmergencyReportResponseDTO;
import org.cognizant.programmanagement.entity.EmergencyReport;
import org.cognizant.programmanagement.exception.ResourceNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmergencyReportService {

    private final EmergencyRepository reportRepo;
    private final IdentityClient identityClient;

    public EmergencyReportService(EmergencyRepository reportRepo, IdentityClient identityClient) {
        this.reportRepo = reportRepo;
        this.identityClient = identityClient;
    }

    /**
     * Updates report status (Manager Point)
     */
    @Transactional
    public EmergencyReportResponseDTO updateReportStatus(int id, String status) {
        EmergencyReport report = reportRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with ID: " + id));

        String oldStatus = (report.getStatus() != null) ? report.getStatus().toString() : "NULL";

        try {
            ReportStatus newStatus = ReportStatus.valueOf(status.trim().toUpperCase());
            report.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status value provided: " + status);
        }

        EmergencyReport updated = reportRepo.save(report);
        logAction("UPDATE_REPORT_STATUS", "Updated Report #" + id + " from " + oldStatus + " to " + status);

        return toResponseDTO(updated);
    }

    /**
     * CREATE REPORT
     * Now correctly validates the Citizen using Feign Client
     */
    @Transactional
    public EmergencyReportResponseDTO createReport(EmergencyReportRequestDTO req) {
        // 1. Validate Citizen via Feign Client from the Request DTO
        CitizenDTO citizen;
        try {
            // We use req.getCitizenId() because that's what comes from the frontend/caller
            citizen = identityClient.getCitizenById(req.getCitizenId());
            if (citizen == null) {
                throw new ResourceNotFoundException("Citizen not found in Identity Service");
            }
        } catch (Exception e) {
            throw new ResourceNotFoundException("Citizen validation failed in Identity Service for ID: " + req.getCitizenId());
        }

        // 2. Map DTO to Entity
        EmergencyReport report = new EmergencyReport();
        report.setCitizenId(citizen.getCitizenId()); // Use validated ID from Feign response
        report.setLocation(req.getLocation());
        report.setType(req.getType());
        report.setStatus(req.getStatus() != null ? req.getStatus() : ReportStatus.SUBMITTED);
        report.setLatitude(req.getLatitude());
        report.setLongitude(req.getLongitude());
        report.setDescription(req.getDescription());
        report.setDate(LocalDateTime.now());

        // 3. Save
        EmergencyReport saved = reportRepo.save(report);

        // 4. Audit
        logAction("CREATE_REPORT", "New report created by Citizen: " + citizen.getName() + " (ID: " + citizen.getCitizenId() + ")");

        return toResponseDTO(saved);
    }

    public List<EmergencyReportResponseDTO> getAllReports() {
        return reportRepo.findAll().stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    public EmergencyReportResponseDTO getReportById(int id) {
        EmergencyReport report = reportRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with ID: " + id));
        return toResponseDTO(report);
    }

    public EmergencyReportDetailsResponseDTO getReportWithCitizen(int id) {
        EmergencyReport report = reportRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with ID: " + id));

        // Fetch citizen details from Identity Service
        CitizenDTO citizen = identityClient.getCitizenById(report.getCitizenId());

        EmergencyReportDetailsResponseDTO details = new EmergencyReportDetailsResponseDTO();
        details.setReport(toResponseDTO(report));
        details.setCitizenName(citizen.getName());
        details.setCitizenAddress(citizen.getAddress());

        return details;
    }

    @Transactional
    public String deleteReport(int id) {
        EmergencyReport report = reportRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with ID: " + id));
        reportRepo.delete(report);
        logAction("DELETE_REPORT", "Deleted Emergency Report ID: " + id);
        return "Emergency Report deleted successfully with ID: " + id;
    }

    private void logAction(String action, String details) {
        try {
            Map<String, Object> logData = new HashMap<>();
            var auth = SecurityContextHolder.getContext().getAuthentication();

            // Logic to get the current logged-in user ID
            Object principal = (auth != null) ? auth.getPrincipal() : null;
            logData.put("userId", (principal instanceof Long) ? principal : 1L);

            logData.put("action", action);
            logData.put("resource", "REPORT_MANAGEMENT");
            logData.put("details", details);
            logData.put("timestamp", LocalDateTime.now().toString());
            logData.put("ipAddress", "127.0.0.1");

            identityClient.saveAuditLog(logData);
        } catch (Exception e) {
            System.err.println(">>> Audit Log Failed: " + e.getMessage());
        }
    }

    private EmergencyReportResponseDTO toResponseDTO(EmergencyReport entity) {
        EmergencyReportResponseDTO dto = new EmergencyReportResponseDTO();
        dto.setReportId(entity.getReportId());
        dto.setCitizenId(entity.getCitizenId());
        dto.setLocation(entity.getLocation());
        dto.setType(entity.getType());
        dto.setStatus(entity.getStatus());
        dto.setDate(entity.getDate());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setDescription(entity.getDescription());
        return dto;
    }
}