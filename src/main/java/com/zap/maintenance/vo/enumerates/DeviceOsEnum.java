package com.zap.maintenance.vo.enumerates;


public enum DeviceOsEnum implements java.io.Serializable
{
    IOS,
    ANDROID;

    /**
     * CodeTypeWsEnum constructor
     */
    private DeviceOsEnum()
    {
    }

    /**
     * Return the MobileOsEnum from a string value
     * @return MobileOsEnum enum object
     */
    public static DeviceOsEnum fromString(java.lang.String value)
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