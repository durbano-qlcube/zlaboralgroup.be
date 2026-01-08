package com.zap.sales.vo.particular;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.zap.maintenance.service.adapter.TimeDateAdapter;
import com.zap.sales.vo.venta.StatusVentaEnum;

import lombok.Data;

@Data
public class VentaPartSearchResponseVo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String uuid;

	@XmlJavaTypeAdapter(TimeDateAdapter.class)
	private Calendar fxVenta;

	private String dni;
	private String nombre;
	private StatusVentaEnum status;
	private Integer charged;
	private Boolean pdteFirma;
	private String usernameCoordinador;
	private String usernameAgente;

    private String nombreFormacion;  
    
	@XmlJavaTypeAdapter(TimeDateAdapter.class)
	private Calendar fechaInicio;
	@XmlJavaTypeAdapter(TimeDateAdapter.class)
	private Calendar fechaFin;
	
	private BigDecimal price;
    private String telefono;  
	private Boolean pdteCobroInicioCurso;
	private Boolean pdteCobroFinCurso;

	public VentaPartSearchResponseVo(String uuid, Calendar fxVenta, String dni, String nombre, StatusVentaEnum status,
			Integer charged, Boolean pdteFirma, String usernameCoordinador, String usernameAgente,   String nombreFormacion,
			Calendar fechaInicio, Calendar fechaFin, BigDecimal price,Boolean pdteCobroInicioCurso, Boolean pdteCobroFinCurso, String telefono) {
		this.uuid = uuid; 
		this.fxVenta = fxVenta;
		this.dni = dni;
		this.nombre = nombre;
		this.status = status;
		this.charged = charged;
		this.pdteFirma = pdteFirma;
		this.usernameCoordinador = usernameCoordinador;
		this.usernameAgente = usernameAgente;
		this.nombreFormacion = nombreFormacion;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
		this.price = price;
		this.pdteCobroInicioCurso = pdteCobroInicioCurso;
		this.pdteCobroFinCurso = pdteCobroFinCurso;
		this.telefono = telefono;

		
	}

}