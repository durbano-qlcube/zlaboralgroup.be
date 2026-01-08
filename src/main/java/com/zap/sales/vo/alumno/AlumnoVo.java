package com.zap.sales.vo.alumno;

import java.io.Serializable;
import java.util.Calendar;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.zap.maintenance.service.adapter.TimeDateAdapter;

import lombok.Data;

@Data
public class AlumnoVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idAlumno; 
    private Integer idFormacion; 
    private String nombreCompleto;
    private String dni;
    
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fechaNacimiento;
    
    private String sexo;
    private String nacionalidad;
    private String telefonoContacto;
    private String email;
    private String horarioLaboral;
    private String nivelEstudios;
    private String puesto;
    private Integer idEmpresa;


}
