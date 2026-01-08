package com.zap.security.vo.parentcompany;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ParentCompanyVo implements Serializable {

    private static final long serialVersionUID = -7999716468894715901L;

    private Long id;
    private String name;
}
