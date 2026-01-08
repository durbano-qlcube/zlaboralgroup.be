package com.zap.maintenance.service.geo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zap.maintenance.entity.geo.ProvinceEntity;
import com.zap.maintenance.exception.geo.ProvinceNotFoundException;
import com.zap.maintenance.exception.geo.ProvinceServiceException;
import com.zap.maintenance.vo.geo.ProvinceVo;



@Stateless
public class ProvinceService implements Serializable
{

	private static final long serialVersionUID = -799844646085705211L;
	private static final Logger LOGGER = LoggerFactory.getLogger(ProvinceService.class.getName());
	


	@PersistenceContext(unitName = "zapLaboralgrouPool")
	EntityManager em;


	
	public ProvinceVo create (ProvinceVo provinciaVo)
	{
		String TAG ="[provinciaService - create]";
		if (provinciaVo == null)
			throw new IllegalArgumentException(TAG +" >> 'provinciaVo' can not be null");
		
		try{
			ProvinceEntity entity = toProvinciaEntity(provinciaVo);
			//entity.setFxCreation(Calendar.getInstance());
			em.persist(entity);
			return toProvinciaVo(entity);
			
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
            throw new ProvinceServiceException(ex);
		}
	}
	
	public void delete (Long idprovincia) throws ProvinceNotFoundException
	{
		String TAG ="[provinciaService - delete idprovincia:"+idprovincia+"]";
		
		if (idprovincia == null)
			throw new IllegalArgumentException(TAG +" >> 'idprovincia' can not be null");

		try{
			ProvinceEntity entity = em.find(ProvinceEntity.class, idprovincia);
			if(entity == null)
				throw new ProvinceNotFoundException();
			
			em.remove(entity);
			
		}catch (ProvinceNotFoundException ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw ex;
           
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
            throw new ProvinceServiceException(ex);
		}	
	}

	public ProvinceVo load (Long idProvincia) throws ProvinceNotFoundException
	{

		String TAG ="[provinciaService - load idProvincia:"+idProvincia+"]";
		if (idProvincia == null)
			throw new IllegalArgumentException(TAG +" >> 'idprovincia' can not be null");

		try{

			ProvinceEntity entity = em.find(ProvinceEntity.class, idProvincia);

			if(entity == null)
				return null;
			else
				return this.toProvinciaVo(entity);

		}catch (javax.persistence.NoResultException ex){
			return null;

		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw new ProvinceServiceException(ex);
		}	
	}

	
	@SuppressWarnings("unchecked")
	public List<ProvinceVo>  loadAll () 
	{
		String TAG ="[provinciaService - loadAll]";
	
		try {
			Query query = em.createNamedQuery("ProvinciaEntity.loadAll");
			
			List<ProvinceEntity> entityList = (List<ProvinceEntity>)  query.getResultList();
			List<ProvinceVo> result = new ArrayList<ProvinceVo>();
			if (entityList!=null && !entityList.isEmpty())
			{	
				for (ProvinceEntity source: entityList)
				{
					result.add(toProvinciaVo(source));
				}
					
			}
						
			return result;
			
		}catch (javax.persistence.NoResultException ex){
			return null;
			
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw new ProvinceServiceException(ex);
		}
	}
	
	public void update (ProvinceVo ProvinciaVo, Boolean copyIfNull) throws ProvinceNotFoundException
	{
		String TAG ="[ProvinciaService - update]";
		
		if (ProvinciaVo == null)
			throw new IllegalArgumentException(TAG +" >> 'ProvinciaVo' can not be null");
		
		if (ProvinciaVo.getIdProvince() == null)
			throw new IllegalArgumentException(TAG +" >> 'ProvinciaVo.getId()' can not be null");

		try{
			
			ProvinceEntity entity = em.find(ProvinceEntity.class, ProvinciaVo.getIdProvince());
			
			if(entity == null)
				throw new ProvinceNotFoundException();
			
			this.toProvinciaEntity(ProvinciaVo, entity, copyIfNull);
			//entity.setFxModification(Calendar.getInstance());
			em.merge(entity);
			
		}catch (ProvinceNotFoundException ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw ex;
           
		}catch (Exception ex){
			LOGGER.error("[ProvinciaService - updateProvincia] - Error: ",ex);
            throw new ProvinceServiceException(ex);
		
		}
	}

	private void toProvinciaEntity(ProvinceVo source, ProvinceEntity target, Boolean copyIfNull)
	{
		if(copyIfNull || source.getName()!=null)
			target.setName(source.getName());

		if(copyIfNull || source.getIdProvince()!=null)
		target.setIdProvince(source.getIdProvince());
		 

	}

	private ProvinceVo toProvinciaVo(ProvinceEntity source)
	{
		ProvinceVo target = new ProvinceVo();

		target.setName(source.getName());
		target.setIdProvince(source.getIdProvince());
		
		
		return target;
	}

	private ProvinceEntity toProvinciaEntity(ProvinceVo source)
	{
		ProvinceEntity target = new ProvinceEntity();
		target.setName(source.getName());
		target.setIdProvince(source.getIdProvince());
		
		
		return target;
	}
}
