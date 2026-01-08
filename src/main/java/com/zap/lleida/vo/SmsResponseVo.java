package com.zap.lleida.vo;

import lombok.Data;

@Data
public class SmsResponseVo {
    private String status;
    private String messageId;
    private String description;
    private String code;  
   
}
