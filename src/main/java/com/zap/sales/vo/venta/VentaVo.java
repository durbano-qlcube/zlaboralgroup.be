package com.zap.sales.vo.venta;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.zap.maintenance.service.adapter.TimeDateAdapter;
import com.zap.stripe.vo.StatusStripeEnum;

import lombok.Data;

@Data
public class VentaVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idVenta;
    private Integer idVentaGestdirect;
    private Integer idEmpresa;
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
	private Boolean charged;
	private Boolean pdteFirma;
	private Boolean pdteCobroInicioCurso;
	private Boolean pdteCobroFinCurso;




	private String usernameAgente;
	private String usernameCoordinador;
	private String usernameSupervisor;
	private String uuIdAgente;
    private String uuIdCoordinador;
	private String uuIdSupervisor;
	private String uuidProvider;
	private String providerName;


    private String origin;
    private String originUserUsername;
    private String originUserUuid;
    private String originGestoriaUuid;
    private Integer originIdVenta;
	private String observaciones;
	
	private String stripePaymentLink;
	private String stripePaymentId;
	private StatusStripeEnum stripePaymentStatus;
	private String stripeCustomerId;
	private String stripeProductId;
	private String stripePrecioId;

	private String ventaSegment;
	private String stripeUuidOrderId;
	private Integer percentageToPay;
	private Long parentCompanyId;

}
