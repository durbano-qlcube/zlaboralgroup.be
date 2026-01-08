package com.zap.maintenance.exception.geo;



public class CountryNotFoundException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8838134532032645758L;

	public CountryNotFoundException() {
		super();
	}

	public CountryNotFoundException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public CountryNotFoundException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public CountryNotFoundException(String arg0) {
		super(arg0);
	}

	public CountryNotFoundException(Throwable arg0) {
		super(arg0);
	}
 

}
