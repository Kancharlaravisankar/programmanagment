package org.cognizant.programmanagement.service;

import org.cognizant.programmanagement.Enum.IncidentStatus;
import org.cognizant.programmanagement.Enum.ReportStatus;
import org.cognizant.programmanagement.client.IdentityClient;
import org.cognizant.programmanagement.dao.EmergencyRepository;
import org.cognizant.programmanagement.dao.IncidentRepository;
import org.cognizant.programmanagement.dto.request.IncidentRequestDTO;
import org.cognizant.programmanagement.dto.request.UserDTO;
import org.cognizant.programmanagement.dto.response.IncidentResponseDTO;
import org.cognizant.programmanagement.entity.EmergencyReport;
import org.cognizant.programmanagement.entity.Incident;
import org.cognizant.programmanagement.exception.ResourceNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        try {
            List<UserDTO> userList = identityClient.allUsers();
            if (userList != null && userList.stream().noneMatch(u -> u.getUserId() == officerId)) {
                throw new RuntimeException("Invalid Officer ID");
            }
        } catch (Exception e) {
            System.err.println("Warning: Identity Service unavailable. Skipping remote validation.");
        }

        // 2. Fetch Report
        EmergencyReport newReport = reportRepo.findById(req.getReportId())
                .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + req.getReportId()));

        if (newReport.getStatus() != ReportStatus.VALIDATED) {
            throw new RuntimeException("Report must be VALIDATED first.");
        }

        // 3. Proximity grouping logic
        List<Incident> openIncidents = incidentRepo.findAll().stream()
                .filter(i -> i.getStatus() == IncidentStatus.OPEN)
                .collect(Collectors.toList());

        Incident matchingIncident = null;
        if (newReport.getLatitude() != null && newReport.getLongitude() != null) {
            for (Incident existing : openIncidents) {
                List<Integer> ids = existing.getReportIdsAsList();
                if (!ids.isEmpty()) {
                    EmergencyReport anchor = reportRepo.findById(ids.get(0)).orElse(null);
                    if (anchor != null && anchor.getLatitude() != null && anchor.getLongitude() != null) {
                        double dist = calculateDistance(newReport.getLatitude(), newReport.getLongitude(),
                                anchor.getLatitude(), anchor.getLongitude());
                        if (dist <= 200) {
                            matchingIncident = existing;
                            break;
                        }
                    }
                }
            }
        }

        Incident saved;
        if (matchingIncident != null) {
            List<Integer> ids = matchingIncident.getReportIdsAsList();
            if (!ids.contains(newReport.getReportId())) {
                ids.add(newReport.getReportId());
                matchingIncident.setReportIdsFromList(ids);
            }
            saved = incidentRepo.save(matchingIncident);
            logAction("GROUP_REPORT", "Added Report " + req.getReportId() + " to Incident " + saved.getIncidentId());
        } else {
            Incident incident = new Incident();
            incident.setOfficerId(officerId);
            incident.setActions(req.getActions());
            incident.setStatus(IncidentStatus.OPEN);
            List<Integer> ids = new ArrayList<>();
            ids.add(newReport.getReportId());
            incident.setReportIdsFromList(ids);
            saved = incidentRepo.save(incident);
            logAction("CREATE_INCIDENT", "New Incident " + saved.getIncidentId());
        }

        return toResponseDTO(saved);
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    // CRUD Methods
    @Transactional
    public IncidentResponseDTO assignOfficer(int id, int offId) {
        Incident i = incidentRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Not found"));
        i.setOfficerId(offId);
        return toResponseDTO(incidentRepo.save(i));
    }

    @Transactional
    public IncidentResponseDTO updateIncidentStatus(int id, String status) {
        Incident i = incidentRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Not found"));
        i.setStatus(IncidentStatus.valueOf(status.toUpperCase()));
        return toResponseDTO(incidentRepo.save(i));
    }

    @Transactional
    public String deleteIncident(int id) {
        Incident i = incidentRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Not found"));
        incidentRepo.delete(i);
        return "Deleted Incident " + id;
    }

    public List<IncidentResponseDTO> getAllIncidents() {
        return incidentRepo.findAll().stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    public IncidentResponseDTO getIncidentById(int id) {
        Incident i = incidentRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Not found"));
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
            System.err.println("Audit log failed.");
        }
    }

    private IncidentResponseDTO toResponseDTO(Incident entity) {
        IncidentResponseDTO dto = new IncidentResponseDTO();
        dto.setIncidentId(entity.getIncidentId());
        List<Integer> ids = entity.getReportIdsAsList();
        if (!ids.isEmpty()) dto.setReportId(ids.get(0));
        dto.setOfficerId(entity.getOfficerId());
        dto.setActions(entity.getActions());
        dto.setStatus(entity.getStatus());
        dto.setDate(entity.getDate());
        return dto;
    }
}