package com.zap.sales.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zap.sales.entity.AlumnoEntity;
import com.zap.sales.entity.EmpresaEntity;
import com.zap.sales.entity.EmpresaEntity;
import com.zap.sales.exception.alumno.TrabajadorNotFoundException;
import com.zap.sales.exception.alumno.TrabajadorServiceException;
import com.zap.sales.exception.empresa.EmpresaNotFoundException;
import com.zap.sales.exception.empresa.EmpresaServiceException;
import com.zap.sales.exception.empresa.EmpresaConVentasException;
import com.zap.sales.exception.empresa.EmpresaNotFoundException;
import com.zap.sales.exception.empresa.EmpresaServiceException;
import com.zap.sales.entity.VentaEntity;
import com.zap.sales.vo.alumno.AlumnoVo;
import com.zap.sales.vo.empresa.EmpresaVo;
import com.zap.sales.vo.empresa.EmpresaSearchRequestVo;
import com.zap.sales.vo.empresa.EmpresaSearchResponseVo;
import com.zap.sales.vo.empresa.EmpresaVo;
import com.zap.security.vo.authenticate.AuthUserVo;

@Stateless
public class EmpresaService implements Serializable {

	@Inject
	EmpresaService empresaService;

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(EmpresaService.class.getName());

	@PersistenceContext(unitName = "zapLaboralgrouPool")
	private EntityManager em;

	@Transactional
	public EmpresaVo create(EmpresaVo empresaVo) {
		String TAG = "[EmpresaService - create]";

		if (empresaVo == null) {
			throw new IllegalArgumentException(TAG + " >> 'empresaVo' can not be null");
		}

		try {
			EmpresaEntity entity = toEmpresaEntity(empresaVo);
			entity.setFechaCreacion(Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid")));
			entity.setUuIdEmpresa(this.getUUID());
			
			em.persist(entity);
			em.flush();

			return toEmpresaVo(entity);

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex);
			throw new EmpresaServiceException("Failed to create empresa", ex);
		}
	}

	private String getUUID()
	{
		
		UUID uuidEmpresa;
		do {
			uuidEmpresa = UUID.randomUUID();
		} while (loadByUuId(uuidEmpresa.toString()) != null);
		return uuidEmpresa.toString();
	}
	
	
	
	@Transactional
	public EmpresaVo saveOrUpdate(EmpresaVo empresaVo) {
		String TAG = "[EmpresaService - saveOrUpdate]";

		if (empresaVo == null) {
			throw new IllegalArgumentException(TAG + " >> 'empresaVo' can not be null");
		}

		try {
			
	        if (empresaVo.getIdEmpresa() != null) {
	            this.update(empresaVo, false);
	            LOGGER.info(TAG + " >> Empresa con ID " + empresaVo.getIdEmpresa() + " actualizada correctamente.");
	            return empresaVo;
	        } else {
	            empresaVo = this.create(empresaVo);
	            LOGGER.info(TAG + " >> Nueva empresa creada correctamente con CIF " + empresaVo.getCif());
	            return empresaVo;
	        }
			

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new EmpresaServiceException(ex);
		}
	}

	
	public void delete(Integer idEmpresa) throws EmpresaNotFoundException
	{
		String TAG = "[empresaService - delete idEmpresa:" + idEmpresa + "]";

		if (idEmpresa == null)
			throw new IllegalArgumentException(TAG + " >> 'idEmpresa' can not be null");

		try {
			EmpresaEntity entity = em.find(EmpresaEntity.class, idEmpresa);
			if (entity == null)
				throw new EmpresaNotFoundException();

			em.remove(entity);

		} catch (EmpresaNotFoundException ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw ex;

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new EmpresaServiceException(ex);
		}
	}
	
	
	


	public EmpresaVo loadByCif(String cif) 
	{
		String TAG = "[EmpresaService - findByCif]";
		if (cif == null) {
			throw new IllegalArgumentException(TAG + " >> 'cif' can not be null");
		}

		try {
			LOGGER.debug(TAG + " >> buscando empresa con CIF: " + cif);

			TypedQuery<EmpresaEntity> query = em.createNamedQuery("EmpresaEntity.findByCif", EmpresaEntity.class)
					.setParameter("cif", cif);

			EmpresaEntity entity = query.getSingleResult();

			if (entity == null)
				return null;
			else
				return this.toEmpresaVo(entity);

		} catch (javax.persistence.NoResultException ex) {
			return null;
			
		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new EmpresaServiceException(ex);
		}
	}
	
	

