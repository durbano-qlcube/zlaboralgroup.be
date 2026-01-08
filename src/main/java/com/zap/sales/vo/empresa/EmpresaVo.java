package com.zap.sales.vo.empresa;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.zap.maintenance.service.adapter.TimeDateAdapter;
import com.zap.sales.vo.alumno.AlumnoVo;
import com.zap.sales.vo.venta.VentaExtVo;

import lombok.Data;

@Data
public class EmpresaVo implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer idEmpresa;
	private String razonSocial;
	private String nombreComercial;
	private String cif;
	private String actividadPrincipal;
	private Integer plantillaMedia;
	private Boolean existeRlt;
	private Boolean esPyme;
	private String cnae;
	private String domicilioFiscal;
        private String tamanoEmpresa;
        private BigDecimal bonificacion;
        private BigDecimal creditosDisponibles;
        private BigDecimal creditosGastados;
        private String estado;
        private Long parentCompanyId;
        private String parentCompanyName;
        private String iban;
	private String repreLegalNombreCom;
	private String repreLegalNif;
	private String repreLegalTelefono;
        private String repreLegalEmail;
        private String uuIdEmpresa;
	


	
	
	// ORIGEN GESTDIRECT
	private String origin;    // ORIGEN
	private String originUserUsername; //USER QUE HACE LA ACCION
	private String originUserUuid;
	private String originEmpresaUuid; // UUID DE REFERENCIA EN GESTDIRECT
	private String originGestoriaUuid;//
	
	private String asesoriaNombre;
	private String asesorNombreCompleto;
	private String asesorTelefono;
	private String asesorEmail;
	
	
	

	@XmlJavaTypeAdapter(TimeDateAdapter.class)
	private Calendar fechaAlta;

	@XmlJavaTypeAdapter(TimeDateAdapter.class)
	private Calendar fechaCreacion;
	@XmlJavaTypeAdapter(TimeDateAdapter.class)
	private Calendar fechaModificacion;
	
	
	private String personaContacto;
	private String emailContacto;
	private String puestoContacto;
	private String telefonoContacto;
	private String observaciones;



	
	private List<VentaExtVo> ventas;
	private List<AlumnoVo> trabajadores;

}
