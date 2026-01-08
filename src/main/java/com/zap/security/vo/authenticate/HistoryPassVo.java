package com.zap.security.vo.authenticate;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class HistoryPassVo  implements Serializable
{

    private static final long serialVersionUID = -2109966671878671439L;

    private Long id;

    private String uuid;
    private String pass;
    
    
}
