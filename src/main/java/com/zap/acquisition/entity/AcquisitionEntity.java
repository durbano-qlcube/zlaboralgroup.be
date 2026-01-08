package com.zap.acquisition.entity;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

import com.zap.acquisition.vo.StatusAcquisitionEnum;

import lombok.Data;

@Entity
@Table(name = "SALE_ACQUISITION")
@XmlRootElement
@NamedQueries({
		@NamedQuery(name = "AcquisitionEntity.loadAll", query = "SELECT m FROM AcquisitionEntity m order by m.fxCreation,m.idAcquisition desc"),
		@NamedQuery(name = "AcquisitionEntity.loadByStatus", query = "SELECT m FROM AcquisitionEntity m where m.status=:status order by m.fxCreation,m.idAcquisition desc"),
		@NamedQuery(name = "AcquisitionEntity.loadByStatus2", query = "SELECT m FROM AcquisitionEntity m WHERE m.status IN :statuses  ORDER BY m.fxCreation,m.idAcquisition DESC "),
	    @NamedQuery(name = "AcquisitionEntity.findByPhone", query = "SELECT m FROM AcquisitionEntity m WHERE m.phone = :phone"),
		// @NamedQuery(name = "AcquisitionEntity.findByCif", query = "SELECT a FROM
		// AcquisitionEntity a WHERE a.cif = :cif"),
		@NamedQuery(name = "AcquisitionEntity.loadGestdirectByStatus", query = "SELECT m FROM AcquisitionEntity m WHERE m.status IN :statuses AND COALESCE(m.fxModification, m.fxCreation) >= :limitDate AND (m.origin like 'GESTDIRECT%') ORDER BY m.fxCreation,m.idAcquisition DESC "),
	   
		@NamedQuery(name = "AcquisitionEntity.countInsertedByDay",
        query = "SELECT FUNCTION('DATE', a.fxInsertion) AS dia, COUNT(a) FROM AcquisitionEntity a "
            + "WHERE a.fxInsertion IS NOT NULL AND FUNCTION('DATE', a.fxInsertion) >= FUNCTION('DATE', :start) AND FUNCTION('DATE', a.fxInsertion) <= FUNCTION('DATE', :end) "
            + "AND (:uuidProvider IS NULL OR a.uuidAgenteCaptador = :uuidProvider) "
            + "GROUP BY FUNCTION('DATE', a.fxInsertion) ORDER BY dia"),
		
                @NamedQuery(name = "AcquisitionEntity.countClosedByDayLastCall",
        query = "SELECT FUNCTION('DATE', a.ocmFxLastCall) AS dia, COUNT(a) FROM AcquisitionEntity a "
            + "WHERE a.ocmFxLastCall IS NOT NULL AND FUNCTION('DATE', a.ocmFxLastCall) >= FUNCTION('DATE', :start) AND FUNCTION('DATE', a.ocmFxLastCall) <= FUNCTION('DATE', :end) "
            + "AND a.status = :status "
            + "AND (:uuidProvider IS NULL OR a.uuidAgenteCaptador = :uuidProvider) "
            + "GROUP BY FUNCTION('DATE', a.ocmFxLastCall) ORDER BY dia"),

                @NamedQuery(name = "AcquisitionEntity.countInsertedByDayLastCall",
        query = "SELECT FUNCTION('DATE', a.ocmFxLastCall) AS dia, COUNT(a) FROM AcquisitionEntity a "
            + "WHERE a.ocmFxLastCall IS NOT NULL AND FUNCTION('DATE', a.ocmFxLastCall) >= FUNCTION('DATE', :start) AND FUNCTION('DATE', a.ocmFxLastCall) <= FUNCTION('DATE', :end) "
            + "AND (:uuidProvider IS NULL OR a.uuidAgenteCaptador = :uuidProvider) "
            + "GROUP BY FUNCTION('DATE', a.ocmFxLastCall) ORDER BY dia"),
		
		@NamedQuery(name = "AcquisitionEntity.countClosedByDay",
        query = "SELECT FUNCTION('DATE', a.fxInsertion) AS dia, COUNT(a) FROM AcquisitionEntity a "
            + "WHERE a.fxInsertion IS NOT NULL AND FUNCTION('DATE', a.fxInsertion) >= FUNCTION('DATE', :start) AND FUNCTION('DATE', a.fxInsertion) <= FUNCTION('DATE', :end) "
            + "AND a.status = :status "
            + "AND (:uuidProvider IS NULL OR a.uuidAgenteCaptador = :uuidProvider) "
            + "GROUP BY FUNCTION('DATE', a.fxInsertion) ORDER BY dia"),

		@NamedQuery(name = "AcquisitionEntity.countInsertedByProviderMonth",
        query = "SELECT a.usernameCaptador, FUNCTION('MONTH', a.fxInsertion) AS mes, COUNT(a) FROM AcquisitionEntity a "
            + "WHERE a.fxInsertion IS NOT NULL AND FUNCTION('YEAR', a.fxInsertion) = :year "
            + "GROUP BY a.usernameCaptador, FUNCTION('MONTH', a.fxInsertion) ORDER BY a.usernameCaptador, mes"),
	    
		@NamedQuery(name = "AcquisitionEntity.countClosedByProviderMonth", query = "SELECT a.usernameCaptador, FUNCTION('MONTH', a.fxInsertion) AS mes, COUNT(a) FROM AcquisitionEntity a "
				+ "WHERE a.fxInsertion IS NOT NULL AND FUNCTION('YEAR', a.fxInsertion) = :year "
				+ "AND a.status = :status "
				+ "GROUP BY a.usernameCaptador, FUNCTION('MONTH', a.fxInsertion) ORDER BY a.usernameCaptador, mes"),

                @NamedQuery(name = "AcquisitionEntity.countInsertedByProviderMonthLastCall",
        query = "SELECT a.usernameCaptador, FUNCTION('MONTH', a.ocmFxLastCall) AS mes, COUNT(a) FROM AcquisitionEntity a "
            + "WHERE a.ocmFxLastCall IS NOT NULL AND FUNCTION('YEAR', a.ocmFxLastCall) = :year "
            + "GROUP BY a.usernameCaptador, FUNCTION('MONTH', a.ocmFxLastCall) ORDER BY a.usernameCaptador, mes"),

		@NamedQuery(name = "AcquisitionEntity.countClosedByProviderMonthLastCall", query = "SELECT a.usernameCaptador, FUNCTION('MONTH', a.ocmFxLastCall) AS mes, COUNT(a) FROM AcquisitionEntity a "
				+ "WHERE a.ocmFxLastCall IS NOT NULL AND FUNCTION('YEAR', a.ocmFxLastCall) = :year "
				+ "AND a.status = :status "
				+ "GROUP BY a.usernameCaptador, FUNCTION('MONTH', a.ocmFxLastCall) ORDER BY a.usernameCaptador, mes"),

		@NamedQuery(name = "AcquisitionEntity.countInsertedByMonthLastCall", query = "SELECT FUNCTION('MONTH', a.ocmFxLastCall) AS mes, COUNT(a) FROM AcquisitionEntity a "
				+ "WHERE a.ocmFxLastCall IS NOT NULL AND FUNCTION('YEAR', a.ocmFxLastCall) = :year "
				+ "AND (:uuidProvider IS NULL OR a.agenteUuid = :uuidProvider) "
				+ "GROUP BY FUNCTION('MONTH', a.ocmFxLastCall) ORDER BY mes"),

		@NamedQuery(name = "AcquisitionEntity.countClosedByMonthLastCall", query = "SELECT FUNCTION('MONTH', a.ocmFxLastCall) AS mes, COUNT(a) FROM AcquisitionEntity a "
				+ "WHERE a.ocmFxLastCall IS NOT NULL AND FUNCTION('YEAR', a.ocmFxLastCall) = :year "
				+ "AND a.status = :status " + "AND (:uuidProvider IS NULL OR a.agenteUuid = :uuidProvider) "
				+ "GROUP BY FUNCTION('MONTH', a.ocmFxLastCall) ORDER BY mes"),

		@NamedQuery(name = "AcquisitionEntity.countInsertedByMonth", query = "SELECT FUNCTION('MONTH', a.fxInsertion) AS mes, COUNT(a) FROM AcquisitionEntity a "
				+ "WHERE a.fxInsertion IS NOT NULL AND FUNCTION('YEAR', a.fxInsertion) = :year "
				+ "AND (:uuidProvider IS NULL OR a.agenteUuid = :uuidProvider) "
				+ "GROUP BY FUNCTION('MONTH', a.fxInsertion) ORDER BY mes"),
		
		 @NamedQuery(name = "AcquisitionEntity.countClosedByMonth",
	        query = "SELECT FUNCTION('MONTH', a.fxInsertion) AS mes, COUNT(a) FROM AcquisitionEntity a "
	            + "WHERE a.fxInsertion IS NOT NULL AND FUNCTION('YEAR', a.fxInsertion) = :year "
	            + "AND a.status = :status "
	            + "AND (:uuidProvider IS NULL OR a.agenteUuid = :uuidProvider) "
	            + "GROUP BY FUNCTION('MONTH', a.fxInsertion) ORDER BY mes")

})

