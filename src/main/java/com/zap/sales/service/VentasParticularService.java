package com.zap.sales.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Objects;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zap.sales.entity.AlumnoEntity;
import com.zap.sales.entity.EmpresaEntity;
import com.zap.sales.entity.FormacionEntity;
import com.zap.sales.entity.PersonaEntity;
import com.zap.sales.entity.VentaEntity;
import com.zap.sales.entity.VentaPartEntity;
import com.zap.sales.entity.VentaPartEntity;
import com.zap.sales.exception.doc.DocNotFoundException;
import com.zap.sales.exception.doc.DocServiceException;
import com.zap.sales.exception.venta.VentaNotFoundException;
import com.zap.sales.exception.venta.VentaServiceException;
import com.zap.sales.vo.particular.VentaPartSearchRequestVo;
import com.zap.sales.vo.particular.VentaPartSearchResponseExtVo;
import com.zap.sales.vo.particular.VentaPartSearchResponseVo;
import com.zap.sales.vo.particular.VentaPartVo;
import com.zap.sales.vo.venta.StatusVentaEnum;
import com.zap.sales.vo.venta.VentaVo;
import com.zap.security.vo.authenticate.AuthUserVo;
import com.zap.security.vo.enumerates.RoleEnum;

@Stateless
public class VentasParticularService implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(VentasParticularService.class.getName());

	@PersistenceContext(unitName = "zapLaboralgrouPool")
	private EntityManager em;
	@Inject
	DocService docService;

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public VentaPartVo create(VentaPartVo ventasVo) {
		String TAG = "[VentasService - create]";
		if (ventasVo == null)
			throw new IllegalArgumentException(TAG + " >> 'ventasVo' can not be null");

		try {
			VentaPartEntity ventaPartEntity = this.toVentaPartEntity(ventasVo);
			ventaPartEntity.setUuId(this.getUUID());
			ventaPartEntity.setFxCreation(Calendar.getInstance());
			if (ventaPartEntity.getCharged() == null) {
				ventaPartEntity.setCharged(0);
			}
			if (ventaPartEntity.getPdteFirma() == null) {
				ventaPartEntity.setPdteFirma(Boolean.FALSE);
			}

			em.persist(ventaPartEntity);

			return toVentaVo(ventaPartEntity);
		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new VentaServiceException(ex);
		}
	}

	private String getUUID() {

		UUID uuidEmpresa;
		do {
			uuidEmpresa = UUID.randomUUID();
		} while (this.loadByuuid(uuidEmpresa.toString()) != null);
		return uuidEmpresa.toString();
	}

	public void delete(String uuIdVenta) throws VentaNotFoundException, Exception {
		String TAG = "[VentaService - delete uuIdVenta:" + uuIdVenta + "]";

		if (uuIdVenta == null) {
			throw new IllegalArgumentException(TAG + " >> 'uuIdVenta' cannot be null");
		}

		try {

			TypedQuery<VentaPartEntity> ventaQuery = em
					.createNamedQuery("VentaPartEntity.findByUuid", VentaPartEntity.class)
					.setParameter("uuId", uuIdVenta);

			VentaPartEntity ventaEntity;
			try {
				ventaEntity = ventaQuery.getSingleResult();
			} catch (NoResultException e) {

				String errorMessage = "Venta no encontrada con uuId: " + uuIdVenta;
				LOGGER.error(TAG + " - " + errorMessage);
				throw new VentaNotFoundException(errorMessage);
			}
			em.remove(ventaEntity);

			LOGGER.info(TAG + " >> Venta con UUID " + uuIdVenta + " eliminada correctamente.");

		} catch (VentaNotFoundException ex) {

			LOGGER.error(TAG + " - Error: ", ex);
			throw ex;

		} catch (Exception ex) {

			LOGGER.error(TAG + " - Error: ", ex);
			throw new VentaServiceException(ex);
		}
	}

	public VentaPartVo load(Integer idVenta) throws DocNotFoundException {

		String TAG = "[docService - load idDoc:" + idVenta + "]";
		if (idVenta == null)
			throw new IllegalArgumentException(TAG + " >> 'iddoc' can not be null");

		try {

			VentaPartEntity entity = em.find(VentaPartEntity.class, idVenta);

			if (entity == null)
				return null;
			else
				return this.toVentaVo(entity);

		} catch (javax.persistence.NoResultException ex) {
			return null;

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new DocServiceException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	public List<VentaPartVo> loadAll() {
		String TAG = "[VentaService - loadAll]";

		try {
			Query query = em.createNamedQuery("VentaPartEntity.loadAll");

			List<VentaPartEntity> entityList = (List<VentaPartEntity>) query.getResultList();
			List<VentaPartVo> result = new ArrayList<>();

			if (entityList != null && !entityList.isEmpty()) {
				for (VentaPartEntity source : entityList) {
					result.add(toVentaVo(source));
				}
			}

			return result;

		} catch (javax.persistence.NoResultException ex) {
			return new ArrayList<>();

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new DocServiceException(ex);
		}
	}

	public VentaPartVo loadVentasByUuid(String uuId) {

		String TAG = "[VentaService - loadVentasByUuid]";

		try {
			TypedQuery<VentaPartEntity> query = em.createNamedQuery("VentaPartEntity.findByUuid",
					VentaPartEntity.class);
			query.setParameter("uuId", uuId);

			VentaPartEntity entity = (VentaPartEntity) query.getSingleResult();

			if (entity == null)
				return null;
			else
				return this.toVentaVo(entity);

		} catch (javax.persistence.NoResultException ex) {
			return null;

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new DocServiceException(ex);
		}

	}

	public VentaPartVo loadByuuid(String uuId) {
		String TAG = "[VentaService - load uuId:" + uuId + "]";
		if (uuId == null)
			throw new IllegalArgumentException(TAG + " >> 'uuId' can not be null");

		try {

			TypedQuery<VentaPartEntity> query = em.createNamedQuery("VentaPartEntity.findByUuid",
					VentaPartEntity.class);
			query.setParameter("uuId", uuId);

			VentaPartEntity entity = query.getSingleResult();

			if (entity == null)
				return null;
			else
				return this.toVentaVo(entity);

		} catch (javax.persistence.NoResultException ex) {
			return null;

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new VentaServiceException(ex);
		}
	}

	public VentaPartVo loadVentasByUuidOrderId(String stripeUuidOrderId) {
		String TAG = "[VentaService - load uuId:" + stripeUuidOrderId + "]";
		if (stripeUuidOrderId == null)
			throw new IllegalArgumentException(TAG + " >> 'uuId' can not be null");

		try {

			TypedQuery<VentaPartEntity> query = em.createNamedQuery("VentaPartEntity.findByUuidOrderId",
					VentaPartEntity.class);
			query.setParameter("stripeUuidOrderId", stripeUuidOrderId);

			VentaPartEntity entity = query.getSingleResult();

			if (entity == null)
				return null;
			else
				return this.toVentaVo(entity);

		} catch (javax.persistence.NoResultException ex) {
			return null;

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new VentaServiceException(ex);
		}
	}

	public VentaPartSearchResponseExtVo search(VentaPartSearchRequestVo request, AuthUserVo user, Integer pageLimit,
			Integer pageNumber) {
		String TAG = "[VentaPartSearchService - search]";

		long start = System.currentTimeMillis();
		LOGGER.info(TAG + " - Inicio de la búsqueda de ventas");

		List<VentaPartSearchResponseVo> paginatedResult = this.searchInternal(request, user, pageLimit, pageNumber);

		long end = System.currentTimeMillis();
		LOGGER.info(TAG + " - Búsqueda principal finalizada en " + (end - start) + " ms");

		List<VentaPartSearchResponseVo> resultSinPaginacion = this.searchInternal(request, user, null, null);
		Long totalRecords = Long.valueOf(resultSinPaginacion.size());
		LOGGER.info("Total de registros: " + totalRecords);

		long pdteDocCount = resultSinPaginacion.stream().filter(venta -> venta.getStatus() == StatusVentaEnum.PDTE_DOC)
				.count();
		long pdtePagoCount = resultSinPaginacion.stream()
				.filter(venta -> venta.getStatus() == StatusVentaEnum.PDTE_PAGO).count();
		long pdteInicioCursoCount = resultSinPaginacion.stream()
				.filter(venta -> venta.getStatus() == StatusVentaEnum.PDTE_INICIO_CURSO).count();
		long ejecucionCursoCount = resultSinPaginacion.stream()
				.filter(venta -> venta.getStatus() == StatusVentaEnum.EJECUCION_CURSO).count();
		long cursoFinalizadoCount = resultSinPaginacion.stream()
				.filter(venta -> venta.getStatus() == StatusVentaEnum.CURSO_FINALIZADO).count();
		long canceladoCount = resultSinPaginacion.stream()
				.filter(venta -> venta.getStatus() == StatusVentaEnum.CANCELADO).count();
//		long cobradoCount = resultSinPaginacion.stream().filter(venta -> Boolean.TRUE.equals(venta.getCharged()))
//				.count();
		long cobradoCount = resultSinPaginacion.stream()
			    .filter(venta -> venta.getCharged() != null && venta.getCharged() == 100)
			    .count();

		long pdteFirmaCount = resultSinPaginacion.stream().filter(venta -> Boolean.TRUE.equals(venta.getPdteFirma()))
				.count();

//		double totalCharged = resultSinPaginacion.stream().filter(venta -> Boolean.TRUE.equals(venta.getCharged()))
//				.mapToDouble(venta -> venta.getPrice() != null ? parsePrice(venta.getPrice().toString()) : 0.0).sum();

		double totalCharged = resultSinPaginacion.stream()
			    .filter(venta -> Integer.valueOf(100).equals(venta.getCharged()))
			    .mapToDouble(venta -> venta.getPrice() != null ? parsePrice(venta.getPrice().toString()) : 0.0)
			    .sum();

		
		BigDecimal totalChargedBigDecimal = BigDecimal.valueOf(totalCharged).setScale(2, RoundingMode.HALF_UP);

//		double totalPendingCharged = resultSinPaginacion.stream()
//				.filter(venta -> !Boolean.TRUE.equals(venta.getCharged()))
//				.mapToDouble(venta -> venta.getPrice() != null ? parsePrice(venta.getPrice().toString()) : 0.0).sum();
//
//		BigDecimal totalPendingChargedBigDecimal = BigDecimal.valueOf(totalPendingCharged).setScale(2,
//				RoundingMode.HALF_UP);
		
		double totalPendingCharged = resultSinPaginacion.stream()
			    .filter(venta -> venta.getCharged() == null || venta.getCharged() != 100 && venta.getStatus() != StatusVentaEnum.CANCELADO)
			    .mapToDouble(venta -> venta.getPrice() != null ? parsePrice(venta.getPrice().toString()) : 0.0)
			    .sum();

		BigDecimal totalPendingChargedBigDecimal = BigDecimal.valueOf(totalPendingCharged)
			    .setScale(2, RoundingMode.HALF_UP);

		totalCharged = totalChargedBigDecimal.doubleValue();
		totalPendingCharged = totalPendingChargedBigDecimal.doubleValue();

		VentaPartSearchResponseExtVo result = new VentaPartSearchResponseExtVo();
		result.setData(paginatedResult);
		result.setPDTE_DOC(pdteDocCount);
		result.setPDTE_PAGO(pdtePagoCount);
		result.setPDTE_INICIO_CURSO(pdteInicioCursoCount);
		result.setEJECUCION_CURSO(ejecucionCursoCount);
		result.setCURSO_FINALIZADO(cursoFinalizadoCount);
		result.setCANCELADO(canceladoCount);
		result.setCOBRADO(cobradoCount);
		result.setPDTE_FIRMA(pdteFirmaCount);
		result.setTOTAL_RECORDS(totalRecords);
		result.setTOTAL_CHARGED(totalCharged);
		result.setTOTAL_PENDING_CHARGED(totalPendingCharged);

		if (pageLimit != null && pageNumber != null) {
			long totalPages = (long) Math.ceil((double) totalRecords / pageLimit);
			result.setTOTAL_PAGES(totalPages);
		}

		return result;
	}

	private List<VentaPartSearchResponseVo> searchInternal(VentaPartSearchRequestVo request, AuthUserVo user,
			Integer pageLimit, Integer pageNumber) {
		String TAG = "[VentaPartSearchService - searchInternal]";
		CriteriaBuilder cb = em.getCriteriaBuilder();
		long start = System.currentTimeMillis();
		LOGGER.info(TAG + " - Inicio de la búsqueda de ventas");

		CriteriaQuery<VentaPartSearchResponseVo> cq = cb.createQuery(VentaPartSearchResponseVo.class);
		Root<VentaPartEntity> venta = cq.from(VentaPartEntity.class);
		Join<VentaPartEntity, PersonaEntity> persona = venta.join("personaEntity", JoinType.LEFT);

		cq.select(cb.construct(VentaPartSearchResponseVo.class, venta.get("uuId"), venta.get("fxVenta"),
				persona.get("dni"), persona.get("nombre"), venta.get("status"), venta.get("charged"),
				venta.get("pdteFirma"), venta.get("coordinadorUserName"), venta.get("usernameAgente"),
				venta.get("nombre"), venta.get("fechaInicio"), venta.get("fechaFin"), venta.get("price"),
				venta.get("pdteCobroInicioCurso"), venta.get("pdteCobroFinCurso"), persona.get("telefono")));

		List<Predicate> predicates = new ArrayList<>();

		if (RoleEnum.AGENTE.toString().equals(user.getRole().toString())) {
			predicates.add(cb.equal(venta.get("uuIdAgente"), user.getUuid()));
		} else if (RoleEnum.CORDINADOR.toString().equals(user.getRole().toString())) {
			predicates.add(cb.equal(venta.get("uuIdCoordinador"), user.getUuid()));
		} else if (RoleEnum.SUPERVISOR.toString().equals(user.getRole().toString())) {
			predicates.add(cb.equal(venta.get("uuIdSupervisor"), user.getUuid()));
		}

		List<StatusVentaEnum> estados = new ArrayList<>();
		if (request.getVpdteDoc())
			estados.add(StatusVentaEnum.PDTE_DOC);
		if (request.getVpdtePago())
			estados.add(StatusVentaEnum.PDTE_PAGO);
		if (request.getVpdteInicioCurso())
			estados.add(StatusVentaEnum.PDTE_INICIO_CURSO);
		if (request.getVejecucionCurso())
			estados.add(StatusVentaEnum.EJECUCION_CURSO);
		if (request.getVcursoFinalizado())
			estados.add(StatusVentaEnum.CURSO_FINALIZADO);
		if (request.getVcancelado())
			estados.add(StatusVentaEnum.CANCELADO);

		if (!estados.isEmpty()) {
			predicates.add(venta.get("status").in(estados));
		}

		if (!StringUtils.isBlank(request.getUuidProvider())) {
			predicates.add(cb.equal(venta.get("uuidProvider"), request.getUuidProvider()));
		}

		List<Long> parentCompanyFilters = new ArrayList<>();
		if (request.getParentCompanyId() != null) {
			parentCompanyFilters.add(request.getParentCompanyId());
		}
		if (request.getParentCompanyIds() != null) {
			parentCompanyFilters.addAll(request.getParentCompanyIds());
		}
		if (parentCompanyFilters.isEmpty()) {
			if (user.getParentCompanyId() != null) {
				parentCompanyFilters.add(user.getParentCompanyId());
			}
			if (user.getParentCompanyIds() != null) {
				parentCompanyFilters.addAll(user.getParentCompanyIds());
			}
		}

		parentCompanyFilters = parentCompanyFilters.stream().filter(Objects::nonNull).distinct()
				.collect(Collectors.toList());

		if (!parentCompanyFilters.isEmpty()) {
			predicates.add(venta.get("parentCompanyId").in(parentCompanyFilters));
		}

		if (request.getFxVentaInit() != null && request.getFxVentaFin() != null) {
			if (request.getFxVentaInit().equals(request.getFxVentaFin())) {
				Calendar inicioDelDia = (Calendar) request.getFxVentaInit().clone();
				inicioDelDia.set(Calendar.HOUR_OF_DAY, 0);
				inicioDelDia.set(Calendar.MINUTE, 0);
				inicioDelDia.set(Calendar.SECOND, 0);
				inicioDelDia.set(Calendar.MILLISECOND, 0);

				Calendar finDelDia = (Calendar) request.getFxVentaFin().clone();
				finDelDia.set(Calendar.HOUR_OF_DAY, 23);
				finDelDia.set(Calendar.MINUTE, 59);
				finDelDia.set(Calendar.SECOND, 59);
				finDelDia.set(Calendar.MILLISECOND, 999);

				predicates.add(cb.greaterThanOrEqualTo(venta.get("fxVenta"), inicioDelDia));
				predicates.add(cb.lessThanOrEqualTo(venta.get("fxVenta"), finDelDia));
			} else {
				predicates.add(cb.greaterThanOrEqualTo(venta.get("fxVenta"), request.getFxVentaInit()));
				predicates.add(cb.lessThanOrEqualTo(venta.get("fxVenta"), request.getFxVentaFin()));
			}
		}

		if (request.getFxInitCurso() != null && request.getFxFinCurso() != null) {

			Calendar inicioDelDiaCurso = getInicioDelDia(request.getFxInitCurso());
			Calendar finDelDiaCurso = getFinDelDia(request.getFxFinCurso());

			LOGGER.info("Fecha y hora inicio del curso: " + inicioDelDiaCurso.getTime());
			LOGGER.info("Fecha y hora fin del curso: " + finDelDiaCurso.getTime());

			predicates.add(cb.greaterThanOrEqualTo(venta.get("fechaInicio"), inicioDelDiaCurso));
			predicates.add(cb.lessThanOrEqualTo(venta.get("fechaFin"), finDelDiaCurso));

		} else if (request.getFxInitCurso() != null) {

			Calendar inicioDelDiaCurso = getInicioDelDia(request.getFxInitCurso());
			predicates.add(cb.equal(venta.get("fechaInicio"), inicioDelDiaCurso));

		} else if (request.getFxFinCurso() != null) {
			Calendar finDelDiaCurso = getInicioDelDia(request.getFxFinCurso());
			predicates.add(cb.equal(venta.get("fechaFin"), finDelDiaCurso));

		}
		if (request.getUsernameCoordinador() != null && !request.getUsernameCoordinador().isEmpty()) {
			predicates.add(cb.equal(venta.get("coordinadorUserName"), request.getUsernameCoordinador()));
		}
		if (request.getUsernameAgente() != null && !request.getUsernameAgente().isEmpty()) {
			predicates.add(cb.equal(venta.get("usernameAgente"), request.getUsernameAgente()));
		}
		if (request.getUsernameSupervisor() != null && !request.getUsernameSupervisor().isEmpty()) {
			predicates.add(cb.equal(venta.get("supervisorUserName"), request.getUsernameSupervisor()));
		}
//		if (request.getVcobrado() != null) {
//			predicates.add(cb.equal(venta.get("charged"), request.getVcobrado()));
//		}
		if (Boolean.TRUE.equals(request.getVcobrado())) {
			predicates.add(cb.equal(venta.get("charged"), 100));
		}

		if (request.getDni() != null && !request.getDni().trim().isEmpty()) {
			predicates.add(cb.like(persona.get("dni"), "%" + request.getDni().trim() + "%"));
		}
		if (request.getNombre() != null && !request.getNombre().trim().isEmpty()) {
			predicates.add(
					cb.like(cb.lower(persona.get("nombre")), "%" + request.getNombre().trim().toLowerCase() + "%"));
		}

		if (!StringUtils.isBlank(request.getPhoneContacto())) {
			Predicate phoneContactoVenta = cb.like(persona.get("telefono"), "%" + request.getPhoneContacto() + "%");
			predicates.add(cb.or(phoneContactoVenta));
		}

		cq.where(predicates.toArray(new Predicate[0]));
		cq.orderBy(cb.desc(venta.get("fxVenta")));

		TypedQuery<VentaPartSearchResponseVo> query = em.createQuery(cq);
		if (pageNumber != null && pageLimit != null) {
			query.setFirstResult(pageNumber * pageLimit);
			query.setMaxResults(pageLimit);
		}

		List<VentaPartSearchResponseVo> result = query.getResultList();

		return result;
	}

	@Transactional
	public VentaPartVo update(VentaPartVo ventaVo, Boolean copyIfNull) throws VentaNotFoundException {
		String TAG = "[VentasService - update]";
		if (ventaVo == null || ventaVo.getUuId() == null)
			throw new IllegalArgumentException(TAG + " >> 'VentaVo' or 'VentaVo.getUuId()' can not be null");

		try {
			VentaPartEntity entity = em.find(VentaPartEntity.class, ventaVo.getIdVenta());
			if (entity == null)
				throw new VentaNotFoundException();
			toVentaPartEntity(ventaVo, entity, copyIfNull);
			VentaPartEntity updatedEntity = em.merge(entity);
			return toVentaVo(updatedEntity);
		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new VentaServiceException(ex);
		}
	}

//	public List<VentaVo> loadVentasByStatus(List<StatusVentaEnum> statuses)
//	{
//
//		String TAG = "[VentaService - loadVentasByStatus]";
//
//		try {
//			TypedQuery<VentaPartEntity> query = em.createNamedQuery("VentaPartEntity.loadByStatuses", VentaPartEntity.class);
//			query.setParameter("statuses", statuses);
//
//			List<VentaPartEntity> entityList = (List<VentaPartEntity>) query.getResultList();
//		
//
//			return Optional.ofNullable(entityList).orElseGet(Collections::emptyList).stream().map(element -> this.toVentaVo(element)).collect(Collectors.toList());
//
//			
//		} catch (javax.persistence.NoResultException ex) {
//			return new ArrayList<>();
//
//		} catch (Exception ex) {
//			LOGGER.error(TAG + " - Error: ", ex.getMessage());
//			throw new DocServiceException(ex);
//		}
//
//	}
//	

	private VentaPartEntity toVentaPartEntity(VentaPartVo source) {
		VentaPartEntity target = new VentaPartEntity();
		target.setIdVenta(source.getIdVenta());
		target.setUuId(source.getUuId());
		target.setFxCreation(source.getFxCreation());
		target.setFxVenta(source.getFxVenta());

		target.setUuId(source.getUuId());
		target.setStatus(source.getStatus());

		target.setPrice(source.getPrice());
		target.setPriceDeductedExpenses(source.getPriceDeductedExpenses());
		target.setPriceWithIva(source.getPriceWithIva());
		target.setIva(source.getIva());
		target.setCommission(source.getCommission());
		target.setCharged(source.getCharged());
		target.setPdteFirma(source.getPdteFirma());

		target.setUsernameAgente(source.getUsernameAgente());
		target.setUuIdAgente(source.getUuIdAgente());
		target.setCoordinadorUserName(source.getUsernameCoordinador());
		target.setUuIdCoordinador(source.getUuIdCoordinador());
		target.setSupervisorUserName(source.getUsernameSupervisor());
		target.setUuIdSupervisor(source.getUuIdSupervisor());
		target.setObservaciones(source.getObservaciones());

		target.setNombre(source.getNombre());
		target.setHoras(source.getHoras());
		target.setAreaProfesional(source.getAreaProfesional());
		target.setFechaInicio(source.getFechaInicio());
		target.setFechaFin(source.getFechaFin());

		target.setStripePaymentLink(source.getStripePaymentLink());
                target.setStripePaymentStatus(source.getStripePaymentStatus());
                target.setPdteCobroInicioCurso(source.getPdteCobroInicioCurso());
                target.setPdteCobroFinCurso(source.getPdteCobroFinCurso());
                target.setPercentageToPay(source.getPercentageToPay());
                target.setParentCompanyId(source.getParentCompanyId());

                PersonaEntity personaEntity = null;
                if (source.getIdPersona() != null) {
                        personaEntity = new PersonaEntity();
                        personaEntity.setIdPersona(source.getIdPersona());
			target.setPersonaEntity(personaEntity);
		}

		return target;
	}

	private void toVentaPartEntity(VentaPartVo source, VentaPartEntity target, Boolean copyIfNull) {

		if (copyIfNull || source.getStatus() != null)
			target.setStatus(source.getStatus());

		if (copyIfNull || source.getUsernameAgente() != null)
			target.setUsernameAgente(source.getUsernameAgente());

		if (copyIfNull || source.getPrice() != null)
			target.setPrice(source.getPrice());

		if (copyIfNull || source.getPriceDeductedExpenses() != null)
			target.setPriceDeductedExpenses(source.getPriceDeductedExpenses());

		if (copyIfNull || source.getPriceWithIva() != null)
			target.setPriceWithIva(source.getPriceWithIva());

		if (copyIfNull || source.getIva() != null)
			target.setIva(source.getIva());

		if (copyIfNull || source.getCommission() != null)
			target.setCommission(source.getCommission());

		if (copyIfNull || source.getFxVenta() != null)
			target.setFxVenta(source.getFxVenta());

		if (copyIfNull || source.getCharged() != null)
			target.setCharged(source.getCharged());

		if (copyIfNull || source.getPdteFirma() != null)
			target.setPdteFirma(source.getPdteFirma());

		if (copyIfNull || source.getUsernameAgente() != null)
			target.setUsernameAgente(source.getUsernameAgente());

		if (copyIfNull || source.getUuIdAgente() != null)
			target.setUuIdAgente(source.getUuIdAgente());

		if (copyIfNull || source.getUsernameCoordinador() != null)
			target.setCoordinadorUserName(source.getUsernameCoordinador());

		if (copyIfNull || source.getUuIdCoordinador() != null)
			target.setUuIdCoordinador(source.getUuIdCoordinador());

		if (copyIfNull || source.getUsernameSupervisor() != null)
			target.setSupervisorUserName(source.getUsernameSupervisor());

		if (copyIfNull || source.getUuIdSupervisor() != null)
			target.setUuIdSupervisor(source.getUuIdSupervisor());

		if (copyIfNull || source.getObservaciones() != null)
			target.setObservaciones(source.getObservaciones());

		if (copyIfNull || source.getNombre() != null)
			target.setNombre(source.getNombre());

		if (copyIfNull || source.getHoras() != null)
			target.setHoras(source.getHoras());

		if (copyIfNull || source.getAreaProfesional() != null)
			target.setAreaProfesional(source.getAreaProfesional());

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

		if (copyIfNull || source.getStripePaymentLink() != null)
			target.setStripePaymentLink(source.getStripePaymentLink());

		if (copyIfNull || source.getStripePaymentId() != null)
			target.setStripePaymentId(source.getStripePaymentId());

		if (copyIfNull || source.getStripePaymentStatus() != null)
			target.setStripePaymentStatus(source.getStripePaymentStatus());

		if (copyIfNull || source.getStripeCustomerId() != null)
			target.setStripeCustomerId(source.getStripeCustomerId());

		if (copyIfNull || source.getStripeProductId() != null)
			target.setStripeProductId(source.getStripeProductId());

		if (copyIfNull || source.getStripePrecioId() != null)
			target.setStripePrecioId(source.getStripePrecioId());

		if (copyIfNull || source.getStripeUuidOrderId() != null)
			target.setStripeUuidOrderId(source.getStripeUuidOrderId());
		
		if (copyIfNull || source.getPdteCobroInicioCurso() != null)
			target.setPdteCobroInicioCurso(source.getPdteCobroInicioCurso());
		
		if (copyIfNull || source.getPdteCobroFinCurso() != null)
			target.setPdteCobroFinCurso(source.getPdteCobroFinCurso());
		
                if (copyIfNull || source.getPercentageToPay() != null)
                        target.setPercentageToPay(source.getPercentageToPay());

                if (copyIfNull || source.getParentCompanyId() != null)
                        target.setParentCompanyId(source.getParentCompanyId());
        }

        private VentaPartVo toVentaVo(VentaPartEntity source) {
                VentaPartVo target = new VentaPartVo();
		target.setIdVenta(source.getIdVenta());
		target.setUuId(source.getUuId());
		target.setFxVenta(source.getFxVenta());
		target.setStatus(source.getStatus());

		target.setPrice(source.getPrice());
		target.setPriceDeductedExpenses(source.getPriceDeductedExpenses());
		target.setPriceWithIva(source.getPriceWithIva());
		target.setIva(source.getIva());
		target.setCommission(source.getCommission());
		target.setCharged(source.getCharged());
		target.setPdteFirma(source.getPdteFirma());

		target.setUsernameAgente(source.getUsernameAgente());
		target.setUuIdAgente(source.getUuIdAgente());
		target.setUsernameCoordinador(source.getCoordinadorUserName());
		target.setUuIdCoordinador(source.getUuIdCoordinador());
		target.setUsernameSupervisor(source.getSupervisorUserName());
		target.setUuIdSupervisor(source.getUuIdSupervisor());
		target.setObservaciones(source.getObservaciones());

		target.setNombre(source.getNombre());
		target.setHoras(source.getHoras());
		target.setAreaProfesional(source.getAreaProfesional());
		target.setFechaInicio(source.getFechaInicio());
		target.setFechaFin(source.getFechaFin());

		target.setStripePaymentLink(source.getStripePaymentLink());
		target.setStripePaymentStatus(source.getStripePaymentStatus());
		target.setStripePaymentId(source.getStripePaymentId());
		target.setPdteCobroInicioCurso(source.getPdteCobroInicioCurso());
                target.setPdteCobroFinCurso(source.getPdteCobroFinCurso());
                target.setPercentageToPay(source.getPercentageToPay());
                target.setParentCompanyId(source.getParentCompanyId());

                if (source.getPersonaEntity() != null) {
                        target.setIdPersona(source.getPersonaEntity().getIdPersona());
                }

		return target;
	}

	public String getUuidOrderId() {

		UUID uuidOrderId;
		do {
			uuidOrderId = UUID.randomUUID();
		} while (loadVentasByUuidOrderId(uuidOrderId.toString()) != null);
		return uuidOrderId.toString();
	}

	private static double parsePrice(String priceStr) {
		if (priceStr != null) {
			priceStr = priceStr.replace(',', '.');

			try {
				return Double.parseDouble(priceStr);
			} catch (NumberFormatException e) {
				LOGGER.error("Error al convertir el valor: " + priceStr);
				return 0.0;
			}
		}
		return 0.0;
	}

	private Calendar getInicioDelDia(Calendar inputCalendar) {
	    Calendar inicioDelDia = (Calendar) inputCalendar.clone();
	    inicioDelDia.set(Calendar.HOUR_OF_DAY, 0);
	    inicioDelDia.set(Calendar.MINUTE, 0);
	    inicioDelDia.set(Calendar.SECOND, 0);
	    inicioDelDia.set(Calendar.MILLISECOND, 0);
	    return inicioDelDia;
	}

	private Calendar getFinDelDia(Calendar inputCalendar) {
	    Calendar finDelDia = (Calendar) inputCalendar.clone();
	    finDelDia.set(Calendar.HOUR_OF_DAY, 23);
	    finDelDia.set(Calendar.MINUTE, 59);
	    finDelDia.set(Calendar.SECOND, 59);
	    finDelDia.set(Calendar.MILLISECOND, 999);
	    return finDelDia;
	}
}
