package com.zap.security.exception.jwt;


public class JwtNotFoundException extends Exception
{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5114219552764374795L;

	public JwtNotFoundException() {
		super();
	}

	public JwtNotFoundException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public JwtNotFoundException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public JwtNotFoundException(String arg0) {
		super(arg0);
	}

	public JwtNotFoundException(Throwable arg0) {
		super(arg0);
	}
	
	
}