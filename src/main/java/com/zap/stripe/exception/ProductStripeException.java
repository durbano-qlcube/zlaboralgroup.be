package com.zap.stripe.exception;

public class ProductStripeException extends Exception {

    public ProductStripeException(String message) {
        super(message);
    }

    public ProductStripeException(String message, Throwable cause) {
        super(message, cause);
    }
}
