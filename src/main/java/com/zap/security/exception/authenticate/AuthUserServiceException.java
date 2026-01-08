package com.zap.security.exception.authenticate;



public class AuthUserServiceException extends java.lang.RuntimeException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3723377990860022574L;

	public AuthUserServiceException() {
		super();
	}

	public AuthUserServiceException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public AuthUserServiceException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public AuthUserServiceException(String arg0) {
		super(arg0);
	}

	public AuthUserServiceException(Throwable arg0) {
		super(arg0);
	}
}