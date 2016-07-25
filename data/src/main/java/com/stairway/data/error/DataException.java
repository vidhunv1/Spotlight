package com.stairway.data.error;

/**
 * Created by vidhun on 26/07/16.
 */
public class DataException extends Throwable {

    public enum Kind {
        NETWORK,

        SESSION_NOT_FOUND,
        OTP_INVALID,

        UNEXPECTED
    }

    private final Kind kind;
    public DataException(String detailMessage, Throwable cause) {
        super(detailMessage, cause);
        kind = null;
    }

    public DataException(String message, Throwable exception, Kind kind) {
        super(message, exception);
        this.kind = kind;
    }

    public DataException(String message, Kind kind) {
        super(message);
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }
}
