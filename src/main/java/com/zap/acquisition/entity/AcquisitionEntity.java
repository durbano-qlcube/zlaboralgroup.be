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
        @NamedQuery(name = "AcquisitionEntity.findByPhone", query = "SELECT m FROM AcquisitionEntity m WHERE m.telefonoContacto = :phone"),
        @NamedQuery(name = "AcquisitionEntity.loadGestdirectByStatus", query = "SELECT m FROM AcquisitionEntity m WHERE m.status IN :statuses AND COALESCE(m.fxModification, m.fxCreation) >= :limitDate ORDER BY m.fxCreation,m.idAcquisition DESC "),

        @NamedQuery(name = "AcquisitionEntity.countInsertedByDay",
                query = "SELECT FUNCTION('DATE', a.fxCreation) AS dia, COUNT(a) FROM AcquisitionEntity a "
                        + "WHERE a.fxCreation IS NOT NULL AND FUNCTION('DATE', a.fxCreation) >= FUNCTION('DATE', :start) AND FUNCTION('DATE', a.fxCreation) <= FUNCTION('DATE', :end) "
                        + "AND (:uuidProvider IS NULL OR a.uuidAgenteCaptador = :uuidProvider) "
                        + "GROUP BY FUNCTION('DATE', a.fxCreation) ORDER BY dia"),

        @NamedQuery(name = "AcquisitionEntity.countClosedByDayLastCall",
                query = "SELECT FUNCTION('DATE', a.fxModification) AS dia, COUNT(a) FROM AcquisitionEntity a "
                        + "WHERE a.fxModification IS NOT NULL AND FUNCTION('DATE', a.fxModification) >= FUNCTION('DATE', :start) AND FUNCTION('DATE', a.fxModification) <= FUNCTION('DATE', :end) "
                        + "AND a.status = :status "
                        + "AND (:uuidProvider IS NULL OR a.uuidAgenteCaptador = :uuidProvider) "
                        + "GROUP BY FUNCTION('DATE', a.fxModification) ORDER BY dia"),

        @NamedQuery(name = "AcquisitionEntity.countInsertedByDayLastCall",
                query = "SELECT FUNCTION('DATE', a.fxModification) AS dia, COUNT(a) FROM AcquisitionEntity a "
                        + "WHERE a.fxModification IS NOT NULL AND FUNCTION('DATE', a.fxModification) >= FUNCTION('DATE', :start) AND FUNCTION('DATE', a.fxModification) <= FUNCTION('DATE', :end) "
                        + "AND (:uuidProvider IS NULL OR a.uuidAgenteCaptador = :uuidProvider) "
                        + "GROUP BY FUNCTION('DATE', a.fxModification) ORDER BY dia"),

        @NamedQuery(name = "AcquisitionEntity.countClosedByDay",
                query = "SELECT FUNCTION('DATE', a.fxCreation) AS dia, COUNT(a) FROM AcquisitionEntity a "
                        + "WHERE a.fxCreation IS NOT NULL AND FUNCTION('DATE', a.fxCreation) >= FUNCTION('DATE', :start) AND FUNCTION('DATE', a.fxCreation) <= FUNCTION('DATE', :end) "
                        + "AND a.status = :status "
                        + "AND (:uuidProvider IS NULL OR a.uuidAgenteCaptador = :uuidProvider) "
                        + "GROUP BY FUNCTION('DATE', a.fxCreation) ORDER BY dia"),

        @NamedQuery(name = "AcquisitionEntity.countInsertedByProviderMonth",
                query = "SELECT a.usernameCaptador, FUNCTION('MONTH', a.fxCreation) AS mes, COUNT(a) FROM AcquisitionEntity a "
                        + "WHERE a.fxCreation IS NOT NULL AND FUNCTION('YEAR', a.fxCreation) = :year "
                        + "GROUP BY a.usernameCaptador, FUNCTION('MONTH', a.fxCreation) ORDER BY a.usernameCaptador, mes"),

        @NamedQuery(name = "AcquisitionEntity.countClosedByProviderMonth", query = "SELECT a.usernameCaptador, FUNCTION('MONTH', a.fxCreation) AS mes, COUNT(a) FROM AcquisitionEntity a "
                + "WHERE a.fxCreation IS NOT NULL AND FUNCTION('YEAR', a.fxCreation) = :year "
                + "AND a.status = :status "
                + "GROUP BY a.usernameCaptador, FUNCTION('MONTH', a.fxCreation) ORDER BY a.usernameCaptador, mes"),

        @NamedQuery(name = "AcquisitionEntity.countInsertedByProviderMonthLastCall",
                query = "SELECT a.usernameCaptador, FUNCTION('MONTH', a.fxModification) AS mes, COUNT(a) FROM AcquisitionEntity a "
                        + "WHERE a.fxModification IS NOT NULL AND FUNCTION('YEAR', a.fxModification) = :year "
                        + "GROUP BY a.usernameCaptador, FUNCTION('MONTH', a.fxModification) ORDER BY a.usernameCaptador, mes"),

        @NamedQuery(name = "AcquisitionEntity.countClosedByProviderMonthLastCall", query = "SELECT a.usernameCaptador, FUNCTION('MONTH', a.fxModification) AS mes, COUNT(a) FROM AcquisitionEntity a "
                + "WHERE a.fxModification IS NOT NULL AND FUNCTION('YEAR', a.fxModification) = :year "
                + "AND a.status = :status "
                + "GROUP BY a.usernameCaptador, FUNCTION('MONTH', a.fxModification) ORDER BY a.usernameCaptador, mes"),

        @NamedQuery(name = "AcquisitionEntity.countInsertedByMonthLastCall", query = "SELECT FUNCTION('MONTH', a.fxModification) AS mes, COUNT(a) FROM AcquisitionEntity a "
                + "WHERE a.fxModification IS NOT NULL AND FUNCTION('YEAR', a.fxModification) = :year "
                + "AND (:uuidProvider IS NULL OR a.agenteUuid = :uuidProvider) "
                + "GROUP BY FUNCTION('MONTH', a.fxModification) ORDER BY mes"),

        @NamedQuery(name = "AcquisitionEntity.countClosedByMonthLastCall", query = "SELECT FUNCTION('MONTH', a.fxModification) AS mes, COUNT(a) FROM AcquisitionEntity a "
                + "WHERE a.fxModification IS NOT NULL AND FUNCTION('YEAR', a.fxModification) = :year "
                + "AND a.status = :status "
                + "AND (:uuidProvider IS NULL OR a.agenteUuid = :uuidProvider) "
                + "GROUP BY FUNCTION('MONTH', a.fxModification) ORDER BY mes"),

        @NamedQuery(name = "AcquisitionEntity.countInsertedByMonth", query = "SELECT FUNCTION('MONTH', a.fxCreation) AS mes, COUNT(a) FROM AcquisitionEntity a "
                + "WHERE a.fxCreation IS NOT NULL AND FUNCTION('YEAR', a.fxCreation) = :year "
                + "AND (:uuidProvider IS NULL OR a.agenteUuid = :uuidProvider) "
                + "GROUP BY FUNCTION('MONTH', a.fxCreation) ORDER BY mes"),

        @NamedQuery(name = "AcquisitionEntity.countClosedByMonth",
                query = "SELECT FUNCTION('MONTH', a.fxCreation) AS mes, COUNT(a) FROM AcquisitionEntity a "
                        + "WHERE a.fxCreation IS NOT NULL AND FUNCTION('YEAR', a.fxCreation) = :year "
                        + "AND a.status = :status "
                        + "AND (:uuidProvider IS NULL OR a.agenteUuid = :uuidProvider) "
                        + "GROUP BY FUNCTION('MONTH', a.fxCreation) ORDER BY mes")

})

