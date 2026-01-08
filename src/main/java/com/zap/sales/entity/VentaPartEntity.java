package com.zap.sales.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.zap.sales.vo.venta.StatusVentaEnum;
import com.zap.stripe.vo.StatusStripeEnum;

import lombok.Data;


@Entity
@Table(name = "ORG_VENTA_PARTICULAR")
@Data
@NamedQueries({
	@NamedQuery(name = "VentaPartEntity.findAll", query = "SELECT a FROM VentaPartEntity a"),
	@NamedQuery(name = "VentaPartEntity.findByUuid", query = "SELECT v FROM VentaPartEntity v WHERE v.uuId = :uuId"),
	@NamedQuery(name = "VentaPartEntity.findByUuidOrderId", query = "SELECT v FROM VentaPartEntity v WHERE v.stripeUuidOrderId = :stripeUuidOrderId"),
	@NamedQuery(name = "VentaPartEntity.loadByStatuses",query = "SELECT v FROM VentaPartEntity v WHERE v.status IN :statuses "),


})
public class VentaPartEntity implements Serializable {

	private static final long serialVersionUID = 1L;


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID_VENTA", nullable = false, insertable = true, updatable = false)
	private Integer idVenta;

	@Column(name = "UUID", length = 255, insertable = true, updatable = true, unique=true)
	private String uuId;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATE_VENTA",  unique = false, nullable = true, insertable = true, updatable = true)
	private Calendar fxVenta;

	@Column(name = "AGENT_USERNAME", length = 255, insertable = true, updatable = true)
	private String usernameAgente;
	
	@Column(name = "UUID_AGENTE", length = 255, insertable = true, updatable = true, unique=false)
	private String uuIdAgente;

	@Column(name = "COORDINADOR_USERNAME", length = 255, insertable = true, updatable = true)
	private String coordinadorUserName;

	@Column(name = "UUID_COORDINADOR", length = 255, insertable = true, updatable = true, unique=false)
	private String uuIdCoordinador;

	@Column(name = "SUPERVISOR_USERNAME", length = 255, insertable = true, updatable = true)
	private String supervisorUserName;

	@Column(name = "UUID_SUPERVISOR", length = 255, insertable = true, updatable = true, unique=false)
	private String uuIdSupervisor;

	@Column(name = "PRICE", length = 255, insertable = true, updatable = true)
	private BigDecimal price;
	
	@Column(name = "PRICE_DEDUCTED_EXPENSES", length = 255, insertable = true, updatable = true)
	private BigDecimal priceDeductedExpenses;
	
	@Column(name = "PRICE_WHITH_IVA", length = 255, insertable = true, updatable = true)
	private BigDecimal priceWithIva;

	@Column(name = "IVA", length = 255, insertable = true, updatable = true)
	private BigDecimal iva;

	@Column(name = "COMMSSION", length = 255, insertable = true, updatable = true)
	private BigDecimal commission;

	@Column(name = "CHARGED", length = 100, insertable = true, updatable = true)
	private Integer charged;

	@Column(name = "PDTE_FIRMA", length = 100, insertable = true, updatable = true)
	private Boolean pdteFirma;
	
	@Column(name = "PDTE_COBRO_INICIO_CURSO", length = 100, insertable = true, updatable = true)
	private Boolean pdteCobroInicioCurso;
	
	@Column(name = "PDTE_COBRO_FIN_CURSO", length = 100, insertable = true, updatable = true)
	private Boolean pdteCobroFinCurso;
	
	@javax.persistence.Column(name = "STATUS", insertable = true, updatable = true, length = 25, columnDefinition = "VARCHAR(25)")
	@javax.persistence.Enumerated(javax.persistence.EnumType.STRING)
	private StatusVentaEnum status;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATE_CREATION",  unique = false, nullable = true, insertable = true, updatable = true)
	private Calendar fxCreation;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATE_MODIFICATION",  unique = false, nullable = true, insertable = true, updatable = true)
	private Calendar fxModification;

	
	@javax.persistence.Column(name = "OBSERVACIONES", insertable = true, updatable = true, length = 1500)
	private String observaciones;

	
	@Column(name = "NOMBRE", insertable = true, updatable = true, length = 300)
	private String nombre;

	@Column(name = "HORAS", insertable = true, updatable = true)
	private Integer horas;

	@Column(name = "AREA_PROFESIONAL", insertable = true, updatable = true, length = 300)
	private String areaProfesional;

	@Column(name = "FECHA_INICIO", insertable = true, updatable = true)
	private Calendar fechaInicio;

	@Column(name = "FECHA_FIN", insertable = true, updatable = true)
	private Calendar fechaFin;

	@Column(name = "STRIPE_PAYMENT_LINK", length = 1000, insertable = true, updatable = true)
	private String stripePaymentLink;
	
	@Column(name = "STRIPE_PAYMENT_ID", length = 255, insertable = true, updatable = true, unique=true)
	private String stripePaymentId;
	
	@Column(name = "STRIPE_PAYMENT_STATUS", length = 50, insertable = true, updatable = true)
	@javax.persistence.Enumerated(javax.persistence.EnumType.STRING)
	private StatusStripeEnum stripePaymentStatus;
	
	@Column(name = "STRIPE_CUSTOMER_ID", length = 255, insertable = true, updatable = true)
	private String stripeCustomerId;

	@Column(name = "STRIPE_PRODUCT_ID", length = 255, insertable = true, updatable = true)
	private String stripeProductId;
	
	@Column(name = "STRIPE_PRECIO_ID", length = 255, insertable = true, updatable = true)
	private String stripePrecioId;
	
        @Column(name = "STRIPE_UUID_ORDER_ID", length = 255, insertable = true, updatable = true)
        private String stripeUuidOrderId;

        @Column(name = "PERCENTAGE_TO_PAY", length = 100, insertable = true, updatable = true)
        private Integer percentageToPay;

        @Column(name = "PARENT_COMPANY_ID", insertable = true, updatable = true)
        private Long parentCompanyId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_PERSONA", referencedColumnName = "ID_PERSONA")
	private PersonaEntity personaEntity;    
	
    @OneToMany(mappedBy = "ventaEntity", cascade={CascadeType.REMOVE}, fetch = FetchType.LAZY)
	private Set<DocEntity> docEntities = new LinkedHashSet <DocEntity>();
}
