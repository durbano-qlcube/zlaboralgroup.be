package com.zap.security.vo.parentcompany;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserParentCompanyVo implements Serializable {

    private static final long serialVersionUID = -2475609634964642664L;

    private Long userId;
    private Long parentCompanyId;
}
