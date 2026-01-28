package com.zap.scheduling.job;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

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
public class SincroOcmToZlaboralJob implements Serializable {

	private static final long serialVersionUID = 8723406388316414111L;

	private static final Logger LOGGER = LoggerFactory.getLogger(SincroOcmToZlaboralJob.class);
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
	@Schedule(hour = "*", minute = "*/3")
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

							String telefonoContacto = StringUtils.defaultIfBlank(ocm.getTelefonoContacto(),
									ocm.getNumber1());
							boolean canUpdateStatus = !StatusAcquisitionEnum.CODIFICADO.equals(existing.getStatus());
							boolean statusChanged = canUpdateStatus && (existing.getStatus() == null
									|| !existing.getStatus().toString().equals(statusOCM.toString()));
							boolean shouldUpdate = statusChanged
									|| !StringUtils.equalsIgnoreCase(ocm.getEndResultDesc(),
											existing.getOcmLastCoding())
									|| !StringUtils.equals(ocm.getObservaciones(), existing.getObservaciones())
									|| !StringUtils.equals(ocm.getEmail(), existing.getEmailContacto())
									|| !StringUtils.equals(ocm.getNombreContacto(), existing.getNombreContacto())
									|| !StringUtils.equals(telefonoContacto, existing.getTelefonoContacto())
									|| !StringUtils.equals(ocm.getNombreEmpresa(), existing.getNombreEmpresa())
									|| !StringUtils.equals(ocm.getActividad(), existing.getActividad())
									|| !StringUtils.equals(ocm.getNempleados(), existing.getNempleados())
									|| !StringUtils.equals(ocm.getTelefonoEmpresa(), existing.getTelefonoEmpresa())
									|| !StringUtils.equals(ocm.getTrabajaEmpresaPrl(),
											existing.getTrabajaEmpresaPrl())
									|| !StringUtils.equals(ocm.getEmpresaActualPrl(),
											existing.getEmpresaActualPrl())
									|| !StringUtils.equals(ocm.getEmpresaActualPrlFechaVto(),
											existing.getEmpresaActualPrlFechaVto())
									|| !StringUtils.equals(ocm.getCp(), existing.getCp())
									|| !StringUtils.equals(ocm.getPoblacion(), existing.getPoblacion())
									|| !StringUtils.equals(ocm.getProvincia(), existing.getProvincia())
									|| !Objects.equals(ocm.getDateFirstcall(), existing.getDateFirstcall())
									|| !Objects.equals(ocm.getDateLastcall(), existing.getDateLastcall())
									|| !Objects.equals(ocm.getDateNextcall(), existing.getDateNextcall())
									|| !StringUtils.equals(ocm.getCampaignProvider(), existing.getCampaignProvider())
									|| !StringUtils.equals(ocm.getCampaignLeadId(), existing.getCampaignLeadId())
									|| !StringUtils.equals(ocm.getCampaignAdsetName(), existing.getCampaignAdsetName())
									|| !StringUtils.equals(ocm.getCampaignAdName(), existing.getCampaignAdName())
									|| !StringUtils.equals(ocm.getCampaignName(), existing.getCampaignName())
									|| !StringUtils.equals(ocm.getCampaignFormName(), existing.getCampaignFormName())
									|| !StringUtils.equals(ocm.getCampaignPlatform(), existing.getCampaignPlatform())
									|| !StringUtils.equals(ocm.getCampaignUrl(), existing.getCampaignUrl())
									|| !StringUtils.equals(ocm.getCampaignProduct(), existing.getCampaignProduct());

