package com.zap.sales.exception.persona;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PersonaYaExisteException extends Exception {

    private static final long serialVersionUID = -1234567890123456789L;

    public PersonaYaExisteException(String message) {
        super(message);
    }

    public PersonaYaExisteException(String message, Throwable cause) {
        super(message, cause);
    }

    public PersonaYaExisteException(Throwable cause) {
        super(cause);
    }
}
