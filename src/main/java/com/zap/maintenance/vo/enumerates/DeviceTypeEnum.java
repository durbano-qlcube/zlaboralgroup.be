package com.zap.maintenance.vo.enumerates;


public enum DeviceTypeEnum implements java.io.Serializable
{
    MOBILE,
    TABLET,
    WEB;

    
    /**
     * Oauth2ProviderEnum constructor
     */
    private DeviceTypeEnum()
    {
    }

    /**
     * Return the Oauth2ProviderEnum from a string value
     * @return Oauth2ProviderEnum enum object
     */
    public static DeviceTypeEnum fromString(java.lang.String value)
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