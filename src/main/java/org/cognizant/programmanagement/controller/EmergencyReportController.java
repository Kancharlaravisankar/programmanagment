package org.cognizant.programmanagement.controller;

import jakarta.validation.Valid;
import org.cognizant.programmanagement.dto.request.EmergencyReportRequestDTO;
import org.cognizant.programmanagement.dto.response.EmergencyReportDetailsResponseDTO;
import org.cognizant.programmanagement.dto.response.EmergencyReportResponseDTO;
import org.cognizant.programmanagement.service.EmergencyReportService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class EmergencyReportController {

    private final EmergencyReportService service;

    public EmergencyReportController(EmergencyReportService service) {
        this.service = service;
    }

    /**
     * POST /api/reports
     * Replaces /createreport
     */
    @PostMapping("/createReport")
    @ResponseStatus(HttpStatus.CREATED)
    public EmergencyReportResponseDTO createReport(@Valid @RequestBody EmergencyReportRequestDTO requestDTO) {
        return service.createReport(requestDTO);
    }

    /**
     * GET /api/reports
     * Replaces /getallreports - Calling /api/reports will now work!
     */
    @GetMapping("/getallreports")
    public List<EmergencyReportResponseDTO> getAllReports() {
        return service.getAllReports();
    }

    /**
     * GET /api/reports/{id}
     * Replaces /getreportbyid/{id}
     */
    @GetMapping("/{id}")
    public EmergencyReportResponseDTO getReportById(@PathVariable int id) {
        return service.getReportById(id);
    }

    /**
     * GET /api/reports/{id}/details
     * Replaces /getreportwithcitizendetails/{id}/details
     */
    @GetMapping("/{id}/details")
    public EmergencyReportDetailsResponseDTO getReportWithCitizen(@PathVariable int id) {
        return service.getReportWithCitizen(id);
    }
    // UPDATE EMERGENCY REPORT STATUS
    @PutMapping("/update-status/{id}")
    public EmergencyReportResponseDTO updateReportStatus(
            @PathVariable int id,
            @RequestParam String status) { // e.g., VALIDATED, REJECTED

        return service.updateReportStatus(id, status);
    }
    /**
     * DELETE /api/reports/{id}
     * Replaces /delete/{id}
     */
    @DeleteMapping("/delete/{id}")
    public String deleteReport(@PathVariable int id) {
        return service.deleteReport(id);
    }
}