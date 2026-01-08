package com.zap.security.vo.authenticate;

import java.io.Serializable;
import java.util.Calendar;

import lombok.Data;

@Data

public class SessionVo  implements Serializable
{

    private static final long serialVersionUID = -2109966671878671439L;

    //private String jwt; 		// Token
    private String name;
    private String surname;
    private String fullname;
    private String token;
    private String role;
    private String email;
    private String loginRedirectUrl;
    private LoginDataVo data;
    private Boolean isTemporalPassword;

    private Calendar expiresAt ;
}
