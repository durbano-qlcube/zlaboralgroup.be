package com.zap.security.vo.authenticate;

import java.io.Serializable;

import lombok.Data;

@Data
public class LoginDataVo  implements Serializable
{

    private static final long serialVersionUID = -2109966671878671439L;

    private String displayName;
    private String photoURL;
    private String email;

}
