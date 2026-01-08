package com.zap.sales.exception.persona;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PersonaServiceException extends RuntimeException {

    private static final long serialVersionUID = 6953124712726404950L;

    public PersonaServiceException(String message) {
        super(message);
    }

    public PersonaServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public PersonaServiceException(Throwable cause) {
        super(cause);
    }
}
