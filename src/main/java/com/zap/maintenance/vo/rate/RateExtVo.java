package com.zap.maintenance.vo.rate;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.zap.maintenance.vo.settings.ComboBoxVo;

import lombok.Data;


@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class RateExtVo implements java.io.Serializable
{
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 2904039297867803908L;


    private Integer idSegment;
    private Integer code;
    private String value;
    private List<ComboBoxVo> discounts;

}