package com.zap.sales.exception.alumno;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AlumnoAsociadoACursoException extends Exception {

    private static final long serialVersionUID = -1234567890123456789L;

    public AlumnoAsociadoACursoException(String message) {
        super(message);
    }

    public AlumnoAsociadoACursoException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlumnoAsociadoACursoException(Throwable cause) {
        super(cause);
    }
}
