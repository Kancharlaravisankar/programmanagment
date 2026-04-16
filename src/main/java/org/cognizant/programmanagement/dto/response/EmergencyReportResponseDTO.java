package org.cognizant.programmanagement.dto.response;

import lombok.Data;
import org.cognizant.programmanagement.Enum.EmergencyType;
import org.cognizant.programmanagement.Enum.ReportStatus;
// 1. Fixed the import path to match your actual Enum package



import java.time.LocalDateTime;

@Data
public class EmergencyReportResponseDTO {

    private int reportId;
    private int citizenId;
    private String location;
    private EmergencyType type;
    private ReportStatus status;
    private LocalDateTime date;
    private Double latitude;
    private Double longitude;
    private String description;
}