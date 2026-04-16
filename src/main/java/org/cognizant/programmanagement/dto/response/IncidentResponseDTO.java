package org.cognizant.programmanagement.dto.response;

import lombok.Data;
import org.cognizant.programmanagement.Enum.IncidentStatus;


import java.time.LocalDateTime;

@Data
public class IncidentResponseDTO {

    private int incidentId;
    private int reportId;
    private int officerId;
    private String actions;
    private LocalDateTime date;
    private IncidentStatus status;
}