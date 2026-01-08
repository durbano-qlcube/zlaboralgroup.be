package com.zap.ocm.vo;

public enum HubSpotStatusEnum {

	PENDING,SEND,ERROR, WAITING ;

	/**
	 * HashEnum constructor
	 */
	private HubSpotStatusEnum() {
	}

	/**
	 * Return the HashEnum from a string value
	 * 
	 * @return HashEnum enum object
	 */
	public static HubSpotStatusEnum fromString(java.lang.String value) {
		return valueOf(value);
	}

	/**
	 * Return a Collection of all literal values for this enumeration
	 * 
	 * @return java.util.Collection literal values
	 */
	public static java.util.Collection<String> literals() {
		final java.util.Collection<String> literals = new java.util.ArrayList<String>(values().length);
		for (int i = 0; i < values().length; i++) {
			literals.add(values()[i].name());
		}
		return literals;
	}

}
