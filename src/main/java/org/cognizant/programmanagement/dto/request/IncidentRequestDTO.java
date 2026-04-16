package org.cognizant.programmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.cognizant.programmanagement.Enum.IncidentStatus;

@Data
public class IncidentRequestDTO {

    @NotNull(message = "Report ID is required")
    private Integer reportId; // Corrected to single ID for the incoming request

    @NotNull(message = "Officer ID is required")
    private Integer officerId;

    @NotBlank(message = "Actions are required")
    private String actions;

    @NotNull(message = "Status is required")
    private IncidentStatus status;
    private Double Longitude;
    private String description;
    private Double Latitude;
}