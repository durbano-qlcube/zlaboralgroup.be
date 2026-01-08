package com.zap.maintenance.vo.rate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;


@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class SegmentVo implements java.io.Serializable
{
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 2904039297867803908L;



    private Integer idSegment;
    private String name;
    private String description;

}