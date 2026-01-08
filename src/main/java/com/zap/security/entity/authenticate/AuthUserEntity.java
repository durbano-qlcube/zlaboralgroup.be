package com.zap.security.entity.authenticate;

import java.io.Serializable;
import java.util.Calendar;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.zap.security.vo.enumerates.RoleEnum;

import lombok.Data;




@Cacheable(true)
@javax.persistence.Entity
@javax.persistence.Table(name = "SEC_AUTHUSER")
@javax.persistence.NamedQueries({
	@javax.persistence.NamedQuery(name = "AuthUserEntity.loadAll", query = "select AuthUser from AuthUserEntity AS AuthUser order by AuthUser.id desc"),
	@javax.persistence.NamedQuery(name = "AuthUserEntity.load", query = "select AuthUser from AuthUserEntity AS AuthUser WHERE AuthUser.id=:id"),
	@javax.persistence.NamedQuery(name = "AuthUserEntity.loadActive", query = "select AuthUser from AuthUserEntity AS AuthUser WHERE AuthUser.isActive=true"),
	@javax.persistence.NamedQuery(name = "AuthUserEntity.loadByEmail", query="SELECT  AuthUser FROM AuthUserEntity AS AuthUser WHERE AuthUser.email=:email"),
	@javax.persistence.NamedQuery(name = "AuthUserEntity.loadByRole", query="SELECT  AuthUser FROM AuthUserEntity AS AuthUser WHERE AuthUser.role IN :role and AuthUser.isActive=true"),
	@javax.persistence.NamedQuery(name = "AuthUserEntity.loadByUuid", query="SELECT  AuthUser FROM AuthUserEntity AS AuthUser WHERE AuthUser.uuid=:uuid"),
	@javax.persistence.NamedQuery(name = "AuthUserEntity.loadByUsername", query="SELECT  AuthUser FROM AuthUserEntity AS AuthUser WHERE AuthUser.username=:username"),
    @javax.persistence.NamedQuery(name = "AuthUserEntity.loadMainProviders", query="SELECT  AuthUser FROM AuthUserEntity AS AuthUser WHERE AuthUser.role = :role AND AuthUser.isActive = true AND AuthUser.isMainProvider = true"),

	@javax.persistence.NamedQuery(
		    name = "AuthUserEntity.loadBySupervisorUuid", 
		    query = "SELECT AuthUser FROM AuthUserEntity AS AuthUser WHERE AuthUser.uuidSupervisor = :uuidSupervisor AND AuthUser.role = 'SUPERVISOR' and isActive = TRUE"
		),
	@javax.persistence.NamedQuery(
		    name = "AuthUserEntity.loadByCordinadorbySupervisorUuid", 
		    query = "SELECT AuthUser FROM AuthUserEntity AS AuthUser WHERE AuthUser.uuidSupervisor = :uuidSupervisor AND AuthUser.role = 'CORDINADOR' and isActive = TRUE"
		),
	@javax.persistence.NamedQuery(
		    name = "AuthUserEntity.loadByCoordinadorUuid", 
		    query = "SELECT AuthUser FROM AuthUserEntity AS AuthUser WHERE AuthUser.uuid = :uuidCoordinador AND AuthUser.role = 'CORDINADOR' and isActive = TRUE"
		),
	@javax.persistence.NamedQuery(
		    name = "AuthUserEntity.loadByCaptadorUuid",
		    query = "SELECT a FROM AuthUserEntity a WHERE a.uuidCordinador IN (SELECT c.uuid FROM AuthUserEntity c WHERE c.uuidSupervisor = :uuidSupervisor ) and a.role IN ('CAPTADOR', 'AGENTE') and isActive = TRUE"
		),
	@javax.persistence.NamedQuery(
		    name = "AuthUserEntity.loadByCordinadorUuid",
		    query = "SELECT a FROM AuthUserEntity a WHERE a.uuidCordinador = :uuidCordinador and a.role IN ('CAPTADOR', 'AGENTE') and isActive = TRUE" 
		),
	@javax.persistence.NamedQuery(
	        name = "AuthUserEntity.loadAllCaptadores",
	        query = "SELECT a FROM AuthUserEntity a WHERE a.role IN ('CAPTADOR', 'AGENTE') and isActive = TRUE"
	    ),
	@javax.persistence.NamedQuery(
		    name = "AuthUserEntity.loadByAgenteCaptadorUuid",
		    query = "SELECT a FROM AuthUserEntity a WHERE a.uuidCordinador IN (SELECT c.uuid FROM AuthUserEntity c WHERE c.uuidSupervisor = :uuidSupervisor ) and a.role = 'AGENTE' and isActive = TRUE"
		),
	@javax.persistence.NamedQuery(
		    name = "AuthUserEntity.loadByAgenteUuid",
		    query = "SELECT a FROM AuthUserEntity a WHERE a.uuidCordinador = :uuidCordinador and a.role = 'AGENTE' and isActive = TRUE" 
		),
	@javax.persistence.NamedQuery(
	        name = "AuthUserEntity.loadAllAgente",
	        query = "SELECT a FROM AuthUserEntity a WHERE a.role = 'AGENTE' and isActive = TRUE"
	    ),
	@javax.persistence.NamedQuery(
		    name = "AuthUserEntity.findColaboradorUuidAndUuidByUsername",
		    query = "SELECT a.colaboradorUuid, a.uuid FROM AuthUserEntity a WHERE a.username = :username"
		),
	
    @javax.persistence.NamedQuery(name = "AuthUserEntity.loadSubProvidersByUsernameLike", query="SELECT  AuthUser FROM AuthUserEntity AS AuthUser WHERE UPPER(AuthUser.username) LIKE :username AND AuthUser.role = :role AND AuthUser.isActive = true AND (AuthUser.isMainProvider = false OR AuthUser.isMainProvider IS NULL)"),
    
    @javax.persistence.NamedQuery(name = "AuthUserEntity.loadMainProviderByUsernameLike", query = "SELECT AuthUser FROM AuthUserEntity AS AuthUser WHERE UPPER(AuthUser.username) LIKE :username AND AuthUser.role = :role AND AuthUser.isActive = true AND AuthUser.isMainProvider = true"),



	//@javax.persistence.NamedQuery(name = "AuthUserEntity.deleteByNif", query="Delete FROM AuthUserEntity AS AuthUser WHERE AuthUser.code=:code"),
	//	@javax.persistence.NamedQuery(name = "AuthUserEntity.loadByCodigoOficial", query="SELECT  AuthUser FROM AuthUserEntity AS AuthUser WHERE AuthUser.codigoOficial=:codigoOficial ORDER BY AuthUser.codigoOficial ASC")
	
	})
