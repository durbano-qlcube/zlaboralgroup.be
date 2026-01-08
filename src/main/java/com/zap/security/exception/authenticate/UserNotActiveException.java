package com.zap.security.exception.authenticate;


public class UserNotActiveException extends Exception
{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5114219552764374795L;

	public UserNotActiveException() {
		super();
	}

	public UserNotActiveException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public UserNotActiveException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public UserNotActiveException(String arg0) {
		super(arg0);
	}

	public UserNotActiveException(Throwable arg0) {
		super(arg0);
	}
	
	
}