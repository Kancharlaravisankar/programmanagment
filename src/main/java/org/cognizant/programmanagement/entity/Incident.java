package org.cognizant.programmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cognizant.programmanagement.Enum.IncidentStatus;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "Incident")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int incidentId;

    @Column(nullable = false, length = 1000)
    private String actions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status;

    @CreationTimestamp
    private LocalDateTime date;

    @Column(name = "OfficerID")
    private Integer officerId;

    // The Microservice Standard: Separate table for IDs
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "incident_reports",
            joinColumns = @JoinColumn(name = "incident_id")
    )
    @Column(name = "report_id")
    private List<Integer> reportIds = new ArrayList<>();
}