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
     * MANAGER VALIDATION POINT
     * Updates the status of the report. This must be 'VALIDATED' for
     * the IncidentService to allow incident creation.
     */
    @Transactional
    public EmergencyReportResponseDTO updateReportStatus(int id, String status) {
        System.out.println(">>> Request to update Report ID: " + id + " to " + status);

        EmergencyReport report = reportRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with ID: " + id));

        String oldStatus = (report.getStatus() != null) ? report.getStatus().toString() : "NULL";

        try {
            ReportStatus newStatus = ReportStatus.valueOf(status.trim().toUpperCase());
            report.setStatus(newStatus);
            System.out.println(">>> Enum converted successfully: " + newStatus);
        } catch (IllegalArgumentException e) {
            System.err.println(">>> Enum conversion failed for: " + status);
            throw new RuntimeException("Invalid status value provided.");
        }

        try {
            EmergencyReport updated = reportRepo.save(report);

            // --- AUDIT LOG ADDED ---
            logAction("UPDATE_REPORT_STATUS", "Updated Report #" + id + " from " + oldStatus + " to " + status);

            System.out.println(">>> Database save successful.");
            return toResponseDTO(updated);
        } catch (Exception e) {
            System.err.println(">>> Database save FAILED: " + e.getMessage());
            throw new RuntimeException("Database error: Ensure @Enumerated(EnumType.STRING) is on the Entity.");
        }
    }

    // CREATE REPORT (Used by Citizen)
    @Transactional
    public EmergencyReportResponseDTO createReport(EmergencyReportRequestDTO req) {
        try {
            identityClient.getCitizenById(req.getCitizenId());
        } catch (Exception e) {
            throw new ResourceNotFoundException("Citizen validation failed in Identity Service for ID: " + req.getCitizenId());
        }

        EmergencyReport report = new EmergencyReport();
        report.setCitizenId(req.getCitizenId());
        report.setLocation(req.getLocation());
        report.setType(req.getType());
        report.setStatus(req.getStatus() != null ? req.getStatus() : ReportStatus.SUBMITTED);
        report.setLatitude(req.getLatitude());
        report.setLongitude(req.getLongitude());
        report.setDescription(req.getDescription());

        EmergencyReport saved = reportRepo.save(report);

        // --- AUDIT LOG ADDED ---
        logAction("CREATE_REPORT", "New report created by Citizen ID: " + req.getCitizenId() + " at " + req.getLocation());

        return toResponseDTO(saved);
    }

    // GET ALL REPORTS
    public List<EmergencyReportResponseDTO> getAllReports() {
        return reportRepo.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // GET BY ID
    public EmergencyReportResponseDTO getReportById(int id) {
        EmergencyReport report = reportRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with ID: " + id));
        return toResponseDTO(report);
    }

    // AGGREGATED DATA: Report + Citizen Details
    public EmergencyReportDetailsResponseDTO getReportWithCitizen(int id) {
        EmergencyReport report = reportRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with ID: " + id));

        CitizenDTO citizen = identityClient.getCitizenById(report.getCitizenId());

        EmergencyReportDetailsResponseDTO details = new EmergencyReportDetailsResponseDTO();
        details.setReport(toResponseDTO(report));
        details.setCitizenName(citizen.getName());
        details.setCitizenAddress(citizen.getAddress());

        return details;
    }

    // DELETE
    @Transactional
    public String deleteReport(int id) {
        EmergencyReport report = reportRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with ID: " + id));
        reportRepo.delete(report);

        // --- AUDIT LOG ADDED ---
        logAction("DELETE_REPORT", "Deleted Emergency Report ID: " + id);

        return "Emergency Report deleted successfully with ID: " + id;
    }

    /**
     * PRIVATE HELPER: Log Action
     * Shared logic to send audit logs to the Identity microservice
     */
    private void logAction(String action, String details) {
        try {
            Map<String, Object> logData = new HashMap<>();

            var auth = SecurityContextHolder.getContext().getAuthentication();
            Long userId = 1L; // Default for testing

            if (auth != null && auth.getPrincipal() instanceof Long) {
                userId = (Long) auth.getPrincipal();
            }

            logData.put("userId", userId);
            logData.put("action", action);
            logData.put("resource", "REPORT_MANAGEMENT");
            logData.put("details", details);
            logData.put("timestamp", LocalDateTime.now().toString());
            logData.put("ipAddress", "127.0.0.1");

            identityClient.saveAuditLog(logData);
            System.out.println(">>> Report Audit Log Sent successfully: " + action);

        } catch (Exception e) {
            System.err.println(">>> Report Audit Log Failed: " + e.getLocalizedMessage());
        }
    }

    // HELPER: Entity to DTO
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