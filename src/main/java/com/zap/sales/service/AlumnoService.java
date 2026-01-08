package com.zap.sales.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zap.sales.entity.AlumnoEntity;
import com.zap.sales.entity.EmpresaEntity;
import com.zap.sales.entity.FormacionEntity;
import com.zap.sales.exception.alumno.AlumnoNotFoundException;
import com.zap.sales.exception.alumno.AlumnoServiceException;
import com.zap.sales.vo.alumno.AlumnoVo;

@Stateless
public class AlumnoService implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(AlumnoService.class.getName());

	@PersistenceContext(unitName = "zapLaboralgrouPool")
	private EntityManager em;

	
	
	public void removeAlumnoFromFormacion(Integer idAlumno, Integer idFormacion) {
		String TAG = "[AlumnoService - removeAlumnoFromFormacion]";
		if (idAlumno == null || idFormacion == null)
			throw new IllegalArgumentException(TAG + " >> 'idAlumno' or 'idFormacion' cannot be null");

		try {

			AlumnoEntity alumnoEntity = em.find(AlumnoEntity.class, idAlumno);
			FormacionEntity formacionEntity = em.find(FormacionEntity.class, idFormacion);

			if (alumnoEntity == null || formacionEntity == null) {
				LOGGER.warn(TAG + " - Alumno or Formacion not found for idAlumno: " + idAlumno + " and idFormacion: "
						+ idFormacion);
				return;
			}

			alumnoEntity.getFormacionEntities().remove(formacionEntity);

			em.merge(alumnoEntity);

			LOGGER.info(TAG + " - Removed alumno id: " + idAlumno + " from formacion id: " + idFormacion);

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: " + ex.getMessage());
			throw new RuntimeException(ex);
		}
	}

	public List<AlumnoVo> loadByIdFormacion(Integer idFormacion)
	{
		String TAG = "[AlumnoService - loadAllByFormacion idFormacion:" + idFormacion + "]";
		if (idFormacion == null)
			throw new IllegalArgumentException(TAG + " >> 'idFormacion' can not be null");

		try {

			TypedQuery<AlumnoEntity> query = em.createNamedQuery("AlumnoEntity.findAlumnosByFormacionId",
					AlumnoEntity.class);
			query.setParameter("idFormacion", idFormacion);
			List<AlumnoEntity> entities = query.getResultList();

			if (entities.isEmpty()) {
				LOGGER.warn(TAG + " - No se encontraron alumnos para el id: " + idFormacion);
				return Collections.emptyList();
			}

			List<AlumnoVo> alumnos = entities.stream().map(this::toAlumnoVo).collect(Collectors.toList());

			return alumnos;

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: " + ex.getMessage());
			throw new AlumnoServiceException(ex);
		}
	}
	
	public AlumnoVo loadByDni(String dni) {
	    String TAG = "[AlumnoService - loadByDni dni:" + dni + "]";
	    if (dni == null) {
	        throw new IllegalArgumentException(TAG + " >> 'dni' cannot be null");
	    }

	    try {
	       
	        TypedQuery<AlumnoEntity> query = em.createNamedQuery("AlumnoEntity.findByDni", AlumnoEntity.class);
	        query.setParameter("dni", dni);
	        List<AlumnoEntity> entities = query.getResultList();

	      
	        if (entities.isEmpty()) {
	            LOGGER.warn(TAG + " - No se encontraron alumnos para el dni: " + dni);
	            return null;  
	        }

	       
	        return this.toAlumnoVo(entities.get(0));

	    } catch (Exception ex) {
	        LOGGER.error(TAG + " - Error: " + ex.getMessage());
	        throw new AlumnoServiceException(ex);
	    }
	}


	public List<AlumnoVo> loadByIdEmpresa(Integer idEmpresa) throws AlumnoNotFoundException
	{
		String TAG = "[AlumnoService - loadByIdEmpresa idEmpresa:" + idEmpresa + "]";
		if (idEmpresa == null)
			throw new IllegalArgumentException(TAG + " >> 'idEmpresa' can not be null");

		try {

			TypedQuery<AlumnoEntity> query = em.createNamedQuery("AlumnoEntity.findAlumnosByIdEmpresa",
					AlumnoEntity.class);
			query.setParameter("idEmpresa", idEmpresa);
			List<AlumnoEntity> entities = query.getResultList();

			if (entities.isEmpty()) {
				LOGGER.warn(TAG + " - No se encontraron alumnos para el id: " + idEmpresa);
				return Collections.emptyList();
			}

			List<AlumnoVo> alumnos = entities.stream().map(this::toAlumnoVo).collect(Collectors.toList());

			return alumnos;

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: " + ex.getMessage());
			throw new AlumnoServiceException(ex);
		}
	}

	

	
	public AlumnoVo create(AlumnoVo alumnoVo)
	{
		String TAG = "[alumnoService - create]";
		
		if (alumnoVo == null)
			throw new IllegalArgumentException(TAG + " >> 'alumnoVo' can not be null");
	
		try {
			AlumnoEntity alumnoEntity = toAlumnoEntity(alumnoVo);
			alumnoEntity.setFechaCreacion(Calendar.getInstance());
			em.persist(alumnoEntity);
			return toAlumnoVo(alumnoEntity);

		} catch (Exception ex) {

			LOGGER.error(TAG + " - Error: ", ex);
			throw new AlumnoServiceException(ex);
		}
	}
	
	public void delete(Integer idAlumno) throws AlumnoNotFoundException
	{
		String TAG = "[alumnoService - delete idAlumno:" + idAlumno + "]";

		if (idAlumno == null)
			throw new IllegalArgumentException(TAG + " >> 'idAlumno' can not be null");

		try {
			AlumnoEntity entity = em.find(AlumnoEntity.class, idAlumno);
			if (entity == null)
				throw new AlumnoNotFoundException();

			em.remove(entity);

		} catch (AlumnoNotFoundException ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw ex;

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new AlumnoServiceException(ex);
		}
	}

	

	public AlumnoVo saveOrUpdate(AlumnoVo alumnoVo) {
	    String TAG = "[AlumnoService - saveOrUpdate]";

	    if (alumnoVo == null) 
	        throw new IllegalArgumentException(TAG + " >> 'alumnoVo' can not be null");

	    try {
	        AlumnoVo loadAlumno = loadByDni(alumnoVo.getDni());

	        if (loadAlumno != null && loadAlumno.getDni() != null) {
	            alumnoVo.setIdAlumno(loadAlumno.getIdAlumno());
	        }

	        if (alumnoVo.getIdAlumno() != null ) {
	            this.update(alumnoVo, false);
	            LOGGER.info(TAG + " >> Alumno con ID " + alumnoVo.getIdAlumno() + " actualizado correctamente.");
	            return alumnoVo;
	        } else {
	            
	            alumnoVo = this.create(alumnoVo);
	            LOGGER.info(TAG + " >> Nuevo alumno creado correctamente con IdAlumno " + alumnoVo.getIdAlumno());
	            return alumnoVo;
	        }

	    } catch (Exception ex) {
	        LOGGER.error(TAG + " - Error: ", ex.getMessage());
	        throw new AlumnoServiceException(ex);
	    }
	}

	
	public AlumnoVo load(Integer idAlumno) throws AlumnoNotFoundException {

		String TAG = "[alumnoService - load idAlumno:" + idAlumno + "]";
		if (idAlumno == null)
			throw new IllegalArgumentException(TAG + " >> 'idalumno' can not be null");

		try {

			AlumnoEntity entity = em.find(AlumnoEntity.class, idAlumno);

			if (entity == null)
				return null;
			else
				return this.toAlumnoVo(entity);

		} catch (javax.persistence.NoResultException ex) {
			return null;

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new AlumnoServiceException(ex);
		}
	}

	
	
	@SuppressWarnings("unchecked")
	public List<AlumnoVo> loadAll() {
	    String TAG = "[alumnoService - loadAll]";

	    try {
	        Query query = em.createNamedQuery("AlumnoEntity.loadAll");

	        List<AlumnoEntity> entityList = (List<AlumnoEntity>) query.getResultList();
	        List<AlumnoVo> result = new ArrayList<>();
	        
	        if (entityList != null && !entityList.isEmpty()) {
	            for (AlumnoEntity source : entityList) {
	                result.add(toAlumnoVo(source));
	            }
	        }

	        return result;

	    } catch (javax.persistence.NoResultException ex) {
	        return new ArrayList<>();

	    } catch (Exception ex) {
	        LOGGER.error(TAG + " - Error: ", ex.getMessage());
	        throw new AlumnoServiceException(ex);
	    }
	}
	
	
	
