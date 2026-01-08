package com.zap.maintenance.service.settings;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zap.maintenance.entity.settings.SettingsEntity;
import com.zap.maintenance.exception.settings.SettingsNotFoundException;
import com.zap.maintenance.exception.settings.SettingsServiceException;
import com.zap.maintenance.vo.crypto.TripleDesSettingsVo;
import com.zap.maintenance.vo.settings.EmailSettingsVo;
import com.zap.maintenance.vo.settings.MongoConfigVo;
import com.zap.maintenance.vo.settings.SettingsVo;

@Singleton
public class SettingsService implements Serializable {

	private static final long serialVersionUID = 7444508708395721919L;
	private static final Logger LOGGER = LoggerFactory.getLogger(SettingsService.class.getName());
	SimpleDateFormat FORMAT_DATE_SHORT = new SimpleDateFormat("yyyy-MM-dd");
	//private static SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("dd-MM-yyyy");

	@PersistenceContext(unitName = "zapLaboralgrouPool")
	EntityManager em;



	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public SettingsVo createSettings(SettingsVo settingsVo) throws SettingsServiceException
	{
		LOGGER.debug("[SettingsService - createSettings] - Init");
		long T1=System.currentTimeMillis();

		if (settingsVo == null)
			throw new IllegalArgumentException("createSettings(SettingsVo SettingsVo) - 'SettingsVo' can not be null");

		try {

			SettingsEntity settingsEntity = toSettingsEntity(settingsVo);
			em.persist(settingsEntity);

			return this.toSettingsVo(settingsEntity);

		}catch (Exception ex){
			LOGGER.error("[SettingsService - createSettings] - Error: ",ex);
			throw new SettingsServiceException("[SettingsService - createSettings] - Error: ", ex);

		}finally{
			LOGGER.debug("[SettingsService - createSettings] - Finish Timing:"+(System.currentTimeMillis()-T1));
		}
	}


	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void updateSettings(SettingsVo settingsVo, Boolean copyIfNull) throws SettingsServiceException, SettingsNotFoundException  
	{
		LOGGER.debug("[SettingsService - updateSettings] - Init");
		long T1=System.currentTimeMillis();

		if (settingsVo == null)
			throw new IllegalArgumentException("updateSettings(SettingsVo SettingsVo, Boolean copyIfNull) - 'SettingsVo' can not be null");

		if (settingsVo.getIdSettings() == null)
			throw new IllegalArgumentException("updateSettings(SettingsVo SettingsVo, Boolean copyIfNull) - 'settingsVo.getIdSettings()' can not be null");

		if (copyIfNull == null)
			throw new IllegalArgumentException("updateSettings(SettingsVo SettingsVo, Boolean copyIfNull) - 'copyIfNull' can not be null");

		try {
			SettingsEntity settingsEntity = em.find(SettingsEntity.class,settingsVo.getIdSettings());
			if (settingsEntity==null)
				throw new SettingsNotFoundException();

			this.toSettingsEntity(settingsVo, settingsEntity, copyIfNull);
			em.merge(settingsEntity);

		}catch (SettingsNotFoundException ex){
			LOGGER.error("[SettingsService - updateSettings] - Error: ",ex);
			throw ex;

		}catch (Exception ex){
			LOGGER.error("[SettingsService - updateSettings] - Error: ",ex);
			throw new SettingsServiceException("[SettingsService - updateSettings] - Error: ", ex);

		}finally{
			LOGGER.debug("[SettingsService - updateSettings] - Finish Timing:"+(System.currentTimeMillis()-T1));
		}
	}



	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void deleteSettings(Long idSettings) throws SettingsNotFoundException  
	{
		LOGGER.debug("[SettingsService - deleteSettings] - Init");
		long T1=System.currentTimeMillis();

		if (idSettings == null)
			throw new IllegalArgumentException("deleteSettings(Long idSettings) - 'idSettings' can not be null");

		LOGGER.debug("[SettingsService - deleteSettings] - input idSettings:"+idSettings);

		try {

			SettingsEntity settingsEntity = em.find(SettingsEntity.class,idSettings);
			if (settingsEntity==null)
				throw new SettingsNotFoundException();

			em.remove(settingsEntity);

		}catch (javax.persistence.NoResultException ex){
			throw new SettingsServiceException(ex);

		}catch (SettingsNotFoundException ex){
			LOGGER.error("[SettingsService - deleteSettings] - Error: ",ex);
			throw ex;

		}catch (Exception ex){
			throw new SettingsServiceException(ex);

		}finally{
			LOGGER.debug("[SettingsService - deleteSettings] >> Finish Timing:"+(System.currentTimeMillis()-T1));
		}
	}


