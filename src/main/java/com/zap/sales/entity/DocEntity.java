package com.zap.sales.entity;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;


import lombok.Data;

@Entity
@Table(name = "ORG_DOC")
@XmlRootElement
@Data
@NamedQueries({ @NamedQuery(name = "DocEntity.findAll", query = "SELECT a FROM DocEntity a"),
	@NamedQuery(name = "DocEntity.findByIdVenta", query = "SELECT a FROM DocEntity a WHERE a.ventaEntity.idVenta= :idVenta"),
	@NamedQuery(name = "DocEntity.findByIdEmpresa", query = "SELECT a FROM DocEntity a WHERE a.empresaEntity.idEmpresa= :idEmpresa"),
	@NamedQuery(name = "DocEntity.findByIdDoc", query = "SELECT a FROM DocEntity a WHERE a.idDoc = :idDoc")

})
public class DocEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID_DOC", nullable = false, insertable = true, updatable = true)
	private Integer idDoc;
	
	@Column(name = "DOCTYPE", length = 500, insertable = true, updatable = true)
	private String docType;

	@Column(name = "PATH", length = 500, insertable = true, updatable = true)
	private String path;

	@Column(name = "FILENAME",length = 500,  insertable = true, updatable = true)
	private Calendar filename;
	
	@Column(name = "FILENAME_ORIGINAL",length = 500,  insertable = true, updatable = true)
	private String filenameOriginal;

	@Column(name = "FILEPATH", length = 500, insertable = true, updatable = true)
	private String filepath;

	@Column(name = "URL", length = 500, insertable = true, updatable = true)
	private String url;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "FECHA_CREACION", insertable = true, updatable = true)
	private Calendar fechaCreacion;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "FECHA_MODIFICACION", insertable = true, updatable = true)
	private Calendar fechaModificacion;

	@Column(name = "USER", length = 500, insertable = true, updatable = true)
	private String user;
	
	
	@javax.persistence.ManyToOne(optional = true, fetch = FetchType.EAGER)
	@javax.persistence.JoinColumn(name = "ID_EMPRESA", nullable = true) 
	private EmpresaEntity empresaEntity;
		
	@javax.persistence.ManyToOne(optional = true, fetch = FetchType.EAGER)
	@javax.persistence.JoinColumn(name = "ID_VENTA", nullable = true) 
	private VentaEntity ventaEntity;

	@Override
	public int hashCode() {
		return Objects.hash(idDoc);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		DocEntity that = (DocEntity) obj;
		return Objects.equals(idDoc, that.idDoc);
	}
}
