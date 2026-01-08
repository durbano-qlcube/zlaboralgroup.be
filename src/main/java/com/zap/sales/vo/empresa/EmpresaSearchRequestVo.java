package com.zap.sales.vo.empresa;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.zap.maintenance.service.adapter.TimeDateAdapter;

import lombok.Data;


@Data
public class EmpresaSearchRequestVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idEmpresa;
    private String razonSocial;
    private String nombreComercial;
    private String cif;
    private String actividadPrincipal;
    private Integer plantillaMedia;

    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fechaCreacion;

    private Boolean existeRlt;
    private Boolean esPyme;
    private String cnae;
    private String domicilioFiscal;
    private String tamanoEmpresa;
    private BigDecimal bonificacion;
    private BigDecimal creditosDisponibles;
    private BigDecimal creditosGastados;
    private String estado;
    private String origin;
    private Long parentCompanyId;
    private String iban;
    private String repreLegalNombreCom;
    private String repreLegalNif;
    private String repreLegalTelefono;
    private String repreLegalEmail;
    private String asesoriaNombre;
    private String asesorNombreCompleto;
    private String asesorTelefono;
    private String uuIdEmpresa;

 
}
