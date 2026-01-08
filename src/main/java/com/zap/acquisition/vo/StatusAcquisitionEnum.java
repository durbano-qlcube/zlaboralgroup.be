package com.zap.acquisition.vo;


public enum StatusAcquisitionEnum implements java.io.Serializable
{
	ENVIAR_OCM,
	REGISTRAR,
	PROCESADO,
	CODIFICADO,CERRADO,
	ERROR,
	ABIERTO;

	/// Se asignan estos estados ya que en el job:ZapCubeToGestDirectLeadsJob metodo: findAcquisitionByOriginId se necesita para devolver el estado, sino devuelve error
//	EN_TRAMITE,
//	EN_EJECUCION,
//	VENTA,
//	COMISIONADO,
//	NO_VENTA,
//	CLIENTE_EXISTENTE;
	
	

	
    /**
     * CodeTypeWsEnum constructor
     */
    private StatusAcquisitionEnum()
    {
    }

    /**
     * Return the MobileOsEnum from a string value
     * @return MobileOsEnum enum object
     */
    public static StatusAcquisitionEnum fromString(java.lang.String value)
    {
        return valueOf(value);
    }
    
    /**
     * Return a Collection of all literal values for this enumeration
     * @return java.util.Collection literal values
     */
    public static java.util.Collection<String> literals()
    {
        final java.util.Collection<String> literals = new java.util.ArrayList<String>(values().length);
        for (int i = 0; i < values().length; i++)
        {
            literals.add(values()[i].name());
        }
        return literals;
    }
}