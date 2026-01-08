package com.zap.stripe.vo;

import lombok.Data;

@Data
public class PriceVo {

    private String id;
    private String productId;
    private long unitAmount; 
    private String currency;
    private boolean active;
    private String interval; 
    private String metadata;

}
