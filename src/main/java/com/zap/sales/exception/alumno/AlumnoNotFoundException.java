package com.zap.sales.exception.alumno;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AlumnoNotFoundException extends Exception {

    private static final long serialVersionUID = -1234567890123456789L;

    public AlumnoNotFoundException(String message) {
        super(message);
    }

    public AlumnoNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlumnoNotFoundException(Throwable cause) {
        super(cause);
    }
}
