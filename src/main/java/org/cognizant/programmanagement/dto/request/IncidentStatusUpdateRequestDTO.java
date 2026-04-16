package org.cognizant.programmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IncidentStatusUpdateRequestDTO {

    @NotBlank(message = "Status is required")
    private String status;
}