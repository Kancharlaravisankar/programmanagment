package org.cognizant.programmanagement.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecoveryProgramRequestDTO {

    @NotBlank(message = "Title is mandatory")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")

    @Pattern(regexp = "^(?=.*[a-zA-Z])[a-zA-Z0-9\\s]*$",
            message = "Title can include numbers (e.g., 'Flood 2026'), but cannot be numbers only.")
    private String title;

    @NotBlank(message = "Description is mandatory")

    @Pattern(regexp = "^(?=.*[a-zA-Z])[a-zA-Z0-9\\s\\.,]*$",
            message = "Description must contain text, not just numbers.")
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;

    @Positive(message = "Budget must be greater than zero")
    private double budget;
}