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
@Table(name = "ORG_VENTA")
@Data
@NamedQueries({
	@NamedQuery(name = "VentaEntity.findAll", query = "SELECT a FROM VentaEntity a"),
	@NamedQuery(name = "VentaEntity.findByUuid", query = "SELECT v FROM VentaEntity v WHERE v.uuId = :uuId"),
	@NamedQuery(name = "VentaEntity.findByUuidOrderId", query = "SELECT v FROM VentaEntity v WHERE v.stripeUuidOrderId = :stripeUuidOrderId"),
	@NamedQuery(name = "VentaEntity.findByidEmpresa", query = "SELECT v FROM VentaEntity v WHERE v.empresaEntity.idEmpresa = :idEmpresa"),
	@NamedQuery(name = "VentaEntity.loadByStatuses",query = "SELECT v FROM VentaEntity v WHERE v.status IN :statuses "),
//	@NamedQuery(name = "VentaEntity.loadGestdirectByStatus",query = "SELECT v FROM VentaEntity v WHERE v.status IN :statuses AND COALESCE(v.fxModification, v.fxCreation) > CURRENT_TIMESTAMP - 60 AND (v.origin like 'GESTDIRECT%')")
//	@NamedQuery(name = "VentaEntity.loadGestdirectByStatus",query = "SELECT v FROM VentaEntity v WHERE v.status IN :statuses AND (v.origin like 'GESTDIRECT%')")
	@NamedQuery(name = "VentaEntity.loadGestdirectByStatus",query = "SELECT v FROM VentaEntity v WHERE v.status IN :statuses AND COALESCE(v.fxModification, v.fxCreation) >= :limitDate AND v.origin LIKE 'GESTDIRECT%'"),
	
	@NamedQuery(name = "VentaEntity.countByAcqInsertionMonth", query = "SELECT FUNCTION('MONTH', a.fxCreation) AS mes, COUNT(v) FROM VentaEntity v JOIN v.empresaEntity e JOIN AcquisitionEntity a ON e.telefonoContacto = a.telefonoContacto "
			+ "WHERE a.fxCreation IS NOT NULL AND FUNCTION('YEAR', a.fxCreation) = :year "
			+ "AND (:uuidProvider IS NULL OR v.uuidProvider = :uuidProvider) "
			+ "GROUP BY FUNCTION('MONTH', a.fxCreation) ORDER BY mes"),

	@NamedQuery(name = "VentaEntity.countByAcqLastCallMonth", query = "SELECT FUNCTION('MONTH', a.fxModification) AS mes, COUNT(v) FROM VentaEntity v JOIN v.empresaEntity e JOIN AcquisitionEntity a ON e.telefonoContacto = a.telefonoContacto "
			+ "WHERE a.fxModification IS NOT NULL AND FUNCTION('YEAR', a.fxModification) = :year "
			+ "AND (:uuidProvider IS NULL OR v.uuidProvider = :uuidProvider) "
			+ "GROUP BY FUNCTION('MONTH', a.fxModification) ORDER BY mes"),

        @NamedQuery(name = "VentaEntity.countByAcqInsertionMonthStatus", query = "SELECT FUNCTION('MONTH', a.fxCreation) AS mes, COUNT(v) FROM VentaEntity v JOIN v.empresaEntity e JOIN AcquisitionEntity a ON e.telefonoContacto = a.telefonoContacto "
                        + "WHERE a.fxCreation IS NOT NULL AND FUNCTION('YEAR', a.fxCreation) = :year "
                        + "AND (:uuidProvider IS NULL OR v.uuidProvider = :uuidProvider) "
                        + "AND v.status = :status "
                        + "GROUP BY FUNCTION('MONTH', a.fxCreation) ORDER BY mes"),

        @NamedQuery(name = "VentaEntity.countByAcqInsertionMonthStatusIn", query = "SELECT FUNCTION('MONTH', a.fxCreation) AS mes, COUNT(v) FROM VentaEntity v JOIN v.empresaEntity e JOIN AcquisitionEntity a ON e.telefonoContacto = a.telefonoContacto "
                        + "WHERE a.fxCreation IS NOT NULL AND FUNCTION('YEAR', a.fxCreation) = :year "
                        + "AND (:uuidProvider IS NULL OR v.uuidProvider = :uuidProvider) "
                        + "AND v.status IN :statuses "
                        + "GROUP BY FUNCTION('MONTH', a.fxCreation) ORDER BY mes"),

        @NamedQuery(name = "VentaEntity.countByAcqLastCallMonthStatus", query = "SELECT FUNCTION('MONTH', a.fxModification) AS mes, COUNT(v) FROM VentaEntity v JOIN v.empresaEntity e JOIN AcquisitionEntity a ON e.telefonoContacto = a.telefonoContacto "
                        + "WHERE a.fxModification IS NOT NULL AND FUNCTION('YEAR', a.fxModification) = :year "
                        + "AND (:uuidProvider IS NULL OR v.uuidProvider = :uuidProvider) "
                        + "AND v.status = :status "
                        + "GROUP BY FUNCTION('MONTH', a.fxModification) ORDER BY mes"),

        @NamedQuery(name = "VentaEntity.countByAcqLastCallMonthStatusIn", query = "SELECT FUNCTION('MONTH', a.fxModification) AS mes, COUNT(v) FROM VentaEntity v JOIN v.empresaEntity e JOIN AcquisitionEntity a ON e.telefonoContacto = a.telefonoContacto "
                        + "WHERE a.fxModification IS NOT NULL AND FUNCTION('YEAR', a.fxModification) = :year "
                        + "AND (:uuidProvider IS NULL OR v.uuidProvider = :uuidProvider) "
                        + "AND v.status IN :statuses "
                        + "GROUP BY FUNCTION('MONTH', a.fxModification) ORDER BY mes"),

	@NamedQuery(name = "VentaEntity.countByAcqInsertionDay", query = "SELECT FUNCTION('DATE', a.fxCreation) AS dia, COUNT(v) FROM VentaEntity v JOIN v.empresaEntity e JOIN AcquisitionEntity a ON e.telefonoContacto = a.telefonoContacto "
			+ "WHERE a.fxCreation IS NOT NULL AND FUNCTION('DATE', a.fxCreation) >= FUNCTION('DATE', :start) "
			+ "AND FUNCTION('DATE', a.fxCreation) <= FUNCTION('DATE', :end) "
			+ "AND (:uuidProvider IS NULL OR v.uuidProvider = :uuidProvider) "
			+ "GROUP BY FUNCTION('DATE', a.fxCreation) ORDER BY dia"),

	@NamedQuery(name = "VentaEntity.countByAcqLastCallDay", query = "SELECT FUNCTION('DATE', a.fxModification) AS dia, COUNT(v) FROM VentaEntity v JOIN v.empresaEntity e JOIN AcquisitionEntity a ON e.telefonoContacto = a.telefonoContacto "
			+ "WHERE a.fxModification IS NOT NULL AND FUNCTION('DATE', a.fxModification) >= FUNCTION('DATE', :start) "
			+ "AND FUNCTION('DATE', a.fxModification) <= FUNCTION('DATE', :end) "
			+ "AND (:uuidProvider IS NULL OR v.uuidProvider = :uuidProvider) "
			+ "GROUP BY FUNCTION('DATE', a.fxModification) ORDER BY dia"),

        @NamedQuery(name = "VentaEntity.countByAcqInsertionDayStatus", query = "SELECT FUNCTION('DATE', a.fxCreation) AS dia, COUNT(v) FROM VentaEntity v JOIN v.empresaEntity e JOIN AcquisitionEntity a ON e.telefonoContacto = a.telefonoContacto "
                        + "WHERE a.fxCreation IS NOT NULL AND FUNCTION('DATE', a.fxCreation) >= FUNCTION('DATE', :start) "
                        + "AND FUNCTION('DATE', a.fxCreation) <= FUNCTION('DATE', :end) "
                        + "AND (:uuidProvider IS NULL OR v.uuidProvider = :uuidProvider) "
                        + "AND v.status = :status "
                        + "GROUP BY FUNCTION('DATE', a.fxCreation) ORDER BY dia"),

        @NamedQuery(name = "VentaEntity.countByAcqLastCallDayStatus", query = "SELECT FUNCTION('DATE', a.fxModification) AS dia, COUNT(v) FROM VentaEntity v JOIN v.empresaEntity e JOIN AcquisitionEntity a ON e.telefonoContacto = a.telefonoContacto "
                        + "WHERE a.fxModification IS NOT NULL AND FUNCTION('DATE', a.fxModification) >= FUNCTION('DATE', :start) "
                        + "AND FUNCTION('DATE', a.fxModification) <= FUNCTION('DATE', :end) "
                        + "AND (:uuidProvider IS NULL OR v.uuidProvider = :uuidProvider) "
                        + "AND v.status = :status "
                        + "GROUP BY FUNCTION('DATE', a.fxModification) ORDER BY dia"),

        @NamedQuery(name = "VentaEntity.countByAcqInsertionDayStatusIn", query = "SELECT FUNCTION('DATE', a.fxCreation) AS dia, COUNT(v) FROM VentaEntity v JOIN v.empresaEntity e JOIN AcquisitionEntity a ON e.telefonoContacto = a.telefonoContacto "
                        + "WHERE a.fxCreation IS NOT NULL AND FUNCTION('DATE', a.fxCreation) >= FUNCTION('DATE', :start) "
                        + "AND FUNCTION('DATE', a.fxCreation) <= FUNCTION('DATE', :end) "
                        + "AND (:uuidProvider IS NULL OR v.uuidProvider = :uuidProvider) "
                        + "AND v.status IN :statuses "
                        + "GROUP BY FUNCTION('DATE', a.fxCreation) ORDER BY dia"),

        @NamedQuery(name = "VentaEntity.countByAcqLastCallDayStatusIn", query = "SELECT FUNCTION('DATE', a.fxModification) AS dia, COUNT(v) FROM VentaEntity v JOIN v.empresaEntity e JOIN AcquisitionEntity a ON e.telefonoContacto = a.telefonoContacto "
                        + "WHERE a.fxModification IS NOT NULL AND FUNCTION('DATE', a.fxModification) >= FUNCTION('DATE', :start) "
                        + "AND FUNCTION('DATE', a.fxModification) <= FUNCTION('DATE', :end) "
                        + "AND (:uuidProvider IS NULL OR v.uuidProvider = :uuidProvider) "
                        + "AND v.status IN :statuses "
                        + "GROUP BY FUNCTION('DATE', a.fxModification) ORDER BY dia"),

	@NamedQuery(name = "VentaEntity.countByStatusAndMonth", query = "SELECT v.status, FUNCTION('MONTH', v.fxVenta) AS mes, COUNT(v) FROM VentaEntity v "
			+ "WHERE v.fxVenta IS NOT NULL AND FUNCTION('YEAR', v.fxVenta) = :year "
			+ "GROUP BY v.status, FUNCTION('MONTH', v.fxVenta) "
			+ "ORDER BY v.status, FUNCTION('MONTH', v.fxVenta)"),

	@NamedQuery(name = "VentaEntity.countByStatusInAndMonth", query = "SELECT v.status, FUNCTION('MONTH', v.fxVenta) AS mes, COUNT(v) FROM VentaEntity v "
			+ "WHERE v.fxVenta IS NOT NULL AND FUNCTION('YEAR', v.fxVenta) = :year "
			+ "AND v.status IN :statuses GROUP BY v.status, FUNCTION('MONTH', v.fxVenta) "
			+ "ORDER BY v.status, FUNCTION('MONTH', v.fxVenta)"),

	@NamedQuery(name = "VentaEntity.countByTipoVentaAndMonth", query = "SELECT v.tipoVenta, FUNCTION('MONTH', v.fxVenta) AS mes, COUNT(v) FROM VentaEntity v "
			+ "WHERE v.fxVenta IS NOT NULL AND FUNCTION('YEAR', v.fxVenta) = :year "
			+ "AND v.status IN :statuses GROUP BY v.tipoVenta, FUNCTION('MONTH', v.fxVenta) "
			+ "ORDER BY v.tipoVenta, FUNCTION('MONTH', v.fxVenta)"),

	@NamedQuery(name = "VentaEntity.countByProviderAcqInsertionMonth", query = "SELECT a.usernameCaptador, FUNCTION('MONTH', a.fxCreation) AS mes, COUNT(v) FROM VentaEntity v JOIN v.empresaEntity e JOIN AcquisitionEntity a ON e.telefonoContacto = a.telefonoContacto "
			+ "WHERE a.fxCreation IS NOT NULL AND FUNCTION('YEAR', a.fxCreation) = :year "
			+ "GROUP BY a.usernameCaptador, FUNCTION('MONTH', a.fxCreation) ORDER BY a.usernameCaptador, mes"),

	@NamedQuery(name = "VentaEntity.countByProviderAcqLastCallMonth", query = "SELECT a.usernameCaptador, FUNCTION('MONTH', a.fxModification) AS mes, COUNT(v) FROM VentaEntity v JOIN v.empresaEntity e JOIN AcquisitionEntity a ON e.telefonoContacto = a.telefonoContacto "
			+ "WHERE a.fxModification IS NOT NULL AND FUNCTION('YEAR', a.fxModification) = :year "
			+ "GROUP BY a.usernameCaptador, FUNCTION('MONTH', a.fxModification) ORDER BY a.usernameCaptador, mes"),

	@NamedQuery(name = "VentaEntity.carteraResumen", query = "SELECT v.status, v.bajaDate, v.fxEfecto FROM VentaEntity v "
			+ "WHERE v.fxVenta IS NOT NULL AND v.fxVenta >= :start AND v.fxVenta <= :end") 
})
public class VentaEntity implements Serializable {