	public SettingsVo loadSettings(Long idSettings) throws SettingsServiceException, SettingsNotFoundException  
	{
		LOGGER.debug("[SettingsService - loadSettings] - Init");
		long T1=System.currentTimeMillis();

		if (idSettings == null)
			throw new IllegalArgumentException("loadSettings(Long idSettings) - 'idSettings' can not be null");

		LOGGER.debug("[SettingsService - loadSettings] - input idSettings:"+idSettings);

		try {
			SettingsEntity settingsEntity = em.find(SettingsEntity.class,idSettings);
			if (settingsEntity==null)
				throw new SettingsNotFoundException();

			return this.toSettingsVo(settingsEntity);

		}catch (SettingsNotFoundException ex){
			LOGGER.error("[SettingsService - loadSettings] - Error: ",ex);
			throw ex;

		}catch (Exception ex){
			LOGGER.error("[SettingsService - loadSettings] - Error: ",ex);
			throw new SettingsServiceException(ex);

		}finally{
			LOGGER.debug("[SettingsService - loadSettings] - Finish Timing:"+(System.currentTimeMillis()-T1));
		}
	}

	@SuppressWarnings("unchecked")
	public List<SettingsVo> loadSettingsByCategory(String category)  
	{
		List<SettingsVo> list = new ArrayList<SettingsVo>();
		if (category == null)
			throw new IllegalArgumentException("loadSettingsByCategory(String category, String subcategory ) - 'category' can not be null");

		LOGGER.debug("[SettingsService - loadSettingsByCategory] - category:"+category);

		try {
			Query query = em.createNamedQuery("SettingsEntity.loadSettingsByCategory1");
			query.setParameter("category", category);
			List<SettingsEntity> entities = (List<SettingsEntity>) query.getResultList();
			
			for (SettingsEntity res : entities)
				list.add(this.toSettingsVo(res));

			return list;

		}catch (javax.persistence.NoResultException ex){
			LOGGER.error("[SettingsService - loadSettingsByCategory] - Error: ",ex);
			return list;

		}catch (Exception ex){
			LOGGER.error("[SettingsService - loadSettingsByCategory] - Error: ",ex);
			throw new SettingsServiceException(ex);

		}finally{
		}
	}


	@SuppressWarnings("unchecked")
	public List<SettingsVo> loadSettingsByCategory(String category, Integer year)  
	{
		List<SettingsVo> list = new ArrayList<SettingsVo>();
		if (category == null)
			throw new IllegalArgumentException("loadSettingsByCategory(String category, String subcategory ) - 'category' can not be null");

		LOGGER.debug("[SettingsService - loadSettingsByCategory] - category:"+category);

		try {
			Query query = em.createNamedQuery("SettingsEntity.loadSettingsByCategory4");
			query.setParameter("category", category);
			query.setParameter("year", year);
			List<SettingsEntity> entities = (List<SettingsEntity>) query.getResultList();
			
			for (SettingsEntity res : entities)
				list.add(this.toSettingsVo(res));

			return list;

		}catch (javax.persistence.NoResultException ex){
			LOGGER.error("[SettingsService - loadSettingsByCategory] - Error: ",ex);
			return list;

		}catch (Exception ex){
			LOGGER.error("[SettingsService - loadSettingsByCategory] - Error: ",ex);
			throw new SettingsServiceException(ex);

		}finally{
		}
	}


	@SuppressWarnings("unchecked")
	public List<SettingsVo> loadSettingsByCategory(String category, String subCategory)  
	{
		List<SettingsVo> list = new ArrayList<SettingsVo>();
		
		if (category == null)
			throw new IllegalArgumentException("loadSettingsByCategory(String category, String subcategory ) - 'category' can not be null");

		if (subCategory == null)
			throw new IllegalArgumentException("loadSettingsByCategory(String category, String subcategory ) - 'subCategory' can not be null");

		try {

			Query query = em.createNamedQuery("SettingsEntity.loadSettingsByCategory2");
			query.setParameter("category", category);
			query.setParameter("subCategory", subCategory);

			List<SettingsEntity> entities = (List<SettingsEntity>) query.getResultList();
			
			for (SettingsEntity res : entities)
				list.add(this.toSettingsVo(res));

			return list;

		}catch (javax.persistence.NoResultException ex){
			LOGGER.error("[SettingsService - loadSettingsByCategory] - Error: ",ex);
			return list;

		}catch (Exception ex){
			LOGGER.error("[SettingsService - loadSettingsByCategory] - Error: ",ex);
			throw new SettingsServiceException(ex);

		}
	}
	
	
	@SuppressWarnings("unchecked")
	public List<SettingsVo> loadSettingsByCategory(String category, String subCategory, Integer year)  
	{
		List<SettingsVo> list = new ArrayList<SettingsVo>();
		
		if (category == null)
			throw new IllegalArgumentException("loadSettingsByCategory(String category, String subcategory ) - 'category' can not be null");

		if (subCategory == null)
			throw new IllegalArgumentException("loadSettingsByCategory(String category, String subcategory ) - 'subCategory' can not be null");

		if (year == null)
			throw new IllegalArgumentException("loadSettingsByCategory(String category, String subcategory ) - 'year' can not be null");

		try {

			Query query = em.createNamedQuery("SettingsEntity.loadSettingsByCategory3");
			query.setParameter("category", category);
			query.setParameter("subCategory", subCategory);
			query.setParameter("year", year);
			List<SettingsEntity> entities = (List<SettingsEntity>) query.getResultList();
			
			for (SettingsEntity res : entities)
				list.add(this.toSettingsVo(res));

			return list;

		}catch (javax.persistence.NoResultException ex){
			LOGGER.error("[SettingsService - loadSettingsByCategory] - Error: ",ex);
			return list;

		}catch (Exception ex){
			LOGGER.error("[SettingsService - loadSettingsByCategory] - Error: ",ex);
			throw new SettingsServiceException(ex);

		}
	}

