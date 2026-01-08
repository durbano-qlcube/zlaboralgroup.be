package com.zap.sales.service;

import java.io.Serializable;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zap.sales.entity.PersonaEntity;
import com.zap.sales.exception.persona.PersonaNotFoundException;
import com.zap.sales.exception.persona.PersonaServiceException;
import com.zap.sales.vo.particular.PersonaVo;

@Stateless
public class PersonaService implements Serializable {

	

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(PersonaService.class.getName());

	@PersistenceContext(unitName = "zapLaboralgrouPool")
	private EntityManager em;

	@Transactional
	public PersonaVo create(PersonaVo personaVo) {
		String TAG = "[personaService - create]";

		if (personaVo == null) {
			throw new IllegalArgumentException(TAG + " >> 'PersonaVo' can not be null");
		}

		try {
			PersonaEntity entity = toPersonaEntity(personaVo);
			entity.setFechaCreacion(Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid")));
			
			em.persist(entity);
			em.flush();

			return toPersonaVo(entity);

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex);
			throw new PersonaServiceException("Failed to create Persona", ex);
		}
	}

	private String getUUID()
	{
		
		UUID uuidPersona;
		do {
			uuidPersona = UUID.randomUUID();
		} while (loadByUuId(uuidPersona.toString()) != null);
		return uuidPersona.toString();
	}
	
	
	
	@Transactional
	public PersonaVo saveOrUpdate(PersonaVo personaVo) {
		String TAG = "[Persona Service - saveOrUpdate]";

		if (personaVo == null) {
			throw new IllegalArgumentException(TAG + " >> 'personaVo' can not be null");
		}

		try {
			
	        if (personaVo.getIdPersona() != null) {
	            this.update(personaVo, false);
	            LOGGER.info(TAG + " >> Persona con ID " + personaVo.getIdPersona() + " actualizada correctamente.");
	            return personaVo;
	        } else {
	        	personaVo = this.create(personaVo);
	            LOGGER.info(TAG + " >> Nueva Persona creada correctamente con CIF " + personaVo.getCif());
	            return personaVo;
	        }
			

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new PersonaServiceException(ex);
		}
	}

	
	public void delete(Integer idPersona) throws PersonaNotFoundException
	{
		String TAG = "[PersonaService - delete idPersona:" + idPersona + "]";

		if (idPersona == null)
			throw new IllegalArgumentException(TAG + " >> 'idPersona' can not be null");

		try {
			PersonaEntity entity = em.find(PersonaEntity.class, idPersona);
			if (entity == null)
				throw new PersonaNotFoundException();

			em.remove(entity);

		} catch (PersonaNotFoundException ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw ex;

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new PersonaServiceException(ex);
		}
	}
	
	
	


	public PersonaVo loadByDni(String Dni) 
	{
		String TAG = "[PersonaService - findByCif]";
		if (Dni == null) {
			throw new IllegalArgumentException(TAG + " >> 'cif' can not be null");
		}

		try {
			LOGGER.debug(TAG + " >> buscando Persona con Dni: " + Dni);

			TypedQuery<PersonaEntity> query = em.createNamedQuery("PersonaEntity.findByDni", PersonaEntity.class)
					.setParameter("dni", Dni);

			PersonaEntity entity = query.getSingleResult();

			if (entity == null)
				return null;
			else
				return this.toPersonaVo(entity);

		} catch (javax.persistence.NoResultException ex) {
			return null;
			
		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new PersonaServiceException(ex);
		}
	}
	

	public PersonaVo loadByIdPersona(Integer idPersona) 
	{
		String TAG = "[FormacionService - loadBybidPersona idFormacion:" + idPersona + "]";
		if (idPersona == null)
			throw new IllegalArgumentException(TAG + " >> 'idVenta' can not be null");

		try {
			Query query = em.createNamedQuery("PersonaEntity.loadByIdPersona");
			query.setParameter("idPersona", idPersona);

			PersonaEntity entity = (PersonaEntity) query.getSingleResult();

			if (entity == null)
				return null;
			else
				return this.toPersonaVo(entity);

		} catch (javax.persistence.NoResultException ex) {
			return null;

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new PersonaServiceException(ex);
		}
	}

