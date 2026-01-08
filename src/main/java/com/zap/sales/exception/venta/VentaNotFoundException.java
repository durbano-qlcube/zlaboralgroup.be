package com.zap.sales.exception.venta;



public class VentaNotFoundException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8838134532032645758L;

	public VentaNotFoundException() {
		super();
	}

	public VentaNotFoundException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public VentaNotFoundException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public VentaNotFoundException(String arg0) {
		super(arg0);
	}

	public VentaNotFoundException(Throwable arg0) {
		super(arg0);
	}
 

}
