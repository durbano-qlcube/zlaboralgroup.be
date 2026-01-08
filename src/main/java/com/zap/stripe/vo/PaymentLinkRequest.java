package com.zap.stripe.vo;

import lombok.Data;

@Data
public class PaymentLinkRequest {

    private String priceId;
    private long quantity;
    private String orderId;  
  
}
