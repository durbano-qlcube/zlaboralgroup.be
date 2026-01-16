package com.zap.ocm.services;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zap.acquisition.vo.AcquisitionVo;
import com.zap.ocm.entity.OcmAgendaEntity;
import com.zap.ocm.entity.OcmDataEntity;
import com.zap.ocm.entity.OcmDataExtEntity;
import com.zap.ocm.exception.ocm.OcmServiceException;
import com.zap.ocm.vo.LeadsMotorVo;
import com.zap.ocm.vo.MotorLeadsVo;


@Stateless
public class OcmService implements Serializable
{

	private static final long serialVersionUID = -8448538504088495308L;
	private static final Logger LOGGER = LoggerFactory.getLogger(OcmService.class.getName());

	@PersistenceContext(unitName = "ocmdb")
	EntityManager em;
	
	private Integer CARGA=652;
	private String DATA_NAME="skill_formacionleadsmotor_data";
	private String DATA_EXT_NAME="skill_formacionleadsmotor_dataexten";
	

	public AcquisitionVo register (AcquisitionVo lead)
	{
		String TAG ="[DkvMotorLeadsNotService - register]";
		if (lead == null)
			throw new IllegalArgumentException(TAG +" >> 'lead' can not be null");
		
		try{
			Boolean isScheduled = false;
			if (lead.getFxScheduling()!=null)
				isScheduled = true;
			
			
			
			OcmDataEntity entity = toOcmDataEntity(lead);
			entity.setDateInsert(Calendar.getInstance());
			if (isScheduled)
			{
				entity.setDateNextcall(lead.getFxScheduling());
				entity.setStatus(3); //AGENDADO GRUPO
				entity.setPriority(50);
			}
			
			em.persist(entity);
			em.flush();
			lead.setOcmId(entity.getId());
			
			
			OcmDataExtEntity entityExt = toOcmDataExtEntity(lead);
			//entity.setOcmDataExtEntity(entityExt);
			entityExt.setOcmDataEntity(entity);
			em.persist(entityExt);
			em.flush();
			
			if (isScheduled)
			{
				OcmAgendaEntity entityAgenda = toOcmAgendaEntity(lead, entity.getId());
				em.persist(entityAgenda);
				em.flush();
			}
			
			
			return lead;
			
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error:{} ",ex.getMessage());
            throw new OcmServiceException(ex);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public List<MotorLeadsVo>  loadLeads() 
	{
		String TAG ="[CallerService - loadLeads ]";

		try {
			List<MotorLeadsVo> result = new ArrayList<>();
			result.addAll(this.loadLeadsZAP());
			//result.addAll(this.loadLeadsExa());


//			Collections.sort(result, new Comparator<DkvMotorLeadsVo>() {
//				public int compare(DkvMotorLeadsVo o1, DkvMotorLeadsVo o2) {
//					return o1.getDateInsert().compareTo(o2.getDateInsert());
//				}
//			});


			return result;
						
			
		}catch (javax.persistence.NoResultException ex){
			return null;
			
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex);
			throw new OcmServiceException(ex);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public List<MotorLeadsVo>  loadLeadsZAP() 
	{
		String TAG ="[CallerService - loadLeadsZAP ]";

		try {
			
			StringBuilder querySt = new StringBuilder();
//			querySt.append("select dat.id,dat.status,dat.priority,dat.number1,dat.dateinsert,dat.datefirstcall, dat.datelastcall,dat.datenextcall "
//					+ ",dat.attempt,dat.lastagent,dat.endresult,dat.endresultdesc,dat.endresultgroup,dex.url,dex.supplier_sweepstake, "
//					+ "dex.TYPE_REG,dex.primaAnual,dex.productoVendido, dex.Observaciones,loa.idload, loa.loadcode, loa.loaddesc, loa.skilldef, "
//					+ "dex.endresult_dkv, dex.endresultdesc_dkv, dex.pago_leads_dkv, dex.tipo_producto_vendido, dat.endtype, dat.bloq, dat.forceani, "
//					+ "dex.nombre, dex.apellidos,dex.email,dex.external_id "
//					+ " "
//					+ "from ocmdb.skill_dkvmotorleads_data dat inner join ocmdb.skill_dkvmotorleads_dataexten dex on dat.id = dex.id  "
//					+ "inner join ocmdb.ocm_skill_loads loa on dat.idload = loa.idLoad "
//					+ "where dat.dateinsert>='2024-06-01 00:00:00' order by dat.dateinsert");
			
			
			querySt.append("select dat.id,dat.status,dat.priority,dat.number1,dat.dateinsert,dat.datefirstcall, dat.datelastcall,dat.datenextcall "
					+ ",dat.attempt,dat.lastagent,dat.endresult,dat.endresultdesc,dat.endresultgroup,dex.url,dex.supplier_sweepstake, "
					+ "dex.TYPE_REG,dex.primaAnual,dex.productoVendido, dex.Observaciones,loa.idload, loa.loadcode, loa.loaddesc, loa.skilldef, "
					+ "dex.endresult_dkv, dex.endresultdesc_dkv, dex.pago_leads_dkv, dex.tipo_producto_vendido, dat.endtype, dat.bloq, dat.forceani, "
					+ "dex.nombre, dex.apellidos,dex.email,dex.external_id "
					+ " "
					+ "from ocmdb.skill_dkvmotorleads_data dat inner join ocmdb.skill_dkvmotorleads_dataexten dex on dat.id = dex.id  "
					+ "inner join ocmdb.ocm_skill_loads loa on dat.idload = loa.idLoad "
					+ "where dat.dateinsert>='2024-06-01 00:00:00' order by dat.dateinsert");
			
			 Query query = em.createNativeQuery(querySt.toString());
			 

			   List<MotorLeadsVo> results = new ArrayList<MotorLeadsVo>();
				List<Object[]> entityList = query.getResultList();
				for (Object[] fila: entityList)
				{

					MotorLeadsVo target = new MotorLeadsVo();
					
					Integer id = (Integer) fila[0];
					target.setIdOcm(id.longValue());
					
					target.setStatus((Integer) fila[1]);
					target.setPriority((Integer) fila[2]);
					target.setNumber1((String) fila[3]);
					
					
					target.setDateInsert(this.toCalendar((Timestamp)fila[4]));
					target.setDateFirstcall(this.toCalendar((Timestamp)fila[5]));
					target.setDateLastcall(this.toCalendar((Timestamp)fila[6]));
					target.setDateNextcall(this.toCalendar((Timestamp)fila[7]));
					
					target.setAttempt((Integer) fila[8]);
					target.setLastagent((String) fila[9]);
					target.setEndresult((Integer) fila[10]);
					target.setEndresultdesc((String) fila[11]);
					target.setEndresultgroup((String) fila[12]);
					target.setUrl((String) fila[13]);
					target.setSupplierSweepstake((String) fila[14]);


					target.setTypeReg((String) fila[15]);
					target.setPrimaAnual((String) fila[16]);
					target.setProductoVendido((String) fila[17]);
					target.setObservaciones((String) fila[18]);
					target.setIdload((Integer) fila[19]);
					target.setLoadCode((String) fila[20]);
					target.setLoadDesc((String) fila[21]);
					target.setSkillDef((String) fila[22]);
					

					target.setEndresultDkv((Integer) fila[23]);
					target.setEndresultdescDkv((String) fila[24]);
					target.setPagoDkvLeads((Integer) fila[25]);
					target.setTipoProductoVendido((String) fila[26]);
					target.setEndtype((Integer) fila[27]);
					target.setBloq((Integer) fila[28]);
					target.setForceani((String) fila[29]);
					target.setNombre((String) fila[30]);
					target.setApellidos((String) fila[31]);
					target.setEmail((String) fila[32]);
					target.setExternalId((String) fila[33]);
					

					
					results.add(target);
				}
			return results;
						
			
		}catch (javax.persistence.NoResultException ex){
			return null;
			
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex);
			throw new OcmServiceException(ex);
		}
	}
	
	
	
	@SuppressWarnings("unchecked")
	public List<MotorLeadsVo>  loadLeadsZAPByMsisdn(String msisdn) 
	{
		String TAG ="[CallerService - loadLeadsZAP msisdn:"+msisdn+" ]";

		try {
			
			StringBuilder querySt = new StringBuilder();
			querySt.append("select dat.id,dat.status,dat.priority,dat.number1,dat.dateinsert,dat.datefirstcall, dat.datelastcall,dat.datenextcall "
					+ ",dat.attempt,dat.lastagent,dat.endresult,dat.endresultdesc,dat.endresultgroup "
					+ " "
					+ "from ocmdb."+DATA_NAME+" dat inner join ocmdb."+DATA_EXT_NAME+" dex on dat.id = dex.id  "
					+ "inner join ocmdb.ocm_skill_loads loa on dat.idload = loa.idLoad "
					+ "where dat.dateinsert>='2024-06-01 00:00:00' and dat.number1='"+msisdn+"' order by dat.dateinsert");
			
			 Query query = em.createNativeQuery(querySt.toString());
			 

			   List<MotorLeadsVo> results = new ArrayList<MotorLeadsVo>();
				List<Object[]> entityList = query.getResultList();
				for (Object[] fila: entityList)
				{

					MotorLeadsVo target = new MotorLeadsVo();
					
					Integer id = (Integer) fila[0];
					target.setIdOcm(id.longValue());
					
					target.setStatus((Integer) fila[1]);
					target.setPriority((Integer) fila[2]);
					target.setNumber1((String) fila[3]);
					
					
					target.setDateInsert(this.toCalendar((Timestamp)fila[4]));
					target.setDateFirstcall(this.toCalendar((Timestamp)fila[5]));
					target.setDateLastcall(this.toCalendar((Timestamp)fila[6]));
					target.setDateNextcall(this.toCalendar((Timestamp)fila[7]));
					
					target.setAttempt((Integer) fila[8]);
					target.setLastagent((String) fila[9]);
					target.setEndresult((Integer) fila[10]);
					target.setEndresultdesc((String) fila[11]);
					target.setEndresultgroup((String) fila[12]);
//					target.setUrl((String) fila[13]);
//					target.setSupplierSweepstake((String) fila[14]);
//
//
//					target.setTypeReg((String) fila[15]);
//					target.setPrimaAnual((String) fila[16]);
//					target.setProductoVendido((String) fila[17]);
//					target.setObservaciones((String) fila[18]);
//					target.setIdload((Integer) fila[19]);
//					target.setLoadCode((String) fila[20]);
//					target.setLoadDesc((String) fila[21]);
//					target.setSkillDef((String) fila[22]);
//					
//
//					target.setEndresultDkv((Integer) fila[23]);
//					target.setEndresultdescDkv((String) fila[24]);
//					target.setPagoDkvLeads((Integer) fila[25]);
//					target.setTipoProductoVendido((String) fila[26]);
//					target.setEndtype((Integer) fila[27]);
//					target.setBloq((Integer) fila[28]);
//					target.setForceani((String) fila[29]);
//					target.setNombre((String) fila[30]);
//					target.setApellidos((String) fila[31]);
//					target.setEmail((String) fila[32]);
//					target.setExternalId((String) fila[33]);
					

					
					results.add(target);
				}
			return results;
						
			
		}catch (javax.persistence.NoResultException ex){
			return null;
			
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex);
			throw new OcmServiceException(ex);
		}
	}	
	
	
	@SuppressWarnings("unchecked")
    public List<LeadsMotorVo> loadLeadsMotorByMsisdn() {
        String TAG = "[CallerService - loadLeadsMotorByMsisdn]";

        try {
        	List<LeadsMotorVo> results = new ArrayList<>();

        	String queryZapDkvMotorLead = "SELECT dat.id, dat.status, dat.priority, dat.number1, dat.dateinsert, dat.datefirstcall, dat.datelastcall, dat.datenextcall, " +
                    "dat.attempt, dat.lastagent, dat.endresult, dat.endresultdesc, dat.endresultgroup, " +
                    "dex.NOMBRE_CONTACTO, dex.TELEFONO_CONTACTO, dex.EMAIL_CONTACTO, dex.NOMBRE_EMPRESA, dex.ACTIVIDAD, dex.NEMPLEADOS, " +
                    "dex.TELEFONO_EMPRESA, dex.TRABAJA_EMPRESA_PRL, dex.EMPRESA_ACTUAL_PRL, dex.EMPRESA_ACTUAL_PRL_FECHA_VTO, " +
                    "dex.CP, dex.POBLACION, dex.PROVINCIA, dex.OBSERVACIONES, dex.CAMPAING_PROVIDER, dex.CAMPAING_LEAD_ID, " +
                    "dex.CAMPAING_AD_NAME, dex.CAMPAING_ADSET_NAME, dex.CAMPAIGN_NAME, dex.CAMPAIGN_FORM_NAME, " +
                    "dex.CAMPAIGN_PLATFORM, dex.CAMPAIGN_URL, dex.CAMPAIGN_PRODUCT " +
                    "FROM ocmdb.skill_laboral_lead_motor_data dat " +
                    "INNER JOIN ocmdb.skill_laboral_lead_motor_dataexten dex ON dat.id = dex.id " +
                    "WHERE DATE(dat.dateinsert) >'2025-01-01' AND dat.idload in (20250117) " +
                    " AND (dat.datelastcall > DATE_SUB(CURDATE(), INTERVAL 1 MONTH) OR dat.datelastcall is null) " +
                    "ORDER BY dat.dateinsert DESC"; 


        	results.addAll(this.executeLeadsMotorQuery(queryZapDkvMotorLead));

        	return results;

        } catch (Exception ex) {
        LOGGER.error(TAG + " - Error: ", ex);
        throw new OcmServiceException(ex);
    }
}
	
	
	private Calendar toCalendar (Timestamp timestamp)
	{
		if (timestamp!=null)
		{	
			Calendar s = Calendar.getInstance();
			s.setTimeInMillis(timestamp.getTime());
			return s;
		}else {
			return null;
		}

		//SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//		LOGGER.info(TAG + " - s:{} ",FORMAT_DATE.format(s.getTime()));
	}
	
	
	private List<LeadsMotorVo> executeLeadsMotorQuery(String queryStr) {

	    List<LeadsMotorVo> results = new ArrayList<>();
	    Query query = em.createNativeQuery(queryStr);
	    List<Object[]> entityList = query.getResultList();

	    for (Object[] row : entityList) {

	        LeadsMotorVo target = new LeadsMotorVo();

	        int i = 0;

	        target.setIdOcm(row[i] != null ? ((Integer) row[i]).longValue() : null); i++;
	        target.setStatus((Integer) row[i++]);
	        target.setPriority((Integer) row[i++]);
	        target.setNumber1((String) row[i++]);

	        target.setDateInsert(toCalendar((Timestamp) row[i++]));
	        target.setDateFirstcall(toCalendar((Timestamp) row[i++]));
	        target.setDateLastcall(toCalendar((Timestamp) row[i++]));
	        target.setDateNextcall(toCalendar((Timestamp) row[i++]));

	        target.setAttempt((Integer) row[i++]);
	        target.setLastAgent((String) row[i++]);
	        target.setEndResult((String) row[i++]);
	        target.setEndResultDesc((String) row[i++]);
	        target.setEndResultGroup((String) row[i++]);

	        target.setNombreContacto((String) row[i++]);
	        target.setTelefonoContacto((String) row[i++]);
	        target.setEmail((String) row[i++]);
	        target.setNombreEmpresa((String) row[i++]);
	        target.setActividad((String) row[i++]);
	        target.setNempleados((String) row[i++]);
	        target.setTelefonoEmpresa((String) row[i++]);
	        target.setTrabajaEmpresaPrl((String) row[i++]);
	        target.setEmpresaActualPrl((String) row[i++]);
	        target.setEmpresaActualPrlFechaVto((String) row[i++]);
	        target.setCp((String) row[i++]);
	        target.setPoblacion((String) row[i++]);
	        target.setProvincia((String) row[i++]);
	        target.setObservaciones((String) row[i++]);

	        target.setCampaignProvider((String) row[i++]);
            target.setCampaignLeadId((String) row[i++]);
            target.setCampaignAdName((String) row[i++]);
            target.setCampaignAdsetName((String) row[i++]);
            target.setCampaignName((String) row[i++]);
            target.setCampaignFormName((String) row[i++]);
            target.setCampaignPlatform((String) row[i++]);
            target.setCampaignUrl((String) row[i++]);
            target.setCampaignProduct((String) row[i++]);

	        results.add(target);
	    }

	    return results;
	}


	
	private OcmDataEntity toOcmDataEntity (AcquisitionVo source)
	{
		OcmDataEntity target = new OcmDataEntity();

		target.setIdLoad(CARGA);
		target.setActive(1);
		target.setStatus(1);
		target.setPriority(70);
		target.setAttempt(0);
		target.setNumber(1);
		target.setNumber1(source.getTelefonoContacto());
		target.setNumber2("");
		target.setNumber3("");
		target.setNumber4("");
		target.setNumber5("");
		target.setStatus1(0);
		target.setStatus2(0);
		target.setStatus3(0);
		target.setStatus4(0);
		target.setStatus5(0);
		

		target.setBloq(0);
		target.setLastagent("");
		target.setEndtype(0);
		target.setEndresult(0);
		target.setEndresultdesc("");
		target.setEndresultgroup("");
		
		target.setDateInsert(Calendar.getInstance());
		target.setDateNextcall(Calendar.getInstance());

		target.setScheduledagent("");
		return target;
		
		
	}
	
	
	private OcmDataExtEntity toOcmDataExtEntity (AcquisitionVo source)
	{
		OcmDataExtEntity target = new OcmDataExtEntity();

		target.setId(source.getOcmId());

		target.setNombre(source.getNombreContacto());
		target.setApellidos("");
		target.setEmail(source.getEmailContacto());
		target.setObservaciones(source.getObservaciones());
		target.setNombreEmpresa(source.getNombreEmpresa());
		target.setCp(source.getCp());
		target.setProvincia(source.getProvincia());
		target.setCiudad(source.getPoblacion());
		target.setSectorProductivo(source.getActividad());
		target.setNtrabajadores(source.getNempleados());
		
		
		
		
		
		return target;


	}
	
	
	private OcmAgendaEntity toOcmAgendaEntity (AcquisitionVo source, Integer idocm)
	{
		OcmAgendaEntity target = new OcmAgendaEntity();

		target.setStatus(3);
		target.setPriority(70);
		
		target.setDateNextcall(source.getFxScheduling());
		target.setScheduledagent("");
		target.setSkilldata("formacionLeadsMotor");
		target.setScriptsource("formacionLeadsMotor");
		target.setIdreg(idocm);
		target.setSkill("formacionLeadsMotor");
		target.setIdLoad(CARGA);
		target.setActive(1);
		
		
		//AGENDADO PERSONAL 
		//STATUS >> (3, agenda grupo, 9, personal, 8 agenda simple)
		//setScheduledagent
		
		return target;
		
		
	}
	
}
