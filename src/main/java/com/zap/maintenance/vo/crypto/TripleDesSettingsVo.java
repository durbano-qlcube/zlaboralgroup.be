package com.zap.maintenance.vo.crypto;

/**
 * 
 */
public class TripleDesSettingsVo
    implements java.io.Serializable
{
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -3622549061786862169L;

    public TripleDesSettingsVo()
    {
    }

    public TripleDesSettingsVo(java.lang.String key1, java.lang.String key2, java.lang.String key3)
    {
        this.key1 = key1;
        this.key2 = key2;
        this.key3 = key3;
    }

    /**
     * Copies constructor from other CryptoSettingsVo
     *
     * @param otherBean, cannot be <code>null</code>
     * @throws java.lang.NullPointerException if the argument is <code>null</code>
     */
    public TripleDesSettingsVo(TripleDesSettingsVo otherBean)
    {
        this(otherBean.getKey1(), otherBean.getKey2(), otherBean.getKey3());
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(TripleDesSettingsVo otherBean)
    {
        if (otherBean != null)
        {
            this.setKey1(otherBean.getKey1());
            this.setKey2(otherBean.getKey2());
            this.setKey3(otherBean.getKey3());
        }
    }

    private java.lang.String key1;

    /**
     * 
     */
    public java.lang.String getKey1()
    {
        return this.key1;
    }

    public void setKey1(java.lang.String key1)
    {
        this.key1 = key1;
    }

    private java.lang.String key2;

    /**
     * 
     */
    public java.lang.String getKey2()
    {
        return this.key2;
    }

    public void setKey2(java.lang.String key2)
    {
        this.key2 = key2;
    }

    private java.lang.String key3;

    /**
     * 
     */
    public java.lang.String getKey3()
    {
        return this.key3;
    }

    public void setKey3(java.lang.String key3)
    {
        this.key3 = key3;
    }

	@Override
	public String toString() {
		return "CryptoSettingsVo [key1=" + key1 + ", key2=" + key2 + ", key3="
				+ key3 + "]";
	}


}