	public EmpresaVo loadByUuId(String uuIdEmpresa) throws EmpresaServiceException {
	    String TAG = "[EmpresaService - findByuuIdEmpresa]";
	    if (uuIdEmpresa == null) {
	        throw new IllegalArgumentException(TAG + " >> 'uuIdEmpresa' cannot be null");
	    }

	    try {
	        TypedQuery<EmpresaEntity> query = em
	                .createNamedQuery("EmpresaEntity.findByuuIdEmpresa", EmpresaEntity.class)
	                .setParameter("uuIdEmpresa", uuIdEmpresa);


	        EmpresaEntity entity = query.getSingleResult();

			if (entity == null)
				return null;
			else
				return this.toEmpresaVo(entity);

		} catch (javax.persistence.NoResultException ex) {
			return null;
			

	    } catch (Exception ex) {
	        LOGGER.error(TAG + " - Error: ", ex);
	        throw new EmpresaServiceException(ex);
	    }
	}

	
	public List<EmpresaVo> loadByEstado(String estado) throws EmpresaServiceException {
	    String TAG = "[EmpresaService - loadByEstado]";
	    if (estado == null) {
	        throw new IllegalArgumentException(TAG + " >> 'estado' cannot be null");
	    }

	    try {
	        TypedQuery<EmpresaEntity> query = em
	                .createNamedQuery("EmpresaEntity.loadByEstado", EmpresaEntity.class)
	                .setParameter("estado", estado);

	        List<EmpresaEntity> entities = query.getResultList();

	        List<EmpresaVo> empresas = new ArrayList<>();
	        for (EmpresaEntity entity : entities) {
	            empresas.add(this.toEmpresaVo(entity));  
	        }

	        return empresas;

	    } catch (Exception ex) {
	        LOGGER.error(TAG + " - Error: ", ex);
	        throw new EmpresaServiceException(ex);
	    }
	}


        public void update(EmpresaVo empresaVo, Boolean copyIfNull) throws EmpresaNotFoundException
        {
                String TAG = "[EmpresaService - update]";

                if (empresaVo == null)
			throw new IllegalArgumentException(TAG + " >> 'EmpresaVo' or 'EmpresaVo.getIdEmpresa()' can not be null");

		try {
			EmpresaEntity entity = em.find(EmpresaEntity.class, empresaVo.getIdEmpresa());

			if (entity == null)
				throw new EmpresaNotFoundException();
			entity.setFechaModificacion(Calendar.getInstance());

			this.toEmpresaEntity(empresaVo, entity, copyIfNull);

			em.merge(entity);

		} catch (EmpresaNotFoundException ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw ex;

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new EmpresaServiceException(ex);
                }
        }


