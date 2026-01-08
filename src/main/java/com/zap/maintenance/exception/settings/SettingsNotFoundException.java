package com.zap.maintenance.exception.settings;



public class SettingsNotFoundException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8838134532032645758L;

	public SettingsNotFoundException() {
		super();
	}

	public SettingsNotFoundException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public SettingsNotFoundException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public SettingsNotFoundException(String arg0) {
		super(arg0);
	}

	public SettingsNotFoundException(Throwable arg0) {
		super(arg0);
	}
 

}