	public SettingsVo loadSettingsByCode(String category, String subCategory, String code)  
	{

		if (category == null)
			throw new IllegalArgumentException("loadSettingsByCode (String category, String subcategory, String code ) - 'category' can not be null");

		if (subCategory == null)
			throw new IllegalArgumentException("loadSettingsByCode (String category, String subcategory, String code ) - 'subCategory' can not be null");

		if (code == null)
			throw new IllegalArgumentException("loadSettingsByCode (String category, String subcategory, String code ) - 'code' can not be null");


		try {
			Query query = em.createNamedQuery("SettingsEntity.loadSettingsByCode");
			query.setParameter("category", category);
			query.setParameter("subCategory", subCategory);
			query.setParameter("code", code);

			SettingsEntity entity = (SettingsEntity) query.getSingleResult();

			if(entity == null)
				throw new SettingsNotFoundException();

			return this.toSettingsVo(entity);

		}catch (SettingsNotFoundException ex){
			LOGGER.error("[SettingsService - loadSettings] - Error: ",ex);
			throw new SettingsServiceException(ex);

		}catch (Exception ex){
			LOGGER.error("[SettingsService - loadSettingsByCode] - Error: ",ex);
			throw new SettingsServiceException(ex);
		}
	}
	
	public SettingsVo loadSettingsByDes(String category, String subCategory, String description)  
	{

		if (category == null)
			throw new IllegalArgumentException("loadSettingsByCode (String category, String subcategory, String description ) - 'category' can not be null");

		if (subCategory == null)
			throw new IllegalArgumentException("loadSettingsByCode (String category, String subcategory, String description ) - 'subCategory' can not be null");

		if (description == null)
			throw new IllegalArgumentException("loadSettingsByCode (String category, String subcategory, String description ) - 'code' can not be null");


		try {
			Query query = em.createNamedQuery("SettingsEntity.loadSettingsByDes");
			query.setParameter("category", category);
			query.setParameter("subCategory", subCategory);
			query.setParameter("description", description);

			SettingsEntity entity = (SettingsEntity) query.getSingleResult();

			if(entity == null)
				throw new SettingsNotFoundException();

			return this.toSettingsVo(entity);

		}catch (SettingsNotFoundException ex){
			LOGGER.error("[SettingsService - loadSettings] - Error: ",ex);
			throw new SettingsServiceException(ex);

		}catch (Exception ex){
			LOGGER.error("[SettingsService - loadSettingsByCode] - Error: ",ex);
			throw new SettingsServiceException(ex);
		}
	}
	
	
	public List<SettingsVo> loadSettingsByCodeMul(String category, String subCategory, String code)  
	{
		List<SettingsVo> list = new ArrayList<SettingsVo>();
		if (category == null)
			throw new IllegalArgumentException("loadSettingsByCode (String category, String subcategory, String code ) - 'category' can not be null");

		if (subCategory == null)
			throw new IllegalArgumentException("loadSettingsByCode (String category, String subcategory, String code ) - 'subCategory' can not be null");

		if (code == null)
			throw new IllegalArgumentException("loadSettingsByCode (String category, String subcategory, String code ) - 'code' can not be null");


		try {
			Query query = em.createNamedQuery("SettingsEntity.loadSettingsByCode");
			query.setParameter("category", category);
			query.setParameter("subCategory", subCategory);
			query.setParameter("code", code);

			List<SettingsEntity> entities = (List<SettingsEntity>) query.getResultList();
			
			for (SettingsEntity res : entities)
				list.add(this.toSettingsVo(res));

			return list;

		}catch (javax.persistence.NoResultException ex){
			LOGGER.error("[SettingsService - loadSettingsByCategory] - Error: ",ex);
			return list;

		}catch (Exception ex){
			LOGGER.error("[SettingsService - loadSettingsByCode] - Error: ",ex);
			throw new SettingsServiceException(ex);
		}
	}

