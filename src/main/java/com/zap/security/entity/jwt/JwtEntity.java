package com.zap.security.entity.jwt;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.QueryHint;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;




@Data
@Cacheable(true)
@javax.persistence.Entity
@javax.persistence.Table(name = "SEC_JWT")
@javax.persistence.NamedQueries({
	@javax.persistence.NamedQuery(name = "JwtEntity.loadAll", query = "select token from JwtEntity AS token"),
	@javax.persistence.NamedQuery(name = "JwtEntity.load", query = "select token from JwtEntity AS token WHERE token.id=:id"),
	@javax.persistence.NamedQuery(name = "JwtEntity.loadByEmail", query="SELECT  token FROM JwtEntity AS token WHERE token.email=:email", hints = {@QueryHint(name="org.hibernate.cacheable",value="true")}),
	@javax.persistence.NamedQuery(name = "JwtEntity.loadByToken", query="SELECT  token FROM JwtEntity AS token WHERE token.token=:token", hints = {@QueryHint(name="org.hibernate.cacheable",value="true")})
	
	})

public class JwtEntity  implements Serializable
{

    private static final long serialVersionUID = -2109966671878671439L;

    // ----------- Attribute Definitions ------------

    @Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="ID", unique=true, insertable=true, updatable=true, nullable=false)
    private Long id;
    
    @javax.persistence.Column(name = "EMAIL", insertable = true, updatable = true, length = 300)
    private String email;
    
    
    @javax.persistence.Column(name = "UUID", insertable = true, updatable = true, length = 50)
    private String uuid;
    
    @javax.persistence.Column(name = "FINGERPRINT", insertable = true, updatable = true, length = 300)
    private String fingerprint;
    
    @javax.persistence.Column(name = "ROLE", insertable = true, updatable = true, length = 50)
    private String role;
    
    @javax.persistence.Column(name = "APP", insertable = true, updatable = true, length = 50)
    private String app;
    
    @javax.persistence.Column(name = "APP_SIGNATURE", insertable = true, updatable = true, length = 50)
    private String appSignature;
    
    @javax.persistence.Column(name = "JWT", insertable = true, updatable = true, length = 1000)
    private String jwt;
    
    @javax.persistence.Column(name = "TOKEN", insertable = true, updatable = true, length = 1000)
    private String token;
    
    
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATE_REVOKATION",  unique = false, nullable = true, insertable = true, updatable = true)
	private Calendar fxRevokation;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATE_CREATION",  unique = false, nullable = true, insertable = true, updatable = true)
	private Calendar fxCreation;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATE_MODIFICATION",  unique = false, nullable = true, insertable = true, updatable = true)
	private Calendar fxModification;

	
	
	
	
	
	
	
    

    
}
