package org.cognizant.programmanagement.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDate;
import java.util.List;
import org.cognizant.programmanagement.Enum.RecoveryStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecoveryProgramResponseDTO {
    private int programId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private double budget;
    private RecoveryStatus status;

    private List<ResourceResponseDTO> resources;
}