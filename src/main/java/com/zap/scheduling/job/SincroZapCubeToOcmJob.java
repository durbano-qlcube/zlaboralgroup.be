package com.zap.scheduling.job;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zap.acquisition.service.AcquisitionService;
import com.zap.acquisition.vo.AcquisitionVo;
import com.zap.acquisition.vo.StatusAcquisitionEnum;
import com.zap.ocm.services.OcmService;
import com.zap.scheduling.exception.JobActivityNotFoundException;
import com.zap.scheduling.service.JobActivityService;
import com.zap.scheduling.vo.JobActivityVo;
import com.zap.scheduling.vo.JobStatusEnum;



@Stateless
public class SincroZapCubeToOcmJob implements Serializable
{

	private static final long serialVersionUID = 8723406388316414111L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SincroZapCubeToOcmJob.class);
	private static final String JOB_NAME="SINCRO_CAPTURADOR_TO_OCM";
	private static final String USERNAME="JOB";
	private String TAG ="";
	private static Boolean hasToExecuteJob=false;
	private Long idJobActivity = 0L; 


	@Inject
	JobActivityService jobActivityService;
	
	@Inject
	OcmService ocmService;
	
	@Inject
	AcquisitionService acquisitionService;
	
	
	
	@PostConstruct
	public void initializes()
	{
		hasToExecuteJob = true ; //settingsService.hasToExecuteJob();
	}
	
	
	@TransactionAttribute(TransactionAttributeType.NEVER)
	@Schedule(hour = "*", minute = "*", second = "*/60")
	public void doExecute()
	{  
		Long t= System.currentTimeMillis();
		try {
			if(hasToExecuteJob)
			{	
				
				idJobActivity = this.initJob();
				TAG = "[SincroZapCubeToOcmJob >> execute - "+idJobActivity+"]";

				LOGGER.info(TAG + "ZAP CUBE TO OCM>> call sincroleads......");
				this.sincroleads(TAG);

				this.finishJob(idJobActivity, JobStatusEnum.OK, "OK");
			}
		}catch (Exception ex){
			LOGGER.error(TAG + " >> - Error: {}",ex);
			this.finishJob(idJobActivity, JobStatusEnum.ERROR, ex.getMessage());

		}finally{
			LOGGER.info(TAG + "finish Job >> *** FINISH >> Finish Timing:{}", (System.currentTimeMillis()-t));
		}

	} 
	
	private void sincroleads(String TAG)
	{
		
		TAG = TAG +" >> sincroleads >>";
		try {
			List<AcquisitionVo> leads = acquisitionService.loadByStatus(StatusAcquisitionEnum.ENVIAR_OCM);
			
			LOGGER.info(TAG + " loaded leads {} with status ENVIAR_OCM ......", leads.size());

			
			
			if(leads!=null && !leads.isEmpty())
			{
				for (AcquisitionVo lead: leads)
				{
					try {
						lead = ocmService.register(lead);
						LOGGER.info(TAG + "{} >> Registered in OCM ......", lead.getPhone());

					
						lead.setStatus(StatusAcquisitionEnum.PROCESADO);
						lead.setFxSendToOcm(Calendar.getInstance());
						lead.setOcmMotor("01-CAPTURADOR LEADS CUBE");
						acquisitionService.update(lead, false);
						LOGGER.info(TAG + "{} >> Updated in Status PROCESADO ......", lead.getPhone());

					} catch (Exception e) {
//						lead.setStatus(StatusAcquisitionEnum.ERROR);
//						acquisitionService.update(lead, false);
						LOGGER.error("{} - response Message:{} {}",  TAG, e.getMessage(), e.getLocalizedMessage());
					}


				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	

	private Long initJob()
	{
		JobActivityVo jobActivityVo = new JobActivityVo();
		jobActivityVo.setJobName(JOB_NAME);
		jobActivityVo.setIsExecute(false);
		jobActivityVo.setJobStatusEnum(JobStatusEnum.PROCESING);
		jobActivityVo.setUsername(USERNAME);
		jobActivityVo.setExecutionInit(Calendar.getInstance());
		jobActivityVo = jobActivityService.createJobActivity(jobActivityVo);

		return jobActivityVo.getIdJobActivity();

	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	private void finishJob(Long idJobActivity, JobStatusEnum jobStatus, String log)
	{
		JobActivityVo jobActivityVo = new JobActivityVo();
		jobActivityVo.setIdJobActivity(idJobActivity);

		if (JobStatusEnum.OK.equals(jobStatus))
			jobActivityVo.setIsExecute(true);

		jobActivityVo.setJobStatusEnum(jobStatus);
		jobActivityVo.setLog(log);
		jobActivityVo.setExecutionFinish(Calendar.getInstance());
		try {
			jobActivityService.updateJobActivity(jobActivityVo, false);

		} catch (JobActivityNotFoundException ex) {
			LOGGER.error("[ExpirationJob - finishJob] >> - Error: ",ex.getMessage());
		}


	}

}
