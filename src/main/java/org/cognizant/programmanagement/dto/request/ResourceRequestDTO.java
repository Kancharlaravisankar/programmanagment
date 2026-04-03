package org.cognizant.programmanagement.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.cognizant.programmanagement.Enum.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceRequestDTO {

    private int programId;

    @NotBlank(message = "Resource name is required")

    @Pattern(regexp = "^(?=.*[a-zA-Z])[a-zA-Z0-9\\s]*$",
            message = "Resource name must contain letters (e.g., 'N95 Masks'), not just numbers.")
    private String name;

    private ResourceType type;

    @Positive(message = "Quantity must be greater than zero")
    private double quantity;

    @NotBlank(message = "Unit is required")
    @Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Unit must be letters only (e.g., Kg, Liters, Boxes)")
    private String unit;
}