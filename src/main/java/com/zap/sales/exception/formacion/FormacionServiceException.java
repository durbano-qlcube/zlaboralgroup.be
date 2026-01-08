package com.zap.sales.exception.formacion;

public class FormacionServiceException extends RuntimeException {

    private static final long serialVersionUID = 6953124712726404950L;

    public FormacionServiceException() {
        super();
    }

    public FormacionServiceException(String message) {
        super(message);
    }

    public FormacionServiceException(Throwable cause) {
        super(cause);
    }

    public FormacionServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public FormacionServiceException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