	public PersonaVo loadByUuId(String uuIdPersona) throws PersonaServiceException {
	    String TAG = "[PersonaService - findByuuIdPersona]";
	    if (uuIdPersona == null) {
	        throw new IllegalArgumentException(TAG + " >> 'uuIdPersona' cannot be null");
	    }

	    try {
	        TypedQuery<PersonaEntity> query = em
	                .createNamedQuery("PersonaEntity.findByuuIdPersona", PersonaEntity.class)
	                .setParameter("uuIdPersona", uuIdPersona);


	        PersonaEntity entity = query.getSingleResult();

			if (entity == null)
				return null;
			else
				return this.toPersonaVo(entity);

		} catch (javax.persistence.NoResultException ex) {
			return null;
			

	    } catch (Exception ex) {
	        LOGGER.error(TAG + " - Error: ", ex);
	        throw new PersonaServiceException(ex);
	    }
	}

	
	public void update(PersonaVo personaVo, Boolean copyIfNull) throws PersonaNotFoundException
	{
		String TAG = "[PersonaService - update]";

		if (personaVo == null)
			throw new IllegalArgumentException(TAG + " >> 'PersonaVo' or 'PersonaVo.getIdPersona()' can not be null");

		try {
			PersonaEntity entity = em.find(PersonaEntity.class, personaVo.getIdPersona());

			if (entity == null)
				throw new PersonaNotFoundException();
			entity.setFechaModificacion(Calendar.getInstance());

			this.toPersonaEntity(personaVo, entity, copyIfNull);

			em.merge(entity);

		} catch (PersonaNotFoundException ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw ex;

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new PersonaServiceException(ex);
		}
	}
	
	
	
   
	 
	private PersonaEntity toPersonaEntity(PersonaVo source) {
		PersonaEntity target = new PersonaEntity();

		target.setIdPersona(source.getIdPersona());
		target.setNombre(source.getNombre());
		target.setDni(source.getDni());
		target.setTelefono(source.getTelefono());
		target.setEmail(source.getEmail());
		target.setDireccion(source.getDireccion());
		target.setCodigoPostal(source.getCodigoPostal());
		target.setCiudad(source.getCiudad());
		target.setActividadPrincipal(source.getActividadPrincipal());
		target.setIban(source.getIban());	
		target.setRazonSocial(source.getRazonSocial());
		target.setNombreComercial(source.getNombreComercial());
		target.setCif(source.getCif());
		target.setActividadPrincipal(source.getActividadPrincipal());
		target.setEsPyme(source.getEsPyme());
		target.setNumeroTrabajadores(source.getNumeroTrabajadores());
		target.setFechaCreacion(source.getFechaCreacion());




		return target;
	}

	private void toPersonaEntity(PersonaVo source, PersonaEntity target, Boolean copyIfNull) {

		if (copyIfNull || source.getIdPersona() != null)
			target.setIdPersona(source.getIdPersona());
		
		if (copyIfNull || source.getNombre() != null)
			target.setNombre(source.getNombre());
		
		if (copyIfNull || source.getDni() != null)
			target.setDni(source.getDni());
		
		if (copyIfNull || source.getTelefono() != null)
			target.setTelefono(source.getTelefono());

		if (copyIfNull || source.getEmail() != null)
			target.setEmail(source.getEmail());
		
		if (copyIfNull || source.getDireccion() != null)
			target.setDireccion(source.getDireccion());
		
		if (copyIfNull || source.getCodigoPostal() != null)
			target.setCodigoPostal(source.getCodigoPostal());

		if (copyIfNull || source.getCiudad() != null)
			target.setCiudad(source.getCiudad());
		
		if (copyIfNull || source.getActividadPrincipal() != null)
			target.setActividadPrincipal(source.getActividadPrincipal());
		
		if (copyIfNull || source.getIban() != null)
			target.setIban(source.getIban());
		
		if (copyIfNull || source.getRazonSocial() != null)
			target.setRazonSocial(source.getRazonSocial());

		if (copyIfNull || source.getNombreComercial() != null)
			target.setNombreComercial(source.getNombreComercial());

		if (copyIfNull || source.getCif() != null)
			target.setCif(source.getCif());

		if (copyIfNull || source.getActividadPrincipal() != null)
			target.setActividadPrincipal(source.getActividadPrincipal());

		if (copyIfNull || source.getEsPyme() != null)
			target.setEsPyme(source.getEsPyme());
		
	
			target.setNumeroTrabajadores(source.getNumeroTrabajadores());

	}
	

	
	
	private PersonaVo toPersonaVo(PersonaEntity source) {
		PersonaVo target = new PersonaVo();

		target.setIdPersona(source.getIdPersona());
		target.setNombre(source.getNombre());
		target.setDni(source.getDni());
		target.setTelefono(source.getTelefono());
		target.setEmail(source.getEmail());
		target.setDireccion(source.getDireccion());
		target.setCodigoPostal(source.getCodigoPostal());
		target.setCiudad(source.getCiudad());
		target.setActividadPrincipal(source.getActividadPrincipal());
		target.setIban(source.getIban());	
		target.setRazonSocial(source.getRazonSocial());
		target.setNombreComercial(source.getNombreComercial());
		target.setCif(source.getCif());
		target.setActividadPrincipal(source.getActividadPrincipal());
		target.setEsPyme(source.getEsPyme());
		target.setNumeroTrabajadores(source.getNumeroTrabajadores());
		target.setFechaCreacion(source.getFechaCreacion());



		return target;
	}
}
