package org.cognizant.programmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cognizant.programmanagement.Enum.IncidentStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentResponseDTO {

    private int incidentId;

    /**
     * In the Microservice architecture, we return a list of IDs.
     * The frontend can use these IDs to fetch full report details
     * from the Emergency Report Microservice.
     */
    private List<Integer> reportIds;

    private Integer officerId;

    private String actions;

    private IncidentStatus status;

    private LocalDateTime date;
}