							if (shouldUpdate) {

								existing.setOcmLastAgent(ocm.getLastAgent());
								existing.setOcmLastCoding(ocm.getEndResultDesc());
								existing.setObservaciones(ocm.getObservaciones());
								existing.setEmailContacto(ocm.getEmail());
								existing.setNombreContacto(ocm.getNombreContacto());
								existing.setTelefonoContacto(telefonoContacto);
								existing.setNombreEmpresa(ocm.getNombreEmpresa());
								existing.setActividad(ocm.getActividad());
								existing.setNempleados(ocm.getNempleados());
								existing.setTelefonoEmpresa(ocm.getTelefonoEmpresa());
								existing.setTrabajaEmpresaPrl(ocm.getTrabajaEmpresaPrl());
								existing.setEmpresaActualPrl(ocm.getEmpresaActualPrl());
								existing.setEmpresaActualPrlFechaVto(ocm.getEmpresaActualPrlFechaVto());
								existing.setCp(ocm.getCp());
								existing.setPoblacion(ocm.getPoblacion());
								existing.setProvincia(ocm.getProvincia());
								existing.setDateFirstcall(ocm.getDateFirstcall());
								existing.setDateLastcall(ocm.getDateLastcall());
								existing.setDateNextcall(ocm.getDateNextcall());
								existing.setDateInsert(ocm.getDateInsert());
								existing.setOcmEndResult(ocm.getEndResult());
								existing.setCampaignProvider(ocm.getCampaignProvider());
								if (canUpdateStatus) {
									if (ocm.getStatus() != null && (ocm.getStatus() == 0 || ocm.getStatus() == 10)) {
										existing.setStatus(StatusAcquisitionEnum.CERRADO);
									} else {
										existing.setStatus(StatusAcquisitionEnum.ABIERTO);
									}
								}

								existing.setCampaignLeadId(ocm.getCampaignLeadId());
								existing.setCampaignAdsetName(ocm.getCampaignAdsetName());
								existing.setCampaignAdName(ocm.getCampaignAdName());
								existing.setCampaignName(ocm.getCampaignName());
								existing.setCampaignFormName(ocm.getCampaignFormName());
								existing.setCampaignPlatform(ocm.getCampaignPlatform());
								existing.setCampaignUrl(ocm.getCampaignUrl());
								existing.setCampaignProduct(ocm.getCampaignProduct());
								existing.setIdLoad(ocm.getIdLoad());

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
		nuevo.setTelefonoContacto(
				StringUtils.defaultIfBlank(ocm.getTelefonoContacto(), ocm.getNumber1()));
		nuevo.setNombreContacto(ocm.getNombreContacto());
		if (ocm.getStatus() != null && (ocm.getStatus() == 0 || ocm.getStatus() == 10)) {
			nuevo.setStatus(StatusAcquisitionEnum.CERRADO);
		} else {
			nuevo.setStatus(StatusAcquisitionEnum.CODIFICADO);
		}
		nuevo.setOcmId(ocm.getIdOcm().intValue());
		nuevo.setOcmLastAgent(ocm.getLastAgent());
		nuevo.setOcmLastCoding(ocm.getEndResultDesc());
		nuevo.setOcmMotor(ocm.getCampaignProvider());
		nuevo.setEmailContacto(ocm.getEmail());
		nuevo.setNombreEmpresa(ocm.getNombreEmpresa());
		nuevo.setActividad(ocm.getActividad());
		nuevo.setNempleados(ocm.getNempleados());
		nuevo.setTelefonoEmpresa(ocm.getTelefonoEmpresa());
		nuevo.setTrabajaEmpresaPrl(ocm.getTrabajaEmpresaPrl());
		nuevo.setEmpresaActualPrl(ocm.getEmpresaActualPrl());
		nuevo.setEmpresaActualPrlFechaVto(ocm.getEmpresaActualPrlFechaVto());
		nuevo.setCp(ocm.getCp());
		nuevo.setPoblacion(ocm.getPoblacion());
		nuevo.setProvincia(ocm.getProvincia());
		nuevo.setObservaciones(ocm.getObservaciones());
		nuevo.setCampaignProvider(ocm.getCampaignProvider());
		nuevo.setOcmEndResult(ocm.getEndResult());
		nuevo.setDateFirstcall(ocm.getDateFirstcall());
		nuevo.setDateLastcall(ocm.getDateLastcall());
		nuevo.setDateNextcall(ocm.getDateNextcall());
		nuevo.setCampaignLeadId(ocm.getCampaignLeadId());
		nuevo.setCampaignAdsetName(ocm.getCampaignAdsetName());
		nuevo.setCampaignAdName(ocm.getCampaignAdName());
		nuevo.setCampaignName(ocm.getCampaignName());
		nuevo.setCampaignFormName(ocm.getCampaignFormName());
		nuevo.setCampaignPlatform(ocm.getCampaignPlatform());
		nuevo.setCampaignUrl(ocm.getCampaignUrl());
		nuevo.setCampaignProduct(ocm.getCampaignProduct());
		nuevo.setIdLoad(ocm.getIdLoad());

		if (ocm != null && ocm.getCampaignProvider() != null && !ocm.getCampaignProvider().trim().isEmpty()) {

		    String provider = ocm.getCampaignProvider().trim().toUpperCase();

		    AuthUserVo authUserVo = authService.loadByUsername(provider);

		    if (authUserVo != null) {

		        nuevo.setUuidProvider(authUserVo.getUuid());
		        nuevo.setAgenteUuid(authUserVo.getUuid());
		        nuevo.setCoordinadorUserName(authUserVo.getCordinadorUsername());
		        nuevo.setCoordinadorUuid(authUserVo.getUuidCordinador());

		        acquisitionService.create(nuevo);

		    } else {
		        LOGGER.warn("{} - AuthUserVo is null for CMP_PROVIDER: {}", TAG, provider);
		    }

		} else {
		    LOGGER.warn("{} - campaignProvider is null or empty", TAG);
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