@Data
public class AuthUserEntity  implements Serializable
{

    private static final long serialVersionUID = -2109966671878671439L;

    @Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="ID_AUTHUSER", unique=true, insertable=true, updatable=true, nullable=false)
    private Long id;
    
    @javax.persistence.Column(name = "UUID", insertable = true, updatable = true, length = 200)
    private String uuid;
    
    @javax.persistence.Column(name = "NAME", insertable = true, updatable = true, length = 200)
    private String name;
    
    @javax.persistence.Column(name = "SURNAME", insertable = true, updatable = true, length = 500)
    private String surname;
    
    @javax.persistence.Column(name = "FULLNAME", insertable = true, updatable = true, length = 250)
    private String fullname;
    
    @javax.persistence.Column(name = "USERNAME", unique=true, insertable = true, updatable = true, length = 300)
    private String username;
    
    @javax.persistence.Column(name = "PASS", insertable = true, updatable = true, length = 350)
    private String password;
    
    @javax.persistence.Column(name = "EMAIL",  insertable = true, updatable = true, length = 300)
    private String email;
    
    @javax.persistence.Column(name = "IS_ACTIVE", insertable = true, updatable = true)
    private Boolean isActive;

    @javax.persistence.Column(name = "IS_MAIN_PROVIDER", insertable = true, updatable = true)
    private Boolean isMainProvider;
    
    @javax.persistence.Column(name = "UUID_CORDINADOR", insertable = true, updatable = true, length = 300)
    private String uuidCordinador;
    
    @javax.persistence.Column(name = "UUID_SUPERVISOR", insertable = true, updatable = true, length = 300)
    private String uuidSupervisor;
    
    @javax.persistence.Column(name = "CORDINADOR_USERNAME", insertable = true, updatable = true, length = 300)
    private String cordinadorUsername;
    
    @javax.persistence.Column(name = "SUPERVISOR_USERNAME", insertable = true, updatable = true, length = 300)
    private String supervisorUsername;
    
    
    @javax.persistence.Column(name = "ROLE", insertable = true, updatable = true, length = 25, columnDefinition = "VARCHAR(25)")
    @javax.persistence.Enumerated(javax.persistence.EnumType.STRING)
    private RoleEnum role;
    
//    @javax.persistence.Column(name = "THEMES", insertable = true, updatable = true, length = 350)
//    private String themes;
    
    
    @javax.persistence.Column(name = "HAS_TO_CHECK_HISTORY_PASS", insertable = true, updatable = true)
    private Boolean hasToCheckHistoryPass;
    
    
    @javax.persistence.Column(name = "IS_TEMPORAL_PASSWORD", insertable = true, updatable = true)
    private Boolean isTemporalPassword;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATE_TEMPORAL",  unique = false, nullable = true, insertable = true, updatable = true)
	private Calendar fxTemporal;
    
    
    @javax.persistence.Column(name = "HOW_MANY_MONTH_PASS_EXPIRES", insertable = true, updatable = true)
    private Integer howManyMonthsPassExpires;
    
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATE_EXPIRATION",  unique = false, nullable = true, insertable = true, updatable = true)
	private Calendar fxExpiration;
    
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATE_CREATION",  unique = false, nullable = true, insertable = true, updatable = true)
	private Calendar fxCreation;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATE_MODIFICATION",  unique = false, nullable = true, insertable = true, updatable = true)
	private Calendar fxModification;

	
	  @javax.persistence.Column(name = "COLABORADOR_UUID", insertable = true, updatable = true, length = 200)
	    private String colaboradorUuid;
	
	
	
	 
    

    
}
