package com.zap.maintenance.vo.notifications;

import java.util.List;

/**
 * 
 */
public class EmailVo implements java.io.Serializable
{
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = -8495060732387475485L;

    public EmailVo()
    {
    }




    private String to;
    private String from;
    private String cc;
    private String cco;
    private String subject;
    private String bodyTxt;
    private String body;
    private String bodyHtml;
    private List<String> toList;
    private String fromName;
    private String unsuscribeLink;
    
    
    
    public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}


	

    /**
     * 
     */
    public String getTo()
    {
        return this.to;
    }

    public void setTo(String to)
    {
        this.to = to;
    }

    

    /**
     * 
     */
    public String getCc()
    {
        return this.cc;
    }

    public void setCc(String cc)
    {
        this.cc = cc;
    }

   

    /**
     * 
     */
    public String getCco()
    {
        return this.cco;
    }

    public void setCco(String cco)
    {
        this.cco = cco;
    }

   

    /**
     * 
     */
    public String getSubject()
    {
        return this.subject;
    }

    public void setSubject(String subject)
    {
        this.subject = subject;
    }


    
    
    
    public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getBodyTxt() {
		return bodyTxt;
	}

	public void setBodyTxt(String bodyTxt) {
		this.bodyTxt = bodyTxt;
	}

	public String getBodyHtml() {
		return bodyHtml;
	}

	public void setBodyHtml(String bodyHtml) {
		this.bodyHtml = bodyHtml;
	}


	
    
    public List<String> getToList() {
		return toList;
	}

	public void setToList(List<String> toList) {
		this.toList = toList;
	}


	 

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}


	 

	public String getUnsuscribeLink() {
		return unsuscribeLink;
	}

	public void setUnsuscribeLink(String unsuscribeLink) {
		this.unsuscribeLink = unsuscribeLink;
	}

	@Override
	public String toString() {
		return "EmailVo [to=" + to + ", from=" + from + ", cc=" + cc + ", cco=" + cco + ", subject=" + subject
				+ ", bodyTxt=" + bodyTxt + ", body=" + body + "]";
	}
	 
	 
    
}