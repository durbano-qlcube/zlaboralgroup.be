package com.zap.stripe.vo;

import java.util.Calendar;
import java.util.Map;

import lombok.Data;

@Data
public class StripeEventVo {

    private String eventType;
    private String eventId;
    private String data;
    private String orderId;
    private Calendar timestamp;

   
}
