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
import org.apache.commons.lang3.StringUtils;

import com.zap.acquisition.service.AcquisitionService;
import com.zap.acquisition.vo.AcquisitionVo;
import com.zap.acquisition.vo.StatusAcquisitionEnum;
import com.zap.ocm.services.OcmService;
import com.zap.ocm.vo.LeadsMotorVo;
import com.zap.ocm.vo.MotorLeadsVo;
import com.zap.scheduling.exception.JobActivityNotFoundException;
import com.zap.scheduling.service.JobActivityService;
import com.zap.scheduling.vo.JobActivityVo;
import com.zap.scheduling.vo.JobStatusEnum;
import com.zap.security.service.authenticate.AuthService;
import com.zap.security.vo.authenticate.AuthUserVo;

@Stateless
public class SincroOcmToZcubeJob implements Serializable {

	private static final long serialVersionUID = 8723406388316414111L;

	private static final Logger LOGGER = LoggerFactory.getLogger(SincroOcmToZcubeJob.class);
	private static final String JOB_NAME = "SINCRO_OCM_TO_ZAP";
	private static final String USERNAME = "JOB";
	private String TAG = "";
	private static Boolean hasToExecuteJob = false;
	private Long idJobActivity = 0L;

	@Inject
	JobActivityService jobActivityService;

	@Inject
	OcmService ocmService;

	@Inject
	AcquisitionService acquisitionService;

	@Inject
	private AuthService authService;

	@PostConstruct
	public void initializes() {
		hasToExecuteJob = true; // settingsService.hasToExecuteJob();
	}

	@TransactionAttribute(TransactionAttributeType.NEVER)
//	@Schedule(hour = "*", minute = "*/3")
	public void doExecute() {
		Long t = System.currentTimeMillis();
		try {
			if (hasToExecuteJob) {
				idJobActivity = this.initJob();
				TAG = "[SincroOcmToZcubeJob >> execute - " + idJobActivity + "]";
				LOGGER.info(TAG + "OCM TO ZAP CUBE>> call sincroleads......");
				this.sincroleads(TAG);

				this.finishJob(idJobActivity, JobStatusEnum.OK, "OK");
			}
		} catch (Exception ex) {
			LOGGER.error(TAG + " >> - Error: {}", ex);
			this.finishJob(idJobActivity, JobStatusEnum.ERROR, ex.getMessage());

		} finally {
			LOGGER.info(TAG + "finish Job >> *** FINISH >> Finish Timing:{}", (System.currentTimeMillis() - t));
		}

	}

	private void sincroleads(String TAG) {
		try {
			TAG = TAG + " >> sincroleads >>";

			List<LeadsMotorVo> Ocm = ocmService.loadLeadsMotorByMsisdn();

//			if (Ocm != null && !Ocm.isEmpty())
			{
				for (LeadsMotorVo ocm : Ocm) {
					try {

						AcquisitionVo existing = acquisitionService.loadByPhone(ocm.getNumber1());

						if (existing != null) {
							StatusAcquisitionEnum statusOCM = null;
							if (ocm.getStatus() != null && (ocm.getStatus() == 0 || ocm.getStatus() == 10)) {
								statusOCM = StatusAcquisitionEnum.CERRADO;
							} else {
								statusOCM = StatusAcquisitionEnum.ABIERTO;
							}

						
							if (!ocm.getEndResultDesc().equalsIgnoreCase(existing.getOcmLastCoding()) || 
									!existing.getStatus().toString().equals(statusOCM.toString()) ||
									(ocm.getDateLastcall() != null && !ocm.getDateLastcall().equals(existing.getOcmFxLastCall()))) {

								existing.setOcmLastAgent(ocm.getLastAgent());
								existing.setOcmLastCoding(ocm.getEndResultDesc());
								existing.setDescription(ocm.getObservaciones());
								existing.setOcmLastAgent(ocm.getLastAgent());
								existing.setOcmLastCoding(ocm.getEndResultDesc());
								existing.setFxInsertion(ocm.getDateInsert());
								existing.setOcmFxLastCall(ocm.getDateLastcall());
								existing.setDatefirstcall(ocm.getDateFirstcall());
								if (ocm.getStatus() != null && (ocm.getStatus() == 0 || ocm.getStatus() == 10)) {
									existing.setStatus(StatusAcquisitionEnum.CERRADO);
								} else {
									existing.setStatus(StatusAcquisitionEnum.ABIERTO);
								}

								existing.setCampaignLeadId(ocm.getCampaignLeadId());
								existing.setCampaignAdsetName(ocm.getCampaignAdsetName());
								existing.setCampaignAdName(ocm.getCampaignAdName());
								existing.setCampaignName(ocm.getCampaignName());
								existing.setCampaignFormName(ocm.getCampaignFormName());
								existing.setCampaignPlatform(ocm.getCampaignPlatform());
								existing.setCampaignUrl(ocm.getCampaignUrl());
								existing.setCampaignProduct(ocm.getCampaignProduct());

								acquisitionService.update(existing, false);
								LOGGER.info("{} - UPDATED MSISDN: {}", TAG, ocm.getNumber1());
							} else {
								LOGGER.debug("{} - NOTHING TO UPDATED FOR MSISDN: {}", TAG, ocm.getNumber1());
							}

						} else {
							LOGGER.info("{} - CREATING ADQUISITION LEADS MSISDN: {}", TAG, ocm.getNumber1());
							this.createAdquisition(ocm);
						}

					} catch (Exception e) {
						LOGGER.error("{} - Error procesando lead con número {}: {}", TAG, ocm.getNumber1(),
								e.getMessage(), e);
					}
				}
			}

		} catch (Exception e) {
			LOGGER.error("{} - Error en sincroleads(): {}", TAG, e.getMessage(), e);
		}
	}

