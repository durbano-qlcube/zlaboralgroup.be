package com.zap.gestdirect.vo;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.zap.maintenance.service.adapter.TimeDateAdapter;
import com.zap.sales.vo.alumno.AlumnoVo;

import lombok.Data;

@Data
public class GestdirectFormacionVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idFormacion;
    private String nombre;
    private Integer horas;
    private String areaProfesional;
    private Integer numeroAlumnos;
    
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Long fechaInicio;
    
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Long fechaFin;
    
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Long fechaNotificacionInicioFundae;
    
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Long fechaNotificacionFinFundae;
    
    private Integer idEmpresa;

    private List<AlumnoVo> alumnos;
}
