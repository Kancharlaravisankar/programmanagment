package org.cognizant.programmanagement.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cognizant.programmanagement.Enum.Role;
import org.cognizant.programmanagement.Enum.UserStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private static final long serialVersionUID = 1L;

    private Integer userId;
    private String name;
    private Role role;
    private String email;
    private String phone;
    private UserStatus status;
    private LocalDateTime createdAt;
}
