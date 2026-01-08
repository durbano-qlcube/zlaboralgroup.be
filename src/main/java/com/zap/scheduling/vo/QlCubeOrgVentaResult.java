package com.zap.scheduling.vo;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.zap.maintenance.service.adapter.TimeDateAdapter;
import com.zap.sales.vo.venta.StatusVentaEnum;
import com.zap.stripe.vo.StatusStripeEnum;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.Data;

@Entity(value = "QLCUBE_ORG_VENTA_RESULT") 
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class QlCubeOrgVentaResult  implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id 
    private String id;

    private Integer idVenta;
    private Integer idVentaGestdirect;
    private Integer idEmpresa;
    private Integer idFormacion;
    private String uuIdVenta;

    private Date fxVenta;
    private Date fxCreation;
    private Date fxModification;
    
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
	
	private String cif;
	private String razonSocial;
	private String phoneVenta;

    
  }
