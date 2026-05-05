package com.ecosync.application.exception;

public class EcoSyncException extends RuntimeException {

    private final ErrorCode errorCode;

    public EcoSyncException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
