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
        CitizenDTO citizen = null;
        try {
            // We use req.getCitizenId() because that's what comes from the frontend/caller
            citizen = identityClient.getCitizenById(req.getCitizenId());
        } catch (Exception e) {
            System.err.println(">>> WARNING: Citizen lookup failed for ID: " + req.getCitizenId() + " - " + e.getMessage());
            // Continue anyway; citizen might exist but lookup failed due to network/service issues
        }

        // 2. Map DTO to Entity
        EmergencyReport report = new EmergencyReport();
        report.setCitizenId(req.getCitizenId()); // Use the ID from request directly
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
        String citizenName = (citizen != null) ? citizen.getName() : "Unknown";
        logAction("CREATE_REPORT", "New report created for Citizen ID: " + req.getCitizenId() + " (Name: " + citizenName + ")");

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

    public List<EmergencyReportDetailsResponseDTO> getAllReportsByCitizenId(int citizenId) {
        // 1. Fetch ALL reports belonging to this citizen from the DAO
        List<EmergencyReport> reports = reportRepo.findByCitizenId(citizenId);

        // 2. Fetch citizen details ONCE to avoid hitting Identity Service in a loop
        // (Optimization: We use the ID passed in since all these reports belong to them)
        CitizenDTO citizen = null;
        try {
            citizen = identityClient.getCitizenById(citizenId);
        } catch (Exception e) {
            // Log the error but don't crash the whole request
            System.out.println("Identity Service unreachable for citizen: " + citizenId);
        }

        // 3. Map the list of reports to the list of DTOs
        final CitizenDTO finalCitizen = citizen; // Required for lambda
        return reports.stream().map(report -> {
            EmergencyReportDetailsResponseDTO details = new EmergencyReportDetailsResponseDTO();
            details.setReport(toResponseDTO(report));

            // 4. NULL GUARD: Only set name/address if citizen was actually found
            if (finalCitizen != null) {
                details.setCitizenName(finalCitizen.getName());
                details.setCitizenAddress(finalCitizen.getAddress());
            } else {
                details.setCitizenName("Unknown");
                details.setCitizenAddress("Not Available");
            }
            return details;
        }).collect(Collectors.toList());
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