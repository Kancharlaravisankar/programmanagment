package org.cognizant.programmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.cognizant.programmanagement.Enum.IncidentStatus;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Data
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

    // Using the new column name 'ReportIDs' and TEXT to avoid all size/constraint issues
    @Column(name = "ReportIDs", columnDefinition = "TEXT")
    private String reportIdsString = "";

    @Transient
    public List<Integer> getReportIdsAsList() {
        if (reportIdsString == null || reportIdsString.isEmpty()) return new ArrayList<>();
        return Arrays.stream(reportIdsString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    public void setReportIdsFromList(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            this.reportIdsString = "";
        } else {
            this.reportIdsString = ids.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
        }
    }

    public Incident() {}
}