package com.dms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown for invalid workflow states: documents already submitted, missing
 * approvers, invalid step transitions, or other workflow-execution rule violations.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class WorkflowExecutionException extends RuntimeException {

    public WorkflowExecutionException(String message) {
        super(message);
    }

    public WorkflowExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}