        public EmpresaVo load(Integer idEmpresa) throws EmpresaNotFoundException {

                String TAG = "[empresaService - load idEmpresa:" + idEmpresa + "]";
                if (idEmpresa == null)
                        throw new IllegalArgumentException(TAG + " >> 'idempresa' can not be null");

		try {

			EmpresaEntity entity = em.find(EmpresaEntity.class, idEmpresa);

			if (entity == null)
				return null;
			else
				return this.toEmpresaVo(entity);

		} catch (javax.persistence.NoResultException ex) {
			return null;

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new EmpresaServiceException(ex);
		}
	}
	
	
	public List<EmpresaSearchResponseVo> search(EmpresaSearchRequestVo request, AuthUserVo user) {
		String TAG = "[empresaService - search]";

		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<EmpresaSearchResponseVo> cq = cb.createQuery(EmpresaSearchResponseVo.class);
			Root<EmpresaEntity> empresa = cq.from(EmpresaEntity.class);

			cq.select(cb.construct(EmpresaSearchResponseVo.class, empresa.get("idEmpresa"), empresa.get("razonSocial"),
					empresa.get("nombreComercial"), empresa.get("cif"), empresa.get("actividadPrincipal"),
					empresa.get("plantillaMedia"), empresa.get("fechaAlta"), empresa.get("existeRlt"),
					empresa.get("esPyme"), empresa.get("cnae"), empresa.get("domicilioFiscal"),
					empresa.get("codigoPostal"), empresa.get("tamanoEmpresa"), empresa.get("bonificacion"), empresa.get("creditosDisponibles"),
					empresa.get("creditosGastados"), empresa.get("estado"), empresa.get("iban"),
					empresa.get("repreLegalNombreCom"), empresa.get("repreLegalNif"), empresa.get("repreLegalTelefono"),
					empresa.get("repreLegalEmail"), empresa.get("asesoriaNombre"), empresa.get("asesorNombreCompleto"),
					empresa.get("asesorTelefono"), empresa.get("uuIdEmpresa"),empresa.get("asesorEmail"), empresa.get("origin"), 
					empresa.get("originUserUsername")));

			List<Predicate> predicates = new ArrayList<>();
			if (request != null) {
				if (!StringUtils.isBlank(request.getCif())) {
					predicates.add(cb.like(empresa.get("cif"), "%" + request.getCif() + "%"));
				}
				if (!StringUtils.isBlank(request.getNombreComercial())) {
					predicates.add(cb.like(empresa.get("nombreComercial"), "%" + request.getNombreComercial() + "%"));
				}
				if (!StringUtils.isBlank(request.getEstado())) {
					predicates.add(cb.like(empresa.get("estado"), "%" + request.getEstado() + "%"));
				}
                                if (!StringUtils.isBlank(request.getOrigin())) {
                                        predicates.add(cb.like(empresa.get("origin"), "%" + request.getOrigin() + "%"));
                                }
                                if (request.getParentCompanyId() != null) {
                                        Subquery<VentaEntity> parentCompanySubquery = cq.subquery(VentaEntity.class);
                                        Root<VentaEntity> venta = parentCompanySubquery.from(VentaEntity.class);
                                        parentCompanySubquery.select(venta);
                                        parentCompanySubquery.where(
                                                        cb.equal(venta.get("empresaEntity").get("idEmpresa"), empresa.get("idEmpresa")),
                                                        cb.equal(venta.get("parentCompanyId"), request.getParentCompanyId()));

                                        Subquery<VentaEntity> anyVentaSubquery = cq.subquery(VentaEntity.class);
                                        Root<VentaEntity> anyVenta = anyVentaSubquery.from(VentaEntity.class);
                                        anyVentaSubquery.select(anyVenta);
                                        anyVentaSubquery.where(
                                                        cb.equal(anyVenta.get("empresaEntity").get("idEmpresa"),
                                                                        empresa.get("idEmpresa")));

                                        predicates.add(cb.or(cb.exists(parentCompanySubquery),
                                                        cb.not(cb.exists(anyVentaSubquery))));
                                }

                        }
			cq.where(cb.and(predicates.toArray(new Predicate[0])));
			cq.orderBy(cb.desc(empresa.get("fechaCreacion")));

			List<EmpresaSearchResponseVo> results = em.createQuery(cq).getResultList();
			if (results == null) {
				return new ArrayList<>();
			}
			return results;

		} catch (javax.persistence.NoResultException ex) {
			return new ArrayList<>();

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new EmpresaServiceException(ex);
		}
	}


    public EmpresaEntity findEmpresaByCifConexion(EntityManager em, String cif) {
        try {
            TypedQuery<EmpresaEntity> query = em.createNamedQuery("EmpresaEntity.findByCif", EmpresaEntity.class);
            query.setParameter("cif", cif);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
   
	 
	private EmpresaEntity toEmpresaEntity(EmpresaVo source) {
		EmpresaEntity target = new EmpresaEntity();
                target.setUuIdEmpresa(source.getUuIdEmpresa());
		target.setRazonSocial(source.getRazonSocial());
		target.setNombreComercial(source.getNombreComercial());
		target.setCif(source.getCif());
		target.setActividadPrincipal(source.getActividadPrincipal());
		target.setPlantillaMedia(source.getPlantillaMedia());
		target.setFechaAlta(source.getFechaAlta());
		target.setExisteRlt(source.getExisteRlt());
		target.setEsPyme(source.getEsPyme());
		target.setCnae(source.getCnae());
		target.setDomicilioFiscal(source.getDomicilioFiscal());
		target.setCodigoPostal(source.getCodigoPostal());
                target.setTamanoEmpresa(source.getTamanoEmpresa());
                target.setCreditosDisponibles(source.getCreditosDisponibles());
                target.setCreditosGastados(source.getCreditosGastados());
                target.setEstado(source.getEstado());
                target.setIban(source.getIban());
		target.setRepreLegalNombreCom(source.getRepreLegalNombreCom());
		target.setRepreLegalNif(source.getRepreLegalNif());
		target.setRepreLegalTelefono(source.getRepreLegalTelefono());
		target.setRepreLegalEmail(source.getRepreLegalEmail());
		
		target.setBonificacion(source.getBonificacion());

		target.setOrigin(source.getOrigin());
		target.setOriginUserUsername(source.getOriginUserUsername());
		target.setOriginUserUuid(source.getOriginUserUuid());
		target.setOriginEmpresaUuid(source.getOriginEmpresaUuid());
		target.setOriginGestoriaUuid(source.getOriginGestoriaUuid());
	
		target.setAsesoriaNombre(source.getAsesoriaNombre());
		target.setAsesorNombreCompleto(source.getAsesorNombreCompleto());
		target.setAsesorTelefono(source.getAsesorTelefono());
		target.setAsesorEmail(source.getAsesorEmail());
		
		target.setPersonaContacto(source.getPersonaContacto());
		target.setEmailContacto(source.getEmailContacto());
		target.setPuestoContacto(source.getPuestoContacto());
		target.setTelefonoContacto(source.getTelefonoContacto());
		target.setObservaciones(source.getObservaciones());
		
		
		
		return target;
	}

	private void toEmpresaEntity(EmpresaVo source, EmpresaEntity target, Boolean copyIfNull) {

		if (copyIfNull || source.getIdEmpresa() != null)
			target.setIdEmpresa(source.getIdEmpresa());

		if (copyIfNull || source.getRazonSocial() != null)
			target.setRazonSocial(source.getRazonSocial());

		if (copyIfNull || source.getNombreComercial() != null)
			target.setNombreComercial(source.getNombreComercial());

		if (copyIfNull || source.getCif() != null)
			target.setCif(source.getCif());

		if (copyIfNull || source.getActividadPrincipal() != null)
			target.setActividadPrincipal(source.getActividadPrincipal());
		
		if (copyIfNull || source.getPlantillaMedia() != null)
			target.setPlantillaMedia(source.getPlantillaMedia());

		if (copyIfNull || source.getFechaAlta() != null)
			target.setFechaAlta(source.getFechaAlta());

		if (copyIfNull || source.getExisteRlt() != null)
			target.setExisteRlt(source.getExisteRlt());

		if (copyIfNull || source.getEsPyme() != null)
			target.setEsPyme(source.getEsPyme());

		if (copyIfNull || source.getCnae() != null)
			target.setCnae(source.getCnae());

		if (copyIfNull || source.getDomicilioFiscal() != null)
			target.setDomicilioFiscal(source.getDomicilioFiscal());

		if (copyIfNull || source.getCodigoPostal() != null)
			target.setCodigoPostal(source.getCodigoPostal());

                if (copyIfNull || source.getTamanoEmpresa() != null)
                        target.setTamanoEmpresa(source.getTamanoEmpresa());

                if (copyIfNull || source.getBonificacion() != null)
                        target.setBonificacion(source.getBonificacion());
		
		if (copyIfNull || source.getCreditosGastados() != null)
			target.setCreditosGastados(source.getCreditosGastados());

                if (copyIfNull || source.getCreditosDisponibles() != null)
                        target.setCreditosDisponibles(source.getCreditosDisponibles());

                if (copyIfNull || source.getEstado() != null)
                        target.setEstado(source.getEstado());

                if (copyIfNull || source.getRepreLegalNombreCom() != null)
                        target.setRepreLegalNombreCom(source.getRepreLegalNombreCom());
		
		if (copyIfNull || source.getRepreLegalNif() != null)
			target.setRepreLegalNif(source.getRepreLegalNif());

		if (copyIfNull || source.getRepreLegalTelefono() != null)
			target.setRepreLegalTelefono(source.getRepreLegalTelefono());

		if (copyIfNull || source.getRepreLegalEmail() != null)
			target.setRepreLegalEmail(source.getRepreLegalEmail());
		

		if (copyIfNull || source.getIban() != null)
			target.setIban(source.getIban());

                if (copyIfNull || source.getUuIdEmpresa() != null)
                        target.setUuIdEmpresa(source.getUuIdEmpresa());
		
		
		if (copyIfNull || source.getOrigin() != null)
		target.setOrigin(source.getOrigin());
		
		if (copyIfNull || source.getOriginUserUsername() != null)
		target.setOriginUserUsername(source.getOriginUserUsername());
		
		if (copyIfNull || source.getOriginUserUuid() != null)
		target.setOriginUserUuid(source.getOriginUserUuid());
		
		if (copyIfNull || source.getOriginEmpresaUuid() != null)
		target.setOriginEmpresaUuid(source.getOriginEmpresaUuid());
	
		if (copyIfNull || source.getOriginGestoriaUuid() != null)
		target.setOriginGestoriaUuid(source.getOriginGestoriaUuid());
		
		if (copyIfNull || source.getAsesoriaNombre() != null)
		target.setAsesoriaNombre(source.getAsesoriaNombre());
		
		if (copyIfNull || source.getAsesorNombreCompleto() != null)
		target.setAsesorNombreCompleto(source.getAsesorNombreCompleto());
		
		if (copyIfNull || source.getAsesorTelefono() != null)
		target.setAsesorTelefono(source.getAsesorTelefono());
		
		if (copyIfNull || source.getAsesorEmail() != null)
		target.setAsesorEmail(source.getAsesorEmail());
		
		if (copyIfNull || source.getFechaModificacion() != null)
			target.setFechaModificacion(source.getFechaModificacion());
	

		if (copyIfNull || source.getPersonaContacto() != null)
			target.setPersonaContacto(source.getPersonaContacto());
		
		if (copyIfNull || source.getEmailContacto() != null)
			target.setEmailContacto(source.getEmailContacto());
		
		if (copyIfNull || source.getPuestoContacto() != null)
			target.setPuestoContacto(source.getPuestoContacto());
		
		if (copyIfNull || source.getTelefonoContacto() != null)
			target.setTelefonoContacto(source.getTelefonoContacto());
		
		if (copyIfNull || source.getObservaciones() != null)
			target.setObservaciones(source.getObservaciones());


	}
	

	
	
	private EmpresaVo toEmpresaVo(EmpresaEntity source) {
		EmpresaVo target = new EmpresaVo();

		target.setIdEmpresa(source.getIdEmpresa());
		target.setRazonSocial(source.getRazonSocial());
		target.setNombreComercial(source.getNombreComercial());
		target.setCif(source.getCif());
		target.setActividadPrincipal(source.getActividadPrincipal());
		target.setPlantillaMedia(source.getPlantillaMedia());
		target.setExisteRlt(source.getExisteRlt());
		target.setEsPyme(source.getEsPyme());
		target.setCnae(source.getCnae());
		target.setDomicilioFiscal(source.getDomicilioFiscal());
		target.setCodigoPostal(source.getCodigoPostal());
		target.setTamanoEmpresa(source.getTamanoEmpresa());
                target.setCreditosDisponibles(source.getCreditosDisponibles());
                target.setCreditosGastados(source.getCreditosGastados());
                target.setEstado(source.getEstado());
                target.setIban(source.getIban());
		target.setRepreLegalNombreCom(source.getRepreLegalNombreCom());
		target.setRepreLegalNif(source.getRepreLegalNif());
		target.setRepreLegalTelefono(source.getRepreLegalTelefono());
		target.setRepreLegalEmail(source.getRepreLegalEmail());
                target.setBonificacion(source.getBonificacion());
                target.setUuIdEmpresa(source.getUuIdEmpresa());
		target.setFechaAlta(source.getFechaAlta());
		target.setAsesorEmail(source.getAsesorEmail());
		

		target.setOrigin(source.getOrigin());
		target.setOriginUserUsername(source.getOriginUserUsername());
		target.setOriginUserUuid(source.getOriginUserUuid());
		target.setOriginEmpresaUuid(source.getOriginEmpresaUuid());
		target.setOriginGestoriaUuid(source.getOriginGestoriaUuid());
	
		target.setAsesoriaNombre(source.getAsesoriaNombre());
		target.setAsesorNombreCompleto(source.getAsesorNombreCompleto());
		target.setAsesorTelefono(source.getAsesorTelefono());
		target.setAsesorEmail(source.getAsesorEmail());
		
		target.setPersonaContacto(source.getPersonaContacto());
		target.setEmailContacto(source.getEmailContacto());
		target.setPuestoContacto(source.getPuestoContacto());
		target.setTelefonoContacto(source.getTelefonoContacto());
		target.setObservaciones(source.getObservaciones());
				

		return target;
	}
}
