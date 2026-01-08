package com.zap.sales.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zap.sales.entity.EmpresaEntity;
import com.zap.sales.entity.FormacionEntity;
import com.zap.sales.entity.VentaEntity;
import com.zap.sales.exception.formacion.FormacionNotFoundException;
import com.zap.sales.exception.formacion.FormacionServiceException;
import com.zap.sales.vo.formacion.FormacionVo;

@Stateless
public class FormacionService implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(FormacionService.class.getName());

	@PersistenceContext(unitName = "zapLaboralgrouPool")
	private EntityManager em;

	@Transactional
	public FormacionVo saveOrUpdate(FormacionVo formacionVo)
	{
		String TAG = "[FormacionService - saveOrUpdate]";

		if (formacionVo == null) 
			throw new IllegalArgumentException(TAG + " >> 'empresaVo' can not be null");


		try {
	        if (formacionVo.getIdFormacion() != null)
	        {
	            this.update(formacionVo, false);
	            LOGGER.info(TAG + " >> Formacion con ID " + formacionVo.getIdFormacion() + " actualizada correctamente.");
	            return formacionVo;
	       
	        } else {
	        
	            formacionVo = this.create(formacionVo);
	            LOGGER.info(TAG + " >> Nueva empresa creada correctamente con IdFormacion() " + formacionVo.getIdFormacion());
	            return formacionVo;
	        }
			

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new FormacionServiceException(ex);
		}
	}
	
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public FormacionVo create(FormacionVo formacionVo) {
		String TAG = "[FormacionService - create]";
		if (formacionVo == null)
			throw new IllegalArgumentException(TAG + " >> 'formacionVo' can not be null");

		try {
			FormacionEntity entity = this.toFormacionEntity(formacionVo);
			entity.setFxCreation(Calendar.getInstance());

			em.persist(entity);
			return toFormacionVo(entity);
		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new FormacionServiceException(ex);
		}
	}

	public void delete(Integer idFormacion) throws FormacionNotFoundException {
		String TAG = "[FormacionService - delete idFormacion:" + idFormacion + "]";

		if (idFormacion == null)
			throw new IllegalArgumentException(TAG + " >> 'idFormacion' can not be null");

		try {
			FormacionEntity entity = em.find(FormacionEntity.class, idFormacion);
			if (entity == null)
				throw new FormacionNotFoundException();

			em.remove(entity);

		} catch (FormacionNotFoundException ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw ex;

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new FormacionServiceException(ex);
		}
	}

	public FormacionVo load(Integer idFormacion) throws FormacionNotFoundException {
		String TAG = "[FormacionService - load idFormacion:" + idFormacion + "]";
		if (idFormacion == null)
			throw new IllegalArgumentException(TAG + " >> 'idFormacion' can not be null");

		try {
			FormacionEntity entity = em.find(FormacionEntity.class, idFormacion);

			if (entity == null)
				return null;
			else
				return this.toFormacionVo(entity);

		} catch (javax.persistence.NoResultException ex) {
			return null;

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new FormacionServiceException(ex);
		}
	}

	
	
	public FormacionVo loadByIdVenta(Integer idVenta) 
	{
		String TAG = "[FormacionService - loadBybIdVenta idFormacion:" + idVenta + "]";
		if (idVenta == null)
			throw new IllegalArgumentException(TAG + " >> 'idVenta' can not be null");

		try {
			Query query = em.createNamedQuery("FormacionEntity.loadByIdVenta");
			query.setParameter("idVenta", idVenta);

			FormacionEntity entity = (FormacionEntity) query.getSingleResult();

			if (entity == null)
				return null;
			else
				return this.toFormacionVo(entity);

		} catch (javax.persistence.NoResultException ex) {
			return null;

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new FormacionServiceException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	public List<FormacionVo> loadAll() {
		String TAG = "[FormacionService - loadAll]";

		try {
			Query query = em.createNamedQuery("FormacionEntity.findAll");

			List<FormacionEntity> entityList = (List<FormacionEntity>) query.getResultList();
			List<FormacionVo> result = new ArrayList<>();
			for (FormacionEntity source : entityList) {
				result.add(toFormacionVo(source));
			}

			return result;

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new FormacionServiceException(ex);
		}
	}

	
	
	public void update(FormacionVo empresaVo, Boolean copyIfNull) throws FormacionNotFoundException
	{
		String TAG = "[FormacionService - update]";

		if (empresaVo == null)
			throw new IllegalArgumentException(TAG + " >> 'FormacionVo' or 'FormacionVo.getIdFormacion()' can not be null");

		try {
			FormacionEntity entity = em.find(FormacionEntity.class, empresaVo.getIdFormacion());

			if (entity == null)
				throw new FormacionNotFoundException();
			entity.setFxModification(Calendar.getInstance());

			this.toFormacionEntity(empresaVo, entity, copyIfNull);

			em.merge(entity);

		} catch (FormacionNotFoundException ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw ex;

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new FormacionServiceException(ex);
		}
	}

	private FormacionEntity toFormacionEntity(FormacionVo source) {
		FormacionEntity target = new FormacionEntity();

		target.setIdFormacion(source.getIdFormacion());
		target.setNombre(source.getNombre());
		target.setHoras(source.getHoras());
		target.setAreaProfesional(source.getAreaProfesional());
		target.setNumeroAlumnos(source.getNumeroAlumnos());
		target.setFechaInicio(source.getFechaInicio());
		target.setFechaFin(source.getFechaFin());
		target.setFechaNotificacionInicioFundae(source.getFechaNotificacionInicioFundae());
		target.setFechaNotificacionFinFundae(source.getFechaNotificacionFinFundae());

		if (source.getIdEmpresa() != null) {
			EmpresaEntity empresa = new EmpresaEntity();
			empresa.setIdEmpresa(source.getIdEmpresa());
			target.setEmpresaEntity(empresa);
		}
		
//		if (source.getIdVenta() != null) {
//			VentaEntity ventaEntity = new VentaEntity();
//			ventaEntity.setIdVenta(source.getIdVenta());
////			ventaEntity.setFormacionEntity(target);
//			target.setVentaEntity(ventaEntity);
//		}
		
		
		return target;
	}


	
	private void toFormacionEntity(FormacionVo source, FormacionEntity target, Boolean copyIfNull)
	{
		if(copyIfNull || source.getIdFormacion()!=null)
			target.setIdFormacion(source.getIdFormacion());

		if(copyIfNull || source.getNombre()!=null)
			target.setNombre(source.getNombre());

		
		if(copyIfNull || source.getHoras()!=null)
		target.setHoras(source.getHoras());
		
		if(copyIfNull || source.getAreaProfesional()!=null)
		target.setAreaProfesional(source.getAreaProfesional());
		
		if(copyIfNull || source.getNumeroAlumnos()!=null)
		target.setNumeroAlumnos(source.getNumeroAlumnos());
		
		if (copyIfNull || source.getFechaInicio() != null) {
		    target.setFechaInicio(source.getFechaInicio());
		} else {
		    target.setFechaInicio(null);
		}

		if (copyIfNull || source.getFechaFin() != null) {
		    target.setFechaFin(source.getFechaFin());
		} else {
		    target.setFechaFin(null);
		}

		if (copyIfNull || source.getFechaNotificacionInicioFundae() != null) {
		    target.setFechaNotificacionInicioFundae(source.getFechaNotificacionInicioFundae());
		}else {
		    target.setFechaNotificacionInicioFundae(null);
		}

		if (copyIfNull || source.getFechaNotificacionFinFundae() != null) {
		    target.setFechaNotificacionFinFundae(source.getFechaNotificacionFinFundae());
		}else {
		    target.setFechaNotificacionFinFundae(null);
		}
		


	}

	

	private FormacionVo toFormacionVo(FormacionEntity source) {
		FormacionVo target = new FormacionVo();

		target.setIdFormacion(source.getIdFormacion());
		target.setNombre(source.getNombre());
		target.setHoras(source.getHoras());
		target.setAreaProfesional(source.getAreaProfesional());
		target.setNumeroAlumnos(source.getNumeroAlumnos());
		target.setFechaInicio(source.getFechaInicio());
		target.setFechaFin(source.getFechaFin());
		target.setFechaNotificacionInicioFundae(source.getFechaNotificacionInicioFundae());
		target.setFechaNotificacionFinFundae(source.getFechaNotificacionFinFundae());
		target.setIdEmpresa(source.getEmpresaEntity().getIdEmpresa());


		return target;
	}
}
