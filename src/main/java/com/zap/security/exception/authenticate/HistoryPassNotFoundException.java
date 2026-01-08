package com.zap.security.exception.authenticate;


public class HistoryPassNotFoundException extends Exception
{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5114219552764374795L;

	public HistoryPassNotFoundException() {
		super();
	}

	public HistoryPassNotFoundException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public HistoryPassNotFoundException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public HistoryPassNotFoundException(String arg0) {
		super(arg0);
	}

	public HistoryPassNotFoundException(Throwable arg0) {
		super(arg0);
	}
	
	
}