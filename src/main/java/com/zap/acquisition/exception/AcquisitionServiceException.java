package com.zap.acquisition.exception;


public class AcquisitionServiceException extends java.lang.RuntimeException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6953124712726404950L;

	public AcquisitionServiceException() {
		super();
	}

	public AcquisitionServiceException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public AcquisitionServiceException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public AcquisitionServiceException(String arg0) {
		super(arg0);
	}

	public AcquisitionServiceException(Throwable arg0) {
		super(arg0);
	}
 
}