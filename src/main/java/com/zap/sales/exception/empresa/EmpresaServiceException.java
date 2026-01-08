package com.zap.sales.exception.empresa;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EmpresaServiceException extends RuntimeException {

    private static final long serialVersionUID = 6953124712726404950L;

    public EmpresaServiceException(String message) {
        super(message);
    }

    public EmpresaServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmpresaServiceException(Throwable cause) {
        super(cause);
    }
}
