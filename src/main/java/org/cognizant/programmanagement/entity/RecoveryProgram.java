package org.cognizant.programmanagement.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import org.cognizant.programmanagement.Enum.RecoveryStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "RecoveryProgram")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecoveryProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProgramID")
    private int programId;

    @Column(name = "Title", nullable = false)
    private String title;

    @Column(name = "Description")
    private String description;

    @Column(name = "ManagerID")
    private Integer managerId;

    @Column(name = "StartDate", nullable = false)
    private LocalDate startDate;

    @Column(name = "EndDate")
    private LocalDate endDate;

    @Column(name = "Budget", nullable = false)
    private double budget;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private RecoveryStatus status;

    @CreationTimestamp
    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "recoveryProgram", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    @Builder.Default // Prevents the list from being null when using the Builder
    private List<Resource> resources = new ArrayList<>();
}