package com.zap.maintenance.entity.settings;

import java.util.Calendar;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;




@Cacheable(true)
@javax.persistence.Entity
@javax.persistence.Table(name = "MNT_SETTINGS")
@javax.persistence.NamedQueries({
	@javax.persistence.NamedQuery(name = "SettingsEntity.findAll", query = "select settings from SettingsEntity AS settings"),
	@javax.persistence.NamedQuery(name = "SettingsEntity.loadByIdSettings", query = "select settings from SettingsEntity AS settings WHERE settings.idSettings=:idSettings"),
	@javax.persistence.NamedQuery(name = "SettingsEntity.loadSettingsByCategory1", query="SELECT  settings FROM SettingsEntity AS settings WHERE settings.category=:category ORDER BY settings.idSettings ASC"),
	@javax.persistence.NamedQuery(name = "SettingsEntity.loadSettingsByCategory2", query="SELECT  settings FROM SettingsEntity AS settings WHERE settings.category=:category and settings.subCategory=:subCategory ORDER BY settings.idSettings ASC"),
	@javax.persistence.NamedQuery(name = "SettingsEntity.loadSettingsByCategory3", query="SELECT  settings FROM SettingsEntity AS settings WHERE settings.category=:category and settings.subCategory=:subCategory ORDER BY settings.idSettings ASC"),
	@javax.persistence.NamedQuery(name = "SettingsEntity.loadSettingsByCategory4", query="SELECT  settings FROM SettingsEntity AS settings WHERE settings.category=:category ORDER BY settings.idSettings ASC"),

	@javax.persistence.NamedQuery(name = "SettingsEntity.loadSettingsByDes", query="SELECT  settings FROM SettingsEntity AS settings WHERE settings.category=:category and settings.subCategory=:subCategory and settings.description=:description ORDER BY settings.idSettings ASC"),

	@javax.persistence.NamedQuery(name = "SettingsEntity.loadSettingsByCode", query="SELECT  settings FROM SettingsEntity AS settings WHERE settings.category=:category and settings.subCategory=:subCategory and settings.code=:code ORDER BY settings.idSettings ASC")
	
	})

public class SettingsEntity  implements java.io.Serializable, Comparable<SettingsEntity>
{

    private static final long serialVersionUID = -2109966671878671439L;

    // ----------- Attribute Definitions ------------

    @javax.persistence.Id
    @javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    @javax.persistence.Column(name = "ID_SETTINGS", nullable = false, insertable = true, updatable = true, length = 19)
    private java.lang.Long idSettings;
    
    @javax.persistence.Column(name = "CATEGORY", insertable = true, updatable = true, length = 75)
    private java.lang.String category;
    
    @javax.persistence.Column(name = "SUBCATEGORY", insertable = true, updatable = true, length = 75)
    private java.lang.String subCategory;
    
    @javax.persistence.Column(name = "CODE", insertable = true, updatable = true, length = 75)
    private java.lang.String code;
    
    @javax.persistence.Column(name = "VALUE", insertable = true, updatable = true, length = 300)
    private java.lang.String value;
    
    @javax.persistence.Column(name = "DESCRIPTION", insertable = true, updatable = true, length = 300)
    private java.lang.String description;
    
    @Temporal(TemporalType.TIMESTAMP)
  	@Column(name = "FECHA_CREACION",  unique = false, nullable = true, insertable = true, updatable = true)
  	private Calendar fxCreacion;

  	@Temporal(TemporalType.TIMESTAMP)
  	@Column(name = "FECHA_MODIFICACION",  unique = false, nullable = true, insertable = true, updatable = true)
  	private Calendar fxModificacion;

    // --------------- Constructors -----------------
    
    


    // -------- Attribute Accessors ----------

    /**
     * Get the idSettings property.
     * 
     * @return java.lang.Long The value of idSettings
     */

    public java.lang.Long getIdSettings()
    {
        return idSettings;
    }


	/**
     * Set the idSettings property.
     * @param value the new value
     */
    public void setIdSettings(java.lang.Long value)
    {
        this.idSettings = value;
    }
    
    /**
     * Get the category property.
     * 
     * @return java.lang.String The value of category
     */

