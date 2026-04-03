package org.cognizant.programmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cognizant.programmanagement.Enum.ResourceStatus;
import org.cognizant.programmanagement.Enum.ResourceType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceResponseDTO {
    private int resourceId;
    private String name;
    private ResourceType type;
    private double quantity;
    private String unit;
    private ResourceStatus status;
    private String receivedBy;
    private int programId;
}