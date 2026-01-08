package com.zap.security.vo.authenticate;

import java.io.Serializable;
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
public class UserProviderVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long idUserProvider;
    private String uuidUser;
    private String uuidProvider;
    private String usernameProvider;

    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar fxCreation;
}
