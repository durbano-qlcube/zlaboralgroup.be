package com.zap.acquisition.vo;

public enum EstadoEnvioCorreoEnum implements java.io.Serializable {

	PENDIENTE,
	ENVIADO;

	private EstadoEnvioCorreoEnum() {
	}

	public static EstadoEnvioCorreoEnum fromString(java.lang.String value) {
		return valueOf(value);
	}

	public static java.util.Collection<String> literals() {
		final java.util.Collection<String> literals = new java.util.ArrayList<String>(values().length);
		for (int i = 0; i < values().length; i++) {
			literals.add(values()[i].name());
		}
		return literals;
	}
}
