package com.zap.maintenance.vo.settings;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SettingsVo implements java.io.Serializable
{
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 2904039297867803908L;

    public SettingsVo()
    {
    	super();
    }

    public SettingsVo(java.lang.Long idSettings)
    {
    	super();
        this.idSettings = idSettings;
    }

    public SettingsVo(java.lang.Long idSettings, java.lang.String category, java.lang.String subCategory, java.lang.String code, java.lang.String value, java.lang.String description)
    {
    	super();
        this.idSettings = idSettings;
        this.category = category;
        this.subCategory = subCategory;
        this.code = code;
        this.value = value;
        this.description = description;
    }
    
    /**
     * Copies constructor from other SettingsVo
     *
     * @param otherBean, cannot be <code>null</code>
     * @throws java.lang.NullPointerException if the argument is <code>null</code>
     */
    public SettingsVo(SettingsVo otherBean)
    {
        this(otherBean.getIdSettings(), otherBean.getCategory(), otherBean.getSubCategory(), otherBean.getCode(), otherBean.getValue(), otherBean.getDescription());
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(SettingsVo otherBean)
    {
        if (otherBean != null)
        {
            this.setIdSettings(otherBean.getIdSettings());
            this.setCategory(otherBean.getCategory());
            this.setSubCategory(otherBean.getSubCategory());
            this.setCode(otherBean.getCode());
            this.setValue(otherBean.getValue());
            this.setDescription(otherBean.getDescription());
        }
    }

    private Integer year;
    
    
    public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	private java.lang.Long idSettings;

    /**
     * 
     */
    public java.lang.Long getIdSettings()
    {
        return this.idSettings;
    }

    public void setIdSettings(java.lang.Long idSettings)
    {
        this.idSettings = idSettings;
    }

    private java.lang.String category;

    /**
     * 
     */
    public java.lang.String getCategory()
    {
        return this.category;
    }

    public void setCategory(java.lang.String category)
    {
        this.category = category;
    }

    private java.lang.String subCategory;

    /**
     * 
     */
    public java.lang.String getSubCategory()
    {
        return this.subCategory;
    }

    public void setSubCategory(java.lang.String subCategory)
    {
        this.subCategory = subCategory;
    }

    private java.lang.String code;

    /**
     * 
     */
    public java.lang.String getCode()
    {
        return this.code;
    }

    public void setCode(java.lang.String code)
    {
        this.code = code;
    }

    private java.lang.String value;

    /**
     * 
     */
    public java.lang.String getValue()
    {
        return this.value;
    }

    public void setValue(java.lang.String value)
    {
        this.value = value;
    }

    private java.lang.String description;

    /**
     * 
     */
    public java.lang.String getDescription()
    {
        return this.description;
    }

    public void setDescription(java.lang.String description)
    {
        this.description = description;
    }

    

	@Override
	public String toString() {
		return "SettingsVo [idSettings=" + idSettings + ", category="
				+ category + ", subCategory=" + subCategory + ", code=" + code
				+ ", value=" + value + ", description=" + description + "]";
	}

    


}