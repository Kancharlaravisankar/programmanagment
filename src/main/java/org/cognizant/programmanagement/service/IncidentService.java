package org.cognizant.programmanagement.service;

import org.cognizant.programmanagement.Enum.IncidentStatus;
import org.cognizant.programmanagement.Enum.ReportStatus;
import org.cognizant.programmanagement.Enum.Role;
import org.cognizant.programmanagement.client.IdentityClient;
import org.cognizant.programmanagement.dao.EmergencyRepository;
import org.cognizant.programmanagement.dao.IncidentRepository;
import org.cognizant.programmanagement.dto.request.IncidentRequestDTO;
import org.cognizant.programmanagement.dto.request.UserDTO;
import org.cognizant.programmanagement.dto.response.IncidentResponseDTO;
import org.cognizant.programmanagement.entity.EmergencyReport;
import org.cognizant.programmanagement.entity.Incident;
import org.cognizant.programmanagement.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepo;
    private final EmergencyRepository reportRepo;
    private final IdentityClient identityClient;

    public IncidentService(IncidentRepository incidentRepo,
                           EmergencyRepository reportRepo,
                           IdentityClient identityClient) {
        this.incidentRepo = incidentRepo;
        this.reportRepo = reportRepo;
        this.identityClient = identityClient;
    }

    @Transactional
    public IncidentResponseDTO createIncident(IncidentRequestDTO req, int officerId) {
        // 1. Validate Officer via Feign
        validateOfficer(officerId);

        // 2. Fetch and Validate the incoming Report
        EmergencyReport newReport = reportRepo.findById(req.getReportId())
                .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + req.getReportId()));

        if (newReport.getStatus() != ReportStatus.VALIDATED) {
            throw new RuntimeException("Report must be VALIDATED first before creating an incident.");
        }

        // 3. Proximity Logic: Find if there's an existing open incident nearby
        List<Incident> openIncidents = incidentRepo.findAll().stream()
                .filter(i -> i.getStatus() == IncidentStatus.OPEN)
                .collect(Collectors.toList());

        Incident matchingIncident = findNearbyIncident(newReport, openIncidents);

        Incident saved;
        if (matchingIncident != null) {
            // Group this report with the existing incident
            if (!matchingIncident.getReportIds().contains(newReport.getReportId())) {
                matchingIncident.getReportIds().add(newReport.getReportId());
            }
            saved = incidentRepo.save(matchingIncident);
            logAction("GROUP_REPORT", "Added Report " + newReport.getReportId() + " to existing Incident " + saved.getIncidentId());
        } else {
            // No nearby incident found, create a new one
            Incident incident = new Incident();
            incident.setOfficerId(officerId);
            incident.setActions(req.getActions());
            incident.setStatus(IncidentStatus.OPEN);
            incident.getReportIds().add(newReport.getReportId());
            saved = incidentRepo.save(incident);
            logAction("CREATE_INCIDENT", "Created new Incident " + saved.getIncidentId() + " for Report " + newReport.getReportId());
        }

        return toResponseDTO(saved);
    }

    private Incident findNearbyIncident(EmergencyReport newReport, List<Incident> openIncidents) {
        if (newReport.getLatitude() == null || newReport.getLongitude() == null) return null;

        for (Incident existing : openIncidents) {
            // Check proximity based on the first report that started this incident
            if (existing.getReportIds() != null && !existing.getReportIds().isEmpty()) {
                Integer firstReportId = existing.getReportIds().get(0);
                EmergencyReport anchor = reportRepo.findById(firstReportId).orElse(null);

                if (anchor != null && anchor.getLatitude() != null && anchor.getLongitude() != null) {
                    double dist = calculateDistance(newReport.getLatitude(), newReport.getLongitude(),
                            anchor.getLatitude(), anchor.getLongitude());

                    // If within 200 meters, consider it the same incident
                    if (dist <= 200) {
                        return existing;
                    }
                }
            }
        }
        return null;
    }

    private void validateOfficer(int officerId) {
        try {
            List<UserDTO> userList = identityClient.allUsers();

            if (userList == null || userList.isEmpty()) {
                throw new RuntimeException("Identity Service returned no users.");
            }

            // Check if the user exists AND has the Role.OFFICER
            boolean isValidOfficer = userList.stream()
                    .anyMatch(u -> u.getUserId() == officerId && u.getRole() == Role.OFFICER);

            if (!isValidOfficer) {
                throw new RuntimeException("Validation Failed: User ID " + officerId + " is not an authorized Officer.");
            }

        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Officer Validation Error: " + e.getMessage());

            // CRITICAL: You must re-throw the exception.
            // If you don't throw it, the @Transactional method thinks everything is fine
            // and continues to create the incident.
            throw e;
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Earth radius in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    // CRUD and Helper Methods
    @Transactional
    public IncidentResponseDTO assignOfficer(int id, int offId) {
        Incident i = incidentRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Incident not found"));
        i.setOfficerId(offId);
        return toResponseDTO(incidentRepo.save(i));
    }

    @Transactional
    public IncidentResponseDTO updateIncidentStatus(int id, String status) {
        Incident i = incidentRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Incident not found"));
        i.setStatus(IncidentStatus.valueOf(status.toUpperCase()));
        return toResponseDTO(incidentRepo.save(i));
    }

    @Transactional
    public String deleteIncident(int id) {
        Incident i = incidentRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Incident not found"));
        incidentRepo.delete(i);
        return "Deleted Incident " + id;
    }

    public List<IncidentResponseDTO> getAllIncidents() {
        return incidentRepo.findAll().stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    public IncidentResponseDTO getIncidentById(int id) {
        Incident i = incidentRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Incident not found"));
        return toResponseDTO(i);
    }

    private void logAction(String action, String details) {
        try {
            Map<String, Object> log = new HashMap<>();
            log.put("action", action);
            log.put("details", details);
            log.put("timestamp", LocalDateTime.now().toString());
            identityClient.saveAuditLog(log);
        } catch (Exception e) {
            System.err.println("Log failed: " + e.getMessage());
        }
    }

    private IncidentResponseDTO toResponseDTO(Incident entity) {
        IncidentResponseDTO dto = new IncidentResponseDTO();
        dto.setIncidentId(entity.getIncidentId());
        dto.setReportIds(entity.getReportIds());
        dto.setOfficerId(entity.getOfficerId());
        dto.setActions(entity.getActions());
        dto.setStatus(entity.getStatus());
        dto.setDate(entity.getDate());
        return dto;
    }
}