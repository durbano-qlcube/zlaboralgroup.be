package com.zap.sales.vo.formacion;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.zap.maintenance.service.adapter.TimeDateAdapter;
import com.zap.sales.vo.alumno.AlumnoVo;

import lombok.Data;

@Data
public class FormacionVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idFormacion;
    private String nombre;
    private Integer horas;
    private String areaProfesional;
    private Integer numeroAlumnos;
    
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fechaInicio;
    
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fechaFin;
    
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fechaNotificacionInicioFundae;
    
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fechaNotificacionFinFundae;
    
    private Integer idEmpresa;
    private Integer idVenta;

    private List<AlumnoVo> alumnos;
}
