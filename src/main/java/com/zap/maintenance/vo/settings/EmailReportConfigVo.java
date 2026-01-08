package com.zap.maintenance.vo.settings;

import java.io.Serializable;

public class EmailReportConfigVo implements Serializable{

	private static final long serialVersionUID = -7554602364937160698L;

	private String from;
	private String subject;
	
	/** Getters & Setters **/
	
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	/** toString **/
	
	@Override
	public String toString() {
		return "EmailReportConfigVo [from=" + from + ", subject=" + subject + "]";
	}
	
}
