package com.zap.stripe.vo;

import lombok.Data;
import java.util.Map;

@Data
public class ProductVo {
    private String id;
    private String object;
    private String name;
    private String description;
    private long created;
    private boolean active;
    private boolean livemode;
    private Map<String, String> metadata; 
    private long price;  

}
