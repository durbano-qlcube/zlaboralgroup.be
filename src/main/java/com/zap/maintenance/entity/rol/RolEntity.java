package com.zap.maintenance.entity.rol;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;


@Entity
@Table(name = "MNT_ROL")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Rol.findAll", query = "SELECT r FROM RolEntity r"),
    @NamedQuery(name = "Rol.findByIdRol", query = "SELECT r FROM RolEntity r WHERE r.idRol = :idRol"),
    @NamedQuery(name = "Rol.findByRol", query = "SELECT r FROM RolEntity r WHERE r.rol = :rol")})


public class RolEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "idRol")
    private Integer idRol;
    
    
    @Lob
    @Size(max = 2147483647)
    @Column(name = "descripcion")
    private String descripcion;
    
    
    @Size(max = 255)
    @Column(name = "rol")
    private String rol;
    
    
   
    
}
