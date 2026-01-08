package com.zap.gestdirect.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.zap.maintenance.service.adapter.TimeDateAdapter;

import lombok.Data;

@Data
public class ResponseGestdirectVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer code;
    private String description;
    
    

    
}
