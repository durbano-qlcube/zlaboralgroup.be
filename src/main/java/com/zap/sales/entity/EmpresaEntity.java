package com.zap.sales.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Entity
@Table(name = "ORG_EMPRESA")
@XmlRootElement
@Data

@NamedQueries({
    @NamedQuery(name = "EmpresaEntity.findAll", query = "SELECT e FROM EmpresaEntity e"),
    @NamedQuery(name = "EmpresaEntity.findByCif", query = "SELECT e FROM EmpresaEntity e WHERE e.cif = :cif"),
    @NamedQuery(name = "EmpresaEntity.findByuuIdEmpresa", query = "SELECT e FROM EmpresaEntity e WHERE e.uuIdEmpresa = :uuIdEmpresa"),
    @NamedQuery(name = "EmpresaEntity.countVentasByEmpresa", query = "SELECT COUNT(v) FROM VentaEntity v WHERE v.empresaEntity.uuIdEmpresa = :uuIdEmpresa"),
    @NamedQuery(name = "EmpresaEntity.findByidEmpresa", query = "SELECT e FROM EmpresaEntity e WHERE e.idEmpresa = :idEmpresa"),
    @NamedQuery(name = "EmpresaEntity.loadByEstado", query = "SELECT e FROM EmpresaEntity e WHERE e.estado = :estado")
})
public class EmpresaEntity implements Serializable {

    private static final long serialVersionUID = 1L;	

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_EMPRESA", nullable = false, insertable = true, updatable = true)
    private Integer idEmpresa;

    @Column(name = "RAZON_SOCIAL", length = 255, insertable = true, updatable = true)
    private String razonSocial;

    @Column(name = "NOMBRE_COMERCIAL", length = 255, insertable = true, updatable = true)
    private String nombreComercial;

    @Column(name = "CIF", length = 20, insertable = true, updatable = true, unique=true)
    private String cif;

    @Column(name = "ACTIVIDAD_PRINCIPAL", length = 255, insertable = true, updatable = true)
    private String actividadPrincipal;

    @Column(name = "PLANTILLA_MEDIA", insertable = true, updatable = true)
    private Integer plantillaMedia;

    @Column(name = "EXISTE_RLT", insertable = true, updatable = true)
    private Boolean existeRlt;

    @Column(name = "ES_PYME", insertable = true, updatable = true)
    private Boolean esPyme;

    @Column(name = "CNAE", length = 20, insertable = true, updatable = true)
    private String cnae;

    @Column(name = "DOMICILIO_FISCAL", columnDefinition = "TEXT", insertable = true, updatable = true)
    private String domicilioFiscal;

    @Column(name = "CODIGO_POSTAL", length = 20, insertable = true, updatable = true)
    private String codigoPostal;

    @Column(name = "TAMANO_EMPRESA", length = 100, insertable = true, updatable = true)
    private String tamanoEmpresa;

    @Column(name = "BONIFICACION",  length = 100, insertable = true, updatable = true)
    private BigDecimal  bonificacion;

    @Column(name = "CREDITOS_DISPONIBLES", insertable = true, updatable = true)
    private BigDecimal  creditosDisponibles;

    @Column(name = "CREDITOS_GASTADOS", insertable = true, updatable = true)
    private BigDecimal  creditosGastados;

    @Column(name = "ESTADO", length = 100, insertable = true, updatable = true)
    private String estado;

    @Column(name = "PARENT_COMPANY_ID", insertable = true, updatable = true)
    private Long parentCompanyId;

    @Column(name = "IBAN", length = 34, insertable = true, updatable = true)
    private String iban;

    @Column(name = "REPRE_LEGAL_NOMBRE_COM", length = 255, insertable = true, updatable = true)
    private String repreLegalNombreCom;

    @Column(name = "REPRE_LEGAL_NIF", length = 20, insertable = true, updatable = true)
    private String repreLegalNif;