	public SettingsVo loadSettingsRequired(String category, String subCategory, String code) throws SettingsNotFoundException  
	{

		if (category == null)
			throw new IllegalArgumentException("loadSettingsRequired (String category, String subcategory, String code ) - 'category' can not be null");

		if (subCategory == null)
			throw new IllegalArgumentException("loadSettingsRequired (String category, String subcategory, String code ) - 'subCategory' can not be null");

		if (code == null)
			throw new IllegalArgumentException("loadSettingsRequired (String category, String subcategory, String code ) - 'code' can not be null");

		//LOGGER.debug("[SettingsService - loadSettingsByCode] - category:"+category+" subcategory:"+subCategory+" code:"+code);

		try {
			Query query = em.createNamedQuery("SettingsEntity.loadSettingsByCode");
			query.setParameter("category", category);
			query.setParameter("subCategory", subCategory);
			query.setParameter("code", code);

			SettingsEntity entity = (SettingsEntity) query.getSingleResult();

			if(entity == null)
				throw new SettingsNotFoundException();

			return this.toSettingsVo(entity);

		}catch (SettingsNotFoundException ex){
			LOGGER.error("[SettingsService - loadSettingsRequired] - Error: ",ex);
			throw ex;

		}catch (Exception ex){
			LOGGER.error("[SettingsService - loadSettingsRequired] - Error: ",ex);
			throw new SettingsServiceException(ex);
		}
	}
	
	
	 



	//******************************************************************
	//******************CONFIGURATION EMAIL
	//******************************************************************

	public EmailSettingsVo getEmailSettings() 
	{
		EmailSettingsVo emailSettingsVo=new EmailSettingsVo();
		List<SettingsVo> result = this.loadSettingsByCategory("EMAIL", "CONFIG");
		if (result!=null && !result.isEmpty())
		{
			java.util.Iterator<SettingsVo> ite=result.iterator();

			while(ite.hasNext())
			{
				SettingsVo settingsVo=(SettingsVo) ite.next();
				if("HOST".equalsIgnoreCase(settingsVo.getCode()))
					emailSettingsVo.setHost(settingsVo.getValue());

				else if("PORT".equalsIgnoreCase(settingsVo.getCode()))
					emailSettingsVo.setPort(settingsVo.getValue());

				else if("START_TTLS".equalsIgnoreCase(settingsVo.getCode()))
					emailSettingsVo.setStarttls(settingsVo.getValue());

				else if("AUTH_IS_NEED".equalsIgnoreCase(settingsVo.getCode()))
					emailSettingsVo.setAuthIsNeeds(settingsVo.getValue());

				else if("DEBUG".equalsIgnoreCase(settingsVo.getCode()))
					emailSettingsVo.setDebug(Boolean.valueOf(settingsVo.getValue()));

				else if("TRANSPORT".equalsIgnoreCase(settingsVo.getCode()))
					emailSettingsVo.setTransport(settingsVo.getValue());

				else if("USER".equalsIgnoreCase(settingsVo.getCode()))
					emailSettingsVo.setUser(settingsVo.getValue());

				else if("PASSWORD".equalsIgnoreCase(settingsVo.getCode()))
					emailSettingsVo.setPass(settingsVo.getValue());

				else if("FROM".equalsIgnoreCase(settingsVo.getCode()))
					emailSettingsVo.setFrom(settingsVo.getValue());

			}
		}
		return emailSettingsVo;
	}
	
	public Map<Long,EmailSettingsVo> getEmailUsers() 
	{
		Map<Long,EmailSettingsVo> map = new HashMap<Long,EmailSettingsVo>();
		List<SettingsVo> result = this.loadSettingsByCategory("EMAIL", "USER");
		if (result!=null && !result.isEmpty())
		{
			java.util.Iterator<SettingsVo> ite=result.iterator();
			while(ite.hasNext())
			{
				SettingsVo settingsVo=(SettingsVo) ite.next();

				EmailSettingsVo emailSettingsVo=new EmailSettingsVo();
				emailSettingsVo.setUser(settingsVo.getValue());
				emailSettingsVo.setPass(settingsVo.getDescription());
				
				if ("CONFIG_1".equals(settingsVo.getCode())){	
					map.put(Long.valueOf(1), emailSettingsVo);

				}else if ("CONFIG_2".equals(settingsVo.getCode())){
					map.put(Long.valueOf(2), emailSettingsVo);
				
				}else if ("CONFIG_3".equals(settingsVo.getCode())){
					map.put(Long.valueOf(3), emailSettingsVo);
					
				}else if ("CONFIG_4".equals(settingsVo.getCode())){
					map.put(Long.valueOf(4), emailSettingsVo);
					
				}else if ("CONFIG_5".equals(settingsVo.getCode())){
					map.put(Long.valueOf(5), emailSettingsVo);
					
				}else if ("CONFIG_6".equals(settingsVo.getCode())){
					map.put(Long.valueOf(6), emailSettingsVo);
					
				}else if ("CONFIG_7".equals(settingsVo.getCode())){
					map.put(Long.valueOf(7), emailSettingsVo);
					
				}else if ("CONFIG_8".equals(settingsVo.getCode())){
					map.put(Long.valueOf(8), emailSettingsVo);
					
				}else if ("CONFIG_9".equals(settingsVo.getCode())){
					map.put(Long.valueOf(9), emailSettingsVo);
					
				}else if ("CONFIG_10".equals(settingsVo.getCode())){
					map.put(Long.valueOf(10), emailSettingsVo);
					
				}else if ("CONFIG_11".equals(settingsVo.getCode())){
					map.put(Long.valueOf(11), emailSettingsVo);
					
				}else if ("CONFIG_12".equals(settingsVo.getCode())){
					map.put(Long.valueOf(12), emailSettingsVo);
					
				}else if ("CONFIG_13".equals(settingsVo.getCode())){
					map.put(Long.valueOf(13), emailSettingsVo);
					
				}else if ("CONFIG_14".equals(settingsVo.getCode())){
					map.put(Long.valueOf(14), emailSettingsVo);
					
				}else if ("CONFIG_15".equals(settingsVo.getCode())){
					map.put(Long.valueOf(15), emailSettingsVo);
					
				}else if ("CONFIG_16".equals(settingsVo.getCode())){
					map.put(Long.valueOf(16), emailSettingsVo);
					
				}else if ("CONFIG_17".equals(settingsVo.getCode())){
					map.put(Long.valueOf(17), emailSettingsVo);
					
				}else if ("CONFIG_18".equals(settingsVo.getCode())){
					map.put(Long.valueOf(18), emailSettingsVo);
				
				}else if ("CONFIG_19".equals(settingsVo.getCode())){
					map.put(Long.valueOf(19), emailSettingsVo);
				
				}else if ("CONFIG_20".equals(settingsVo.getCode())){
					map.put(Long.valueOf(20), emailSettingsVo);
				
				}else if ("CONFIG_21".equals(settingsVo.getCode())){
					map.put(Long.valueOf(21), emailSettingsVo);
					
				}else if ("CONFIG_22".equals(settingsVo.getCode())){
					map.put(Long.valueOf(22), emailSettingsVo);
				}
			}
		}
		return map;
	}

	
	
