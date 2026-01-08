package com.zap.security.vo.authenticate;

import java.io.Serializable;
import java.util.Calendar;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ChangePassVo  implements Serializable
{

    private static final long serialVersionUID = -2109966671878671439L;

   
    private String email;
    private String password;
    private Integer howManyMonthsPassExpires;
    private Boolean hasToCheckHistoryPass;
    private String verificationToken;
    private Boolean temporal;
 
}
