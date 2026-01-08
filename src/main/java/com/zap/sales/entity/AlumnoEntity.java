package com.zap.sales.entity;

import java.io.Serializable;
import java.sql.Date;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Entity
@Table(name = "ORG_ALUMNO")
@XmlRootElement
@Data
@NamedQueries({ @NamedQuery(name = "AlumnoEntity.findAll", query = "SELECT a FROM AlumnoEntity a"),
	@NamedQuery(name = "AlumnoEntity.findByCursoId", query = "SELECT a FROM AlumnoEntity a WHERE a.idAlumno= :idAlumno"),
	@NamedQuery(name = "AlumnoEntity.countCursosByAlumnoId", query = "SELECT COUNT(f) FROM AlumnoEntity a JOIN a.formacionEntities f WHERE a.idAlumno = :idAlumno"), 
	@NamedQuery(name = "AlumnoEntity.findAlumnosByFormacionId", query = "SELECT a FROM AlumnoEntity a JOIN a.formacionEntities f WHERE f.idFormacion = :idFormacion"),
	@NamedQuery(name = "AlumnoEntity.findAlumnosByIdEmpresa", query = "SELECT a FROM AlumnoEntity a  WHERE a.empresaEntity.idEmpresa = :idEmpresa"),
	@NamedQuery(name = "AlumnoEntity.findByDni", query = "SELECT a FROM AlumnoEntity a WHERE a.dni = :dni")
})
public class AlumnoEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID_ALUMNO", nullable = false, insertable = true, updatable = true)
	private Integer idAlumno;

	@Column(name = "NOMBRECOMPLETO", length = 255, insertable = true, updatable = true)
	private String nombreCompleto;

	@Column(name = "DNI", length = 20, insertable = true, updatable = true, unique = true)
	private String dni;

	@Column(name = "FECHA_NACIMIENTO", insertable = true, updatable = true)
	private Calendar fechaNacimiento;

	@Column(name = "SEXO", length = 10, insertable = true, updatable = true)
	private String sexo;

	@Column(name = "NACIONALIDAD", length = 255, insertable = true, updatable = true)
	private String nacionalidad;

	@Column(name = "TELEFONO_CONTACTO", length = 20, insertable = true, updatable = true)
	private String telefonoContacto;

	@Column(name = "EMAIL", length = 255, insertable = true, updatable = true)
	private String email;

	@Column(name = "HORARIO_LABORAL", length = 255, insertable = true, updatable = true)
	private String horarioLaboral;

	@Column(name = "NIVEL_ESTUDIOS", length = 255, insertable = true, updatable = true)
	private String nivelEstudios;

	@Column(name = "PUESTO", length = 255, insertable = true, updatable = true)
	private String puesto;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "FECHA_CREACION", insertable = true, updatable = true)
	private Calendar fechaCreacion;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "FECHA_MODIFICACION", insertable = true, updatable = true)
	private Calendar fechaModificacion;

	@javax.persistence.ManyToOne(optional = false, fetch = FetchType.EAGER)
	@javax.persistence.JoinColumn(name = "ID_EMPRESA")
	private EmpresaEntity empresaEntity;

	@ManyToMany()
	@JoinTable(name = "ORG_FORMACION_ALUMNO", joinColumns = @JoinColumn(name = "ID_ALUMNO"), inverseJoinColumns = @JoinColumn(name = "ID_FORMACION"))
	private Set<FormacionEntity> formacionEntities = new HashSet<FormacionEntity>();

	@Override
	public int hashCode() {
		return Objects.hash(idAlumno);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		AlumnoEntity that = (AlumnoEntity) obj;
		return Objects.equals(idAlumno, that.idAlumno);
	}
}