	public String getGeneratedEmailPath() throws SettingsServiceException, SettingsNotFoundException
	{

		SettingsVo settingsVo= this.loadSettingsByCode("EMAIL", "CONFIG", "PATH");
		return settingsVo.getValue();
	}


	public String getEmailFrom() throws SettingsServiceException, SettingsNotFoundException
	{

		SettingsVo settingsVo= this.loadSettingsByCode("EMAIL", "CONFIG", "FROM");
		return settingsVo.getValue();
	}

	public MongoConfigVo getMongoConfig() {
        MongoConfigVo config = new MongoConfigVo();
        try {

            config.setIp("34.255.206.106"); 
            config.setPort(28018);    
            config.setUser("zsincro"); 
            config.setPass("M4^V%8H<uqeRZ"); 
            config.setMongoDb("zsincro"); 
            
            LOGGER.info("[SettingsService - getMongoConfig] - Configuracion de Mongo obtenida correctamente.");
        } catch (Exception e) {
            LOGGER.error("[SettingsService - getMongoConfig] - Error al obtener configuración de Mongo.", e);
        }
        return config;
    }

	//******************************************************************
	//****************** SEGURIDAD *************************************
	//******************************************************************

	public TripleDesSettingsVo getTripleDesSettings() 
	{

		TripleDesSettingsVo tripleDesSettingsVo=new TripleDesSettingsVo();
		List<SettingsVo> result = this.loadSettingsByCategory("CRYPTO", "DES");
		if (result!=null && !result.isEmpty())
		{
			java.util.Iterator<SettingsVo> ite=result.iterator();

			while(ite.hasNext())
			{
				SettingsVo settingsVo=(SettingsVo) ite.next();
				if("KEY1".equalsIgnoreCase(settingsVo.getCode()))
					tripleDesSettingsVo.setKey1(settingsVo.getValue());

				else if("KEY2".equalsIgnoreCase(settingsVo.getCode()))
					tripleDesSettingsVo.setKey2(settingsVo.getValue());

				else if("KEY3".equalsIgnoreCase(settingsVo.getCode()))
					tripleDesSettingsVo.setKey3(settingsVo.getValue());

			}
		}
		return tripleDesSettingsVo;
	}


	
	public String getRootSharePath()
	{   		
		return "/opt/share/sidex/";
	}
	
	
	


	//******************************************************************
	//****************** AWS API CONFIG ********************************
	//******************************************************************    

	

	//******************************************************************
	//****************** ROLES ********************************
	//******************************************************************  
	
	
	

	//******************************************************************
	//****************** GLOBAL CONFIG ********************************
	//******************************************************************    

	public String getTokenizer() 
	{
		//SettingsVo settingsVo= this.loadSettingsByCode("GLOBAL", "CONFIG", "TOKENIZER");
		return "#";
	}


	public String getAppPath() 
	{
		SettingsVo settingsVo= this.loadSettingsByCode("GLOBAL", "CONFIG", "SHARE");
		return settingsVo.getValue();
	}

	
	public String loadPathTemplateContrato() 
	{
		SettingsVo settingsVo= this.loadSettingsByCode("GLOBAL", "CONFIG", "TEMPLATE_CONTRATO");
		return settingsVo.getValue();
	}

