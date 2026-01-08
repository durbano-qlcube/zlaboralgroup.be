package com.zap.security.vo.authenticate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.zap.maintenance.service.adapter.TimeDateAdapter;

import lombok.Data;

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ProviderConfigVo implements Serializable {
    private static final long serialVersionUID = -8960117956241130069L;

    private Long idProviderConfig;
    private Long userId;
    private String userUuid;
    private BigDecimal costoLead;
    private BigDecimal invalidacion;

    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar invalidacionDesde;

    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar invalidacionHasta;

    private Integer totalInsertados;

    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fxCreation;

    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fxModification;
}
