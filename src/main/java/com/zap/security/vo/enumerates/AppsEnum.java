package com.zap.security.vo.enumerates;


public enum AppsEnum implements java.io.Serializable
{
	ZAP;

    /**
     * CodeTypeWsEnum constructor
     */
    private AppsEnum()
    {
    }

    /**
     * Return the MobileOsEnum from a string value
     * @return MobileOsEnum enum object
     */
    public static AppsEnum fromString(java.lang.String value)
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