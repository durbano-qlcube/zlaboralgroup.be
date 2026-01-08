	package com.zap.sales.entity;
	
	import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.MapsId;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

	
	@Entity
	@Table(name = "ORG_FORMACION")
	@XmlRootElement
	@Data
	@NamedQueries({
	    @NamedQuery(name = "FormacionEntity.findAll", query = "SELECT f FROM FormacionEntity f"),
	    @NamedQuery(name = "FormacionEntity.loadByIdEmpresa", query = "SELECT f FROM FormacionEntity f where f.empresaEntity.idEmpresa=:idEmpresa"),
	    @NamedQuery(name = "FormacionEntity.loadByIdVenta",  query = "SELECT f FROM FormacionEntity f JOIN f.ventaEntities v WHERE v.idVenta = :idVenta"),

	    @NamedQuery(name = "FormacionEntity.findById", query = "SELECT f FROM FormacionEntity f where f.idFormacion=:idFormacion")
	})
	
	public class FormacionEntity implements Serializable, Comparable<FormacionEntity> {
	
	    private static final long serialVersionUID = 1L;
	
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name = "ID_FORMACION", nullable = false, insertable = true, updatable = true)
	    private Integer idFormacion;
	
	    @Column(name = "NOMBRE", insertable = true, updatable = true, length = 300)
	    private String nombre;
	
	    @Column(name = "HORAS", insertable = true, updatable = true)
	    private Integer horas;
	
	    @Column(name = "AREA_PROFESIONAL", insertable = true, updatable = true, length = 300)
	    private String areaProfesional;
	
	    @Column(name = "NUMERO_ALUMNOS", insertable = true, updatable = true)
	    private Integer numeroAlumnos;
	  
	    @Column(name = "FECHA_INICIO", insertable = true, updatable = true)
	    private Calendar fechaInicio;
	   
	    @Column(name = "FECHA_FIN", insertable = true, updatable = true)
	    private Calendar fechaFin;
	    
	    @Column(name = "FECHA_NOTIF_INICIO_FUNDAE", insertable = true, updatable = true)
	    private Calendar fechaNotificacionInicioFundae;
	  
	    @Column(name = "FECHA_NOTIF_FIN_FUNDAE", insertable = true, updatable = true)
	    private Calendar fechaNotificacionFinFundae;
	    
		@Temporal(TemporalType.TIMESTAMP)
		@Column(name = "DATE_CREATION",  unique = false, nullable = true, insertable = true, updatable = true)
		private Calendar fxCreation;
	
		@Temporal(TemporalType.TIMESTAMP)
		@Column(name = "DATE_MODIFICATION",  unique = false, nullable = true, insertable = true, updatable = true)
		private Calendar fxModification;
		
	    @Column(name = "ORIGIN_ID_FORMACION", insertable = true, updatable = true)
	    private Integer originIdFormacion;
	   
	    @javax.persistence.ManyToOne(optional = false, fetch = FetchType.EAGER)
		@javax.persistence.JoinColumn(name = "ID_EMPRESA")
		private EmpresaEntity empresaEntity;
	    
	    @ManyToMany(mappedBy = "formacionEntities", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
	    private Set<AlumnoEntity> alumnoEntities = new HashSet<>();
		 
//	    @OneToOne
//	    @MapsId // Indica que el idFormacion será el mismo que idVenta de VentaEntity
//	    @JoinColumn(name = "ID_FORMACION")  // Especifica la columna de la clave foránea
//	    private VentaEntity ventaEntity;


	    @OneToMany(mappedBy = "formacionEntity", cascade={CascadeType.REMOVE}, fetch = FetchType.LAZY)
		 private Set<VentaEntity> ventaEntities = new LinkedHashSet <VentaEntity>();
		 
		 
		 
		 @Override
		    public int compareTo(FormacionEntity other) {
		        return this.idFormacion.compareTo(other.idFormacion);
		    }
		 @Override
		    public int hashCode() {
		      
		        return Objects.hash(idFormacion);  
		    }

		    @Override
		    public boolean equals(Object o) {
		        if (this == o) return true;
		        if (o == null || getClass() != o.getClass()) return false;
		        FormacionEntity that = (FormacionEntity) o;
		        return Objects.equals(idFormacion, that.idFormacion);
		    }
	}
