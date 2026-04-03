package org.cognizant.programmanagement.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;
import org.cognizant.programmanagement.Enum.ResourceType;
import org.cognizant.programmanagement.Enum.ResourceStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "Resource")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ResourceID")
    private int resourceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProgramID", nullable = false)
    @JsonBackReference
    private RecoveryProgram recoveryProgram;

    @Enumerated(EnumType.STRING)
    @Column(name = "Type", nullable = false)
    private ResourceType type;

    @Column(name = "Name", nullable = false)
    private String name;

    @Column(name = "Quantity")
    private double quantity;

    @Column(name = "Unit")
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private ResourceStatus status;

    @Column(name = "ReceivedBy", length = 3000)
    @Builder.Default
    private String receivedBy = "";

    // All manual getters and setters have been removed!
}