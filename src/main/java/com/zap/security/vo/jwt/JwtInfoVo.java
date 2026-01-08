package com.zap.security.vo.jwt;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.zap.security.vo.enumerates.RoleEnum;

import lombok.Data;


@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class JwtInfoVo  implements Serializable
{

    private static final long serialVersionUID = -2109966671878671439L;

    private String issuer;
    private String subject;
    private String audience;
    private String uuid;
    private String app;
    private String signature;
    private RoleEnum role;
    private String userFingerprint;
    
	
	

    
}
