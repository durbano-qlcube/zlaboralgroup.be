package com.zap.stripe.vo;

import lombok.Data;

@Data
public class CustomerVo {
    private String id;
    private String object;
    private String email;
    private String name;
    private String description;
    private long created;
    private boolean delinquent;
    private String invoicePrefix;
    private InvoiceSettingsVo invoiceSettings; 
    private boolean livemode;
    private String taxExempt;

    
}


