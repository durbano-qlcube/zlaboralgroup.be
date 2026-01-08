package com.zap.sales.exception.formacion;

public class FormacionNotFoundException extends Exception {

    private static final long serialVersionUID = -8959830975108995786L;

    public FormacionNotFoundException() {
        super();
    }

    public FormacionNotFoundException(String message) {
        super(message);
    }

    public FormacionNotFoundException(Throwable cause) {
        super(cause);
    }

    public FormacionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public FormacionNotFoundException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
