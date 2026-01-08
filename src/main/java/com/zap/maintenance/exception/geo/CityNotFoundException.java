package com.zap.maintenance.exception.geo;



public class CityNotFoundException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8838134532032645758L;

	public CityNotFoundException() {
		super();
	}

	public CityNotFoundException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public CityNotFoundException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public CityNotFoundException(String arg0) {
		super(arg0);
	}

	public CityNotFoundException(Throwable arg0) {
		super(arg0);
	}
 

}