@Data
public class AcquisitionEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    
	@javax.persistence.Id
	@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
	@javax.persistence.Column(name = "ID_ADQUISITION", nullable = false, insertable = true, updatable = true, length = 19)
    private Integer idAcquisition;
    

	@javax.persistence.Column(name = "NAME", insertable = true, updatable = true, length = 300)
    private String name;
    
	@javax.persistence.Column(name = "SURNAME1", insertable = true, updatable = true, length = 300)
    private String surname;
	
	@javax.persistence.Column(name = "SURNAME2", insertable = true, updatable = true, length = 300)
    private String surname2;
	
	@javax.persistence.Column(name = "FULLNAME", insertable = true, updatable = true, length = 300)
    private String fullname;
	
	@javax.persistence.Column(name = "EMAIL", insertable = true, updatable = true, length = 300)
    private String email;
	
	@javax.persistence.Column(name = "PHONE", insertable = true, updatable = true, length = 300)
    private String phone;

        @javax.persistence.Column(name = "DESCRIPTION", insertable = true, updatable = true, length = 600)
    private String description;

        @javax.persistence.Column(name = "CAMPAIGN", insertable = true, updatable = true, length = 50)
    private String campaign;

        @Column(name = "CAMPAIGN_LEAD_ID", length = 255, insertable = true, updatable = true)
        private String campaignLeadId;

        @Column(name = "CAMPAIGN_ADSET_NAME", length = 255, insertable = true, updatable = true)
        private String campaignAdsetName;

        @Column(name = "CAMPAIGN_AD_NAME", length = 255, insertable = true, updatable = true)
        private String campaignAdName;

        @Column(name = "CAMPAIGN_NAME", length = 255, insertable = true, updatable = true)
        private String campaignName;

        @Column(name = "CAMPAIGN_FORM_NAME", length = 255, insertable = true, updatable = true)
        private String campaignFormName;

        @Column(name = "CAMPAIGN_PLATFORM", length = 255, insertable = true, updatable = true)
        private String campaignPlatform;

        @Column(name = "CAMPAIGN_URL", length = 255, insertable = true, updatable = true)
        private String campaignUrl;

        @Column(name = "CAMPAIGN_PRODUCT", length = 255, insertable = true, updatable = true)
        private String campaignProduct;

    @Column(name = "PARENT_COMPANY_ID", insertable = true, updatable = true)
    private Long parentCompanyId;

	@Column(name = "COORDINADOR_UUID", length = 255, insertable = true, updatable = true)
	private String coordinadorUuid;
	
	@Column(name = "COORDINADOR_USERNAME", length = 255, insertable = true, updatable = true)
	private String coordinadorUserName;
	
	@Column(name = "SUPERVISOR_UUID", length = 255, insertable = true, updatable = true)
	private String supervisorUuid;
	
	@Column(name = "SUPERVISOR_USERNAME", length = 255, insertable = true, updatable = true)
	private String supervisorUserName;
	
        @javax.persistence.Column(name = "AGENTE_USERNAME", insertable = true, updatable = true, length = 100)
    private String agenteUsername;

        @Column(name = "AGENTE_UUID", insertable = true, updatable = true, length = 300)
        private String agenteUuid;

        @Column(name = "UUID_AGENTE_CAPTADOR", insertable = true, updatable = true, length = 300)
        private String uuidAgenteCaptador;

        @Column(name = "UUID_PROVIDER", insertable = true, updatable = true, length = 300)
        private String uuidProvider;
	

    @javax.persistence.Column(name = "STATUS", insertable = true, updatable = true, length = 25, columnDefinition = "VARCHAR(25)")
    @javax.persistence.Enumerated(javax.persistence.EnumType.STRING)
    private StatusAcquisitionEnum status;
	
    @Column(name = "NOMBRE_COMERCIAL", length = 255, insertable = true, updatable = true)
    private String nombreComercial;

    @Column(name = "CIF", length = 20, insertable = true, updatable = true)
    private String cif;
	
    
	@javax.persistence.Column(name = "USERNAME_CAPTADOR", insertable = true, updatable = true, length = 100)
    private String usernameCaptador;
	
    @Temporal(TemporalType.TIMESTAMP)
	@Column(name = "FX_SHEDULING",  unique = false, nullable = true, insertable = true, updatable = true)
	private Calendar fxScheduling;
	
    @Temporal(TemporalType.TIMESTAMP)
	@Column(name = "FX_CREATION",  unique = false, nullable = true, insertable = true, updatable = true)
	private Calendar fxCreation;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "FX_MODIFICATION",  unique = false, nullable = true, insertable = true, updatable = true)
	private Calendar fxModification;
	
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "OCM_FX_SENT_TO_OCM",  unique = false, nullable = true, insertable = true, updatable = true)
	private Calendar fxSendToOcm;
	
    @Temporal(TemporalType.TIMESTAMP)
 	@Column(name = "OCM_FX_LAST_CALL",  unique = false, nullable = true, insertable = true, updatable = true)
 	private Calendar ocmFxLastCall;
    
    @Temporal(TemporalType.TIMESTAMP)
  	@Column(name = "OCM_FX_FIRST_CALL",  unique = false, nullable = true, insertable = true, updatable = true)
  	private Calendar ocmFxFirstCall;
    
    @Temporal(TemporalType.TIMESTAMP)
   	@Column(name = "FX_INSERTION",  unique = false, nullable = true, insertable = true, updatable = true)
   	private Calendar fxInsertion;
    
    @Temporal(TemporalType.TIMESTAMP)
 	@Column(name = "OCM_FX_NEXT_CALL",  unique = false, nullable = true, insertable = true, updatable = true)
 	private Calendar ocmFxNextCall;
	
	@javax.persistence.Column(name = "OCM_LAST_CODING", insertable = true, updatable = true, length = 100)
	private String ocmLastCoding;
	
	@javax.persistence.Column(name = "OCM_MOTOR", insertable = true, updatable = true, length = 50)
	private String ocmMotor;
	
	@javax.persistence.Column(name = "OCM_LAST_AGENT", insertable = true, updatable = true, length = 50)
	private String ocmLastAgent;
	
	@javax.persistence.Column(name = "OCM_END_RESULT", insertable = true, updatable = true)
    private Integer EndResult;
	
	@javax.persistence.Column(name = "OCM_ID", insertable = true, updatable = true)
	private Integer ocmId;
	
		
	//GEST DIRECT
    @Column(name = "ORIGIN", length = 255, insertable = true, updatable = true)
    private String origin;
    
	@Column(name = "ORIGIN_USER_USERNAME", length = 100, insertable = true, updatable = true)
	private String originUserUsername;
    
    @javax.persistence.Column(name = "ORIGIN_USER_USERUUID", insertable = true, updatable = true, length = 100)
    private String originUserUuid;
    
	@javax.persistence.Column(name = "ORIGIN_GESTORIA_UUID", insertable = true, updatable = true, length = 100)
	private String originGestoriaUuid;
	
	@javax.persistence.Column(name = "ORIGIN_ID_ADQUISITION", insertable = true, updatable = true)
    private Integer originIdAdquision;

    @Column(name = "RAZON_SOCIAL", length = 255, insertable = true, updatable = true)
    private String razonSocial;
    

	

	
	
}
