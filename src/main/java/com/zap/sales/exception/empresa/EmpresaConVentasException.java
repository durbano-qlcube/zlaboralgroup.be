package com.zap.sales.exception.empresa;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EmpresaConVentasException extends Exception {
    public EmpresaConVentasException(String message) {
        super(message);
    }
}
