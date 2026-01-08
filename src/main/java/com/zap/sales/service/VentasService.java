package com.zap.sales.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
import javax.persistence.criteria.Expression;
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
import com.zap.sales.entity.VentaEntity;
import com.zap.sales.exception.doc.DocNotFoundException;
import com.zap.sales.exception.doc.DocServiceException;
import com.zap.sales.exception.venta.VentaNotFoundException;
import com.zap.sales.exception.venta.VentaServiceException;
import com.zap.sales.vo.empresa.EmpresaVo;
import com.zap.sales.vo.venta.StatusVentaEnum;
import com.zap.sales.vo.venta.VentaExtVo;
import com.zap.sales.vo.venta.VentaSearchRequestVo;
import com.zap.sales.vo.venta.VentaSearchResponseExtVo;
import com.zap.sales.vo.venta.VentaSearchResponseVo;
import com.zap.sales.vo.venta.VentaVo;
import com.zap.security.service.authenticate.AuthService;
import com.zap.security.vo.authenticate.AuthUserVo;
import com.zap.security.vo.enumerates.RoleEnum;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Stateless
public class VentasService implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(VentasService.class.getName());

	@PersistenceContext(unitName = "zapLaboralgrouPool")
	private EntityManager em;
	@Inject
	DocService docService;
	@Inject
    private AuthService authService;
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public VentaVo create(VentaVo ventasVo) {
		String TAG = "[VentasService - create]";
		if (ventasVo == null)
			throw new IllegalArgumentException(TAG + " >> 'ventasVo' can not be null");

		try {
			VentaEntity ventaEntity = this.toVentaEntity(ventasVo);
			ventaEntity.setUuId(this.getUUID());
			ventaEntity.setFxCreation(Calendar.getInstance());
			if (ventaEntity.getCharged() == null) {
				ventaEntity.setCharged(Boolean.FALSE);
			}
			if (ventaEntity.getPdteFirma() == null) {
				ventaEntity.setPdteFirma(Boolean.FALSE);
			}

			em.persist(ventaEntity);

			return toVentaVo(ventaEntity);
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

			TypedQuery<VentaEntity> ventaQuery = em.createNamedQuery("VentaEntity.findByUuid", VentaEntity.class)
					.setParameter("uuId", uuIdVenta);

			VentaEntity ventaEntity;
			try {
				ventaEntity = ventaQuery.getSingleResult();
			} catch (NoResultException e) {

				String errorMessage = "Venta no encontrada con uuId: " + uuIdVenta;
				LOGGER.error(TAG + " - " + errorMessage);
				throw new VentaNotFoundException(errorMessage);
			}

			FormacionEntity formacionEntity = ventaEntity.getFormacionEntity();
			if (formacionEntity != null) {

				for (AlumnoEntity alumno : formacionEntity.getAlumnoEntities()) {
					alumno.getFormacionEntities().remove(formacionEntity);
					em.merge(alumno);
				}
				em.remove(formacionEntity);

				for (AlumnoEntity alumno : formacionEntity.getAlumnoEntities()) {
					if (alumno.getFormacionEntities().isEmpty()) {
						em.remove(alumno);
					}
				}
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

	public VentaVo load(Integer idVenta) throws DocNotFoundException {

		String TAG = "[docService - load idDoc:" + idVenta + "]";
		if (idVenta == null)
			throw new IllegalArgumentException(TAG + " >> 'iddoc' can not be null");

		try {

			VentaEntity entity = em.find(VentaEntity.class, idVenta);

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
	public List<VentaVo> loadAll() {
		String TAG = "[VentaService - loadAll]";

		try {
			Query query = em.createNamedQuery("VentaEntity.findAll");

			List<VentaEntity> entityList = (List<VentaEntity>) query.getResultList();
			List<VentaVo> result = new ArrayList<>();

			if (entityList != null && !entityList.isEmpty()) {
				for (VentaEntity source : entityList) {
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
	
	public List<VentaExtVo> loadAllExt() {
	    List<VentaExtVo> lista = new ArrayList<>();

	    List<VentaEntity> entityList = em.createNamedQuery("VentaEntity.findAll").getResultList();

	    for (VentaEntity entity : entityList) {
	        VentaExtVo ext = new VentaExtVo();
	        ext.setVenta(toVentaVo(entity));
	        
	        if (entity.getEmpresaEntity() != null) {
	            EmpresaVo empresaVo = new EmpresaVo();
	            empresaVo.setIdEmpresa(entity.getEmpresaEntity().getIdEmpresa());
	            empresaVo.setRazonSocial(entity.getEmpresaEntity().getRazonSocial());
	            empresaVo.setCif(entity.getEmpresaEntity().getCif());
	            empresaVo.setTelefonoContacto(entity.getEmpresaEntity().getTelefonoContacto());

	            ext.setEmpresa(empresaVo);
	        }

	        lista.add(ext);
	    }

	    return lista;
	}


	public VentaVo loadVentasByUuid(String uuId) {

		String TAG = "[VentaService - loadVentasByUuid]";

		try {
			TypedQuery<VentaEntity> query = em.createNamedQuery("VentaEntity.findByUuid", VentaEntity.class);
			query.setParameter("uuId", uuId);

			VentaEntity entity = (VentaEntity) query.getSingleResult();

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

	public VentaVo loadVentasByUuidOrderId(String stripeUuidOrderId) {

		String TAG = "[VentaService - loadVentasByUuid]";

		try {
			TypedQuery<VentaEntity> query = em.createNamedQuery("VentaEntity.findByUuidOrderId", VentaEntity.class);
			query.setParameter("stripeUuidOrderId", stripeUuidOrderId);

			VentaEntity entity = (VentaEntity) query.getSingleResult();

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

	public List<VentaVo> loadVentasByIdEmpresa(Integer idEmpresa) {

		String TAG = "[VentaService - loadVentasByIdEmpresa]";

		try {
			TypedQuery<VentaEntity> query = em.createNamedQuery("VentaEntity.findByidEmpresa", VentaEntity.class);
			query.setParameter("idEmpresa", idEmpresa);

			List<VentaEntity> entityList = (List<VentaEntity>) query.getResultList();

			return Optional.ofNullable(entityList).orElseGet(Collections::emptyList).stream()
					.map(element -> this.toVentaVo(element)).collect(Collectors.toList());

		} catch (javax.persistence.NoResultException ex) {
			return new ArrayList<>();

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new DocServiceException(ex);
		}

	}

	public VentaVo loadByuuid(String uuId) {
		String TAG = "[VentaService - load uuId:" + uuId + "]";
		if (uuId == null)
			throw new IllegalArgumentException(TAG + " >> 'uuId' can not be null");

		try {

			TypedQuery<VentaEntity> query = em.createNamedQuery("VentaEntity.findByUuid", VentaEntity.class);
			query.setParameter("uuId", uuId);

			VentaEntity entity = query.getSingleResult();

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

	public VentaSearchResponseExtVo search(VentaSearchRequestVo request, AuthUserVo user, Integer pageLimit,
			Integer pageNumber) {
		String TAG = "[VentasService - search]";

		long start = System.currentTimeMillis();
		LOGGER.info(TAG + " - Inicio de la búsqueda de ventas");

		List<VentaSearchResponseVo> paginatedResult = this.searchInternal(request, user, pageLimit, pageNumber);
		attachProviderNames(paginatedResult);

		long end = System.currentTimeMillis();
		LOGGER.info(TAG + " - Búsqueda principal finalizada en " + (end - start) + " ms");

		List<VentaSearchResponseVo> resultSinPaginacion = this.searchInternal(request, user, null, null);
		Long totalRecords = Long.valueOf(resultSinPaginacion.size());
		LOGGER.info("Total de registros: " + totalRecords);

		long pdteDocCount = resultSinPaginacion.stream().filter(venta -> venta.getStatus() == StatusVentaEnum.PDTE_DOC)
				.count();
		long pdteInicioCursoCount = resultSinPaginacion.stream()
				.filter(venta -> venta.getStatus() == StatusVentaEnum.PDTE_INICIO_CURSO).count();
		long ejecucionCursoCount = resultSinPaginacion.stream()
				.filter(venta -> venta.getStatus() == StatusVentaEnum.EJECUCION_CURSO).count();
		long cursoFinalizadoCount = resultSinPaginacion.stream()
				.filter(venta -> venta.getStatus() == StatusVentaEnum.CURSO_FINALIZADO).count();
		long notificadoFundaeCount = resultSinPaginacion.stream()
				.filter(venta -> venta.getStatus() == StatusVentaEnum.NOTIFICADO_FUNDAE).count();
		long canceladoCount = resultSinPaginacion.stream()
				.filter(venta -> venta.getStatus() == StatusVentaEnum.CANCELADO).count();
		long cobradoCount = resultSinPaginacion.stream().filter(venta -> Boolean.TRUE.equals(venta.getCharged()))
				.count();
		long pdteFirmaCount = resultSinPaginacion.stream().filter(venta -> Boolean.TRUE.equals(venta.getPdteFirma()))
				.count();
		long carteraCount = resultSinPaginacion.stream().filter(venta -> "CARTERA".equals(venta.getEmpresaStatus()))
				.count();

		double totalCharged = resultSinPaginacion.stream().filter(venta -> Boolean.TRUE.equals(venta.getCharged()))
				.mapToDouble(venta -> venta.getPrice() != null ? parsePrice(venta.getPrice().toString()) : 0.0).sum();

		BigDecimal totalChargedBigDecimal = BigDecimal.valueOf(totalCharged).setScale(2, RoundingMode.HALF_UP);

//		double totalPendingCharged = resultSinPaginacion.stream()
//				.filter(venta -> !Boolean.TRUE.equals(venta.getCharged()))
//				.mapToDouble(venta -> venta.getPrice() != null ? parsePrice(venta.getPrice().toString()) : 0.0).sum();
		
		double totalPendingCharged = resultSinPaginacion.stream()
		        .filter(venta -> !Boolean.TRUE.equals(venta.getCharged()) && venta.getStatus() != StatusVentaEnum.CANCELADO)
		        .mapToDouble(venta -> venta.getPrice() != null ? parsePrice(venta.getPrice().toString()) : 0.0)
		        .sum();

		BigDecimal totalPendingChargedBigDecimal = BigDecimal.valueOf(totalPendingCharged).setScale(2,
				RoundingMode.HALF_UP);

		totalCharged = totalChargedBigDecimal.doubleValue();
		totalPendingCharged = totalPendingChargedBigDecimal.doubleValue();

		VentaSearchResponseExtVo result = new VentaSearchResponseExtVo();
		result.setData(paginatedResult);
		result.setPDTE_DOC(pdteDocCount);
		result.setPDTE_INICIO_CURSO(pdteInicioCursoCount);
		result.setEJECUCION_CURSO(ejecucionCursoCount);
		result.setCURSO_FINALIZADO(cursoFinalizadoCount);
		result.setNOTIFICADO_FUNDAE(notificadoFundaeCount);
		result.setCANCELADO(canceladoCount);
		result.setCOBRADO(cobradoCount);
		result.setPDTE_FIRMA(pdteFirmaCount);
		result.setCARTERA(carteraCount);
		result.setTOTAL_RECORDS(totalRecords);
		result.setTOTAL_CHARGED(totalCharged);
		result.setTOTAL_PENDING_CHARGED(totalPendingCharged);

		if (pageLimit != null && pageNumber != null) {
			long totalPages = (long) Math.ceil((double) totalRecords / pageLimit);
			result.setTOTAL_PAGES(totalPages);
		}

		return result;
	}

	private void attachProviderNames(List<VentaSearchResponseVo> results) {
		if (results == null || results.isEmpty()) {
			return;
		}

		Map<String, String> providerNames = new HashMap<>();
		for (VentaSearchResponseVo ventaResult : results) {
			String uuidProvider = ventaResult.getUuidProvider();
			if (StringUtils.isBlank(uuidProvider) || providerNames.containsKey(uuidProvider)) {
				continue;
			}
			try {
				AuthUserVo provider = authService.loadByUuid(uuidProvider);
				if (provider != null) {
					String name = StringUtils.firstNonBlank(provider.getUsername(), provider.getName(),
							provider.getFullname(), provider.getEmail());
					providerNames.put(uuidProvider, name);
				}
			} catch (Exception ex) {
				LOGGER.error("[VentasService - search] - Error loading provider:{}", uuidProvider, ex);
			}
		}

		for (VentaSearchResponseVo ventaResult : results) {
			if (!StringUtils.isBlank(ventaResult.getUuidProvider())) {
				ventaResult.setProviderName(providerNames.get(ventaResult.getUuidProvider()));
			}
		}
	}

	private List<String> buildProviderFilter(VentaSearchRequestVo request) {
		List<String> providerUuids = new ArrayList<>();

		if (request == null) {
			return providerUuids;
		}

		if (!StringUtils.isBlank(request.getUuidSubProvider())) {
			providerUuids.add(request.getUuidSubProvider());
			return distinctProviderUuids(providerUuids);
		}

		if (StringUtils.isBlank(request.getUuidProvider())) {
			return providerUuids;
		}

		providerUuids.add(request.getUuidProvider());

		try {
			AuthUserVo provider = authService.loadByUuid(request.getUuidProvider());
			if (provider != null && RoleEnum.PROVIDER.equals(provider.getRole())) {
				List<AuthUserVo> providers = authService.loadSubProvidersByUsernameLike(provider.getUsername());
				if (providers != null && !providers.isEmpty()) {
					providerUuids
							.addAll(providers.stream().map(AuthUserVo::getUuid).collect(Collectors.toList()));
				}
			}
		} catch (Exception ex) {
			LOGGER.error("[VentasService - buildProviderFilter] - Error loading provider:{}", request.getUuidProvider(),
					ex);
		}

		return distinctProviderUuids(providerUuids);
	}

	private List<String> distinctProviderUuids(List<String> providerUuids) {
		return providerUuids.stream().filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
	}

	private List<String> buildProviderUuidsForProvider(AuthUserVo provider) {
		List<String> providerUuids = new ArrayList<>();
		if (provider == null) {
			return providerUuids;
		}

		if (!StringUtils.isBlank(provider.getUuid())) {
			providerUuids.add(provider.getUuid());
		}

		if (!RoleEnum.PROVIDER.equals(provider.getRole())) {
			return distinctProviderUuids(providerUuids);
		}

		try {
			List<AuthUserVo> subProviders = authService.loadSubProvidersByUsernameLike(provider.getUsername());
			if (subProviders != null && !subProviders.isEmpty()) {
				providerUuids.addAll(subProviders.stream().map(AuthUserVo::getUuid).collect(Collectors.toList()));
			}
		} catch (Exception ex) {
			LOGGER.error("[VentasService - buildProviderUuidsForProvider] - Error loading provider:{}",
					provider.getUuid(), ex);
		}

		return distinctProviderUuids(providerUuids);
	}

	private List<VentaSearchResponseVo> searchInternal(VentaSearchRequestVo request, AuthUserVo user, Integer pageLimit,
			Integer pageNumber) {
		String TAG = "[VentasService - search]";
		CriteriaBuilder cb = em.getCriteriaBuilder();
		long start = System.currentTimeMillis();
		LOGGER.info(TAG + " - Inicio de la búsqueda de ventas");

		CriteriaQuery<VentaSearchResponseVo> cq = cb.createQuery(VentaSearchResponseVo.class);
		Root<VentaEntity> venta = cq.from(VentaEntity.class);
		Join<VentaEntity, EmpresaEntity> empresa = venta.join("empresaEntity", JoinType.LEFT);
		Join<VentaEntity, FormacionEntity> formacion = venta.join("formacionEntity", JoinType.LEFT);

		cq.select(cb.construct(VentaSearchResponseVo.class, venta.get("uuId"), venta.get("fxVenta"), empresa.get("cif"),
				empresa.get("nombreComercial"), empresa.get("estado"), formacion.get("nombre"), venta.get("status"),
				venta.get("charged"), venta.get("pdteFirma"), venta.get("coordinadorUserName"),
				venta.get("usernameAgente"), venta.get("originUserUsername"), formacion.get("fechaInicio"),
				formacion.get("fechaFin"), venta.get("price"), venta.get("pdteCobroInicioCurso"),
				venta.get("pdteCobroFinCurso"), venta.get("uuidProvider")));

		List<Predicate> predicates = new ArrayList<>();

		if (RoleEnum.AGENTE.toString().equals(user.getRole().toString())) {
			predicates.add(cb.equal(venta.get("uuIdAgente"), user.getUuid()));
		} else if (RoleEnum.CORDINADOR.toString().equals(user.getRole().toString())) {
			predicates.add(cb.equal(venta.get("uuIdCoordinador"), user.getUuid()));
		} else if (RoleEnum.SUPERVISOR.toString().equals(user.getRole().toString())) {
			predicates.add(cb.equal(venta.get("uuIdSupervisor"), user.getUuid()));
		} else if (RoleEnum.PROVIDER.toString().equals(user.getRole().toString())) {
			List<String> providerUuids = buildProviderUuidsForProvider(user);
			if (!providerUuids.isEmpty()) {
				predicates.add(venta.get("uuidProvider").in(providerUuids));
			} else {
				predicates.add(cb.disjunction());
			}
		} else if (RoleEnum.PARTNER.toString().equals(user.getRole().toString())) {
			List<AuthUserVo> providers = authService.loadProvidersByUserUuid(user.getUuid());
			List<String> ids = new ArrayList<>();
			if (providers != null) {
				for (AuthUserVo p : providers) {
					ids.add(p.getUuid());
				}
			}
			if (!ids.isEmpty()) {
				predicates.add(venta.get("uuidProvider").in(ids));
			} else {
				predicates.add(cb.disjunction());
			}
		}

		List<StatusVentaEnum> estados = new ArrayList<>();
		if (request.getVpdteDoc())
			estados.add(StatusVentaEnum.PDTE_DOC);
		if (request.getVpdteInicioCurso())
			estados.add(StatusVentaEnum.PDTE_INICIO_CURSO);
		if (request.getVejecucionCurso())
			estados.add(StatusVentaEnum.EJECUCION_CURSO);
		if (request.getVcursoFinalizado())
			estados.add(StatusVentaEnum.CURSO_FINALIZADO);
		if (request.getVnotificacionFundae())
			estados.add(StatusVentaEnum.NOTIFICADO_FUNDAE);
		if (request.getVcancelado())
			estados.add(StatusVentaEnum.CANCELADO);

		if (!estados.isEmpty()) {
			predicates.add(venta.get("status").in(estados));
		}

		List<String> providerUuids = buildProviderFilter(request);
		if (!providerUuids.isEmpty()) {
			predicates.add(venta.get("uuidProvider").in(providerUuids));
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
		} else if (request.getFxVentaInit() != null) {
			predicates.add(cb.greaterThanOrEqualTo(venta.get("fxVenta"), request.getFxVentaInit()));
		} else if (request.getFxVentaFin() != null) {
			predicates.add(cb.lessThanOrEqualTo(venta.get("fxVenta"), request.getFxVentaFin()));
		}

		if (request.getFxInitCurso() != null && request.getFxFinCurso() != null) {

			Calendar inicioDelDiaCurso = getInicioDelDia(request.getFxInitCurso());
			Calendar finDelDiaCurso = getFinDelDia(request.getFxFinCurso());

			LOGGER.info("Fecha y hora inicio del curso: " + inicioDelDiaCurso.getTime());
			LOGGER.info("Fecha y hora fin del curso: " + finDelDiaCurso.getTime());

			predicates.add(cb.greaterThanOrEqualTo(formacion.get("fechaInicio"), inicioDelDiaCurso));
			predicates.add(cb.lessThanOrEqualTo(formacion.get("fechaFin"), finDelDiaCurso));

		} else if (request.getFxInitCurso() != null) {

			Calendar inicioDelDiaCurso = getInicioDelDia(request.getFxInitCurso());
			predicates.add(cb.equal(formacion.get("fechaInicio"), inicioDelDiaCurso));

		} else if (request.getFxFinCurso() != null) {

			Calendar finDelDiaCurso = getInicioDelDia(request.getFxFinCurso());
			predicates.add(cb.equal(formacion.get("fechaFin"), finDelDiaCurso));

		}

		if (Boolean.TRUE.equals(request.getVcartera())) {
			predicates.add(cb.equal(empresa.get("estado"), "CARTERA"));
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
		if (request.getVcobrado() != null) {
			predicates.add(cb.equal(venta.get("charged"), request.getVcobrado()));
		}

		if (request.getCif() != null && !request.getCif().trim().isEmpty()) {
			predicates.add(cb.like(empresa.get("cif"), "%" + request.getCif().trim() + "%"));
		}
		if (request.getNombreComercial() != null && !request.getNombreComercial().trim().isEmpty()) {
			String nombreComercial = request.getNombreComercial().trim().toLowerCase();
			Expression<String> nombreComercialExpr = cb.lower(empresa.get("nombreComercial"));
			Expression<String> razonSocialExpr = cb.lower(empresa.get("razonSocial"));
			Predicate nombreComercialPredicate = cb.like(nombreComercialExpr, "%" + nombreComercial + "%");
			Predicate razonSocialPredicate = cb.like(razonSocialExpr, "%" + nombreComercial + "%");
			predicates.add(cb.or(nombreComercialPredicate, razonSocialPredicate));
		}

		if (!StringUtils.isBlank(request.getPhoneContacto())) {
			Predicate phoneContactoVenta = cb.like(empresa.get("repreLegalTelefono"),
					"%" + request.getPhoneContacto() + "%");
			Predicate phoneContactoPredicate = cb.like(empresa.get("telefonoContacto"),
					"%" + request.getPhoneContacto() + "%");
			Predicate phoneContacto2Predicate = cb.like(empresa.get("asesorTelefono"),
					"%" + request.getPhoneContacto() + "%");
			predicates.add(cb.or(phoneContactoVenta, phoneContactoPredicate, phoneContacto2Predicate));
		}

		cq.where(predicates.toArray(new Predicate[0]));
		cq.orderBy(cb.desc(venta.get("fxVenta")));

		TypedQuery<VentaSearchResponseVo> query = em.createQuery(cq);
		if (pageNumber != null && pageLimit != null) {
			query.setFirstResult(pageNumber * pageLimit);
			query.setMaxResults(pageLimit);
		}

		List<VentaSearchResponseVo> result = query.getResultList();

		long end = System.currentTimeMillis();
		LOGGER.info(TAG + " - Búsqueda principal finalizada en " + (end - start) + " ms");

		return result;
	}

	@Transactional
	public VentaVo update(VentaVo ventaVo, Boolean copyIfNull) throws VentaNotFoundException {
		String TAG = "[VentasService - update]";
		if (ventaVo == null || ventaVo.getUuId() == null)
			throw new IllegalArgumentException(TAG + " >> 'VentaVo' or 'VentaVo.getUuId()' can not be null");

		try {
			VentaEntity entity = em.find(VentaEntity.class, ventaVo.getIdVenta());
			if (entity == null)
				throw new VentaNotFoundException();
			toVentaEntity(ventaVo, entity, copyIfNull);
			VentaEntity updatedEntity = em.merge(entity);
			return toVentaVo(updatedEntity);
		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new VentaServiceException(ex);
		}
	}

	public List<VentaVo> loadVentasByStatus(List<StatusVentaEnum> statuses) {

		String TAG = "[VentaService - loadVentasByStatus]";

		try {
			TypedQuery<VentaEntity> query = em.createNamedQuery("VentaEntity.loadByStatuses", VentaEntity.class);
			query.setParameter("statuses", statuses);

			List<VentaEntity> entityList = (List<VentaEntity>) query.getResultList();

			return Optional.ofNullable(entityList).orElseGet(Collections::emptyList).stream()
					.map(element -> this.toVentaVo(element)).collect(Collectors.toList());

		} catch (javax.persistence.NoResultException ex) {
			return new ArrayList<>();

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new DocServiceException(ex);
		}

	}

	public List<VentaVo> loadGestdirectByStatus(List<StatusVentaEnum> statuses) {
		String TAG = "[VentaService - loadGestdirectByStatus]";

		try {

			Calendar limitDateCal = Calendar.getInstance();
			limitDateCal.add(Calendar.DAY_OF_YEAR, -60);

			LOGGER.info("Status list: " + statuses);
			LOGGER.info("Limit date: " + limitDateCal.getTime());

			TypedQuery<VentaEntity> query = em.createNamedQuery("VentaEntity.loadGestdirectByStatus",
					VentaEntity.class);
			query.setParameter("statuses", statuses);
			query.setParameter("limitDate", limitDateCal);

			List<VentaEntity> entityList = query.getResultList();
			return Optional.ofNullable(entityList).orElseGet(Collections::emptyList).stream().map(this::toVentaVo)
					.collect(Collectors.toList());

		} catch (javax.persistence.NoResultException ex) {
			return new ArrayList<>();

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new DocServiceException(ex);
		}
	}

	private VentaEntity toVentaEntity(VentaVo source) {
		VentaEntity target = new VentaEntity();
		target.setIdVenta(source.getIdVenta());
		target.setIdVentaGestdirect(source.getIdVentaGestdirect());
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
		target.setUuidProvider(source.getUuidProvider());

		target.setOrigin(source.getOrigin());
		target.setOriginUserUsername(source.getOriginUserUsername());
		target.setOriginUserUuid(source.getOriginUserUuid());
		target.setOriginGestoriaUuid(source.getOriginGestoriaUuid());
		target.setOriginIdVenta(source.getOriginIdVenta());
		target.setObservaciones(source.getObservaciones());
		target.setStripePaymentLink(source.getStripePaymentLink());
		target.setStripePaymentStatus(source.getStripePaymentStatus());
		target.setPdteCobroInicioCurso(source.getPdteCobroInicioCurso());
		target.setPdteCobroFinCurso(source.getPdteCobroFinCurso());
		target.setParentCompanyId(source.getParentCompanyId());
		
		EmpresaEntity empresaEntity = null;
		if (source.getIdEmpresa() != null) {
			empresaEntity = new EmpresaEntity();
			empresaEntity.setIdEmpresa(source.getIdEmpresa());
			target.setEmpresaEntity(empresaEntity);
		}

		// FormacionEntity formacionEntity = new FormacionEntity();
		// formacionEntity.setVentaEntity(target);

		// if (empresaEntity != null)
		// formacionEntity.setEmpresaEntity(empresaEntity);

		// target.setFormacionEntity(formacionEntity);
		if (source.getIdFormacion() != null) {
			FormacionEntity formacion = new FormacionEntity();
			formacion.setIdFormacion(source.getIdFormacion());
			target.setFormacionEntity(formacion);
		}

		return target;
	}

	private void toVentaEntity(VentaVo source, VentaEntity target, Boolean copyIfNull) {

		if (copyIfNull || source.getIdVentaGestdirect() != null)
			target.setIdVentaGestdirect(source.getIdVentaGestdirect());

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

		if (copyIfNull || source.getOrigin() != null)
			target.setOrigin(source.getOrigin());

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
		if (copyIfNull || source.getUuidProvider() != null)
			target.setUuidProvider(source.getUuidProvider());

		if (copyIfNull || source.getOrigin() != null)
			target.setOrigin(source.getOrigin());

		if (copyIfNull || source.getOriginUserUsername() != null)
			target.setOriginUserUsername(source.getOriginUserUsername());

		if (copyIfNull || source.getOriginUserUuid() != null)
			target.setOriginUserUuid(source.getOriginUserUuid());

		if (copyIfNull || source.getOriginGestoriaUuid() != null)
			target.setOriginGestoriaUuid(source.getOriginGestoriaUuid());

		if (copyIfNull || source.getOriginIdVenta() != null)
			target.setOriginIdVenta(source.getOriginIdVenta());

		if (copyIfNull || source.getObservaciones() != null)
			target.setObservaciones(source.getObservaciones());

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

		if (copyIfNull || source.getParentCompanyId() != null)
			target.setParentCompanyId(source.getParentCompanyId());
		
	}

	private VentaVo toVentaVo(VentaEntity source) {
		VentaVo target = new VentaVo();
		target.setIdVenta(source.getIdVenta());
		target.setIdVentaGestdirect(source.getIdVentaGestdirect());
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
		target.setUuidProvider(source.getUuidProvider());

		target.setOrigin(source.getOrigin());
		target.setOriginUserUsername(source.getOriginUserUsername());
		target.setOriginUserUuid(source.getOriginUserUuid());
		target.setOriginGestoriaUuid(source.getOriginGestoriaUuid());
		target.setOriginIdVenta(source.getOriginIdVenta());
		target.setObservaciones(source.getObservaciones());
		target.setStripePaymentLink(source.getStripePaymentLink());
		target.setStripePaymentStatus(source.getStripePaymentStatus());
		target.setStripePaymentId(source.getStripePaymentId());
		target.setPdteCobroInicioCurso(source.getPdteCobroInicioCurso());
		target.setPdteCobroFinCurso(source.getPdteCobroFinCurso());
		target.setParentCompanyId(source.getParentCompanyId());


		if (source.getEmpresaEntity() != null) {
			target.setIdEmpresa(source.getEmpresaEntity().getIdEmpresa());
		}

		if (source.getFormacionEntity() != null) {
			target.setIdFormacion(source.getFormacionEntity().getIdFormacion());
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
