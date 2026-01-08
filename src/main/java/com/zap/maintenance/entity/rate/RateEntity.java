package com.zap.maintenance.entity.rate;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;


@Entity
@Table(name = "MNT_RATE")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RateEntity.loadAll", query = "SELECT t FROM RateEntity t"),
    @NamedQuery(name = "RateEntity.loadByIdSegment", query = "SELECT t FROM RateEntity t WHERE t.segmentoEntity.idSegment = :idSegment"),
//    @NamedQuery(name = "RateEntity.findByEstado", query = "SELECT t FROM RateEntity t WHERE t.estado = :estado"),
//    @NamedQuery(name = "RateEntity.findByEliminada", query = "SELECT t FROM RateEntity t WHERE t.eliminada = :eliminada")
    })
@Data
public class RateEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    
    
	@javax.persistence.Id
	@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
	@javax.persistence.Column(name = "ID_RATE", nullable = false, insertable = true, updatable = true, length = 19)
    private Integer idRate;
    
	
	@javax.persistence.Column(name = "NAME", insertable = true, updatable = true, length = 50)
    private String name;
    
    
	@javax.persistence.Column(name = "DESCRIPTION", insertable = true, updatable = true, length = 300)
    private String description;
    
    
	@javax.persistence.Column(name = "DISCOUNT", insertable = true, updatable = true, length = 300)
    private String discounts;
	
//    @Lob
//    @Size(max = 65535)
//    @Column(name = "ponderacion")
//    private String ponderacion;
//    
//    
//    @Column(name = "estado")
//    private Integer estado;
//    
//    
//    @Column(name = "eliminada")
//    private Integer eliminada;
   
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATE_CREATION",  unique = false, nullable = true, insertable = true, updatable = true)
	private Calendar fxCreation;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATE_MODIFICATION",  unique = false, nullable = true, insertable = true, updatable = true)
	private Calendar fxModification;


	 @javax.persistence.ManyToOne(optional = false, fetch = FetchType.EAGER)
	 @javax.persistence.JoinColumn(name = "ID_SEGMENTO")
	 private SegmentEntity segmentoEntity;
    
}
