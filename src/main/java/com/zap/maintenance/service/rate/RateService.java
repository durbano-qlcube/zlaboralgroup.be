package com.zap.maintenance.service.rate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zap.maintenance.entity.rate.RateEntity;
import com.zap.maintenance.entity.rate.SegmentEntity;
import com.zap.maintenance.exception.geo.ProvinceServiceException;
import com.zap.maintenance.exception.rate.RateNotFoundException;
import com.zap.maintenance.exception.rate.RateServiceException;
import com.zap.maintenance.vo.rate.RateVo;



@Stateless
public class RateService implements Serializable
{

	private static final long serialVersionUID = -799844646085705211L;
	private static final Logger LOGGER = LoggerFactory.getLogger(RateService.class.getName());
	


	@PersistenceContext(unitName = "zapLaboralgrouPool")
	EntityManager em;


	
	public RateVo create (RateVo rateVo)
	{
		String TAG ="[rateService - create]";
		if (rateVo == null)
			throw new IllegalArgumentException(TAG +" >> 'rateVo' can not be null");
		
		try{
			RateEntity entity = toRateEntity(rateVo);
			//entity.setFxCreation(Calendar.getInstance());
			em.persist(entity);
			return toRateVo(entity);
			
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
            throw new RateServiceException(ex);
		}
	}
	
	public void delete (Long idrate) throws RateNotFoundException
	{
		String TAG ="[rateService - delete idrate:"+idrate+"]";
		
		if (idrate == null)
			throw new IllegalArgumentException(TAG +" >> 'idrate' can not be null");

		try{
			RateEntity entity = em.find(RateEntity.class, idrate);
			if(entity == null)
				throw new RateNotFoundException();
			
			em.remove(entity);
			
		}catch (RateNotFoundException ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw ex;
           
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
            throw new RateServiceException(ex);
		}	
	}

	public RateVo load (Long idRate) throws RateNotFoundException
	{

		String TAG ="[rateService - load idRate:"+idRate+"]";
		if (idRate == null)
			throw new IllegalArgumentException(TAG +" >> 'idrate' can not be null");

		try{

			RateEntity entity = em.find(RateEntity.class, idRate);

			if(entity == null)
				return null;
			else
				return this.toRateVo(entity);

		}catch (javax.persistence.NoResultException ex){
			return null;

		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw new RateServiceException(ex);
		}	
	}

	
	
	@SuppressWarnings("unchecked")
	public List<RateVo>  loadAll () 
	{
		String TAG ="[rateService - loadAll]";
	
		try {
			Query query = em.createNamedQuery("RateEntity.loadAll");
			
			List<RateEntity> entityList = (List<RateEntity>)  query.getResultList();
			List<RateVo> result = new ArrayList<RateVo>();
			if (entityList!=null && !entityList.isEmpty())
			{	
				for (RateEntity source: entityList)
				{
					result.add(toRateVo(source));
				}
					
			}
						
			return result;
			
		}catch (javax.persistence.NoResultException ex){
			return null;
			
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw new RateServiceException(ex);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public List<RateVo> loadByIdSegment (Integer idSegment) 
	{
		String TAG ="[provinciaService - loadByIdSegment idSegment:"+idSegment+"]";
	
		if (idSegment == null)
			throw new IllegalArgumentException(TAG +" >> 'idSegment' can not be null");

		try {
			Query query = em.createNamedQuery("RateEntity.loadByIdSegment");
			query.setParameter("idSegment", idSegment);
			
			List<RateEntity> entityList = (List<RateEntity>)  query.getResultList();
			List<RateVo> result = new ArrayList<RateVo>();
			if (entityList!=null && !entityList.isEmpty())
			{	
				for (RateEntity source: entityList)
				{
					result.add(toRateVo(source));
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
	
	public void update (RateVo RateVo, Boolean copyIfNull) throws RateNotFoundException
	{
		String TAG ="[RateService - update]";
		
		if (RateVo == null)
			throw new IllegalArgumentException(TAG +" >> 'RateVo' can not be null");
		
		if (RateVo.getIdRate() == null)
			throw new IllegalArgumentException(TAG +" >> 'RateVo.getId()' can not be null");

		try{
			
			RateEntity entity = em.find(RateEntity.class, RateVo.getIdRate());
			
			if(entity == null)
				throw new RateNotFoundException();
			
			this.toRateEntity(RateVo, entity, copyIfNull);
			//entity.setFxModification(Calendar.getInstance());
			em.merge(entity);
			
		}catch (RateNotFoundException ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw ex;
           
		}catch (Exception ex){
			LOGGER.error("[RateService - updateRate] - Error: ",ex);
            throw new RateServiceException(ex);
		
		}
	}

	private void toRateEntity(RateVo source, RateEntity target, Boolean copyIfNull)
	{
		if(copyIfNull || source.getIdRate()!=null)
			target.setIdRate(source.getIdRate());

		if(copyIfNull || source.getName()!=null)
			target.setName(source.getName());

		if(copyIfNull || source.getDescription()!=null)
			target.setDescription(source.getDescription());

		if(copyIfNull || source.getDiscounts()!=null)
			target.setDiscounts(source.getDiscounts());

		
		 
		 

	}

	private RateVo toRateVo(RateEntity source)
	{
		RateVo target = new RateVo();
		target.setIdRate(source.getIdRate());
		target.setName(source.getName());
		target.setDescription(source.getDescription());
		target.setDiscounts(source.getDiscounts());

		target.setIdSegment(source.getSegmentoEntity().getIdSegment());
		return target;
	}

	private RateEntity toRateEntity(RateVo source)
	{
		RateEntity target = new RateEntity();
		target.setIdRate(source.getIdRate());
		target.setName(source.getName());
		target.setDescription(source.getDescription());
		target.setDiscounts(source.getDiscounts());

		if(source.getIdSegment()!=null)
		{
			SegmentEntity segment=new SegmentEntity();
			segment.setIdSegment(source.getIdSegment());
			target.setSegmentoEntity(segment);
		}

		return target;
	}
}