	public String loadUrl() 
	{
		SettingsVo settingsVo= this.loadSettingsByCode("GLOBAL", "CONFIG", "URL");
		return settingsVo.getValue();
	}

	
	public String loadUrlGestdirect() 
	{
		SettingsVo settingsVo= this.loadSettingsByCode("GLOBAL", "CONFIG", "URL_GESTDIRECT");
		return settingsVo.getValue();
	}
	
	public String loadUrlSaveDoc() 
	{
		SettingsVo settingsVo= this.loadSettingsByCode("GLOBAL", "CONFIG", "URLSAVEDOC");
		return settingsVo.getValue();
	}

	
	

	
	//******************************************************************
	//****************** MOBILE NOTIFICATIONs CONFIG *******************
	//******************************************************************    

//	public String getMobileIosCertificatePath() 
//	{
//		SettingsVo settingsVo= this.loadSettingsByCode("MOBILE", "IOS", "CERTIFICATE_PATH");
//		return settingsVo.getValue();
//	}


//	public String getMobileIosCertificatePass() 
//	{
//		SettingsVo settingsVo= this.loadSettingsByCode("MOBILE", "IOS", "CERTIFICATE_PASS");
//		return settingsVo.getValue();
//	}
//
//
//	public Boolean getIsMobileIosNotificationsProd() 
//	{
//		SettingsVo settingsVo= this.loadSettingsByCode("MOBILE", "IOS", "NOTIFICATIONS_PROD");
//		return new Boolean(settingsVo.getValue());
//	}


//	public String getMobileAndoridApiKey() 
//	{
//		SettingsVo settingsVo= this.loadSettingsByCode("MOBILE", "ANDROID", "API_KEY");
//		return settingsVo.getValue();
//	}

	

	
	//******************************************************************
	//****************** INCIDENCE ********************************
	//******************************************************************   

	
	
	
	
	//******************************************************************
	//****************** MOBILE  CONFIG ********************************
	//******************************************************************   

	
	
	
	


	//******************************************************************
	//****************** IMG CONFIG GENERATE****************************
	//******************************************************************   

//	public QrSettingsVo getQrSettings()
//	{
//		QrSettingsVo qrSettingsVo=new QrSettingsVo();
//		List<SettingsVo> result = this.loadSettingsByCategory("IMAGE", "QR");
//		if (result!=null && !result.isEmpty())
//		{
//			java.util.Iterator<SettingsVo> ite=result.iterator();
//
//			while(ite.hasNext())
//			{
//				SettingsVo settingsVo=(SettingsVo) ite.next();
//				if("RELATIVE_PATH".equalsIgnoreCase(settingsVo.getCode()))
//					qrSettingsVo.setRelativePath(settingsVo.getValue());
//
//				else if("FULL_PATH".equalsIgnoreCase(settingsVo.getCode()))
//					qrSettingsVo.setFullPath(settingsVo.getValue());
//
//				else if("WIDTH".equalsIgnoreCase(settingsVo.getCode()))
//					qrSettingsVo.setWidth(settingsVo.getValue());
//
//				else if("HEIGHT".equalsIgnoreCase(settingsVo.getCode()))
//					qrSettingsVo.setHeight(settingsVo.getValue());
//
//				else if("EXTENSION".equalsIgnoreCase(settingsVo.getCode()))
//					qrSettingsVo.setExtension(settingsVo.getValue());
//
//				else if("URL".equalsIgnoreCase(settingsVo.getCode()))
//					qrSettingsVo.setUrl(settingsVo.getValue());
//
//			}
//		}
//		return qrSettingsVo;
//	}
//
//	public BarcodeSettingsVo getBarcodeSettings() 
//	{
//
//		BarcodeSettingsVo barcodeSettingsVo=new BarcodeSettingsVo();
//		List<SettingsVo> result = this.loadSettingsByCategory("IMAGE", "BARCODE");
//		if (result!=null && !result.isEmpty())
//		{
//			java.util.Iterator<SettingsVo> ite=result.iterator();
//
//			while(ite.hasNext())
//			{
//				SettingsVo settingsVo=(SettingsVo) ite.next();
//				if("RELATIVE_PATH".equalsIgnoreCase(settingsVo.getCode()))
//					barcodeSettingsVo.setRelativePath(settingsVo.getValue());
//
//				else if("FULL_PATH".equalsIgnoreCase(settingsVo.getCode()))
//					barcodeSettingsVo.setFullPath(settingsVo.getValue());
//
//				else if("WIDTH".equalsIgnoreCase(settingsVo.getCode()))
//					barcodeSettingsVo.setWidth(settingsVo.getValue());
//
//				else if("HEIGHT".equalsIgnoreCase(settingsVo.getCode()))
//					barcodeSettingsVo.setHeight(settingsVo.getValue());
//
//				else if("EXTENSION".equalsIgnoreCase(settingsVo.getCode()))
//					barcodeSettingsVo.setExtension(settingsVo.getValue());
//
//				else if("URL".equalsIgnoreCase(settingsVo.getCode()))
//					barcodeSettingsVo.setUrl(settingsVo.getValue());
//
//			}
//		}
//		return barcodeSettingsVo;
//	}


