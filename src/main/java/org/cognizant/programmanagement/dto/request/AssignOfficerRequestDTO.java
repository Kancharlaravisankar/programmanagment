package org.cognizant.programmanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignOfficerRequestDTO {

    @NotNull(message = "Officer ID is required")
    private Integer officerId;
}