package com.zap.maintenance.exception.rate;



public class RateNotFoundException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8838134532032645758L;

	public RateNotFoundException() {
		super();
	}

	public RateNotFoundException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public RateNotFoundException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public RateNotFoundException(String arg0) {
		super(arg0);
	}

	public RateNotFoundException(Throwable arg0) {
		super(arg0);
	}
 

}
