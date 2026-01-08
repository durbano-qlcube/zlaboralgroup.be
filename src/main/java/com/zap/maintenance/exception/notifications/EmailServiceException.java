package com.zap.maintenance.exception.notifications;


public class EmailServiceException extends java.lang.RuntimeException
{
    /** 
     * The serial version UID of this class. Needed for serialization. 
     */
    private static final long serialVersionUID = -682144332321544995L;

    
	public EmailServiceException() {
		super();
	}

	public EmailServiceException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public EmailServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public EmailServiceException(String message) {
		super(message);
	}

	public EmailServiceException(Throwable cause) {
		super(cause);
	}
  
}