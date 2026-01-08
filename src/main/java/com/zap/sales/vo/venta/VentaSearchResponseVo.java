package com.zap.sales.vo.venta;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.zap.maintenance.service.adapter.TimeDateAdapter;

import lombok.Data;

@Data
public class VentaSearchResponseVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String uuid;  
    
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fxVenta;
    
    private String cif;  
    private String nombreComercial;  
    private String empresaStatus;  
    private String nombreFormacion;  
    private StatusVentaEnum status;
    private Boolean charged;  
	private Boolean pdteFirma;
    private String usernameCoordinador;
    private String usernameAgente; 
    private String originUserUsername;
    private String uuidProvider;
    private String providerName;
    private BigDecimal price;
    
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fechaInicio;  
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fechaFin;
    private Boolean pdteCobroInicioCurso;
    private Boolean pdteCobroFinCurso;
    
    public VentaSearchResponseVo(String uuid, Calendar fxVenta, String cif, String nombreComercial, String empresaStatus,
            String nombreFormacion, StatusVentaEnum status, Boolean charged, Boolean pdteFirma,
            String usernameCoordinador, String usernameAgente,
            String originUserUsername, Calendar fechaInicio, Calendar fechaFin, BigDecimal price,
            Boolean pdteCobroInicioCurso, Boolean pdteCobroFinCurso, String uuidProvider) {
		this.uuid = uuid;
		this.fxVenta = fxVenta;
		this.cif = cif;
		this.nombreComercial = nombreComercial;
		this.empresaStatus = empresaStatus;
		this.nombreFormacion = nombreFormacion;
		this.status = status;
		this.charged = charged;
		this.pdteFirma = pdteFirma;
		this.usernameCoordinador = usernameCoordinador;
		this.usernameAgente = usernameAgente;
		this.originUserUsername = originUserUsername;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
		this.price = price;
		this.pdteCobroInicioCurso = pdteCobroInicioCurso;
		this.pdteCobroFinCurso = pdteCobroFinCurso;
		this.uuidProvider = uuidProvider;
		}


}