@Data
public class AcquisitionEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @javax.persistence.Id
    @javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    @javax.persistence.Column(name = "ID_ADQUISITION", nullable = false, insertable = true, updatable = true, length = 19)
    private Integer idAcquisition;

    @Column(name = "NOMBRE_CONTACTO", insertable = true, updatable = true, length = 300)
    private String nombreContacto;

    @Column(name = "TELEFONO_CONTACTO", insertable = true, updatable = true, length = 25)
    private String telefonoContacto;

    @Column(name = "EMAIL_CONTACTO", insertable = true, updatable = true, length = 300)
    private String emailContacto;

    @Column(name = "CP", insertable = true, updatable = true, length = 10)
    private String cp;

    @Column(name = "POBLACION", insertable = true, updatable = true, length = 300)
    private String poblacion;

    @Column(name = "PROVINCIA", insertable = true, updatable = true, length = 200)
    private String provincia;

    @Column(name = "NOMBRE_EMPRESA", insertable = true, updatable = true, length = 300)
    private String nombreEmpresa;

    @Column(name = "ACTIVIDAD", insertable = true, updatable = true, length = 500)
    private String actividad;

    @Column(name = "NEMPLEADOS", insertable = true, updatable = true, length = 200)
    private String nempleados;

    @Column(name = "TELEFONO_EMPRESA", insertable = true, updatable = true, length = 20)
    private String telefonoEmpresa;

    @Column(name = "TRABAJA_EMPRESA_PRL", insertable = true, updatable = true, length = 200)
    private String trabajaEmpresaPrl;

    @Column(name = "EMPRESA_ACTUAL_PRL", insertable = true, updatable = true, length = 300)
    private String empresaActualPrl;

    @Column(name = "EMPRESA_ACTUAL_PRL_FECHA_VTO", insertable = true, updatable = true, length = 20)
    private String empresaActualPrlFechaVto;

    @Column(name = "OBSERVACIONES", insertable = true, updatable = true, columnDefinition = "LONGTEXT")
    private String observaciones;

    @Column(name = "CAMPAING_PROVIDER", length = 300, insertable = true, updatable = true)
    private String campaignProvider;

    @Column(name = "CAMPAING_LEAD_ID", length = 300, insertable = true, updatable = true)
    private String campaignLeadId;

    @Column(name = "CAMPAING_ADSET_NAME", length = 300, insertable = true, updatable = true)
    private String campaignAdsetName;

    @Column(name = "CAMPAING_AD_NAME", length = 300, insertable = true, updatable = true)
    private String campaignAdName;

    @Column(name = "CAMPAIGN_NAME", length = 300, insertable = true, updatable = true)
    private String campaignName;

    @Column(name = "CAMPAIGN_FORM_NAME", length = 300, insertable = true, updatable = true)
    private String campaignFormName;

    @Column(name = "CAMPAIGN_PLATFORM", length = 300, insertable = true, updatable = true)
    private String campaignPlatform;

    @Column(name = "CAMPAIGN_URL", length = 300, insertable = true, updatable = true)
    private String campaignUrl;

    @Column(name = "CAMPAIGN_PRODUCT", length = 300, insertable = true, updatable = true)
    private String campaignProduct;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE_FIRSTCALL", unique = false, nullable = true, insertable = true, updatable = true)
    private Calendar dateFirstcall;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE_LASTCALL", unique = false, nullable = true, insertable = true, updatable = true)
    private Calendar dateLastcall;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE_NEXTCALL", unique = false, nullable = true, insertable = true, updatable = true)
    private Calendar dateNextcall;

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

    @Column(name = "AGENTE_USERNAME", insertable = true, updatable = true, length = 100)
    private String agenteUsername;

    @Column(name = "AGENTE_UUID", insertable = true, updatable = true, length = 300)
    private String agenteUuid;

    @Column(name = "UUID_AGENTE_CAPTADOR", insertable = true, updatable = true, length = 300)
    private String uuidAgenteCaptador;

    @Column(name = "UUID_PROVIDER", insertable = true, updatable = true, length = 300)
    private String uuidProvider;

    @Column(name = "STATUS", insertable = true, updatable = true, length = 25, columnDefinition = "VARCHAR(25)")
    @javax.persistence.Enumerated(javax.persistence.EnumType.STRING)
    private StatusAcquisitionEnum status;

    @Column(name = "USERNAME_CAPTADOR", insertable = true, updatable = true, length = 100)
    private String usernameCaptador;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FX_SHEDULING", unique = false, nullable = true, insertable = true, updatable = true)
    private Calendar fxScheduling;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FX_CREATION", unique = false, nullable = true, insertable = true, updatable = true)
    private Calendar fxCreation;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FX_MODIFICATION", unique = false, nullable = true, insertable = true, updatable = true)
    private Calendar fxModification;

    @Column(name = "OCM_LAST_CODING", insertable = true, updatable = true, length = 100)
    private String ocmLastCoding;

    @Column(name = "OCM_MOTOR", insertable = true, updatable = true, length = 50)
    private String ocmMotor;

    @Column(name = "OCM_LAST_AGENT", insertable = true, updatable = true, length = 50)
    private String ocmLastAgent;

    @Column(name = "OCM_END_RESULT", insertable = true, updatable = true)
    private String ocmEndResult;

    @Column(name = "OCM_ID", insertable = true, updatable = true)
    private Integer ocmId;
    
    @Column(name = "ID_LOAD", insertable = true, updatable = true)
    private Integer idLoad;
}
