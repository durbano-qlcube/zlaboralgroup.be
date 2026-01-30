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
import com.zap.acquisition.vo.EstadoEnvioCorreoEnum;
import com.zap.acquisition.vo.StatusAcquisitionEnum;
import com.zap.maintenance.service.notifications.EmailService;
import com.zap.maintenance.vo.notifications.EmailVo;
import com.zap.scheduling.exception.JobActivityNotFoundException;
import com.zap.scheduling.service.JobActivityService;
import com.zap.scheduling.vo.JobActivityVo;
import com.zap.scheduling.vo.JobStatusEnum;

@Stateless
public class SendCodifiedLeadsEmailJob implements Serializable {

	private static final long serialVersionUID = 4277657997411187406L;

	private static final Logger LOGGER = LoggerFactory.getLogger(SendCodifiedLeadsEmailJob.class);
	private static final String JOB_NAME = "SEND_CODIFIED_LEADS_EMAIL";
	private static final String USERNAME = "JOB";
	private static final String TARGET_EMAIL = "emanuel97gus@gmail.com";
	private static final String EMAIL_SUBJECT_PREFIX = "AVANLEAD LEAD --- ";
	private String TAG = "";
	private static Boolean hasToExecuteJob = false;
	private Long idJobActivity = 0L;

	@Inject
	JobActivityService jobActivityService;

	@Inject
	AcquisitionService acquisitionService;

	@Inject
	EmailService emailService;

	@PostConstruct
	public void initializes() {
		hasToExecuteJob = true; // settingsService.hasToExecuteJob();
	}

	@TransactionAttribute(TransactionAttributeType.NEVER)
//	@Schedule(hour = "*", minute = "*/10", second = "0", persistent = false)
	public void doExecute() {
		Long t = System.currentTimeMillis();
		try {
			if (hasToExecuteJob) {
				idJobActivity = this.initJob();
				TAG = "[SendCodifiedLeadsEmailJob >> execute - " + idJobActivity + "]";
				LOGGER.info(TAG + " - Sending CODIFICADO leads email notifications...");
				this.sendCodifiedLeads(TAG);

				this.finishJob(idJobActivity, JobStatusEnum.OK, "OK");
			}
		} catch (Exception ex) {
			LOGGER.error(TAG + " >> - Error: {}", ex);
			this.finishJob(idJobActivity, JobStatusEnum.ERROR, ex.getMessage());

		} finally {
			LOGGER.info(TAG + "finish Job >> *** FINISH >> Finish Timing:{}", (System.currentTimeMillis() - t));
		}

	}

	private void sendCodifiedLeads(String tag) {
		try {
			String jobTag = tag + " >> sendCodifiedLeads >>";
			List<AcquisitionVo> leads = loadCodifiedPendingLeads();

			LOGGER.info(jobTag + " loaded leads {} with status CODIFICADO...", leads.size());

			if (leads != null && !leads.isEmpty()) {
				for (AcquisitionVo lead : leads) {
					try {
						ensurePendingEmailStatus(lead);

						EmailVo emailVo = new EmailVo();
						emailVo.setTo(TARGET_EMAIL);
						emailVo.setSubject(EMAIL_SUBJECT_PREFIX + "Lead CODIFICADO: " + safeValue(lead.getNombreEmpresa()));
						emailVo.setSubject("Lead CODIFICADO: " + safeValue(lead.getNombreEmpresa()));
						emailVo.setBody(buildLeadEmailBody(lead));

						emailService.sendEmailHtml(emailVo);
						markEmailSent(lead);
						LOGGER.info("{} - Email sent for lead id:{}", jobTag, lead.getIdAcquisition());
					} catch (Exception e) {
						LOGGER.error("{} - Error sending email for lead id:{} - {}", jobTag,
								lead.getIdAcquisition(), e.getMessage(), e);
					}
				}
			}

		} catch (Exception e) {
			LOGGER.error("{} - Error en sendCodifiedLeads(): {}", tag, e.getMessage(), e);
		}
	}

	private List<AcquisitionVo> loadCodifiedPendingLeads() {
		return acquisitionService.loadByStatusAndEmailStatus(StatusAcquisitionEnum.CODIFICADO,
				EstadoEnvioCorreoEnum.PENDIENTE);
	}

	private void ensurePendingEmailStatus(AcquisitionVo lead) {
		if (lead == null || EstadoEnvioCorreoEnum.ENVIADO.equals(lead.getEstadoEnvioCorreo())) {
			return;
		}
		lead.setEstadoEnvioCorreo(EstadoEnvioCorreoEnum.PENDIENTE);
		lead.setFechaEnvioCorreo(null);
		updateLeadEmailStatus(lead);
	}

	private void markEmailSent(AcquisitionVo lead) {
		if (lead == null) {
			return;
		}
		lead.setEstadoEnvioCorreo(EstadoEnvioCorreoEnum.ENVIADO);
		lead.setFechaEnvioCorreo(Calendar.getInstance());
		updateLeadEmailStatus(lead);
	}

	private void updateLeadEmailStatus(AcquisitionVo lead) {
		try {
			acquisitionService.update(lead, false);
		} catch (Exception e) {
			LOGGER.error("{} - Error updating email status for lead id:{} - {}", TAG, lead.getIdAcquisition(),
					e.getMessage(), e);
		}
	}

	private String buildLeadEmailBody(AcquisitionVo lead) {
		StringBuilder body = new StringBuilder();
		body.append("<html><body>");
		body.append("<h2>Lead CODIFICADO</h2>");
		body.append("<table border=\"1\" cellpadding=\"6\" cellspacing=\"0\">");
		appendRow(body, "Nombre contacto", safeValue(lead.getNombreContacto()));
		appendRow(body, "Teléfono contacto", safeValue(lead.getTelefonoContacto()));
		appendRow(body, "Email contacto", safeValue(lead.getEmailContacto()));
		appendRow(body, "Empresa", safeValue(lead.getNombreEmpresa()));
		appendRow(body, "Actividad", safeValue(lead.getActividad()));
		appendRow(body, "Nº empleados", safeValue(lead.getNempleados()));
		appendRow(body, "Teléfono empresa", safeValue(lead.getTelefonoEmpresa()));
		appendRow(body, "Provincia", safeValue(lead.getProvincia()));
		appendRow(body, "Población", safeValue(lead.getPoblacion()));
		appendRow(body, "CP", safeValue(lead.getCp()));
		body.append("</table>");
		body.append("</body></html>");
		return body.toString();
	}

	private void appendRow(StringBuilder body, String label, String value) {
		body.append("<tr><td><strong>").append(label).append("</strong></td><td>")
				.append(value).append("</td></tr>");
	}

	private String safeValue(Object value) {
		return value == null ? "-" : String.valueOf(value);
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
			LOGGER.error("[SendCodifiedLeadsEmailJob - finishJob] >> - Error: ", ex.getMessage());
		}

	}
}
