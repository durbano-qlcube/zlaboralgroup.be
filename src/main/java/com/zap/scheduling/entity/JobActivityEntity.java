package com.zap.scheduling.entity;


import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.zap.scheduling.vo.JobStatusEnum;



@javax.persistence.Entity
@javax.persistence.Table(name = "MNT_JOB_ACTIVITY")
@javax.persistence.NamedQueries({
	@javax.persistence.NamedQuery(name = "JobActivityEntity.loadAll", query = "SELECT  job FROM JobActivityEntity AS job"),
	@javax.persistence.NamedQuery(name = "JobActivityEntity.isJobExecuted", query = "SELECT job FROM JobActivityEntity AS job WHERE job.jobName=:jobName and date(job.executionInit)=:currentDate"),
	
})

public class JobActivityEntity implements Serializable, Comparable<JobActivityEntity>
{
	private static final long serialVersionUID = 7847645372201362008L;

	// ----------- Attribute Definitions ------------
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="ID_JOB_ACTIVITY", unique=true, insertable=true, updatable=true, nullable=false)
	private Long idJobActivity;

    @javax.persistence.Column(name = "JOB_NAME",  unique = false, nullable = true, insertable = true, updatable = true, length =100)
	private String jobName;
    
    @javax.persistence.Column(name = "USERNAME",  unique = false, nullable = true, insertable = true, updatable = true, length =100)
	private String username;
    
	@javax.persistence.Column(name = "IS_EXECUTE", nullable = true, insertable = true, updatable = true)
	private Boolean isExecute =Boolean.valueOf(false);
    
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "EXECUTION_INIT",  unique = false, nullable = true, insertable = true, updatable = true)
	private Calendar executionInit;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "EXECUTION_FINISH",  unique = false, nullable = true, insertable = true, updatable = true)
	private Calendar executionFinish;
	
	@javax.persistence.Column(name = "STATUS", insertable = true, updatable = true, length = 10, columnDefinition = "VARCHAR(10)")
    @javax.persistence.Enumerated(javax.persistence.EnumType.STRING)
	private JobStatusEnum jobStatusEnum;
	
    @javax.persistence.Column(name = "LOG",  unique = false, nullable = true, insertable = true, updatable = true, length =500)
	private String log;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATE_CREATION",  unique = false, nullable = true, insertable = true, updatable = true)
	private Calendar dateCreation;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATE_MODIFICATION",  unique = false, nullable = true, insertable = true, updatable = true)
	private Calendar dateModification;
	
    
	// --------- Relationship Definitions -----------


	
	// --------- GETTERs & SETTERs -----------



	public Long getIdJobActivity() {
		return idJobActivity;
	}


	public void setIdJobActivity(Long idJobActivity) {
		this.idJobActivity = idJobActivity;
	}


	public String getJobName() {
		return jobName;
	}


	public void setJobName(String jobName) {
		this.jobName = jobName;
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public Boolean getIsExecute() {
		return isExecute;
	}


	public void setIsExecute(Boolean isExecute) {
		this.isExecute = isExecute;
	}


	public Calendar getExecutionInit() {
		return executionInit;
	}


	public void setExecutionInit(Calendar executionInit) {
		this.executionInit = executionInit;
	}


	public Calendar getExecutionFinish() {
		return executionFinish;
	}


	public void setExecutionFinish(Calendar executionFinish) {
		this.executionFinish = executionFinish;
	}


	public JobStatusEnum getJobStatusEnum() {
		return jobStatusEnum;
	}


	public void setJobStatusEnum(JobStatusEnum jobStatusEnum) {
		this.jobStatusEnum = jobStatusEnum;
	}


	public String getLog() {
		return log;
	}


	public void setLog(String log) {
		this.log = log;
	}


	public Calendar getDateCreation() {
		return dateCreation;
	}


	public void setDateCreation(Calendar dateCreation) {
		this.dateCreation = dateCreation;
	}


	public Calendar getDateModification() {
		return dateModification;
	}


	public void setDateModification(Calendar dateModification) {
		this.dateModification = dateModification;
	}

	

	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((executionFinish == null) ? 0 : executionFinish.hashCode());
		result = prime * result
				+ ((executionInit == null) ? 0 : executionInit.hashCode());
		result = prime * result
				+ ((idJobActivity == null) ? 0 : idJobActivity.hashCode());
		result = prime * result
				+ ((isExecute == null) ? 0 : isExecute.hashCode());
		result = prime * result + ((jobName == null) ? 0 : jobName.hashCode());
		result = prime * result
				+ ((jobStatusEnum == null) ? 0 : jobStatusEnum.hashCode());
		result = prime * result + ((log == null) ? 0 : log.hashCode());
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JobActivityEntity other = (JobActivityEntity) obj;
		if (executionFinish == null) {
			if (other.executionFinish != null)
				return false;
		} else if (!executionFinish.equals(other.executionFinish))
			return false;
		if (executionInit == null) {
			if (other.executionInit != null)
				return false;
		} else if (!executionInit.equals(other.executionInit))
			return false;
		if (idJobActivity == null) {
			if (other.idJobActivity != null)
				return false;
		} else if (!idJobActivity.equals(other.idJobActivity))
			return false;
		if (isExecute == null) {
			if (other.isExecute != null)
				return false;
		} else if (!isExecute.equals(other.isExecute))
			return false;
		if (jobName == null) {
			if (other.jobName != null)
				return false;
		} else if (!jobName.equals(other.jobName))
			return false;
		if (jobStatusEnum != other.jobStatusEnum)
			return false;
		if (log == null) {
			if (other.log != null)
				return false;
		} else if (!log.equals(other.log))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

	
	

	@Override
	public String toString() {
		return "JobActivityEntity [idJobActivity=" + idJobActivity
				+ ", jobName=" + jobName + ", username=" + username
				+ ", isExecute=" + isExecute + ", executionInit="
				+ executionInit + ", executionFinish=" + executionFinish
				+ ", jobStatusEnum=" + jobStatusEnum + ", log=" + log
				+ ", dateCreation=" + dateCreation + ", dateModification="
				+ dateModification + "]";
	}


	@Override
	public int compareTo(JobActivityEntity o)
	{
		int cmp = 0;
		if (this.getIdJobActivity() != null)
		{
			cmp = this.getIdJobActivity().compareTo(o.getIdJobActivity());
		}
		else
		{
			cmp =-1;
		}
		return cmp;
	}
	
}
