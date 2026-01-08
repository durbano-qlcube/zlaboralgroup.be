package com.zap.sales.exception.empresa;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EmpresaYaExisteException extends Exception {

    private static final long serialVersionUID = -1234567890123456789L;

    public EmpresaYaExisteException(String message) {
        super(message);
    }

    public EmpresaYaExisteException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmpresaYaExisteException(Throwable cause) {
        super(cause);
    }
}
