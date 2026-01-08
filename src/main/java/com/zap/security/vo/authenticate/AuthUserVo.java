package com.zap.security.vo.authenticate;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.zap.maintenance.service.adapter.TimeDateAdapter;
import com.zap.security.vo.enumerates.RoleEnum;
import com.zap.security.vo.parentcompany.ParentCompanyVo;

import lombok.Data;

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AuthUserVo  implements Serializable
{

    private static final long serialVersionUID = -2109966671878671439L;

    private Long id;

    private String uuid;
    private String name;
    private String surname;
	private String fullname;
	
    private String username;
    private String email;
    private String password;

    private Boolean isActive;
    private Boolean isMainProvider;
    private RoleEnum role;

    private Long parentCompanyId;
    private String parentCompanyName;
    private List<Long> parentCompanyIds;
    private List<ParentCompanyVo> parentCompanies;
    
    private String uuidCordinador;
    private String uuidSupervisor;
    private String cordinadorUsername;
    private String supervisorUsername;
    
    private String themes;
    private Boolean hasToCheckHistoryPass;
    private Boolean isTemporalPassword;
    
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
	private Calendar fxTemporal;
	
    private Integer howManyMonthsPassExpires;
    
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fxExpiration;
    
    private List<String> uuidProviders;

    private List<UserProviderVo> providers;

    private List<ProviderConfigVo> providerConfig;
}
