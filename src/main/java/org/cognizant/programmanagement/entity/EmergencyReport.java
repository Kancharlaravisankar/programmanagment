package org.cognizant.programmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.cognizant.programmanagement.Enum.EmergencyType;
import org.cognizant.programmanagement.Enum.ReportStatus;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "EmergencyReport")
public class EmergencyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int reportId;

    @Column(nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50)
    private EmergencyType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private ReportStatus status;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    private LocalDateTime date;

    @Column(name = "CitizenID", nullable = false)
    private Integer citizenId;

    // REMOVED: The @OneToMany list of Incidents is gone because we
    // are storing IDs manually in the Incident table now.

    public EmergencyReport() {}
}