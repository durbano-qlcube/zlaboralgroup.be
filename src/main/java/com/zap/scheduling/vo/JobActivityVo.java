package com.zap.scheduling.vo;


import java.io.Serializable;
import java.util.Calendar;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.zap.maintenance.service.adapter.TimeDateAdapter;

import lombok.Data;



@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class JobActivityVo implements Serializable
{
	private static final long serialVersionUID = 7847645372201362008L;

	private Long idJobActivity;

	private String jobName;
    
	private String username;
    
	private Boolean isExecute = Boolean.valueOf(false);
    
	@XmlJavaTypeAdapter(TimeDateAdapter.class)
	private Calendar executionInit;
	
	@XmlJavaTypeAdapter(TimeDateAdapter.class)
	private Calendar executionFinish;
	
	private JobStatusEnum jobStatusEnum;
	
	private String log;
	
	@XmlJavaTypeAdapter(TimeDateAdapter.class)
	private Calendar dateCreation;

	@XmlJavaTypeAdapter(TimeDateAdapter.class)
	private Calendar dateModification;
	


	

	
}