	//******************************************************************
	//****************** DISTRIBUTION LIST *****************************
	//******************************************************************   

	


	//******************************************************************
	//****************** JOB CONFIG ************************************
	//******************************************************************  

	
	
	


	//******************************************************************
	//****************** CLM CONCEPT_TRANSACTION ***********************
	//******************************************************************  

	

	//******************************************************************
	//****************** CLM MESSAGE ***********************************
	//******************************************************************

	public String getJWTKey() 
	{
		return "243_&77AAsAttendis_678TTyy_x#eeqw";
	}
	
	
//	public List<ComboBoxVo> getCouponCatalogue() 
//	{
//		List<ComboBoxVo> comboBoxVoList = new ArrayList<ComboBoxVo>();
//		List<SettingsVo> result = this.loadSettingsByCategory("COUPON", "CODE");
//		if (result!=null && !result.isEmpty())
//		{
//			java.util.Iterator<SettingsVo> ite=result.iterator();
//			while(ite.hasNext())
//			{
//				SettingsVo settingsVo=(SettingsVo) ite.next();
//				ComboBoxVo ComboBoxVo = new ComboBoxVo();
//				ComboBoxVo.setCode(settingsVo.getValue());
//				ComboBoxVo.setDescription(settingsVo.getCode());
//				comboBoxVoList.add(ComboBoxVo);    			
//			}
//		}
//		return comboBoxVoList;
//	}
//	
//	
//	public List<ComboBoxVo> getPromosCatalogue() 
//	{
//		List<ComboBoxVo> comboBoxVoList = new ArrayList<ComboBoxVo>();
//		List<SettingsVo> result = this.loadSettingsByCategory("COUPON", "CODE");
//		if (result!=null && !result.isEmpty())
//		{
//			java.util.Iterator<SettingsVo> ite=result.iterator();
//			while(ite.hasNext())
//			{
//				SettingsVo settingsVo=(SettingsVo) ite.next();
//				ComboBoxVo ComboBoxVo = new ComboBoxVo();
//				ComboBoxVo.setCode(settingsVo.getValue());
//				ComboBoxVo.setDescription(settingsVo.getCode());
//				comboBoxVoList.add(ComboBoxVo);    			
//			}
//		}
//		return comboBoxVoList;
//	}
	
	
	
	//    public PushMessageWarnVo getPushMessageWarnExpiration(String language) 
	//    {
	//    	String lang = this.loadDefaultLanguage(language);
	//    	
	//    	PushMessageWarnVo messageVo = new PushMessageWarnVo();
	//    	List<SettingsVo> result = this.loadSettingsByCategory("CLM", "MSG");
	//    	if (result!=null && !result.isEmpty())
	//		{
	//			java.util.Iterator<SettingsVo> ite=result.iterator();
	//		
	//			while(ite.hasNext())
	//			{
	//				SettingsVo settingsVo=(SettingsVo) ite.next();
	//				if(("PUSH_EXPIRED_WARN_TEXT_"+lang).equalsIgnoreCase(settingsVo.getCode()))
	//					messageVo.setText(settingsVo.getValue());
	//				
	//				else if(("PUSH_EXPIRED_WARN_SENDER_"+lang).equalsIgnoreCase(settingsVo.getCode()))
	//					messageVo.setSender(settingsVo.getValue());
	//				
	//				else if("PUSH_EXPIRED_WARN_DAYS_BEFORE".equalsIgnoreCase(settingsVo.getCode()))
	//					messageVo.setDays(Integer.valueOf(settingsVo.getValue()));
	//				
	//			}
	//			
	//			Calendar expirationDate = Calendar.getInstance();
	//	    	expirationDate.add(Calendar.DAY_OF_YEAR, messageVo.getDays());
	//	    	messageVo.setExpirationDate(expirationDate);
	//	    	
	////	    	String text =messageVo.getText();
	////	    	text = text.replaceAll("\\{0\\}", FORMAT_DATE.format(expirationDate.getTime()));
	////	    	messageVo.setText(text);
	//	    	
	//		}
	//    	LOGGER.debug("[SettingsService - getPushMessageWarnExpiration] - loaded push Message Warn >> ",messageVo.toString());
	//        return messageVo;
	//    }


	//    public MessageVo getPushMessageTransaction(String language) 
	//    {
	//    	String lang = this.loadDefaultLanguage(language);
	//		
	//    	MessageVo messageVo = new MessageVo();
	//    	List<SettingsVo> result = this.loadSettingsByCategory("MCPLUS10", "PUSH_TRANSAC");
	//    	if (result!=null && !result.isEmpty())
	//		{
	//			java.util.Iterator<SettingsVo> ite=result.iterator();
	//		
	//			while(ite.hasNext())
	//			{
	//				SettingsVo settingsVo=(SettingsVo) ite.next();
	//				if(("TEXT_"+lang).equalsIgnoreCase(settingsVo.getCode()))
	//					messageVo.setText(settingsVo.getValue());
	//				
	//				else if(("SENDER_"+lang).equalsIgnoreCase(settingsVo.getCode()))
	//					messageVo.setSender(settingsVo.getValue());
	//				
	//			}
	//		}
	//        return messageVo;
	//    }
	//    
	//    
	//    