//	public void update (AlumnoVo AlumnoVo, Boolean copyIfNull) throws AlumnoNotFoundException
//	{
//		String TAG ="[AlumnoService - update]";
//		
//		if (AlumnoVo == null)
//			throw new IllegalArgumentException(TAG +" >> 'AlumnoVo' can not be null");
//		
//		if (AlumnoVo.getIdAlumno() == null)
//			throw new IllegalArgumentException(TAG +" >> 'AlumnoVo.getId()' can not be null");
//
//		try{
//			
//			AlumnoEntity entity = em.find(AlumnoEntity.class, AlumnoVo.getIdAlumno());
//			
//			if(entity == null)
//				throw new AlumnoNotFoundException();
//			
//			this.toAlumnoEntity(AlumnoVo, entity, copyIfNull);
//			entity.setFechaModificacion(Calendar.getInstance());
//			em.merge(entity);
//			
//		}catch (AlumnoNotFoundException ex){
//			LOGGER.error(TAG + " - Error: ",ex.getMessage());
//			throw ex;
//           
//		}catch (Exception ex){
//			LOGGER.error("[AlumnoService - updateAlumno] - Error: ",ex);
//            throw new AlumnoServiceException(ex);
//		
//		}
//	}
	
	public AlumnoVo update(AlumnoVo alumnoVo,  Boolean copyIfNull) throws AlumnoNotFoundException {
		String TAG = "[AlumnoService - update]";
		if (alumnoVo == null || alumnoVo.getIdAlumno() == null)
			throw new IllegalArgumentException(TAG + " >> 'AlumnoVo' or 'AlumnoVo.getIdAlumno()' can not be null");

		try {
			AlumnoEntity entity = em.find(AlumnoEntity.class, alumnoVo.getIdAlumno());
			if (entity == null)
				throw new AlumnoNotFoundException();

			this.toAlumnoEntity(alumnoVo, entity, copyIfNull);

			AlumnoEntity updatedEntity = em.merge(entity);
			if (alumnoVo.getIdFormacion() != null) {
				FormacionEntity formacionEntity = em.find(FormacionEntity.class, alumnoVo.getIdFormacion());

				if (formacionEntity != null) {

					updatedEntity.getFormacionEntities().add(formacionEntity);
					em.merge(updatedEntity);
				}
			}
			return toAlumnoVo(updatedEntity);
		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new AlumnoServiceException(ex);
		}
	}

	
	
	
	
	private void toAlumnoEntity(AlumnoVo source, AlumnoEntity target, Boolean copyIfNull)
	{
		if(copyIfNull || source.getIdAlumno()!=null)
			target.setIdAlumno(source.getIdAlumno());

		if(copyIfNull || source.getNombreCompleto()!=null)
			target.setNombreCompleto(source.getNombreCompleto());

		if(copyIfNull || source.getDni()!=null)
			target.setDni(source.getDni());

		if(copyIfNull || source.getFechaNacimiento()!=null)
			target.setFechaNacimiento(source.getFechaNacimiento());

		if(copyIfNull || source.getSexo()!=null)
			target.setSexo(source.getSexo());

		if(copyIfNull || source.getNacionalidad()!=null)
			target.setNacionalidad(source.getNacionalidad());

		if(copyIfNull || source.getTelefonoContacto()!=null)
			target.setTelefonoContacto(source.getTelefonoContacto());

		if(copyIfNull || source.getEmail()!=null)
			target.setEmail(source.getEmail());

		if(copyIfNull || source.getHorarioLaboral()!=null)
			target.setHorarioLaboral(source.getHorarioLaboral());

		if(copyIfNull || source.getNivelEstudios()!=null)
			target.setNivelEstudios(source.getNivelEstudios());

		if(copyIfNull || source.getPuesto()!=null)
			target.setPuesto(source.getPuesto());




	}

	
	
	
	private AlumnoEntity toAlumnoEntity(AlumnoVo source) {
		AlumnoEntity target = new AlumnoEntity();

		target.setNombreCompleto(source.getNombreCompleto());
		target.setDni(source.getDni());
		target.setFechaNacimiento(source.getFechaNacimiento());
		target.setSexo(source.getSexo());
		target.setNacionalidad(source.getNacionalidad());
		target.setTelefonoContacto(source.getTelefonoContacto());
		target.setEmail(source.getEmail());
		target.setHorarioLaboral(source.getHorarioLaboral());
		target.setNivelEstudios(source.getNivelEstudios());
		target.setPuesto(source.getPuesto());
		target.setIdAlumno(source.getIdAlumno());

		if (source.getIdEmpresa() != null) {
			EmpresaEntity empresa = new EmpresaEntity();
			empresa.setIdEmpresa(source.getIdEmpresa());
			target.setEmpresaEntity(empresa);

		}

		if (source.getIdFormacion() != null) {
			Set<FormacionEntity> formaciones = new TreeSet<>();
			FormacionEntity formacion = new FormacionEntity();
			formacion.setIdFormacion(source.getIdFormacion());
			formaciones.add(formacion);
			target.setFormacionEntities(formaciones);
		}

		return target;
	}



	private AlumnoVo toAlumnoVo(AlumnoEntity source) {
		AlumnoVo target = new AlumnoVo();

		target.setIdAlumno(source.getIdAlumno());
		target.setNombreCompleto(source.getNombreCompleto());
		target.setDni(source.getDni());
		target.setFechaNacimiento(source.getFechaNacimiento());
		target.setSexo(source.getSexo());
		target.setNacionalidad(source.getNacionalidad());
		target.setTelefonoContacto(source.getTelefonoContacto());
		target.setEmail(source.getEmail());
		target.setHorarioLaboral(source.getHorarioLaboral());
		target.setNivelEstudios(source.getNivelEstudios());
		target.setPuesto(source.getPuesto());
		target.setIdEmpresa(source.getEmpresaEntity().getIdEmpresa());

		return target;
	}
	
	
}
