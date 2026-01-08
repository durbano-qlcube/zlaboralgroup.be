package com.zap.security.exception.jwt;



public class JwtServiceException extends java.lang.RuntimeException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3723377990860022574L;

	public JwtServiceException() {
		super();
	}

	public JwtServiceException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public JwtServiceException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public JwtServiceException(String arg0) {
		super(arg0);
	}

	public JwtServiceException(Throwable arg0) {
		super(arg0);
	}
}