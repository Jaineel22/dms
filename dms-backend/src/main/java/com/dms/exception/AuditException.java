package com.dms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown for invalid audit operations: serialization failures, unrecognized
 * entity types, or other audit-logging rule violations.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AuditException extends RuntimeException {

    public AuditException(String message) {
        super(message);
    }

    public AuditException(String message, Throwable cause) {
        super(message, cause);
    }
}