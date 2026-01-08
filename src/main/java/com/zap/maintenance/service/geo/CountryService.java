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

import com.zap.maintenance.entity.geo.CountryEntity;
import com.zap.maintenance.exception.geo.CountryNotFoundException;
import com.zap.maintenance.exception.geo.CountryServiceException;
import com.zap.maintenance.vo.geo.CountryVo;



@Stateless
public class CountryService implements Serializable
{

	private static final long serialVersionUID = -799844646085705211L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CountryService.class.getName());
	


	@PersistenceContext(unitName = "zapLaboralgrouPool")
	EntityManager em;


	
	public CountryVo create (CountryVo countryVo)
	{
		String TAG ="[countryService - create]";
		if (countryVo == null)
			throw new IllegalArgumentException(TAG +" >> 'countryVo' can not be null");
		
		try{
			CountryEntity entity = toCountryEntity(countryVo);
			//entity.setFxCreation(Calendar.getInstance());
			em.persist(entity);
			return toCountryVo(entity);
			
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
            throw new CountryServiceException(ex);
		}
	}
	
	public void delete (Long idcountry) throws CountryNotFoundException
	{
		String TAG ="[countryService - delete idcountry:"+idcountry+"]";
		
		if (idcountry == null)
			throw new IllegalArgumentException(TAG +" >> 'idcountry' can not be null");

		try{
			CountryEntity entity = em.find(CountryEntity.class, idcountry);
			if(entity == null)
				throw new CountryNotFoundException();
			
			em.remove(entity);
			
		}catch (CountryNotFoundException ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw ex;
           
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
            throw new CountryServiceException(ex);
		}	
	}

	public CountryVo load (Long idCountry) throws CountryNotFoundException
	{

		String TAG ="[countryService - load idCountry:"+idCountry+"]";
		if (idCountry == null)
			throw new IllegalArgumentException(TAG +" >> 'idcountry' can not be null");

		try{

			CountryEntity entity = em.find(CountryEntity.class, idCountry);

			if(entity == null)
				return null;
			else
				return this.toCountryVo(entity);

		}catch (javax.persistence.NoResultException ex){
			return null;

		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw new CountryServiceException(ex);
		}	
	}

	
	@SuppressWarnings("unchecked")
	public List<CountryVo>  loadAll () 
	{
		String TAG ="[countryService - loadAll]";
	
		try {
			Query query = em.createNamedQuery("country.loadAll");
			
			List<CountryEntity> entityList = (List<CountryEntity>)  query.getResultList();
			List<CountryVo> result = new ArrayList<CountryVo>();
			if (entityList!=null && !entityList.isEmpty())
			{	
				for (CountryEntity source: entityList)
				{
					result.add(toCountryVo(source));
				}
					
			}
						
			return result;
			
		}catch (javax.persistence.NoResultException ex){
			return null;
			
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw new CountryServiceException(ex);
		}
	}
	
	public void update (CountryVo CountryVo, Boolean copyIfNull) throws CountryNotFoundException
	{
		String TAG ="[CountryService - update]";
		
		if (CountryVo == null)
			throw new IllegalArgumentException(TAG +" >> 'CountryVo' can not be null");
		
		if (CountryVo.getIdCountry() == null)
			throw new IllegalArgumentException(TAG +" >> 'CountryVo.getId()' can not be null");

		try{
			
			CountryEntity entity = em.find(CountryEntity.class, CountryVo.getIdCountry());
			
			if(entity == null)
				throw new CountryNotFoundException();
			
			this.toCountryEntity(CountryVo, entity, copyIfNull);
			//entity.setFxModification(Calendar.getInstance());
			em.merge(entity);
			
		}catch (CountryNotFoundException ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw ex;
           
		}catch (Exception ex){
			LOGGER.error("[CountryService - updateCountry] - Error: ",ex);
            throw new CountryServiceException(ex);
		
		}
	}

	private void toCountryEntity(CountryVo source, CountryEntity target, Boolean copyIfNull)
	{
		if(copyIfNull || source.getIdCountry()!=null)
			target.setIdCountry(source.getIdCountry());

		if(copyIfNull || source.getName()!=null)
			target.setName(source.getName());

		if(copyIfNull || source.getAlfa2()!=null)
			target.setAlfa2(source.getAlfa2());

		if(copyIfNull || source.getAlfa3()!=null)
			target.setAlfa3(source.getAlfa3());
	}

	private CountryVo toCountryVo(CountryEntity source)
	{
		CountryVo target = new CountryVo();

		
		target.setIdCountry(source.getIdCountry());
		target.setName(source.getName());
		target.setAlfa2(source.getAlfa2());
		target.setAlfa3(source.getAlfa3());
		
		return target;
	}

	private CountryEntity toCountryEntity(CountryVo source)
	{
		CountryEntity target = new CountryEntity();
		target.setIdCountry(source.getIdCountry());
		target.setName(source.getName());
		target.setAlfa2(source.getAlfa2());
		target.setAlfa3(source.getAlfa3());
		
		
		return target;
	}
}
