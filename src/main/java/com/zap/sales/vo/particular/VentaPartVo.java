package com.zap.sales.vo.particular;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import com.zap.maintenance.service.adapter.TimeDateAdapter;
import com.zap.sales.vo.venta.StatusVentaEnum;
import com.zap.stripe.vo.StatusStripeEnum;

import lombok.Data;

@Data
public class VentaPartVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idVenta;
    private Integer idPersona;
    private Integer idFormacion;


    private String uuId;

    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fxVenta;
    
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fxCreation;
    
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fxModification;
    
    private StatusVentaEnum status;

    private BigDecimal price;
    private BigDecimal priceDeductedExpenses;
    private BigDecimal priceWithIva;
    private BigDecimal iva;
    private BigDecimal commission;
    private Integer charged;
	private Boolean pdteFirma;
	private Boolean pdteCobroInicioCurso;
	private Boolean pdteCobroFinCurso;

    private String usernameAgente;
    private String usernameCoordinador;
    private String usernameSupervisor;
    private String uuIdAgente;
    private String uuIdCoordinador;
    private String uuIdSupervisor;

    private String observaciones;

    private Long parentCompanyId;
    
    ///DATOS FORMACION
    private String nombre;
    private Integer horas;
    private String areaProfesional;
    
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fechaInicio;
    
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fechaFin;
    
	private String stripePaymentLink;
	private String stripePaymentId;
	private StatusStripeEnum stripePaymentStatus;
	private String stripeCustomerId;
	private String stripeProductId;
	private String stripePrecioId;
	private String stripeUuidOrderId;
	private Integer percentageToPay;

}
