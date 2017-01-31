package com.stairway.spotlight;

public class DataException extends Throwable {

    public enum Kind {
        NETWORK,

        SESSION_NOT_FOUND,
        OTP_INVALID,

        UNEXPECTED
    }

    private final DataException.Kind kind;
    public DataException(String detailMessage, Throwable cause) {
        super(detailMessage, cause);
        kind = null;
    }

    public DataException(String message, Throwable exception, DataException.Kind kind) {
        super(message, exception);
        this.kind = kind;
    }

    public DataException(String message, DataException.Kind kind) {
        super(message);
        this.kind = kind;
    }

    public DataException.Kind getKind() {
        return kind;
    }
}

