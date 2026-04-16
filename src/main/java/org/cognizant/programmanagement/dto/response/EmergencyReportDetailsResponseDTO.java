package org.cognizant.programmanagement.dto.response;

import lombok.Data;

@Data
public class EmergencyReportDetailsResponseDTO {

    private EmergencyReportResponseDTO report;

    private String citizenName;
    private String citizenAddress;
}