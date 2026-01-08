package com.zap.sales.exception.alumno;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TrabajadorNotFoundException extends Exception {

    private static final long serialVersionUID = -1234567890123456789L;

    public TrabajadorNotFoundException(String message) {
        super(message);
    }

    public TrabajadorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TrabajadorNotFoundException(Throwable cause) {
        super(cause);
    }
}
