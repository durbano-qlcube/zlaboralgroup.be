package com.zap.sales.exception.venta;


public class VentaServiceException extends java.lang.RuntimeException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6953124712726404950L;

	public VentaServiceException() {
		super();
	}

	public VentaServiceException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public VentaServiceException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public VentaServiceException(String arg0) {
		super(arg0);
	}

	public VentaServiceException(Throwable arg0) {
		super(arg0);
	}
 
}