	private static final long serialVersionUID = 1L;


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID_VENTA", nullable = false, insertable = true, updatable = true)
	private Integer idVenta;

	@Column(name = "ID_VENTA_GESTDIRECT", length = 255, insertable = true, updatable = true, unique=true)
	private Integer idVentaGestdirect;
	
	@Column(name = "UUID", length = 255, insertable = true, updatable = true, unique=true)
	private String uuId;

        @Temporal(TemporalType.TIMESTAMP)
        @Column(name = "DATE_VENTA",  unique = false, nullable = true, insertable = true, updatable = true)
        private Calendar fxVenta;

        @Temporal(TemporalType.TIMESTAMP)
        @Column(name = "FX_EFECTO", unique = false, nullable = true, insertable = true, updatable = true)
        private Calendar fxEfecto;

        @Temporal(TemporalType.TIMESTAMP)
        @Column(name = "BAJA_DATE", unique = false, nullable = true, insertable = true, updatable = true)
        private Calendar bajaDate;

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
	
        @Column(name = "UUID_PROVIDER", length = 100, insertable = true, updatable = true)
        private String uuidProvider;

        @Column(name = "TIPO_VENTA", length = 100, insertable = true, updatable = true)
        private String tipoVenta;

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
	private Boolean charged;
	
