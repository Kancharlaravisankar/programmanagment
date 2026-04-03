package org.cognizant.programmanagement.exception;
public class RecoveryModuleException extends RuntimeException {
    private String errorCode;

    public RecoveryModuleException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
