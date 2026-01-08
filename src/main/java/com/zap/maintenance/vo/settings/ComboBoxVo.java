package com.zap.maintenance.vo.settings;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ComboBoxVo implements Comparable
{
	private static final long serialVersionUID = -7836406887983296193L;
	
	

	
	private String	val;
	private String	code;
	private String	category;
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	
	public String getVal() {
		return val;
	}
	public void setVal(String val) {
		this.val = val;
	}
	
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	@Override
	public String toString() {
		return "ComboBoxVo [val=" + val + ", code=" + code + "]";
	}
	
	@Override
	public int compareTo(Object arg0) {
		
	    return this.val.compareTo(((String)arg0));
	}
	

}
