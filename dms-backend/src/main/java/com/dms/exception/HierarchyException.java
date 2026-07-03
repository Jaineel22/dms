package com.dms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class HierarchyException extends RuntimeException {

    public HierarchyException(String message) {
        super(message);
    }

    public HierarchyException(String message, Throwable cause) {
        super(message, cause);
    }
}