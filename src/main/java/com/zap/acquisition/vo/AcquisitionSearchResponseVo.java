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
    private String nombreContacto;
    private String telefonoContacto;
    private String emailContacto;
    private String nombreEmpresa;
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
    private Calendar fxModification;

    @SerializedName("DateInsert")
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar dateInsert;

    @SerializedName("DateLastcall")
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar dateLastcall;

    public AcquisitionSearchResponseVo(Integer idAcquisition, String nombreContacto, String telefonoContacto,
            String emailContacto, String nombreEmpresa, String ocmLastCoding, String ocmLastAgent,
            String usernameCaptador, StatusAcquisitionEnum status, Calendar fxCreation, Calendar fxModification,
            Calendar dateLastcall, String uuidProvider, String providerName) {
        super();
        this.idAcquisition = idAcquisition;
        this.nombreContacto = nombreContacto;
        this.telefonoContacto = telefonoContacto;
        this.emailContacto = emailContacto;
        this.nombreEmpresa = nombreEmpresa;
        this.ocmLastCoding = ocmLastCoding;
        this.ocmLastAgent = ocmLastAgent;
        this.usernameCaptador = usernameCaptador;
        this.status = status;
        this.fxCreation = fxCreation;
        this.fxModification = fxModification;
        this.dateInsert = fxCreation;
        this.dateLastcall = dateLastcall;
        this.uuidProvider = uuidProvider;
        this.providerName = providerName;
    }
}
