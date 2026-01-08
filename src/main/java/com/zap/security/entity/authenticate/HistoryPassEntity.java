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


import lombok.Data;




@Cacheable(true)
@javax.persistence.Entity
@javax.persistence.Table(name = "SEC_HISTORY_PASS")
@javax.persistence.NamedQueries({
	@javax.persistence.NamedQuery(name = "HistoryPassEntity.loadAll", query = "select AuthUser from HistoryPassEntity AS AuthUser order by AuthUser.id desc"),
	@javax.persistence.NamedQuery(name = "HistoryPassEntity.load", query = "select AuthUser from HistoryPassEntity AS AuthUser WHERE AuthUser.id=:id"),
	@javax.persistence.NamedQuery(name = "HistoryPassEntity.loadByUuid", query="SELECT  AuthUser FROM HistoryPassEntity AS AuthUser WHERE AuthUser.uuid=:uuid"),
	})
@Data
public class HistoryPassEntity  implements Serializable
{

    private static final long serialVersionUID = -2109966671878671439L;

    @Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="ID", unique=true, insertable=true, updatable=true, nullable=false)
    private Long id;
    
    @javax.persistence.Column(name = "UUID", insertable = true, updatable = true, length = 200)
    private String uuid;
    
    @javax.persistence.Column(name = "PASS", insertable = true, updatable = true, length = 200)
    private String pass;
    
       
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATE_CREATION",  unique = false, nullable = true, insertable = true, updatable = true)
	private Calendar fxCreation;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATE_MODIFICATION",  unique = false, nullable = true, insertable = true, updatable = true)
	private Calendar fxModification;

	
	
	
	
	 
    

    
}
