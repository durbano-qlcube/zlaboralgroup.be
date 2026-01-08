package com.zap.sales.exception.empresa;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EmpresaNotFoundException extends Exception {

    private static final long serialVersionUID = -1234567890123456789L;

    public EmpresaNotFoundException(String message) {
        super(message);
    }

    public EmpresaNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmpresaNotFoundException(Throwable cause) {
        super(cause);
    }
}
