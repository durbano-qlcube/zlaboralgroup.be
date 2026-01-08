package com.zap.stripe.vo;

import lombok.Data;

@Data
public class PaymentLinkVo {

    private String id;
    private String url;
    private boolean active;
    

}
