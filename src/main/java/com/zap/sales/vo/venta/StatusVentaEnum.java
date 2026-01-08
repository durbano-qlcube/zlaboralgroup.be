package com.zap.sales.vo.venta;


public enum StatusVentaEnum
{

	
	
	PDTE_DOC,
//	PDTE_FIRMA,
	PDTE_INICIO_CURSO,
	EJECUCION_CURSO,
	CURSO_FINALIZADO,
	NOTIFICADO_FUNDAE,
	CANCELADO,
	EN_EJECUCION,
	ERROR,
	VENTA,
	COMISIONADO,
	PDTE_PAGO;
	
    /**
     * CodeTypeWsEnum constructor
     */
    private StatusVentaEnum()
    {
    }

    /**
     * Return the MobileOsEnum from a string value
     * @return MobileOsEnum enum object
     */
    public static StatusVentaEnum fromString(java.lang.String value)
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