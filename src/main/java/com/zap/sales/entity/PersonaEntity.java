package com.zap.sales.entity;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.*;

import lombok.Data;

@Entity
@Table(name = "ORG_PERSONA")
@Data

@NamedQueries({
    @NamedQuery(name = "PersonaEntity.findAll", query = "SELECT e FROM PersonaEntity e"),
    @NamedQuery(name = "PersonaEntity.findByDni", query = "SELECT e FROM PersonaEntity e WHERE e.dni = :dni"),
    @NamedQuery(name = "PersonaEntity.loadByIdPersona",  query = "SELECT f FROM PersonaEntity f WHERE f.idPersona = :idPersona"),

})
public class PersonaEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PERSONA", nullable = false, updatable = false)
    private Integer idPersona;

    @Column(name = "NOMBRE", nullable = false, length = 255)
    private String nombre;

    @Column(name = "DNI", nullable = false, unique = true, length = 50)
    private String dni;

    @Column(name = "TELEFONO", length = 20)
    private String telefono;

    @Column(name = "EMAIL", length = 255)
    private String email;

    @Column(name = "DIRECCION", length = 500)
    private String direccion;

    @Column(name = "CODIGO_POSTAL", length = 20)
    private String codigoPostal;

    @Column(name = "CIUDAD", length = 255)
    private String ciudad;

    @Column(name = "ACTIVIDAD_PRINCIPAL", length = 255)
    private String actividadPrincipal;

    @Column(name = "IBAN", length = 34, unique = true)
    private String iban;

    @Column(name = "RAZON_SOCIAL", length = 255)
    private String razonSocial;

    @Column(name = "NOMBRE_COMERCIAL", length = 255)
    private String nombreComercial;

    @Column(name = "CIF", length = 50, unique = true)
    private String cif;

    @Column(name = "ES_PYME")
    private Boolean esPyme;
    
    @Column(name = "NUMERO_TRABAJADORES", nullable = true, updatable = true)
    private Integer numeroTrabajadores;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_CREACION", insertable = true, updatable = true)
    private Calendar fechaCreacion;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_MODIFICACION", insertable = true, updatable = true)
    private Calendar fechaModificacion;
}
