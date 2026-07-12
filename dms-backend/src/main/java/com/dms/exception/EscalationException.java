package com.dms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown for invalid escalation attempts: ineligible target user,
 * an approval that has already been escalated, or no active approval to escalate.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EscalationException extends RuntimeException {

    public EscalationException(String message) {
        super(message);
    }

    public EscalationException(String message, Throwable cause) {
        super(message, cause);
    }
}