    public java.lang.String getCategory()
    {
        return category;
    }

    /**
     * Set the category property.
     * @param value the new value
     */
    public void setCategory(java.lang.String value)
    {
        this.category = value;
    }
    
    /**
     * Get the subCategory property.
     * 
     * @return java.lang.String The value of subCategory
     */

    public java.lang.String getSubCategory()
    {
        return subCategory;
    }

    /**
     * Set the subCategory property.
     * @param value the new value
     */
    public void setSubCategory(java.lang.String value)
    {
        this.subCategory = value;
    }
    
    /**
     * Get the code property.
     * 
     * @return java.lang.String The value of code
     */

    public java.lang.String getCode()
    {
        return code;
    }

    /**
     * Set the code property.
     * @param value the new value
     */
    public void setCode(java.lang.String value)
    {
        this.code = value;
    }
    
    /**
     * Get the value property.
     * 
     * @return java.lang.String The value of value
     */

    public java.lang.String getValue()
    {
        return value;
    }

    /**
     * Set the value property.
     * @param value the new value
     */
    public void setValue(java.lang.String value)
    {
        this.value = value;
    }
    
    /**
     * Get the description property.
     * 
     * @return java.lang.String The value of description
     */
    @javax.persistence.Column(name = "DESCRIPTION", insertable = true, updatable = true, length = 255)
    public java.lang.String getDescription()
    {
        return description;
    }

    /**
     * Set the description property.
     * @param value the new value
     */
    public void setDescription(java.lang.String value)
    {
        this.description = value;
    }
    

    
    
    
    // ------------- Relations ------------------


	/**
     * Indicates if the argument is of the same type and all values are equal.
     *
     * @param object The target object to compare with
     * @return boolean True if both objects a 'equal'
     */
    public boolean equals(Object object)
    {
        if (this == object)
        {
            return true;
        }
        if (!(object instanceof SettingsEntity))
        {
            return false;
        }
        final SettingsEntity that = (SettingsEntity)object;
        if (this.getIdSettings() == null || that.getIdSettings() == null || !this.getIdSettings().equals(that.getIdSettings()))
        {
            return false;
        }
        return true;
    }

    /**
     * Returns a hash code value for the object
     *
     * @return int The hash code value
     */
    public int hashCode()
    {
        int hashCode = 1;
        hashCode = 29 * hashCode + (getIdSettings() == null ? 0 : getIdSettings().hashCode());

        return hashCode;
    }

    /**
     * Returns a String representation of the object
     *
     * @return String Textual representation of the object displaying name/value pairs for all attributes
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("SettingsEntity(=");
        sb.append("idSettings: ");
        sb.append(getIdSettings());
        sb.append(", category: ");
        sb.append(getCategory());
        sb.append(", subCategory: ");
        sb.append(getSubCategory());
        sb.append(", code: ");
        sb.append(getCode());
        sb.append(", value: ");
        sb.append(getValue());
        sb.append(", description: ");
        sb.append(getDescription());
        sb.append(")");
        return sb.toString();
    }

    /**
     * @see java.lang.Comparable#compareTo(T)
     */
    public int compareTo(SettingsEntity o)
    {
        int cmp = 0;
        if (this.getIdSettings() != null)
        {
            cmp = this.getIdSettings().compareTo(o.getIdSettings());
        }
        else
        {
            if (this.getCategory() != null)
            {
                cmp = (cmp != 0 ? cmp : this.getCategory().compareTo(o.getCategory()));
            }
            if (this.getSubCategory() != null)
            {
                cmp = (cmp != 0 ? cmp : this.getSubCategory().compareTo(o.getSubCategory()));
            }
            if (this.getCode() != null)
            {
                cmp = (cmp != 0 ? cmp : this.getCode().compareTo(o.getCode()));
            }
            if (this.getValue() != null)
            {
                cmp = (cmp != 0 ? cmp : this.getValue().compareTo(o.getValue()));
            }
            if (this.getDescription() != null)
            {
                cmp = (cmp != 0 ? cmp : this.getDescription().compareTo(o.getDescription()));
            }
        }
        return cmp;
    }
}
