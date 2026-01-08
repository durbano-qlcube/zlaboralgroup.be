package com.zap.stripe.vo;


public enum StatusStripeEnum implements java.io.Serializable
{
	EXITOSO,
	FALLIDO,
	VENCIDO,
	PENDIENTE;

	

	
    /**
     * CodeTypeWsEnum constructor
     */
    private StatusStripeEnum()
    {
    }

    /**
     * Return the MobileOsEnum from a string value
     * @return MobileOsEnum enum object
     */
    public static StatusStripeEnum fromString(java.lang.String value)
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