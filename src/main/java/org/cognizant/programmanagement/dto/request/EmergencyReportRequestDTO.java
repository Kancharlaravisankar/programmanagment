package org.cognizant.programmanagement.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.cognizant.programmanagement.Enum.EmergencyType;
import org.cognizant.programmanagement.Enum.ReportStatus;

@Data
public class EmergencyReportRequestDTO {

    @NotNull(message = "Citizen ID is required")
    private Integer citizenId;
    @NotBlank(message = "Location is required")
    private String location;
    @NotNull(message = "Type is required")
    private EmergencyType type;
    @NotNull(message = "Status is required")

    private ReportStatus status;
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;
    private String description;
}
