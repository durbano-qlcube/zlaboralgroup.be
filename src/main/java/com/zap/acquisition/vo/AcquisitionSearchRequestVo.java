package com.zap.acquisition.vo;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.zap.maintenance.service.adapter.TimeDateAdapter;

import lombok.Data;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class AcquisitionSearchRequestVo implements Serializable {
    private static final long serialVersionUID = -7836406887983296193L;

    private String nombreContacto;
    private String emailContacto;
    private String telefonoContacto;
    private String ocmLastCoding;
    private String ocmLastAgent;

    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fxInicio;

    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fxFin;

    private String uuidCaptador;
    private String status;

    private Long parentCompanyId;
    private List<Long> parentCompanyIds;
    private String uuidProvider;
    private String uuidSubProvider;
}
