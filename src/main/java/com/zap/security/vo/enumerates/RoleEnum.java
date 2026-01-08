package com.zap.security.vo.enumerates;


public enum RoleEnum implements java.io.Serializable
{
        SUPER_ADMNISTRADOR,
        ADMINISTRADOR,
        SUBADMINISTRADOR,
	SUPERVISOR,
	CORDINADOR,
	AGENTE,
	CAPTADOR,
	BACKOFFICE,
	GESTORIA,
	COLABORADOR,
	PROVIDER,
	PARTNER;
	
	
//	FORMADOR,
//	
//	SUPERVISOR_QA,
//	AGENTE_QA;


    /**
     * CodeTypeWsEnum constructor
     */
    private RoleEnum()
    {
    }

    /**
     * Return the MobileOsEnum from a string value
     * @return MobileOsEnum enum object
     */
    public static RoleEnum fromString(java.lang.String value)
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