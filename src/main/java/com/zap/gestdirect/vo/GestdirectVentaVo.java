	package com.zap.gestdirect.vo;
	
	import java.io.Serializable;
	import java.math.BigDecimal;
	import java.util.Calendar;
	
	import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
	
	import com.zap.maintenance.service.adapter.TimeDateAdapter;
	
	import lombok.Data;
	
	@Data
	public class GestdirectVentaVo implements Serializable {
	
	    private static final long serialVersionUID = 1L;
	
	    private Integer idVenta;
	    private Integer idEmpresa;
	    private Integer idFormacion;
	    private String uuId;
	    private String origin;
	    private Long parentCompanyId;
	    
	    @XmlJavaTypeAdapter(TimeDateAdapter.class)
	    private Long fxVenta;
	    
	    @XmlJavaTypeAdapter(TimeDateAdapter.class)
	    private Long fxCreation;
	    
	    @XmlJavaTypeAdapter(TimeDateAdapter.class)
	    private Long fxModification;
	    
	    
	    private GestdirectStatusVentaEnum status;
		private String usernameColaborador;
		private String gestoriaUuid;
		private String gestoriaName;
		private String colaboradorUuId;
		private String usernameVendedor;
		private String vendedorUuid;
		private BigDecimal price;
		private BigDecimal priceDeductedExpenses;
		private BigDecimal priceWithIva;
		private BigDecimal iva;
		private BigDecimal commission;
		private Boolean charged;
		private Boolean pdteFirma;

		
	    private String gestoriaPhone;
	    private String gestoriaEmail;
	    private String originUsername;
	
	    private Integer idVentaCube;
	    
	    
	
	
	    
	}
