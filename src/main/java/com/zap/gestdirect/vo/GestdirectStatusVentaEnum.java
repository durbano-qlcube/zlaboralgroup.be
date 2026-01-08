package com.zap.gestdirect.vo;


public enum GestdirectStatusVentaEnum
{

	
	
	EN_TRAMITE,
	EN_EJECUCION,
	VENTA,
	COMISIONADO,
	NO_VENTA,
	CLIENTE_EXISTENTE,
	ERROR;
//	ENVIAR_OCM,
//	PDTE_DOC,
//	PDTE_FIRMA,
//	EJECUCION_CURSO,
//	CURSO_FINALIZADO,
//	NOTIFICADO_FUNDAE,
//	CANCELADO;
    /**
     * CodeTypeWsEnum constructor
     */
    private GestdirectStatusVentaEnum()
    {
    }

    /**
     * Return the MobileOsEnum from a string value
     * @return MobileOsEnum enum object
     */
    public static GestdirectStatusVentaEnum fromString(java.lang.String value)
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