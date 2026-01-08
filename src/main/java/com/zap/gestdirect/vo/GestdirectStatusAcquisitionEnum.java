package com.zap.gestdirect.vo;


public enum GestdirectStatusAcquisitionEnum implements java.io.Serializable
{

//	EN_TRAMITE,
//	EN_EJECUCION,
//	FINALIZADO,
//	COMISIONADO,
//	CANCELADO,
//	VENTA,
//	NO_VENTA,
//	CLIENTE_EXISTENTE,
//	ERROR;
	
	
	EN_TRAMITE,
	EN_EJECUCION,
	VENTA,
	COMISIONADO,
	NO_VENTA,
	CLIENTE_EXISTENTE,
	ERROR,
	ENVIAR_OCM;
	
    /**
     * CodeTypeWsEnum constructor
     */
    private GestdirectStatusAcquisitionEnum()
    {
    }

    /**
     * Return the MobileOsEnum from a string value
     * @return MobileOsEnum enum object
     */
    public static GestdirectStatusAcquisitionEnum fromString(java.lang.String value)
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