	//    public PushMessageWarnVo getPushMessageComeBack(String language) 
	//    {
	//    	String lang = this.loadDefaultLanguage(language);
	//    	
	//    	PushMessageWarnVo messageVo = new PushMessageWarnVo();
	//    	List<SettingsVo> result = this.loadSettingsByCategory("MCPLUS10", "PUSH_EXPIRED_COME_BACK");
	//    	if (result!=null && !result.isEmpty())
	//		{
	//			java.util.Iterator<SettingsVo> ite=result.iterator();
	//		
	//			while(ite.hasNext())
	//			{
	//				SettingsVo settingsVo=(SettingsVo) ite.next();
	//				if(("TEXT_"+lang).equalsIgnoreCase(settingsVo.getCode()))
	//					messageVo.setText(settingsVo.getValue());
	//				
	//				else if(("SENDER_"+lang).equalsIgnoreCase(settingsVo.getCode()))
	//					messageVo.setSender(settingsVo.getValue());
	//				
	////				else if("DAYS_BEFORE".equalsIgnoreCase(settingsVo.getCode()))
	////					messageVo.setDays(Integer.valueOf(settingsVo.getValue()));
	//				
	//			}
	//			
	////			Calendar expirationDate = Calendar.getInstance();
	////	    	expirationDate.add(Calendar.DAY_OF_YEAR, messageVo.getDays());
	////	    	messageVo.setExpirationDate(expirationDate);
	//		}
	//    	LOGGER.debug("[SettingsService - getPushMessageWarnExpiration] - loaded push Message");
	//        return messageVo;
	//    }

	//    public PushBirthdayVo getPushMessageBirthday(String language) 
	//    {
	//    	String lang = this.loadDefaultLanguage(language);
	//    	
	//    	PushBirthdayVo messageVo = new PushBirthdayVo();
	//    	List<SettingsVo> result = this.loadSettingsByCategory("MCPLUS10", "PUSH_BIRTHDAY");
	//    	if (result!=null && !result.isEmpty())
	//		{
	//			java.util.Iterator<SettingsVo> ite=result.iterator();
	//		
	//			while(ite.hasNext())
	//			{
	//				SettingsVo settingsVo=(SettingsVo) ite.next();
	//				if(("TEXT_"+lang).equalsIgnoreCase(settingsVo.getCode()))
	//					messageVo.setText(settingsVo.getValue());
	//				
	//				else if(("SENDER_"+lang).equalsIgnoreCase(settingsVo.getCode()))
	//					messageVo.setSender(settingsVo.getValue());
	//				
	//			}
	//	    	
	//		}
	//    	LOGGER.debug("[SettingsService - getPushMessageBirthday] - loaded push Birthday >> ",messageVo.toString());
	//        return messageVo;
	//    }


	//******************************************************************
	//****************** CLM URL ***************************************
	//******************************************************************  	

	

	//******************************************************************
	//****************** CLM CONFIGRATION ******************************
	//******************************************************************  

	private String loadDefaultLanguage(String language)
	{
		if ("ES".equalsIgnoreCase(language))
			return "ES";
		if ("EN".equalsIgnoreCase(language))
			return "EN";
		else
			return "ES";
	}


	private SettingsEntity toSettingsEntity(SettingsVo source)
	{
		SettingsEntity target =new SettingsEntity();
		target.setIdSettings(source.getIdSettings());
		target.setCategory(source.getCategory());
		target.setSubCategory(source.getSubCategory());
		target.setCode(source.getCode());
		target.setValue(source.getValue());
		target.setDescription(source.getDescription());

		return target;
	}


	private SettingsVo toSettingsVo(SettingsEntity source)
	{
		SettingsVo target =new SettingsVo();
		target.setIdSettings(source.getIdSettings());
		target.setCategory(source.getCategory());
		target.setSubCategory(source.getSubCategory());
		target.setCode(source.getCode());
		target.setValue(source.getValue());
		target.setDescription(source.getDescription());
		return target;
	}

	private void toSettingsEntity(SettingsVo source, SettingsEntity target, boolean copyIfNull)
	{
		if (copyIfNull || source.getIdSettings() != null)
		{
			target.setIdSettings(source.getIdSettings());
		}
		if (copyIfNull || source.getCategory() != null)
		{
			target.setCategory(source.getCategory());
		}
		if (copyIfNull || source.getSubCategory() != null)
		{
			target.setSubCategory(source.getSubCategory());
		}
		if (copyIfNull || source.getCode() != null)
		{ 
			target.setCode(source.getCode());
		}
		if (copyIfNull || source.getValue() != null)
		{
			target.setValue(source.getValue());
		}
		if (copyIfNull || source.getDescription() != null)
		{
			target.setDescription(source.getDescription());
		}
		
		
	}
}