	@Column(name = "PDTE_COBRO_INICIO_CURSO", length = 100, insertable = true, updatable = true)
	private Boolean pdteCobroInicioCurso;
	
	@Column(name = "PDTE_COBRO_FIN_CURSO", length = 100, insertable = true, updatable = true)
	private Boolean pdteCobroFinCurso;
	
	@Column(name = "PDTE_FIRMA", length = 100, insertable = true, updatable = true)
	private Boolean pdteFirma;

	@javax.persistence.Column(name = "STATUS", insertable = true, updatable = true, length = 25, columnDefinition = "VARCHAR(25)")
	@javax.persistence.Enumerated(javax.persistence.EnumType.STRING)
	private StatusVentaEnum status;
	
	
	
	@Column(name = "ORIGIN", length = 100, insertable = true, updatable = true)
	private String origin;
	
	@javax.persistence.Column(name = "ORIGIN_GESTORIA_UUID", insertable = true, updatable = true, length = 100)
	private String originGestoriaUuid;
	
    @javax.persistence.Column(name = "ORIGIN_USER_USERUUID", insertable = true, updatable = true, length = 200)
    private String originUserUuid;
    
	@Column(name = "ORIGIN_USER_USERNAME", length = 100, insertable = true, updatable = true)
	private String originUserUsername;
    
	@javax.persistence.Column(name = "ORIGIN_ID_VENTA", insertable = true, updatable = true)
    private Integer originIdVenta;

	
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATE_CREATION",  unique = false, nullable = true, insertable = true, updatable = true)
	private Calendar fxCreation;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATE_MODIFICATION",  unique = false, nullable = true, insertable = true, updatable = true)
	private Calendar fxModification;

	
	@javax.persistence.Column(name = "OBSERVACIONES", insertable = true, updatable = true, length = 1500)
	private String observaciones;
	
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

	@Column(name = "PARENT_COMPANY_ID", insertable = true, updatable = true)
	private Long parentCompanyId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_EMPRESA", referencedColumnName = "ID_EMPRESA")
	private EmpresaEntity empresaEntity;


//    @OneToOne(mappedBy = "ventaEntity", cascade = CascadeType.REFRESH, optional = false)
//	private FormacionEntity formacionEntity;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_FORMACION", referencedColumnName = "ID_FORMACION")
	private FormacionEntity formacionEntity;

	


    @OneToMany(mappedBy = "ventaEntity", cascade={CascadeType.REMOVE}, fetch = FetchType.LAZY)
	private Set<DocEntity> docEntities = new LinkedHashSet <DocEntity>();
}
