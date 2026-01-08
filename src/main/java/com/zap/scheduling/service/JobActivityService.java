package com.zap.scheduling.service;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zap.scheduling.entity.JobActivityEntity;
import com.zap.scheduling.exception.JobActivityNotFoundException;
import com.zap.scheduling.exception.JobActivityServiceException;
import com.zap.scheduling.vo.JobActivityVo;



@Stateless
public class JobActivityService implements Serializable{

	private static final long serialVersionUID = 2649869513517272345L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JobActivityService.class.getName());
	
	@PersistenceContext(unitName = "zapLaboralgrouPool")
	EntityManager em;
	
	public JobActivityService(){
		super();
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public JobActivityVo createJobActivity (JobActivityVo jobActivityVo)
	{
		
		LOGGER.debug("[JobActivityService - createJobActivity] - Init");
		long currentSystemTime = System.currentTimeMillis();
		
		if (jobActivityVo == null)
			throw new IllegalArgumentException("createJobActivity(JobActivityVo jobActivityVo) - 'jobActivityVo' can not be null");
		
		
		try{
			JobActivityEntity entity = toJobActivityEntity(jobActivityVo);
			entity.setDateCreation(Calendar.getInstance());
			em.persist(entity);
//			em.flush();
			return toJobActivityVo(entity);
			
		}catch (Exception ex){
			LOGGER.error("[JobActivityService - createJobActivity] - Error: {}",ex);
            throw new JobActivityServiceException(ex);
		
		}finally{
	    	LOGGER.debug("[JobActivityService - createJobActivity] - Finish Timing:{}", (System.currentTimeMillis()-currentSystemTime));
		}
		
	}
	
	public JobActivityVo loadJobActivityById (Long idJobActivity) throws JobActivityNotFoundException{
		
		LOGGER.debug("[JobActivityService - loadJobActivityById] - Init");
		long currentSystemTime = System.currentTimeMillis();
		
		if (idJobActivity == null)
			throw new IllegalArgumentException("loadJobActivityById(idJobActivity) - 'idJobActivity' can not be null");
		
		LOGGER.debug("[JobActivityService - loadJobActivityById] - input idJobActivity:{}", idJobActivity);
//		EntityManager emanager = NotificationEntityManagerFactory.createEntityManager();
		
		try{
			
			JobActivityEntity entity = em.find(JobActivityEntity.class, idJobActivity);
			
			if(entity == null)
				throw new JobActivityNotFoundException();
			
			return this.toJobActivityVo(entity);
			
		}catch (JobActivityNotFoundException ex){
			LOGGER.error("[JobActivityService - loadJobActivityById] - Error: {}",ex);
			throw ex;
           
		}catch (Exception ex){
			LOGGER.error("[JobActivityService - loadJobActivityById] - Error: {}",ex);
			throw new JobActivityServiceException(ex);

		}finally{
//			emanager.close();
	    	LOGGER.debug("[JobActivityService - loadJobActivityById] - Finish Timing:{}", (System.currentTimeMillis()-currentSystemTime));
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void deleteJobActivityById (Long idJobActivity) throws JobActivityNotFoundException{
		
		LOGGER.debug("[JobActivityService - deleteJobActivityById] - Init");
		long currentSystemTime = System.currentTimeMillis();
		
		if (idJobActivity == null)
			throw new IllegalArgumentException("deleteJobActivityById(idJobActivity) - 'idJobActivity' can not be null");
		
		LOGGER.debug("[JobActivityService - deleteJobActivityById] - input idJobActivity:{}", idJobActivity);
//		EntityManager emanager = NotificationEntityManagerFactory.createEntityManager();
		
		try{
			
			JobActivityEntity entity = em.find(JobActivityEntity.class, idJobActivity);
			
			if(entity == null)
				throw new JobActivityNotFoundException();
			
//			emanager.getTransaction().begin();
			em.remove(entity);
//			emanager.flush();
//			emanager.getTransaction().commit();

		}catch (JobActivityNotFoundException ex){
			LOGGER.error("[JobActivityService - deleteJobActivityById] - Error: {}",ex);
			throw ex;
           
		}catch (Exception ex){
			LOGGER.error("[JobActivityService - deleteJobActivityById] - Error: {}",ex);
//			emanager.getTransaction().rollback();
			throw new JobActivityServiceException(ex);

		}finally{
//			emanager.close();
	    	LOGGER.debug("[JobActivityService - deleteJobActivityById] - Finish Timing:{}", (System.currentTimeMillis()-currentSystemTime));
		}
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void updateJobActivity (JobActivityVo jobActivityVo, Boolean copyIfNull) throws JobActivityNotFoundException{
		
		LOGGER.debug("[JobActivityService - updateJobActivity] - Init");
		long currentSystemTime = System.currentTimeMillis();
		
		if (jobActivityVo == null)
			throw new IllegalArgumentException("updateJobActivity(jobActivityVo) - 'jobActivityVo' can not be null");
		
		if (jobActivityVo.getIdJobActivity() == null)
			throw new IllegalArgumentException("updateJobActivity(updateJobActivity) - 'idJobActivity' can not be null");
		
		if (copyIfNull == null)
			throw new IllegalArgumentException("updateJobActivity(JobActivityVo updateJobActivity, Boolean copyIfNull) - 'copyIfNull' can not be null");

//		EntityManager emanager = NotificationEntityManagerFactory.createEntityManager();
//		emanager.getTransaction().begin();
		
		try{
			
			JobActivityEntity entity = em.find(JobActivityEntity.class, jobActivityVo.getIdJobActivity());
			
			if(entity == null)
				throw new JobActivityNotFoundException();
			
			this.toJobActivityEntity(jobActivityVo, entity, copyIfNull);
			entity.setDateModification(Calendar.getInstance());
			em.merge(entity);
//			emanager.flush();
//			emanager.getTransaction().commit();
			
		}catch (JobActivityNotFoundException ex){
			LOGGER.error("[JobActivityService - updateJobActivity] - Error: {}",ex);
//			emanager.getTransaction().rollback();
			throw ex;
           
		}catch (Exception ex){
			LOGGER.error("[JobActivityService - updateJobActivity] - Error: {}",ex);
//			emanager.getTransaction().rollback();
			throw new JobActivityServiceException(ex);

		}finally{
//			emanager.close();
	    	LOGGER.debug("[JobActivityService - updateJobActivity] - Finish Timing:{}", (System.currentTimeMillis()-currentSystemTime));
		}
	}
	
	
	public Boolean isJobExecuted (String jobName, Calendar currentDate)
	{
	
		LOGGER.debug("[JobActivityService - isJobExecuted] - Init");
		long currentSystemTime=System.currentTimeMillis();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		Boolean result = false;

		if (jobName == null)
			throw new IllegalArgumentException("[JobActivityService - isJobExecuted] - isJobExecuted (String jobName, Calendar currentDate) - 'jobName' can not be null");

		if (currentDate == null)
			throw new IllegalArgumentException("[JobActivityService - isJobExecuted] - isJobExecuted (String jobName, Calendar currentDate) - 'currentDate' can not be null");


//		EntityManager emanager = NotificationEntityManagerFactory.createEntityManager();
		
		try {
			Query query = em.createNamedQuery("JobActivityEntity.isJobExecuted");
			query.setParameter("jobName", jobName);
			query.setParameter("currentDate", currentDate,TemporalType.DATE);

			JobActivityEntity entity = (JobActivityEntity) query.getSingleResult();
			result =  true;

		}catch (javax.persistence.NoResultException ex){
			LOGGER.error("[JobActivityService - isJobExecuted] - Error: not found jobName: {} for day: {}",jobName, dateFormat.format(currentDate.getTime()));
			
		}catch (javax.persistence.NonUniqueResultException ex){
			LOGGER.error("[JobActivityService - isJobExecuted] - Error: {}",ex);
			throw new JobActivityServiceException(ex);
			
		}catch (Exception ex){
			LOGGER.error("[JobActivityService - isJobExecuted] - Error: {}",ex);
			throw new JobActivityServiceException(ex);

		}finally{
//			emanager.close();
			LOGGER.debug("[JobActivityService - isJobExecuted] - Finish Timing: {}",(System.currentTimeMillis()-currentSystemTime));
		}
		
		return result;
	}
	
	
	private void toJobActivityEntity(JobActivityVo source, JobActivityEntity target, Boolean copyIfNull)
	{

		if(copyIfNull || source.getIdJobActivity()!=null)
			target.setIdJobActivity(source.getIdJobActivity());

		if(copyIfNull || source.getJobName()!=null)
			target.setJobName(source.getJobName());

		if(copyIfNull || source.getUsername()!=null)
			target.setUsername(source.getUsername());

		if(copyIfNull || source.getIsExecute()!=null)
			target.setIsExecute(source.getIsExecute());

		if(copyIfNull || source.getExecutionInit()!=null)
			target.setExecutionInit(source.getExecutionInit());

		if(copyIfNull || source.getExecutionFinish()!=null)
			target.setExecutionFinish(source.getExecutionFinish());

		if(copyIfNull || source.getJobStatusEnum()!=null)
			target.setJobStatusEnum(source.getJobStatusEnum());

		if(copyIfNull || source.getLog()!=null)
			target.setLog(source.getLog());

		if(copyIfNull || source.getDateCreation()!=null)
			target.setDateCreation(source.getDateCreation());

	}

	private JobActivityVo toJobActivityVo(JobActivityEntity source) {
		JobActivityVo target = new JobActivityVo();
		target.setIdJobActivity(source.getIdJobActivity());

		target.setJobName(source.getJobName());
		target.setUsername(source.getUsername());
		target.setIsExecute(source.getIsExecute());
		target.setExecutionInit(source.getExecutionInit());
		target.setExecutionFinish(source.getExecutionFinish());
		target.setJobStatusEnum(source.getJobStatusEnum());
		target.setLog(source.getLog());
		
		target.setDateCreation(source.getDateCreation());
		target.setDateModification(source.getDateModification());
		return target;
	}


	private JobActivityEntity toJobActivityEntity(JobActivityVo source)
	{
		JobActivityEntity target = new JobActivityEntity();
		target.setJobName(source.getJobName());
		target.setUsername(source.getUsername());
		target.setIsExecute(source.getIsExecute());
		target.setExecutionInit(source.getExecutionInit());
		target.setExecutionFinish(source.getExecutionFinish());
		target.setJobStatusEnum(source.getJobStatusEnum());
		target.setLog(source.getLog());
		
		return target;
	}

}
