package com.zap.sales.exception.doc;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DocServiceException extends RuntimeException {

    private static final long serialVersionUID = 6953124712726404950L;

    public DocServiceException(String message) {
        super(message);
    }

    public DocServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DocServiceException(Throwable cause) {
        super(cause);
    }
}
