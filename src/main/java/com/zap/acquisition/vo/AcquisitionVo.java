package com.zap.acquisition.vo;

import java.io.Serializable;
import java.util.Calendar;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.zap.maintenance.service.adapter.TimeDateAdapter;

import lombok.Data;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class AcquisitionVo implements Serializable {
    private static final long serialVersionUID = -7836406887983296193L;

    private Integer idAcquisition;
    private String nombreContacto;
    private String telefonoContacto;
    private String emailContacto;
    private String cp;
    private String poblacion;
    private String provincia;

    private String nombreEmpresa;
    private String actividad;
    private String nempleados;
    private String telefonoEmpresa;
    private String trabajaEmpresaPrl;
    private String empresaActualPrl;
    private String empresaActualPrlFechaVto;
    private String observaciones;

    private String campaignProvider;
    private String campaignLeadId;
    private String campaignAdsetName;
    private String campaignAdName;
    private String campaignName;
    private String campaignFormName;
    private String campaignPlatform;
    private String campaignUrl;
    private String campaignProduct;

    private StatusAcquisitionEnum status;
    private Long parentCompanyId;

    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fxScheduling;

    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fxCreation;

    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fxModification;

    private String ocmLastCoding;
    private String ocmMotor;
    private Integer ocmId;
    private String ocmLastAgent;
    private Integer ocmEndResult;

    private String coordinadorUuid;
    private String coordinadorUserName;
    private String supervisorUuid;
    private String supervisorUserName;
    private String agenteUsername;
    private String agenteUuid;
    private String uuidAgenteCaptador;
    private String uuidProvider;

    private String usernameCaptador;
}
