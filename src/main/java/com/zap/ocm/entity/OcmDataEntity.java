package com.zap.ocm.entity;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;





@Cacheable(true)
@javax.persistence.Entity
@javax.persistence.Table(name = "skill_formacionleadsmotor_data")
@javax.persistence.NamedQueries({
//	@javax.persistence.NamedQuery(name = "OcmDataEntity.loadAll", query = "select AuthUser from AuthUserEntity AS AuthUser order by AuthUser.id desc"),
//	@javax.persistence.NamedQuery(name = "OcmDataEntity.load", query = "select AuthUser from AuthUserEntity AS AuthUser WHERE AuthUser.id=:id"),
//	@javax.persistence.NamedQuery(name = "OcmDataEntity.loadByIdOcm", query="SELECT  lead FROM DkvMotorLeadsEntity AS lead WHERE lead.idOcm=:idOcm"),
//	@javax.persistence.NamedQuery(name = "OcmDataEntity.loadByHubspotStatus", query="SELECT lead FROM DkvMotorLeadsEntity AS lead WHERE lead.hubspotStatus=:hubspotStatus order by lead.dateInsert desc"),

	
	})
@Data
public class OcmDataEntity  implements Serializable
{

    private static final long serialVersionUID = -2109966671878671439L;

    @Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id", unique=true, insertable=true, updatable=true, nullable=false)
    private Integer id;
    
    @javax.persistence.Column(name = "idLoad", unique = true,  insertable= true, updatable = true)
    private Integer idLoad;

    
    @javax.persistence.Column(name = "active", insertable = true, updatable = true)
    private Integer active;
    
    @javax.persistence.Column(name = "status", insertable = true, updatable = true)
    private Integer status;
    
    @javax.persistence.Column(name = "priority", insertable = true, updatable = true)
    private Integer priority;

    @javax.persistence.Column(name = "number", insertable = true, updatable = true, length = 15)
    private Integer number;
    
    @javax.persistence.Column(name = "number1", insertable = true, updatable = true, length = 20)
    private String number1;
    
    @javax.persistence.Column(name = "number2", insertable = true, updatable = true, length = 20)
    private String number2;
    
    @javax.persistence.Column(name = "number3", insertable = true, updatable = true, length = 20)
    private String number3;
    
    @javax.persistence.Column(name = "number4", insertable = true, updatable = true, length = 20)
    private String number4;
    
    @javax.persistence.Column(name = "number5", insertable = true, updatable = true, length = 20)
    private String number5;
    
    
    
    @javax.persistence.Column(name = "status1", insertable = true, updatable = true)
    private Integer status1;  
    
    @javax.persistence.Column(name = "status2", insertable = true, updatable = true)
    private Integer status2; 
    
    @javax.persistence.Column(name = "status3", insertable = true, updatable = true)
    private Integer status3; 
    
    @javax.persistence.Column(name = "status4", insertable = true, updatable = true)
    private Integer status4;  
    
    @javax.persistence.Column(name = "status5", insertable = true, updatable = true)
    private Integer status5;
    
    
    
//    @javax.persistence.Column(name = "datevalid1", insertable = true, updatable = true, length = 15)
//    private String datevalid1;
   
    
    
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dateinsert",  unique = false, nullable = true, insertable = true, updatable = true)
    private Calendar dateInsert;
    
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "datefirstcall",  unique = false, nullable = true, insertable = true, updatable = true)
    private Calendar dateFirstcall;
    
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "datelastcall",  unique = false, nullable = true, insertable = true, updatable = true)
    private Calendar dateLastcall;
    
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "datenextcall",  unique = false, nullable = true, insertable = true, updatable = true)
    private Calendar dateNextcall;
    
    @javax.persistence.Column(name = "scheduledagent", insertable = true, updatable = true, length = 20)
    private String scheduledagent;
    
    @javax.persistence.Column(name = "attempt", insertable = true, updatable = true)
    private Integer attempt;
    
    @javax.persistence.Column(name = "lastagent", insertable = true, updatable = true, length = 20)
    private String lastagent;
    
    @javax.persistence.Column(name = "endresult", insertable = true, updatable = true)
    private Integer endresult;
    
    @javax.persistence.Column(name = "endresultdesc", insertable = true, updatable = true, length = 90)
    private String endresultdesc;
    
    @javax.persistence.Column(name = "endresultgroup", insertable = true, updatable = true, length = 90)
    private String endresultgroup;
    
    @javax.persistence.Column(name = "endtype", insertable = true, updatable = true, length = 90)
    private Integer endtype;
    
    @javax.persistence.Column(name = "bloq", insertable = true, updatable = true)
    private Integer bloq;
    
    @javax.persistence.Column(name = "forceani", insertable = true, updatable = true, length = 20)
    private String forceani;
    
    
    @OneToOne(mappedBy = "ocmDataEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    private OcmDataExtEntity ocmDataExtEntity;
    
}
