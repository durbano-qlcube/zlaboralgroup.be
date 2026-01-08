package com.zap.sales.exception.persona;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PersonaNotFoundException extends Exception {

    private static final long serialVersionUID = -1234567890123456789L;

    public PersonaNotFoundException(String message) {
        super(message);
    }

    public PersonaNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PersonaNotFoundException(Throwable cause) {
        super(cause);
    }
}
