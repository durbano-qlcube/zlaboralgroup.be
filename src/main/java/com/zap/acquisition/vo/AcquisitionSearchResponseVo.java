package com.zap.acquisition.vo;

import java.io.Serializable;
import java.util.Calendar;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.gson.annotations.SerializedName;
import com.zap.maintenance.service.adapter.TimeDateAdapter;

import lombok.Data;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class AcquisitionSearchResponseVo implements Serializable {
    private static final long serialVersionUID = -7836406887983296193L;

    private Integer idAcquisition;
    private String fullname;
    private String phone;
    private String uuidProvider;
    private String providerName;

    @SerializedName("lastOcmCoding")
    private String ocmLastCoding;

    @SerializedName("lastOcmAgent")
    private String ocmLastAgent;

    @SerializedName("usernameCaptador")
    private String usernameCaptador;

    private StatusAcquisitionEnum status;

    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fxCreation;

    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    @SerializedName("fxInsertion")
    private Calendar fxInsertion;

    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    @SerializedName("fxLastCall")
    private Calendar ocmFxLastCall;

    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    @SerializedName("ocmFxFirstCall")
    private Calendar ocmFxFirstCall;

    private String email;

    private String razonSocial;

    public AcquisitionSearchResponseVo(Integer idAcquisition, String fullname, String phone, String ocmLastCoding,
            String ocmLastAgent, String usernameCaptador, StatusAcquisitionEnum status, Calendar fxCreation,
            Calendar fxInsertion, Calendar ocmFxLastCall, Calendar ocmFxFirstCall, String email, String razonSocial,
            String uuidProvider, String providerName) {
        super();
        this.idAcquisition = idAcquisition;
        this.fullname = fullname;
        this.phone = phone;
        this.uuidProvider = uuidProvider;
        this.providerName = providerName;
        this.ocmLastCoding = ocmLastCoding;
        this.ocmLastAgent = ocmLastAgent;
        this.usernameCaptador = usernameCaptador;
        this.status = status;
        this.fxCreation = fxCreation;
        this.fxInsertion = fxInsertion;
        this.ocmFxLastCall = ocmFxLastCall;
        this.ocmFxFirstCall = ocmFxFirstCall;
        this.email = email;
        this.razonSocial = razonSocial;
    }
}
