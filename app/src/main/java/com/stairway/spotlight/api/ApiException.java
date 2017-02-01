package com.stairway.spotlight.api;

public class ApiException extends Throwable {

    public enum Kind {
        NETWORK,

        SESSION_NOT_FOUND,
        OTP_INVALID,

        UNEXPECTED
    }

    private final ApiException.Kind kind;
    public ApiException(String detailMessage, Throwable cause) {
        super(detailMessage, cause);
        kind = null;
    }

    public ApiException(String message, Throwable exception, ApiException.Kind kind) {
        super(message, exception);
        this.kind = kind;
    }

    public ApiException(String message, ApiException.Kind kind) {
        super(message);
        this.kind = kind;
    }

    public ApiException.Kind getKind() {
        return kind;
    }
}

