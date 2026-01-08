package com.zap.sales.exception.alumno;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TrabajadorServiceException extends RuntimeException {

    private static final long serialVersionUID = 6953124712726404950L;

    public TrabajadorServiceException(String message) {
        super(message);
    }

    public TrabajadorServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public TrabajadorServiceException(Throwable cause) {
        super(cause);
    }
}
