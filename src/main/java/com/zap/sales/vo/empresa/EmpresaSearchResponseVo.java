package com.zap.sales.vo.empresa;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.zap.maintenance.service.adapter.TimeDateAdapter;

import lombok.Data;


@Data
public class EmpresaSearchResponseVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idEmpresa;
    private String razonSocial;
    private String nombreComercial;
    private String cif;
    private String actividadPrincipal;
    private Integer plantillaMedia;

    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fechaAlta;

    private Boolean existeRlt;
    private Boolean esPyme;
    private String cnae;
    private String domicilioFiscal;
    private String tamanoEmpresa;
    private BigDecimal bonificacion;
    private BigDecimal creditosDisponibles;
    private BigDecimal creditosGastados;
    private String estado;
    private String iban;
    private String repreLegalNombreCom;
    private String repreLegalNif;
    private String repreLegalTelefono;
    private String repreLegalEmail;
    private String asesoriaNombre;
    private String asesorNombreCompleto;
    private String asesorTelefono;
    private String uuIdEmpresa;
    private String asesorEmail;
    private String origin;
    private String originUserUsername;

    public EmpresaSearchResponseVo(Integer idEmpresa, String razonSocial, String nombreComercial, String cif,
                                    String actividadPrincipal, Integer plantillaMedia, Calendar fechaAlta,
                                    Boolean existeRlt, Boolean esPyme, String cnae, String domicilioFiscal,
                                    String tamanoEmpresa, BigDecimal bonificacion, BigDecimal creditosDisponibles,
                                    BigDecimal creditosGastados, String estado,  String iban,
                                    String repreLegalNombreCom, String repreLegalNif, String repreLegalTelefono,
                                    String repreLegalEmail, String asesoriaNombre, String asesorNombreCompleto,
                                    String asesorTelefono, String uuIdEmpresa,  String asesorEmail, String origin, 
                                    String originUserUsername) {
        this.idEmpresa = idEmpresa;
        this.razonSocial = razonSocial;
        this.nombreComercial = nombreComercial;
        this.cif = cif;
        this.actividadPrincipal = actividadPrincipal;
        this.plantillaMedia = plantillaMedia;
        this.fechaAlta = fechaAlta;
        this.existeRlt = existeRlt;
        this.esPyme = esPyme;
        this.cnae = cnae;
        this.domicilioFiscal = domicilioFiscal;
        this.tamanoEmpresa = tamanoEmpresa;
        this.bonificacion = bonificacion;
        this.creditosDisponibles = creditosDisponibles;
        this.creditosGastados = creditosGastados;
        this.estado = estado;
        this.iban = iban;
        this.repreLegalNombreCom = repreLegalNombreCom;
        this.repreLegalNif = repreLegalNif;
        this.repreLegalTelefono = repreLegalTelefono;
        this.repreLegalEmail = repreLegalEmail;
        this.asesoriaNombre = asesoriaNombre;
        this.asesorNombreCompleto = asesorNombreCompleto;
        this.asesorTelefono = asesorTelefono;
        this.uuIdEmpresa = uuIdEmpresa;
        this.asesorEmail = asesorEmail;
        this.origin = origin;
        this.originUserUsername = originUserUsername;
    }
}
