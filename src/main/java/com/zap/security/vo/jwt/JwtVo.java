package com.zap.security.vo.jwt;

import java.io.Serializable;
import java.util.Calendar;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.zap.maintenance.service.adapter.TimeDateAdapter;

import lombok.Data;


@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class JwtVo  implements Serializable
{

    private static final long serialVersionUID = -2109966671878671439L;

    private Long id;
    private String email;
    private String uuid;
    private String role;
    private String app;
    private String fingerprint;
    private String appSignature;
   
    private String jwt;
    private String token;
    
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
	private Calendar fxRevokation;
    
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
	private Calendar fxCreation;
    
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
	private Calendar fxModification;
	
	
	
    
    

    
}
