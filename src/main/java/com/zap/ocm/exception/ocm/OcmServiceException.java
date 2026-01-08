package com.zap.ocm.exception.ocm;


public class OcmServiceException extends java.lang.RuntimeException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6953124712726404950L;

	public OcmServiceException() {
		super();
	}

	public OcmServiceException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public OcmServiceException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public OcmServiceException(String arg0) {
		super(arg0);
	}

	public OcmServiceException(Throwable arg0) {
		super(arg0);
	}
 
}