    @Column(name = "REPRE_LEGAL_TELEFONO", length = 20, insertable = true, updatable = true)
    private String repreLegalTelefono;

    @Column(name = "REPRE_LEGAL_EMAIL", length = 255, insertable = true, updatable = true)
    private String repreLegalEmail;

    @Column(name = "UUID_EMPRESA", length = 255, unique = true, insertable = true, updatable = true)
    private String uuIdEmpresa;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_ALTA", insertable = true, updatable = true)
    private Calendar fechaAlta;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_CREACION", insertable = true, updatable = true)
    private Calendar fechaCreacion;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_MODIFICACION", insertable = true, updatable = true)
    private Calendar fechaModificacion;
    
    
    
    
    
    //INIT >> INFO GEST DIRECT
    
	@javax.persistence.Column(name = "ORIGIN", insertable = true, updatable = true, length = 50)
    private String origin;
	
	@javax.persistence.Column(name = "ORIGIN_USER_USERNAME", insertable = true, updatable = true, length = 100)
    private String originUserUsername;

	@javax.persistence.Column(name = "ORIGIN_USER_USERUUID", insertable = true, updatable = true, length = 100)
	private String originUserUuid;
	
	@javax.persistence.Column(name = "ORIGIN_GESTORIA_UUID", insertable = true, updatable = true, length = 100)
	private String originGestoriaUuid;
	
	@javax.persistence.Column(name = "ORIGIN_EMPRESA_UUID", insertable = true, updatable = true, length = 100)
	private String originEmpresaUuid;
	
	
    @Column(name = "ASESORIA_NOMBRE", length = 255, insertable = true, updatable = true)
    private String asesoriaNombre;

	@Column(name = "ASESOR_NOMBRE_COMPLETO", length = 255, insertable = true, updatable = true)
    private String asesorNombreCompleto;

    @Column(name = "ASESOR_TELEFONO", length = 20, insertable = true, updatable = true)
    private String asesorTelefono;
    
    @Column(name = "ASESOR_EMAIL", length = 100, insertable = true, updatable = true)
    private String asesorEmail;
	
    //FIN >> INFO GEST DIRECT
	
    @Column(name = "PERSONA_CONTACTO", insertable = true, updatable = true, length = 50)
    private String personaContacto;
	
    @Column(name = "EMAIL_CONTACTO", insertable = true, updatable = true, length = 50)
    private String emailContacto;
	
    @Column(name = "PUESTO_CONTACTO", insertable = true, updatable = true, length = 50)
    private String puestoContacto;
	
    @Column(name = "TELEFONO_CONTACTO", insertable = true, updatable = true, length = 50)
    private String telefonoContacto;
	
    @Column(name = "OBSERVACIONES", insertable = true, updatable = true, length = 250)
    private String observaciones;

    
    
	 @OneToMany(mappedBy = "empresaEntity", cascade={CascadeType.REMOVE}, fetch = FetchType.LAZY)
	 private Set<AlumnoEntity> alumnoEntityEntities = new LinkedHashSet <AlumnoEntity>();
	 
	 @OneToMany(mappedBy = "empresaEntity", cascade={CascadeType.REMOVE}, fetch = FetchType.LAZY)
	 private Set<FormacionEntity> formacionEntities = new LinkedHashSet <FormacionEntity>();
	 
	 @OneToMany(mappedBy = "empresaEntity", cascade={CascadeType.REMOVE}, fetch = FetchType.LAZY)
	 private Set<VentaEntity> ventaEntities = new LinkedHashSet <VentaEntity>();
	 
	 @Override
	    public int hashCode() {
	        return Objects.hash(idEmpresa);
	    }

	    @Override
	    public boolean equals(Object obj) {
	        if (this == obj) return true;
	        if (obj == null || getClass() != obj.getClass()) return false;
	        EmpresaEntity that = (EmpresaEntity) obj;
	        return Objects.equals(idEmpresa, that.idEmpresa);
	    }
    
}
