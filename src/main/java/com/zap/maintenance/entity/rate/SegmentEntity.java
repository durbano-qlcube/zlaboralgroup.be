package com.zap.maintenance.entity.rate;

import java.io.Serializable;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;


import lombok.Data;

/**
 *
 * @author AVV
 */
@Entity
@Table(name = "MNT_SEGMENT")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "SegmentEntity.loadAll", query = "SELECT s FROM SegmentEntity s"),
//    @NamedQuery(name = "Segmentoventa.findById", query = "SELECT s FROM SegmentEntity s WHERE s.id = :id"),
//    @NamedQuery(name = "Segmentoventa.findByEstado", query = "SELECT s FROM SegmentEntity s WHERE s.estado = :estado"),
//    @NamedQuery(name = "Segmentoventa.findByEliminada", query = "SELECT s FROM SegmentEntity s WHERE s.eliminada = :eliminada")
    
})

@Data
public class SegmentEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @javax.persistence.Id
	@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
	@javax.persistence.Column(name = "ID_SEGMENTO", nullable = false, insertable = true, updatable = true, length = 19)
    private Integer idSegment;
    
    
    
    @javax.persistence.Column(name = "NAME", insertable = true, updatable = true, length = 50)
    private String name;
    
    
    @javax.persistence.Column(name = "DESCRIPTION", insertable = true, updatable = true, length = 300)
    private String description;
    
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATE_CREATION",  unique = false, nullable = true, insertable = true, updatable = true)
	private Calendar fxCreation;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATE_MODIFICATION",  unique = false, nullable = true, insertable = true, updatable = true)
	private Calendar fxModification;
	
    
	 @OneToMany(mappedBy = "segmentoEntity", cascade={CascadeType.REMOVE}, fetch = FetchType.LAZY)
	 private Set<RateEntity> rateEntities = new LinkedHashSet <RateEntity>();
	 

  
    
}
