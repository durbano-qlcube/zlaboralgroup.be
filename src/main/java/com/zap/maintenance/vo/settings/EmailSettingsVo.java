package com.zap.maintenance.vo.settings;

/**
 * 
 */
public class EmailSettingsVo    implements java.io.Serializable
{
    /**
     * The serial version UID of this class. Needed for serialization.
     */
    private static final long serialVersionUID = 6204093156810055731L;

    public EmailSettingsVo()
    {
    }

    public EmailSettingsVo(java.lang.String host, java.lang.String port, java.lang.String starttls, java.lang.String authIsNeeds, java.lang.Boolean debug, java.lang.String transport, java.lang.String user, java.lang.String pass, java.lang.String from, java.lang.String logo)
    {
        this.host = host;
        this.port = port;
        this.starttls = starttls;
        this.authIsNeeds = authIsNeeds;
        this.debug = debug;
        this.transport = transport;
        this.user = user;
        this.pass = pass;
        this.from = from;
        this.logo = logo;
    }

    /**
     * Copies constructor from other EmailSettingsVo
     *
     * @param otherBean, cannot be <code>null</code>
     * @throws java.lang.NullPointerException if the argument is <code>null</code>
     */
    public EmailSettingsVo(EmailSettingsVo otherBean)
    {
        this(otherBean.getHost(), otherBean.getPort(), otherBean.getStarttls(), otherBean.getAuthIsNeeds(), otherBean.getDebug(), otherBean.getTransport(), otherBean.getUser(), otherBean.getPass(), otherBean.getFrom(), otherBean.getLogo());
    }

    /**
     * Copies all properties from the argument value object into this value object.
     */
    public void copy(EmailSettingsVo otherBean)
    {
        if (otherBean != null)
        {
            this.setHost(otherBean.getHost());
            this.setPort(otherBean.getPort());
            this.setStarttls(otherBean.getStarttls());
            this.setAuthIsNeeds(otherBean.getAuthIsNeeds());
            this.setDebug(otherBean.getDebug());
            this.setTransport(otherBean.getTransport());
            this.setUser(otherBean.getUser());
            this.setPass(otherBean.getPass());
            this.setFrom(otherBean.getFrom());
            this.setLogo(otherBean.getLogo());
        }
    }

    private java.lang.String host;

    /**
     * 
     */
    public java.lang.String getHost()
    {
        return this.host;
    }

    public void setHost(java.lang.String host)
    {
        this.host = host;
    }

    private java.lang.String port;

    /**
     * 
     */
    public java.lang.String getPort()
    {
        return this.port;
    }

    public void setPort(java.lang.String port)
    {
        this.port = port;
    }

    private java.lang.String starttls;

    /**
     * 
     */
    public java.lang.String getStarttls()
    {
        return this.starttls;
    }

    public void setStarttls(java.lang.String starttls)
    {
        this.starttls = starttls;
    }

    private java.lang.String authIsNeeds;

    /**
     * 
     */
    public java.lang.String getAuthIsNeeds()
    {
        return this.authIsNeeds;
    }

    public void setAuthIsNeeds(java.lang.String authIsNeeds)
    {
        this.authIsNeeds = authIsNeeds;
    }

    private java.lang.Boolean debug;

    /**
     * 
     */
    public java.lang.Boolean getDebug()
    {
        return this.debug;
    }

    public void setDebug(java.lang.Boolean debug)
    {
        this.debug = debug;
    }

    private java.lang.String transport;

    /**
     * 
     */
    public java.lang.String getTransport()
    {
        return this.transport;
    }

    public void setTransport(java.lang.String transport)
    {
        this.transport = transport;
    }

    private java.lang.String user;

    /**
     * 
     */
    public java.lang.String getUser()
    {
        return this.user;
    }

    public void setUser(java.lang.String user)
    {
        this.user = user;
    }

    private java.lang.String pass;

    /**
     * 
     */
    public java.lang.String getPass()
    {
        return this.pass;
    }

    public void setPass(java.lang.String pass)
    {
        this.pass = pass;
    }

    private java.lang.String from;

    /**
     * 
     */
    public java.lang.String getFrom()
    {
        return this.from;
    }

    public void setFrom(java.lang.String from)
    {
        this.from = from;
    }

    private java.lang.String logo;

    /**
     * 
     */

	public java.lang.String getLogo() {
		return logo;
	}

	public void setLogo(java.lang.String logo) {
		this.logo = logo;
	}

	@Override
	public String toString() {
		return "EmailSettingsVo [host=" + host + ", port=" + port
				+ ", starttls=" + starttls + ", authIsNeeds=" + authIsNeeds
				+ ", debug=" + debug + ", transport=" + transport + ", user="
				+ user + ", pass=" + pass + ", from=" + from + ", logoNubbler="
				+ logo + "]";
	}

	
	
    
}