	private void createAdquisition(LeadsMotorVo ocm) throws Exception {

		AcquisitionVo nuevo = new AcquisitionVo();
		nuevo.setPhone(ocm.getNumber1());
		nuevo.setName(ocm.getNombre());
		nuevo.setSurname(ocm.getApellidos());
		if (ocm.getStatus() != null && (ocm.getStatus() == 0 || ocm.getStatus() == 10)) {
			nuevo.setStatus(StatusAcquisitionEnum.CERRADO);
		} else {
			nuevo.setStatus(StatusAcquisitionEnum.CODIFICADO);
		}
		nuevo.setOcmId(ocm.getIdOcm().intValue());
		nuevo.setOcmLastAgent(ocm.getLastAgent());
		nuevo.setOcmLastCoding(ocm.getEndResultDesc());
		nuevo.setOcmMotor(ocm.getCampaignProvider());
		nuevo.setEmail(ocm.getEmail());
		nuevo.setDescription(ocm.getObservaciones());
		nuevo.setCif(null);
		nuevo.setFxInsertion(ocm.getDateInsert());
		nuevo.setOcmFxLastCall(ocm.getDateLastcall());
		nuevo.setDateNextcall(ocm.getDateNextcall());
		nuevo.setDatefirstcall(ocm.getDateFirstcall());
		nuevo.setOcmLastCoding(ocm.getEndResultDesc());
		nuevo.setEndResult(ocm.getEndResult());
		nuevo.setFullname(ocm.getFullname());
		nuevo.setCampaignLeadId(ocm.getCampaignLeadId());
		nuevo.setCampaignAdsetName(ocm.getCampaignAdsetName());
		nuevo.setCampaignAdName(ocm.getCampaignAdName());
		nuevo.setCampaignName(ocm.getCampaignName());
		nuevo.setCampaignFormName(ocm.getCampaignFormName());
		nuevo.setCampaignPlatform(ocm.getCampaignPlatform());
		nuevo.setCampaignUrl(ocm.getCampaignUrl());
		nuevo.setCampaignProduct(ocm.getCampaignProduct());

		if (ocm.getProvider() != null) {
			AuthUserVo authUserVo = authService.loadByUsername(ocm.getProvider().toUpperCase());
			if (authUserVo != null) {

				nuevo.setParentCompanyId(authUserVo.getParentCompanyId());
				nuevo.setUuidProvider(authUserVo.getUuid());
				nuevo.setAgenteUuid(authUserVo.getUuid());
				nuevo.setCoordinadorUserName(authUserVo.getCordinadorUsername());
				nuevo.setCoordinadorUuid(authUserVo.getUuidCordinador());
				acquisitionService.create(nuevo);
			} else {
				LOGGER.warn("{} - AuthUserVo is null for CMP_PROVIDER: {}", TAG, ocm.getCampaignProvider());
			}
		}

		LOGGER.info("{} - CREATED REGISTER CMP_PROVIDER:{} MSISDN: {}", TAG, ocm.getCampaignProvider(),
				ocm.getNumber1());

	}

	public boolean haPasadoUnMes(Calendar fechaInicial) {
		// Obtener la fecha actual
		Calendar fechaActual = Calendar.getInstance();

		// Agregar un mes a la fecha inicial
		fechaInicial.add(Calendar.MONTH, 1);

		// Comparar si la fecha actual es después de la fecha inicial más un mes
		return fechaActual.after(fechaInicial);
	}

	private Long initJob() {
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
	private void finishJob(Long idJobActivity, JobStatusEnum jobStatus, String log) {
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
			LOGGER.error("[ExpirationJob - finishJob] >> - Error: ", ex.getMessage());
		}

	}

}
