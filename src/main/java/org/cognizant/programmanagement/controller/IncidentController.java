package org.cognizant.programmanagement.controller;

import jakarta.validation.Valid;
import org.cognizant.programmanagement.dto.request.AssignOfficerRequestDTO;
import org.cognizant.programmanagement.dto.request.IncidentRequestDTO;
import org.cognizant.programmanagement.dto.request.IncidentStatusUpdateRequestDTO;
import org.cognizant.programmanagement.dto.response.IncidentResponseDTO;
import org.cognizant.programmanagement.service.IncidentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService service;

    public IncidentController(IncidentService service) {
        this.service = service;
    }

    /**
     * POST /api/incidents/createincident
     * Pass the officerId from the DTO to the service for Identity validation
     */
    @PostMapping("/createincident")
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentResponseDTO createIncident(@Valid @RequestBody IncidentRequestDTO requestDTO) {
        // Passing requestDTO and the officerId extracted from it
        return service.createIncident(requestDTO, requestDTO.getOfficerId());
    }

    // GET ALL INCIDENTS
    @GetMapping("/getallincident")
    public List<IncidentResponseDTO> getAllIncidents() {
        return service.getAllIncidents();
    }

    // GET INCIDENT BY ID
    @GetMapping("/getincidentbyid/{id}")
    public IncidentResponseDTO getIncidentById(@PathVariable int id) {
        return service.getIncidentById(id);
    }

    // UPDATE INCIDENT STATUS
    @PutMapping("/updateincident/{id}/status")
    public IncidentResponseDTO updateStatus(
            @PathVariable int id,
            @RequestBody IncidentStatusUpdateRequestDTO statusRequest) {
        return service.updateIncidentStatus(id, statusRequest.getStatus());
    }

    /**
     * PUT /api/incidents/assignofficer/{id}/assign-officer
     * Reassigns an incident to a new officer after validating them via Feign
     */
    @PutMapping("/assignofficer/{id}/assign-officer")
    public IncidentResponseDTO assignOfficer(
            @PathVariable int id,
            @RequestBody AssignOfficerRequestDTO requestDTO) {
        return service.assignOfficer(id, requestDTO.getOfficerId());
    }

    // DELETE INCIDENT
    @DeleteMapping("/delete/{id}")
    public String deleteIncident(@PathVariable int id) {
        return service.deleteIncident(id);
    }
}