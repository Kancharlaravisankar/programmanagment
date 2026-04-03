package org.cognizant.programmanagement.Enum;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum RecoveryStatus {
    CANCELLED, COMPLETED, PLANNED, ACTIVE, SUSPENDED;

    /*@JsonCreator
    public static RecoveryStatus fromString(String value) {
        for (RecoveryStatus status : RecoveryStatus.values()) {
            if (status.name().equalsIgnoreCase(value)) return status;
        }
        return null;
    }*/
}