package com.zap.gestdirect.vo;

import java.io.Serializable;
import java.util.Calendar;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.zap.maintenance.service.adapter.TimeDateAdapter;

import lombok.Data;

@Data
public class GestdirectAlumnoVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idAlumno; 
    private String nombreCompleto;
    private String dni;  

    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Long fechaNacimiento;
   
    private String sexo;
    private String nacionalidad;
    private String telefonoContacto;
    private String email;
    private String horarioLaboral;
    private String nivelEstudios;
    private String puesto;
   
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fechaCreacion;
   
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fechaModificacion;

 
    private Integer idEmpresa;
    private Integer idFormacion; 


}
