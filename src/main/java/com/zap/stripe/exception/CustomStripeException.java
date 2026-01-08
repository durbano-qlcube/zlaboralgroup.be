package com.zap.stripe.exception;

public class CustomStripeException extends Exception {

    public CustomStripeException(String message) {
        super(message);
    }

    public CustomStripeException(String message, Throwable cause) {
        super(message, cause);
    }
}
