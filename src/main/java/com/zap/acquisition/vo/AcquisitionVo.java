package com.zap.acquisition.vo;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.Column;
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
	private String nombreComercial;
	private String cif;
	private String name;
	private String surname;
	private String surname2;
	private String fullname;
        private String email;
        private String phone;
        private String description;
        private String campaign;
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
	private Calendar fxSendToOcm;

	@XmlJavaTypeAdapter(TimeDateAdapter.class)
	private Calendar fxInsertion;

	@XmlJavaTypeAdapter(TimeDateAdapter.class)
	private Calendar dateNextcall;
	
	@XmlJavaTypeAdapter(TimeDateAdapter.class)
	private Calendar datefirstcall;

	private String ocmLastCoding;
	private String ocmMotor;
	private Integer ocmId;
	private String ocmLastAgent;

	@XmlJavaTypeAdapter(TimeDateAdapter.class)
	private Calendar ocmFxLastCall;

	private String coordinadorUuid;
	private String coordinadorUserName;
	private String supervisorUuid;
	private String supervisorUserName;
	private String agenteUsername;
	private String agenteUuid;

	private String origin;
	private String originUserUsername;
	private String originUserUuid;
	private String originGestoriaUuid;
	private Integer originIdAdquision;

	@XmlJavaTypeAdapter(TimeDateAdapter.class)
	private Calendar fxCreation;

	@XmlJavaTypeAdapter(TimeDateAdapter.class)
	private Calendar fxModification;

	private String razonSocial;

	private String uuidProvider;
	private Integer endResult;
	private String endResultDesc;
	private String endResultGroup;
}
