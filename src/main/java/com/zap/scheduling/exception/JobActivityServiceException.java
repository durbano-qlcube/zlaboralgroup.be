package com.zap.scheduling.exception;



public class JobActivityServiceException extends java.lang.RuntimeException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3723377990860022574L;

	public JobActivityServiceException() {
		super();
	}

	public JobActivityServiceException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public JobActivityServiceException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public JobActivityServiceException(String arg0) {
		super(arg0);
	}

	public JobActivityServiceException(Throwable arg0) {
		super(arg0);
	}
}