package com.zap.security.service.authenticate;

import java.io.Serializable;
import java.util.Calendar;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zap.security.entity.authenticate.HistoryPassEntity;
import com.zap.security.exception.authenticate.HistoryPassNotFoundException;
import com.zap.security.exception.authenticate.HistoryPassServiceException;
import com.zap.security.vo.authenticate.HistoryPassVo;



@Stateless
public class HistoryPassService implements Serializable
{

	private static final long serialVersionUID = -799844646085705211L;
	private static final Logger LOGGER = LoggerFactory.getLogger(HistoryPassService.class.getName());
	


	@PersistenceContext(unitName = "zapLaboralgrouPool")
	EntityManager em;
	
	public HistoryPassVo create (HistoryPassVo historyPassVo)
	{
		String TAG ="[historyPassService - create]";
		if (historyPassVo == null)
			throw new IllegalArgumentException(TAG +" >> 'historyPassVo' can not be null");
		
		try{
			HistoryPassEntity entity = toHistoryPassEntity(historyPassVo);
			entity.setFxCreation(Calendar.getInstance());
			em.persist(entity);
			return toHistoryPassVo(entity);
			
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
            throw new HistoryPassServiceException(ex);
		}
	}
	
	public void delete (Long idhistoryPass) throws HistoryPassNotFoundException
	{
		String TAG ="[historyPassService - delete idhistoryPass:"+idhistoryPass+"]";
		
		if (idhistoryPass == null)
			throw new IllegalArgumentException(TAG +" >> 'idhistoryPass' can not be null");

		try{
			HistoryPassEntity entity = em.find(HistoryPassEntity.class, idhistoryPass);
			if(entity == null)
				throw new HistoryPassNotFoundException();
			
			em.remove(entity);
			
		}catch (HistoryPassNotFoundException ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw ex;
           
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
            throw new HistoryPassServiceException(ex);
		}	
	}

	public void deleteByCode (String nif) throws HistoryPassNotFoundException
	{
		String TAG ="[historyPassService - delete nif:"+nif+"]";
		
		if (nif == null)
			throw new IllegalArgumentException(TAG +" >> 'nif' can not be null");

		try{
				Query query = em.createNamedQuery("HistoryPassEntity.deleteByNif");
				query.setParameter("code", nif);
				query.executeUpdate();
				
           
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
            throw new HistoryPassServiceException(ex);
		}	
	}

	public HistoryPassVo load (Long idHistoryPass) throws HistoryPassNotFoundException
	{

		String TAG ="[historyPassService - load idHistoryPass:"+idHistoryPass+"]";
		if (idHistoryPass == null)
			throw new IllegalArgumentException(TAG +" >> 'idhistoryPass' can not be null");

		try{

			HistoryPassEntity entity = em.find(HistoryPassEntity.class, idHistoryPass);

			if(entity == null)
				return null;
			else
				return this.toHistoryPassVo(entity);

		}catch (javax.persistence.NoResultException ex){
			return null;

		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw new HistoryPassServiceException(ex);
		}	
	}

	
	
	
	@SuppressWarnings("unchecked")
	public HistoryPassVo loadByUuid(String uuid) 
	{
		String TAG ="[historyPassService - loadByUuid uuid:"+uuid+"]";

		if (uuid == null)
			throw new IllegalArgumentException(TAG +" >> 'uuid' can not be null");

		try {
			Query query = em.createNamedQuery("HistoryPassEntity.loadByUuid");
			query.setParameter("uuid", uuid);

			HistoryPassEntity entity = (HistoryPassEntity)  query.getSingleResult();
			if (entity == null)
				return null;
			else
				return this.toHistoryPassVo(entity);

		} catch (javax.persistence.NoResultException ex) {
			return null;


		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex);
			throw new HistoryPassServiceException(ex);
		}
	}

	
	@SuppressWarnings("unchecked")
	public void checkPass(String uuid, String pass) 
	{
		String TAG ="[historyPassService - checkPass uuid:"+uuid+"]";

		if (uuid == null)
			throw new IllegalArgumentException(TAG +" >> 'uuid' can not be null");

		try {
			Query query = em.createNamedQuery("HistoryPassEntity.loadByUuid");
			query.setParameter("uuid", uuid);

//			HistoryPassEntity entity = (HistoryPassEntity)  query.getSingleResult();
//			if (entity == null)
//				return null;
//			else
//				return this.toHistoryPassVo(entity);
//
//		} catch (javax.persistence.NoResultException ex) {
//			return null;


		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex);
			throw new HistoryPassServiceException(ex);
		}
	}
	
	public void update (HistoryPassVo HistoryPassVo, Boolean copyIfNull) throws HistoryPassNotFoundException
	{
		String TAG ="[HistoryPassService - update]";
		
		if (HistoryPassVo == null)
			throw new IllegalArgumentException(TAG +" >> 'HistoryPassVo' can not be null");
		
		if (HistoryPassVo.getId() == null)
			throw new IllegalArgumentException(TAG +" >> 'HistoryPassVo.getId()' can not be null");

		try{
			
			HistoryPassEntity entity = em.find(HistoryPassEntity.class, HistoryPassVo.getId());
			
			if(entity == null)
				throw new HistoryPassNotFoundException();
			
			this.toHistoryPassEntity(HistoryPassVo, entity, copyIfNull);
			entity.setFxModification(Calendar.getInstance());
			em.merge(entity);
			
		}catch (HistoryPassNotFoundException ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw ex;
           
		}catch (Exception ex){
			LOGGER.error("[HistoryPassService - updateHistoryPass] - Error: ",ex);
            throw new HistoryPassServiceException(ex);
		
		}
	}

	private void toHistoryPassEntity(HistoryPassVo source, HistoryPassEntity target, Boolean copyIfNull)
	{
		if(copyIfNull || source.getId()!=null)
			target.setId(source.getId());

		if(copyIfNull || source.getUuid()!=null)
			target.setUuid(source.getUuid());
		
		
		if(copyIfNull || source.getPass()!=null)
		target.setPass(source.getPass());
		
		
		
		
		 
		 

	}

	private HistoryPassVo toHistoryPassVo(HistoryPassEntity source)
	{
		HistoryPassVo target = new HistoryPassVo();

		target.setId(source.getId());
		target.setUuid(source.getUuid());
		target.setPass(source.getPass());
		
		
		return target;
	}

	private HistoryPassEntity toHistoryPassEntity(HistoryPassVo source)
	{
		HistoryPassEntity target = new HistoryPassEntity();
		target.setId(source.getId());
		target.setUuid(source.getUuid());
		target.setPass(source.getPass());
		
		
		return target;
	}
}
