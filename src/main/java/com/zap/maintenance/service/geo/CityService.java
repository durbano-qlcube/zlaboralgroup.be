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

import com.zap.maintenance.entity.geo.CityEntity;
import com.zap.maintenance.exception.geo.CityNotFoundException;
import com.zap.maintenance.exception.geo.CityServiceException;
import com.zap.maintenance.exception.geo.ProvinceServiceException;
import com.zap.maintenance.vo.geo.CityVo;



@Stateless
public class CityService implements Serializable
{

	private static final long serialVersionUID = -799844646085705211L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CityService.class.getName());
	


	@PersistenceContext(unitName = "zapLaboralgrouPool")
	EntityManager em;


	
	public CityVo create (CityVo cityVo)
	{
		String TAG ="[cityService - create]";
		if (cityVo == null)
			throw new IllegalArgumentException(TAG +" >> 'cityVo' can not be null");
		
		try{
			CityEntity entity = toCityEntity(cityVo);
			//entity.setFxCreation(Calendar.getInstance());
			em.persist(entity);
			return toCityVo(entity);
			
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
            throw new CityServiceException(ex);
		}
	}
	
	public void delete (Long idcity) throws CityNotFoundException
	{
		String TAG ="[cityService - delete idcity:"+idcity+"]";
		
		if (idcity == null)
			throw new IllegalArgumentException(TAG +" >> 'idcity' can not be null");

		try{
			CityEntity entity = em.find(CityEntity.class, idcity);
			if(entity == null)
				throw new CityNotFoundException();
			
			em.remove(entity);
			
		}catch (CityNotFoundException ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw ex;
           
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
            throw new CityServiceException(ex);
		}	
	}

	public CityVo load (Long idCity) throws CityNotFoundException
	{

		String TAG ="[cityService - load idCity:"+idCity+"]";
		if (idCity == null)
			throw new IllegalArgumentException(TAG +" >> 'idcity' can not be null");

		try{

			CityEntity entity = em.find(CityEntity.class, idCity);

			if(entity == null)
				return null;
			else
				return this.toCityVo(entity);

		}catch (javax.persistence.NoResultException ex){
			return null;

		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw new CityServiceException(ex);
		}	
	}

	
	
	@SuppressWarnings("unchecked")
	public List<CityVo>  loadAll () 
	{
		String TAG ="[cityService - loadAll]";
	
		try {
			Query query = em.createNamedQuery("city.loadAll");
			
			List<CityEntity> entityList = (List<CityEntity>)  query.getResultList();
			List<CityVo> result = new ArrayList<CityVo>();
			if (entityList!=null && !entityList.isEmpty())
			{	
				for (CityEntity source: entityList)
				{
					result.add(toCityVo(source));
				}
					
			}
						
			return result;
			
		}catch (javax.persistence.NoResultException ex){
			return null;
			
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw new CityServiceException(ex);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public List<CityVo> loadByIdProvince (Integer idProvince) 
	{
		String TAG ="[provinciaService - loadByIdProvince idProvince:"+idProvince+"]";
	
		if (idProvince == null)
			throw new IllegalArgumentException(TAG +" >> 'idProvince' can not be null");

		try {
			Query query = em.createNamedQuery("city.loadByIdProvince");
			query.setParameter("idProvince", idProvince);
			
			List<CityEntity> entityList = (List<CityEntity>)  query.getResultList();
			List<CityVo> result = new ArrayList<CityVo>();
			if (entityList!=null && !entityList.isEmpty())
			{	
				for (CityEntity source: entityList)
				{
					result.add(toCityVo(source));
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
	
	public void update (CityVo CityVo, Boolean copyIfNull) throws CityNotFoundException
	{
		String TAG ="[CityService - update]";
		
		if (CityVo == null)
			throw new IllegalArgumentException(TAG +" >> 'CityVo' can not be null");
		
		if (CityVo.getIdCity() == null)
			throw new IllegalArgumentException(TAG +" >> 'CityVo.getId()' can not be null");

		try{
			
			CityEntity entity = em.find(CityEntity.class, CityVo.getIdCity());
			
			if(entity == null)
				throw new CityNotFoundException();
			
			this.toCityEntity(CityVo, entity, copyIfNull);
			//entity.setFxModification(Calendar.getInstance());
			em.merge(entity);
			
		}catch (CityNotFoundException ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw ex;
           
		}catch (Exception ex){
			LOGGER.error("[CityService - updateCity] - Error: ",ex);
            throw new CityServiceException(ex);
		
		}
	}

	private void toCityEntity(CityVo source, CityEntity target, Boolean copyIfNull)
	{
		if(copyIfNull || source.getIdCity()!=null)
			target.setIdCity(source.getIdCity());

		if(copyIfNull || source.getName()!=null)
			target.setName(source.getName());

		if(copyIfNull || source.getIdProvince()!=null)
			target.setIdProvince(source.getIdProvince());

		
		
		 
		 

	}

	private CityVo toCityVo(CityEntity source)
	{
		CityVo target = new CityVo();

		target.setIdCity(source.getIdCity());
		target.setName(source.getName());
		target.setIdProvince(source.getIdProvince());


		return target;
	}

	private CityEntity toCityEntity(CityVo source)
	{
		CityEntity target = new CityEntity();
		target.setIdCity(source.getIdCity());
		target.setName(source.getName());
		target.setIdProvince(source.getIdProvince());

		return target;
	}
}
