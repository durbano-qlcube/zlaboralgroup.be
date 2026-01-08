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
@javax.persistence.Table(name = "skill_formacionleadsagenda_scheduled")
@javax.persistence.NamedQueries({
//	@javax.persistence.NamedQuery(name = "OcmDataEntity.loadAll", query = "select AuthUser from AuthUserEntity AS AuthUser order by AuthUser.id desc"),
//	@javax.persistence.NamedQuery(name = "OcmDataEntity.load", query = "select AuthUser from AuthUserEntity AS AuthUser WHERE AuthUser.id=:id"),
//	@javax.persistence.NamedQuery(name = "OcmDataEntity.loadByIdOcm", query="SELECT  lead FROM DkvMotorLeadsEntity AS lead WHERE lead.idOcm=:idOcm"),
//	@javax.persistence.NamedQuery(name = "OcmDataEntity.loadByHubspotStatus", query="SELECT lead FROM DkvMotorLeadsEntity AS lead WHERE lead.hubspotStatus=:hubspotStatus order by lead.dateInsert desc"),

	
	})
@Data
public class OcmAgendaEntity  implements Serializable
{

    private static final long serialVersionUID = -2109966671878671439L;

    @Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id", unique=true, insertable=true, updatable=true, nullable=false)
    private Integer id;
    
    
    @javax.persistence.Column(name = "status", insertable = true, updatable = true)
    private Integer status;
    
    @javax.persistence.Column(name = "priority", insertable = true, updatable = true)
    private Integer priority;
    
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "datenextcall",  unique = false, nullable = true, insertable = true, updatable = true)
    private Calendar dateNextcall;
    
    @javax.persistence.Column(name = "scheduledagent", insertable = true, updatable = true, length = 20)
    private String scheduledagent;
    
    @javax.persistence.Column(name = "skilldata", insertable = true, updatable = true, length = 20)
    private String skilldata;
    
    @javax.persistence.Column(name = "scriptsource", insertable = true, updatable = true, length = 20)
    private String scriptsource;
    
    @javax.persistence.Column(name = "idreg", unique = true,  insertable= true, updatable = true)
    private Integer idreg;
    
    @javax.persistence.Column(name = "skill", insertable = true, updatable = true, length = 45)
    private String skill;
    
    @javax.persistence.Column(name = "idLoad", unique = true,  insertable= true, updatable = true)
    private Integer idLoad;

    @javax.persistence.Column(name = "active", insertable = true, updatable = true)
    private Integer active;
    
    
    
    
}
