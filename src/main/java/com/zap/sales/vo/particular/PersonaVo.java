package com.zap.sales.vo.particular;

import java.io.Serializable;
import java.util.Calendar;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.zap.maintenance.service.adapter.TimeDateAdapter;

import lombok.Data;

@Data
public class PersonaVo implements Serializable {

    private static final long serialVersionUID = 1L;
    
	private Integer idPersona;
    private String nombre;
    private String dni;
    private String telefono;
    private String email;
    private String direccion;
    private String codigoPostal;
    private String ciudad;
    private String actividadPrincipal;
    private String iban;
    private String razonSocial;
    private String nombreComercial;
    private String cif;
    private Boolean esPyme;
	private Integer numeroTrabajadores;
    
	@XmlJavaTypeAdapter(TimeDateAdapter.class)
	private Calendar fechaCreacion;
	
	@XmlJavaTypeAdapter(TimeDateAdapter.class)
	private Calendar fechaModificacion;
}
