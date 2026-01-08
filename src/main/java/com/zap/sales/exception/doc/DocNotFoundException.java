package com.zap.sales.exception.doc;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DocNotFoundException extends Exception {

    private static final long serialVersionUID = -1234567890123456789L;

    public DocNotFoundException(String message) {
        super(message);
    }

    public DocNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public DocNotFoundException(Throwable cause) {
        super(cause);
    }
}
