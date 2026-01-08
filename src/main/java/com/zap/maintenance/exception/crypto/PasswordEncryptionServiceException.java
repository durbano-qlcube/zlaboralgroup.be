package com.zap.maintenance.exception.crypto;

public class PasswordEncryptionServiceException
    extends java.lang.RuntimeException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8644381862538294618L;

	public PasswordEncryptionServiceException() {
		super();
	}

	public PasswordEncryptionServiceException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		
	}

	public PasswordEncryptionServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public PasswordEncryptionServiceException(String message) {
		super(message);
	}

	public PasswordEncryptionServiceException(Throwable cause) {
		super(cause);
	}
  

}