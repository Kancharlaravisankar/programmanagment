package org.cognizant.programmanagement.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CitizenDTO {
    private Integer citizenId;
    private String name;
    private LocalDate dob;
    private String gender; // You can use String or the Enum here
    private String address;
    private String contactInfo; // Matches your CitizenResponseDTO
    private String status;
}