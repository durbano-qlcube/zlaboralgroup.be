package com.zap.sales.vo.venta;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.zap.maintenance.service.adapter.TimeDateAdapter;

import lombok.Data;

@Data
public class VentaSearchRequestVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fxVenta;
    
    private StatusVentaEnum status;
	private String usernameAgente;
	private String usernameCoordinador;
	private String usernameSupervisor;
	private String uuIdAgente;
	private String uuIdCoordinador;
	private String uuIdSupervisor;
	private String originUserUsername;
	private String colaboradorUuId;

    @XmlJavaTypeAdapter(TimeDateAdapter.class)
	private Calendar fxVentaInit;

    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fxVentaFin;
    
    
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
	private Calendar fxInitCurso;

    @XmlJavaTypeAdapter(TimeDateAdapter.class)
	private Calendar fxFinCurso;
    
    private String cif;
    private String nombreComercial;
    
	private Boolean VpdteDoc;	
	private Boolean VpdteInicioCurso;

	private Boolean VejecucionCurso;
	private Boolean VcursoFinalizado;
	private Boolean VnotificacionFundae;
	private Boolean Vcancelado;
	private Boolean Vcobrado;
	private Boolean Vcartera;
    private String  phoneContacto;

    private Long parentCompanyId;
    private List<Long> parentCompanyIds;
    private String uuidProvider;
    private String uuidSubProvider;


    
}
