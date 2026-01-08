package com.zap.sales.exception.persona;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PersonaConVentasException extends Exception {
    public PersonaConVentasException(String message) {
        super(message);
    }
}
