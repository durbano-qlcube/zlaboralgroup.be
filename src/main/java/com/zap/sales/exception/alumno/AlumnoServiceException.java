package com.zap.sales.exception.alumno;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AlumnoServiceException extends RuntimeException {

    private static final long serialVersionUID = 6953124712726404950L;

    public AlumnoServiceException(String message) {
        super(message);
    }

    public AlumnoServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlumnoServiceException(Throwable cause) {
        super(cause);
    }
}
