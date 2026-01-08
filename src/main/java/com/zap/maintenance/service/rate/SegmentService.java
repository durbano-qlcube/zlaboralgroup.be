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

import com.zap.maintenance.entity.rate.SegmentEntity;
import com.zap.maintenance.exception.rate.SegmentNotFoundException;
import com.zap.maintenance.exception.rate.SegmentServiceException;
import com.zap.maintenance.vo.rate.SegmentVo;



@Stateless
public class SegmentService implements Serializable
{

	private static final long serialVersionUID = -799844646085705211L;
	private static final Logger LOGGER = LoggerFactory.getLogger(SegmentService.class.getName());
	


	@PersistenceContext(unitName = "zapLaboralgrouPool")
	EntityManager em;


	
	public SegmentVo create (SegmentVo segmentVo)
	{
		String TAG ="[segmentService - create]";
		if (segmentVo == null)
			throw new IllegalArgumentException(TAG +" >> 'segmentVo' can not be null");
		
		try{
			SegmentEntity entity = toSegmentEntity(segmentVo);
			//entity.setFxCreation(Calendar.getInstance());
			em.persist(entity);
			return toSegmentVo(entity);
			
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
            throw new SegmentServiceException(ex);
		}
	}
	
	public void delete (Long idsegment) throws SegmentNotFoundException
	{
		String TAG ="[segmentService - delete idsegment:"+idsegment+"]";
		
		if (idsegment == null)
			throw new IllegalArgumentException(TAG +" >> 'idsegment' can not be null");

		try{
			SegmentEntity entity = em.find(SegmentEntity.class, idsegment);
			if(entity == null)
				throw new SegmentNotFoundException();
			
			em.remove(entity);
			
		}catch (SegmentNotFoundException ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw ex;
           
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
            throw new SegmentServiceException(ex);
		}	
	}

	public SegmentVo load (Long idSegment) throws SegmentNotFoundException
	{

		String TAG ="[segmentService - load idSegment:"+idSegment+"]";
		if (idSegment == null)
			throw new IllegalArgumentException(TAG +" >> 'idsegment' can not be null");

		try{

			SegmentEntity entity = em.find(SegmentEntity.class, idSegment);

			if(entity == null)
				return null;
			else
				return this.toSegmentVo(entity);

		}catch (javax.persistence.NoResultException ex){
			return null;

		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw new SegmentServiceException(ex);
		}	
	}

	
	
	@SuppressWarnings("unchecked")
	public List<SegmentVo>  loadAll () 
	{
		String TAG ="[segmentService - loadAll]";
	
		try {
			Query query = em.createNamedQuery("SegmentEntity.loadAll");
			
			List<SegmentEntity> entityList = (List<SegmentEntity>)  query.getResultList();
			List<SegmentVo> result = new ArrayList<SegmentVo>();
			if (entityList!=null && !entityList.isEmpty())
			{	
				for (SegmentEntity source: entityList)
				{
					result.add(toSegmentVo(source));
				}
					
			}
						
			return result;
			
		}catch (javax.persistence.NoResultException ex){
			return null;
			
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw new SegmentServiceException(ex);
		}
	}
	
	
	public void update (SegmentVo SegmentVo, Boolean copyIfNull) throws SegmentNotFoundException
	{
		String TAG ="[SegmentService - update]";
		
		if (SegmentVo == null)
			throw new IllegalArgumentException(TAG +" >> 'SegmentVo' can not be null");
		
		if (SegmentVo.getIdSegment() == null)
			throw new IllegalArgumentException(TAG +" >> 'SegmentVo.getId()' can not be null");

		try{
			
			SegmentEntity entity = em.find(SegmentEntity.class, SegmentVo.getIdSegment());
			
			if(entity == null)
				throw new SegmentNotFoundException();
			
			this.toSegmentEntity(SegmentVo, entity, copyIfNull);
			//entity.setFxModification(Calendar.getInstance());
			em.merge(entity);
			
		}catch (SegmentNotFoundException ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw ex;
           
		}catch (Exception ex){
			LOGGER.error("[SegmentService - updateSegment] - Error: ",ex);
            throw new SegmentServiceException(ex);
		
		}
	}

	private void toSegmentEntity(SegmentVo source, SegmentEntity target, Boolean copyIfNull)
	{
		if(copyIfNull || source.getIdSegment()!=null)
			target.setIdSegment(source.getIdSegment());

		if(copyIfNull || source.getName()!=null)
			target.setName(source.getName());

		if(copyIfNull || source.getDescription()!=null)
		target.setDescription(source.getDescription());
		
		 
		 

	}

	private SegmentVo toSegmentVo(SegmentEntity source)
	{
		SegmentVo target = new SegmentVo();

		target.setIdSegment(source.getIdSegment());
		target.setName(source.getName());
		target.setDescription(source.getDescription());

		return target;
	}

	private SegmentEntity toSegmentEntity(SegmentVo source)
	{
		SegmentEntity target = new SegmentEntity();
		target.setIdSegment(source.getIdSegment());
		target.setName(source.getName());
		target.setDescription(source.getDescription());

		return target;
	}
}
