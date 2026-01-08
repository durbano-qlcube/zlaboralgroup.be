package com.zap.acquisition.service;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zap.acquisition.entity.AcquisitionEntity;
import com.zap.acquisition.exception.AcquisitionNotFoundException;
import com.zap.acquisition.exception.AcquisitionServiceException;
import com.zap.acquisition.exception.AdquisitionDuplicatePhoneException;
import com.zap.acquisition.vo.AcquisitionSearchRequestVo;
import com.zap.acquisition.vo.AcquisitionSearchResponseVo;
import com.zap.acquisition.vo.AcquisitionVo;
import com.zap.acquisition.vo.StatusAcquisitionEnum;
import com.zap.sales.exception.venta.VentaServiceException;
import com.zap.security.service.authenticate.AuthService;
import com.zap.security.service.authenticate.UserProviderService;
import com.zap.security.vo.authenticate.AuthUserVo;
import com.zap.security.vo.authenticate.UserProviderVo;
import com.zap.security.vo.enumerates.RoleEnum;



@Stateless
public class AcquisitionService implements Serializable
{

	private static final long serialVersionUID = -799844646085705211L;
	private static final Logger LOGGER = LoggerFactory.getLogger(AcquisitionService.class.getName());
	private SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@PersistenceContext(unitName = "zapLaboralgrouPool")
	EntityManager em;

	@Inject
	AuthService authService;

	@Inject
	UserProviderService userProviderService;

	public AcquisitionVo create(AcquisitionVo acquisitionVo) throws AdquisitionDuplicatePhoneException {
		String TAG = "[acquisitionService - create]";
		if (acquisitionVo == null)
			throw new IllegalArgumentException(TAG + " >> 'acquisitionVo' can not be null");

		try {

			AcquisitionEntity entity = toAcquisitionEntity(acquisitionVo);
			String fullname = this.crearFullname(acquisitionVo.getName(), acquisitionVo.getSurname(),
					acquisitionVo.getSurname2());
			
			if (fullname == null) {
			    entity.setFullname(fullname); 
			}

			entity.setFxCreation(Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid")));
			em.persist(entity);
			return toAcquisitionVo(entity);

		} catch (javax.persistence.PersistenceException ex) {
			LOGGER.error(TAG + " - Error:{} ", ex.getMessage());
			throw new AdquisitionDuplicatePhoneException(ex);

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error:{} ", ex.getMessage());
			throw new AcquisitionServiceException(ex);
		}
	}

	public void delete(Integer idacquisition) throws AcquisitionNotFoundException {
		String TAG = "[acquisitionService - delete idacquisition:" + idacquisition + "]";

		if (idacquisition == null)
			throw new IllegalArgumentException(TAG + " >> 'idacquisition' can not be null");

		try {
			AcquisitionEntity entity = em.find(AcquisitionEntity.class, idacquisition);
			if (entity == null)
				throw new AcquisitionNotFoundException();

			em.remove(entity);

		} catch (AcquisitionNotFoundException ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw ex;

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new AcquisitionServiceException(ex);
		}
	}

	public AcquisitionVo load(Integer idAcquisition) throws AcquisitionNotFoundException {

		String TAG = "[acquisitionService - load idAcquisition:" + idAcquisition + "]";
		if (idAcquisition == null)
			throw new IllegalArgumentException(TAG + " >> 'idacquisition' can not be null");

		try {

			AcquisitionEntity entity = em.find(AcquisitionEntity.class, idAcquisition);

			if (entity == null)
				return null;
			else
				return this.toAcquisitionVo(entity);

		} catch (javax.persistence.NoResultException ex) {
			return null;

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new AcquisitionServiceException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	public List<AcquisitionVo> loadAll(AuthUserVo user) {
		String TAG = "[acquisitionService - loadAll]";

		try {

			Query query = em.createNamedQuery("AcquisitionEntity.loadAll");
			List<AcquisitionEntity> entityList = (List<AcquisitionEntity>) query.getResultList();

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<AcquisitionEntity> cq = cb.createQuery(AcquisitionEntity.class);
			Root<AcquisitionEntity> acquisition = cq.from(AcquisitionEntity.class);

			List<Predicate> predicates = new ArrayList<>();

			if (RoleEnum.AGENTE.equals(user.getRole())) {
				predicates.add(cb.equal(acquisition.get("agenteUsername"), user.getUsername()));
			} else if (RoleEnum.CAPTADOR.equals(user.getRole())) {
				predicates.add(cb.equal(acquisition.get("usernameCaptador"), user.getUsername()));
			} else if (RoleEnum.CORDINADOR.equals(user.getRole())) {
				Predicate coordinadorPredicate = cb.equal(acquisition.get("coordinadorUserName"), user.getUsername());
				Predicate agentePredicate = cb.equal(acquisition.get("agenteUsername"), user.getUsername());
				predicates.add(cb.or(coordinadorPredicate, agentePredicate));
			} else if (RoleEnum.SUPERVISOR.equals(user.getRole())) {
				Predicate supervisorPredicate = cb.equal(acquisition.get("supervisorUserName"), user.getUsername());
				Predicate agentePredicate = cb.equal(acquisition.get("agenteUsername"), user.getUsername());
				predicates.add(cb.or(supervisorPredicate, agentePredicate));
			}

			cq.where(cb.and(predicates.toArray(new Predicate[0])));

			cq.orderBy(cb.desc(acquisition.get("idAcquisition")));

			List<AcquisitionEntity> filteredEntityList = em.createQuery(cq).getResultList();
			List<AcquisitionVo> result = new ArrayList<>();
			for (AcquisitionEntity source : filteredEntityList) {
				result.add(toAcquisitionVo(source));
			}

			return result;

		} catch (javax.persistence.NoResultException ex) {
			return null;
		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new AcquisitionServiceException(ex);
		}
	}

	public AcquisitionVo loadByPhone(String phone) {
		String TAG = "[AcquisitionService - loadByPhone phone:" + phone + "]";

		try {
			Query query = em.createNamedQuery("AcquisitionEntity.findByPhone");
			query.setParameter("phone", phone);

			AcquisitionEntity entity = (AcquisitionEntity) query.getSingleResult();
			return this.toAcquisitionVo(entity);

		} catch (javax.persistence.NoResultException ex) {
			return null;

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}", ex.getMessage());
			throw new AcquisitionServiceException(ex);
		}
	}

	public void update(AcquisitionVo acquisitionVo, Boolean copyIfNull)
			throws AcquisitionNotFoundException, AdquisitionDuplicatePhoneException {
		String TAG = "[AcquisitionService - update]";

		if (acquisitionVo == null)
			throw new IllegalArgumentException(TAG + " >> 'AcquisitionVo' can not be null");

		if (acquisitionVo.getIdAcquisition() == null)
			throw new IllegalArgumentException(TAG + " >> 'AcquisitionVo.getId()' can not be null");

		try {

			AcquisitionEntity entity = em.find(AcquisitionEntity.class, acquisitionVo.getIdAcquisition());

			if (entity == null)
				throw new AcquisitionNotFoundException();

			String fullname = this.crearFullname(acquisitionVo.getName(), acquisitionVo.getSurname(),
					acquisitionVo.getSurname2());
			
			if (fullname == null) {
				acquisitionVo.setFullname(fullname);
			}


			this.toAcquisitionEntity(acquisitionVo, entity, copyIfNull);
			entity.setFxModification(Calendar.getInstance());
			em.merge(entity);

		} catch (AcquisitionNotFoundException ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw ex;

		} catch (javax.persistence.PersistenceException ex) {
			LOGGER.error(TAG + " - Error:{} ", ex.getMessage());
			throw new AdquisitionDuplicatePhoneException(ex);

		} catch (Exception ex) {
			LOGGER.error("[AcquisitionService - updateAcquisition] - Error: ", ex);
			throw new AcquisitionServiceException(ex);

		}
	}

	private String crearFullname(String nombre, String apellido1, String apellido2) {
		return StringUtils.join(new String[] { nombre, apellido1, apellido2 }, " ");
	}

	public List<AcquisitionVo> loadByStatus(StatusAcquisitionEnum status) {
		String TAG = "[AcquisitionService - loadByStatus status:" + status.toString() + "]";

		try {
			Query query = em.createNamedQuery("AcquisitionEntity.loadByStatus");
			query.setParameter("status", status);
			// query.setMaxResults(maxResults);

			List<AcquisitionEntity> entityList = (List<AcquisitionEntity>) query.getResultList();

			return Optional.ofNullable(entityList).orElseGet(Collections::emptyList).stream()
					.map(element -> this.toAcquisitionVo(element)).collect(Collectors.toList());

		} catch (javax.persistence.NoResultException ex) {
			return new ArrayList<>();

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error:{} ", ex.getMessage());
			throw new AcquisitionServiceException(ex);

		}
	}

	public List<AcquisitionSearchResponseVo> search(AcquisitionSearchRequestVo request, AuthUserVo user) {
		String TAG = "[AcquisitionService - search]";

		try {

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<AcquisitionSearchResponseVo> cq = cb.createQuery(AcquisitionSearchResponseVo.class);
			Root<AcquisitionEntity> acquisition = cq.from(AcquisitionEntity.class);

                        cq.select(cb.construct(AcquisitionSearchResponseVo.class, acquisition.get("idAcquisition"),
                                        acquisition.get("fullname"), acquisition.get("phone"), acquisition.get("ocmLastCoding"),
                                        acquisition.get("ocmLastAgent"), acquisition.get("usernameCaptador"), acquisition.get("status"),
                                        acquisition.get("fxCreation"), acquisition.get("fxInsertion"), acquisition.get("ocmFxLastCall"),
                                        acquisition.get("ocmFxFirstCall"), acquisition.get("email"), acquisition.get("razonSocial"),
                                        acquisition.get("uuidProvider"), cb.nullLiteral(String.class)

                        ));

			List<Predicate> predicates = new ArrayList<>();
			if (request != null) {
				if (request.getFxInicio() != null) {
					predicates.add(cb.greaterThanOrEqualTo(acquisition.get("fxCreation"), request.getFxInicio()));
				}

				if (request.getFxFin() != null) {
					predicates.add(cb.lessThanOrEqualTo(acquisition.get("fxCreation"), request.getFxFin()));
				}

                                if (request.getFxLastCallInit() != null) {
                                        predicates.add(cb.greaterThanOrEqualTo(acquisition.get("ocmFxLastCall"), request.getFxLastCallInit()));
                                }

                                if (request.getFxLastCallFin() != null) {
                                        predicates.add(cb.lessThanOrEqualTo(acquisition.get("ocmFxLastCall"), request.getFxLastCallFin()));
                                }

				if (!StringUtils.isBlank(request.getFullname())) {
					predicates.add(cb.like(acquisition.get("fullname"), "%" + request.getFullname() + "%"));
				}

				if (!StringUtils.isBlank(request.getPhone())) {
					predicates.add(cb.like(acquisition.get("phone"), "%" + request.getPhone() + "%"));
				}
				if (!StringUtils.isBlank(request.getOcmLastCoding())) {
					predicates.add(cb.like(acquisition.get("ocmLastCoding"), "%" + request.getOcmLastCoding() + "%"));
				}
				if (!StringUtils.isBlank(request.getOcmLastAgent())) {
					predicates.add(cb.like(acquisition.get("ocmLastAgent"), "%" + request.getOcmLastAgent() + "%"));
				}

				if (!StringUtils.isBlank(request.getUuidCaptador())) {
					predicates.add(cb.like(acquisition.get("agenteUuid"), "%" + request.getUuidCaptador() + "%"));
				}

				if (!StringUtils.isBlank(request.getStatus())) {
					predicates.add(
							cb.equal(acquisition.get("status"), StatusAcquisitionEnum.valueOf(request.getStatus())));
				}

                                List<Long> parentCompanyFilters = new ArrayList<>();
                                if (request.getParentCompanyId() != null) {
                                        parentCompanyFilters.add(request.getParentCompanyId());
                                }
                                if (request.getParentCompanyIds() != null) {
                                        parentCompanyFilters.addAll(request.getParentCompanyIds());
                                }

                                parentCompanyFilters = parentCompanyFilters.stream().filter(Objects::nonNull).distinct()
                                                .collect(Collectors.toList());

                                if (!parentCompanyFilters.isEmpty()) {
                                        predicates.add(acquisition.get("parentCompanyId").in(parentCompanyFilters));
                                }

				List<String> providerUuids = buildProviderFilter(request);
				if (!providerUuids.isEmpty()) {
					predicates.add(acquisition.get("uuidProvider").in(providerUuids));
				}

				if (!RoleEnum.ADMINISTRADOR.equals(user.getRole()) && !RoleEnum.SUPER_ADMNISTRADOR.equals(user.getRole()) && !RoleEnum.BACKOFFICE.equals(user.getRole())) {
					List<String> allowedUuids = buildAllowedUuids(user);
					if (!allowedUuids.isEmpty()) {
						predicates.add(acquisition.get("agenteUuid").in(allowedUuids));
					}
				}

			}
			cq.where(cb.and(predicates.toArray(new Predicate[0])));

			cq.orderBy(cb.desc(acquisition.get("fxCreation")), cb.desc(acquisition.get("idAcquisition")));

			List<AcquisitionSearchResponseVo> result = em.createQuery(cq).getResultList();
			Map<String, String> providerNames = new HashMap<>();
			if (result != null) {
				for (AcquisitionSearchResponseVo acquisitionResult : result) {
					String uuidProvider = acquisitionResult.getUuidProvider();
					if (StringUtils.isBlank(uuidProvider) || providerNames.containsKey(uuidProvider)) {
						continue;
					}
					try {
						AuthUserVo provider = authService.loadByUuid(uuidProvider);
						if (provider != null) {
							String name = StringUtils.firstNonBlank( provider.getUsername());
							providerNames.put(uuidProvider, name);
						}
					} catch (Exception ex) {
						LOGGER.error("[AcquisitionService - search] - Error loading provider:{}", uuidProvider, ex);
					}
				}

				for (AcquisitionSearchResponseVo acquisitionResult : result) {
					if (!StringUtils.isBlank(acquisitionResult.getUuidProvider())) {
						acquisitionResult.setProviderName(providerNames.get(acquisitionResult.getUuidProvider()));
					}
				}
			}

			return result;

		} catch (javax.persistence.NoResultException ex) {
			return null;

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new VentaServiceException(ex);
		}
	}

	private List<String> buildProviderFilter(AcquisitionSearchRequestVo request) {
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
					providerUuids.addAll(
							providers.stream().map(AuthUserVo::getUuid).collect(Collectors.toList()));
				}
			}
		} catch (Exception ex) {
			LOGGER.error("[AcquisitionService - search] - Error loading provider:{}", request.getUuidProvider(), ex);
		}

		return distinctProviderUuids(providerUuids);
	}

	private List<String> distinctProviderUuids(List<String> providerUuids) {
		return providerUuids.stream().filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
	}

	private List<String> buildAllowedUuids(AuthUserVo user) {
		Set<String> uuids = new HashSet<>();

		collectUserAndProviders(user, uuids);

		if (RoleEnum.CORDINADOR.equals(user.getRole())) {
			List<AuthUserVo> agents = authService.loadByRole(RoleEnum.AGENTE, RoleEnum.CAPTADOR);
			if (agents != null) {
				for (AuthUserVo agent : agents) {
					if (user.getUuid().equals(agent.getUuidCordinador())) {
						collectUserAndProviders(agent, uuids);
					}
				}
			}
		} else if (RoleEnum.SUPERVISOR.equals(user.getRole())) {
			List<AuthUserVo> coordinators = authService.loadByRole(RoleEnum.CORDINADOR);
			List<AuthUserVo> agents = authService.loadByRole(RoleEnum.AGENTE, RoleEnum.CAPTADOR);

			if (coordinators != null) {
				for (AuthUserVo coord : coordinators) {
					if (user.getUuid().equals(coord.getUuidSupervisor())) {
						collectUserAndProviders(coord, uuids);

						if (agents != null) {
							for (AuthUserVo agent : agents) {
								if (coord.getUuid().equals(agent.getUuidCordinador())) {
									collectUserAndProviders(agent, uuids);
								}
							}
						}
					}
				}
			}
		} else if (RoleEnum.PARTNER.equals(user.getRole())) {
			List<AuthUserVo> providers = authService.loadProvidersByUserUuid(user.getUuid());
			if (providers != null) {
				for (AuthUserVo provider : providers) {
					uuids.add(provider.getUuid());
					try {
						List<UserProviderVo> rels = userProviderService.loadByProviderUuid(provider.getUuid());
						if (rels != null) {
							for (UserProviderVo rel : rels) {
								uuids.add(rel.getUuidUser());
							}
						}
					} catch (Exception e) {
						LOGGER.error("[AcquisitionService - buildAllowedUuids] - Error loading agents for provider:{}",
								provider.getUuid());
					}
				}
			}
		}

		return new ArrayList<>(uuids);
	}

	private void collectUserAndProviders(AuthUserVo user, Set<String> uuids) {
		if (user == null) {
			return;
		}

		if (!StringUtils.isBlank(user.getUuid())) {
			uuids.add(user.getUuid());
		}

		if (user.getUuidProviders() != null) {
			for (String providerUuid : user.getUuidProviders()) {
				if (!StringUtils.isBlank(providerUuid)) {
					uuids.add(providerUuid);
				}
			}
		}

		if (StringUtils.isBlank(user.getUuid())) {
			return;
		}

		try {
			List<AuthUserVo> providers = authService.loadProvidersByUserUuid(user.getUuid());
			if (providers != null) {
				for (AuthUserVo provider : providers) {
					if (!StringUtils.isBlank(provider.getUuid())) {
						uuids.add(provider.getUuid());
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("[AcquisitionService - collectUserAndProviders] - Error loading providers for user:{}",
					user.getUuid(), e);
		}
	}

//	public List<AcquisitionEntity> findAcquisitionByOriginId(EntityManager em, Integer originIdAdquision) {
//	    return em.createNamedQuery("AcquisitionEntity.findByOriginId", AcquisitionEntity.class)
//	            .setParameter("originId", originIdAdquision)
//	            .getResultList();
//	}
//	

	public List<AcquisitionVo> loadByStatus(List<StatusAcquisitionEnum> estados) {
		String TAG = "[AcquisitionService - loadByStatusList]";

		try {
			TypedQuery<AcquisitionEntity> query = em.createNamedQuery("AcquisitionEntity.loadByStatus2",
					AcquisitionEntity.class);
			query.setParameter("statuses", estados);
			// query.setMaxResults(maxResults);

			List<AcquisitionEntity> entityList = (List<AcquisitionEntity>) query.getResultList();

			return Optional.ofNullable(entityList).orElseGet(Collections::emptyList).stream()
					.map(element -> this.toAcquisitionVo(element)).collect(Collectors.toList());

		} catch (javax.persistence.NoResultException ex) {
			return new ArrayList<>();

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error:{} ", ex.getMessage());
			throw new AcquisitionServiceException(ex);

		}
	}

	public List<AcquisitionVo> loadGestdirectByStatus(List<StatusAcquisitionEnum> estados) {
		String TAG = "[AcquisitionService - loadGestdirectByStatus]";

		try {
			Calendar limitDateCal = Calendar.getInstance();
			limitDateCal.add(Calendar.DAY_OF_YEAR, -60);

			LOGGER.info("Status list: " + estados);
			LOGGER.info("Limit date: " + limitDateCal.getTime());

			TypedQuery<AcquisitionEntity> query = em.createNamedQuery("AcquisitionEntity.loadGestdirectByStatus",
					AcquisitionEntity.class);
			query.setParameter("statuses", estados);
			query.setParameter("limitDate", limitDateCal);

			List<AcquisitionEntity> entityList = query.getResultList();
			return Optional.ofNullable(entityList).orElseGet(Collections::emptyList).stream().map(this::toAcquisitionVo)
					.collect(Collectors.toList());

		} catch (javax.persistence.NoResultException ex) {
			return new ArrayList<>();

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			throw new AcquisitionServiceException(ex);
		}
	}

	private void toAcquisitionEntity(AcquisitionVo source, AcquisitionEntity target, Boolean copyIfNull) {
		if (copyIfNull || source.getIdAcquisition() != null)
			target.setIdAcquisition(source.getIdAcquisition());

		if (copyIfNull || source.getNombreComercial() != null)
			target.setNombreComercial(source.getNombreComercial());

		if (copyIfNull || source.getRazonSocial() != null)
			target.setRazonSocial(source.getRazonSocial());

		if (copyIfNull || source.getCif() != null)
			target.setCif(source.getCif());

		if (copyIfNull || source.getName() != null)
			target.setName(source.getName());

		if (copyIfNull || source.getSurname() != null)
			target.setSurname(source.getSurname());

		if (copyIfNull || source.getSurname2() != null)
			target.setSurname2(source.getSurname2());

		if (copyIfNull || source.getFullname() != null)
			target.setFullname(source.getFullname());

		if (copyIfNull || source.getEmail() != null)
			target.setEmail(source.getEmail());

		if (copyIfNull || source.getPhone() != null)
			target.setPhone(source.getPhone());

                if (copyIfNull || source.getDescription() != null)
                        target.setDescription(source.getDescription());

                if (copyIfNull || source.getCampaign() != null)
                        target.setCampaign(source.getCampaign());

                if (copyIfNull || source.getCampaignLeadId() != null)
                        target.setCampaignLeadId(source.getCampaignLeadId());

                if (copyIfNull || source.getCampaignAdsetName() != null)
                        target.setCampaignAdsetName(source.getCampaignAdsetName());

                if (copyIfNull || source.getCampaignAdName() != null)
                        target.setCampaignAdName(source.getCampaignAdName());

                if (copyIfNull || source.getCampaignName() != null)
                        target.setCampaignName(source.getCampaignName());

                if (copyIfNull || source.getCampaignFormName() != null)
                        target.setCampaignFormName(source.getCampaignFormName());

                if (copyIfNull || source.getCampaignPlatform() != null)
                        target.setCampaignPlatform(source.getCampaignPlatform());

                if (copyIfNull || source.getCampaignUrl() != null)
                        target.setCampaignUrl(source.getCampaignUrl());

                if (copyIfNull || source.getCampaignProduct() != null)
                        target.setCampaignProduct(source.getCampaignProduct());

                if (copyIfNull || source.getStatus() != null)
                        target.setStatus(source.getStatus());
		if (copyIfNull || source.getParentCompanyId() != null)
			target.setParentCompanyId(source.getParentCompanyId());

		if (copyIfNull || source.getFxScheduling() != null)
			target.setFxScheduling(source.getFxScheduling());

		if (copyIfNull || source.getFxSendToOcm() != null)
			target.setFxSendToOcm(source.getFxSendToOcm());

		if (copyIfNull || source.getOcmLastCoding() != null)
			target.setOcmLastCoding(source.getOcmLastCoding());

		if (copyIfNull || source.getOcmMotor() != null)
			target.setOcmMotor(source.getOcmMotor());

		if (copyIfNull || source.getOcmLastAgent() != null)
			target.setOcmLastAgent(source.getOcmLastAgent());

		if (copyIfNull || source.getOcmId() != null)
			target.setOcmId(source.getOcmId());

		if (copyIfNull || source.getCoordinadorUuid() != null)
			target.setCoordinadorUuid(source.getCoordinadorUuid());

		if (copyIfNull || source.getCoordinadorUserName() != null)
			target.setCoordinadorUserName(source.getCoordinadorUserName());

		if (copyIfNull || source.getSupervisorUuid() != null)
			target.setSupervisorUuid(source.getSupervisorUuid());

		if (copyIfNull || source.getSupervisorUserName() != null)
			target.setSupervisorUserName(source.getSupervisorUserName());

		if (copyIfNull || source.getAgenteUuid() != null)
			target.setAgenteUuid(source.getAgenteUuid());

		if (copyIfNull || source.getAgenteUsername() != null)
			target.setAgenteUsername(source.getAgenteUsername());

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

		if (copyIfNull || source.getOriginIdAdquision() != null)
			target.setOriginIdAdquision(source.getOriginIdAdquision());

		if (copyIfNull || source.getDateNextcall() != null)
			target.setOcmFxNextCall(source.getDateNextcall());
		
		if (copyIfNull || source.getDatefirstcall() != null)
			target.setOcmFxFirstCall(source.getDatefirstcall());
		
		if (copyIfNull || source.getEndResult() != null)
			target.setEndResult(source.getEndResult());

		
	}

	private AcquisitionVo toAcquisitionVo(AcquisitionEntity source) {
		AcquisitionVo target = new AcquisitionVo();

		target.setIdAcquisition(source.getIdAcquisition());
		target.setNombreComercial(source.getNombreComercial());
		target.setCif(source.getCif());
		target.setName(source.getName());
		target.setSurname(source.getSurname());
		target.setSurname2(source.getSurname2());
		target.setFullname(source.getFullname());
		target.setEmail(source.getEmail());
		target.setPhone(source.getPhone());
		target.setDescription(source.getDescription());
		target.setCampaign(source.getCampaign());
		target.setCampaignLeadId(source.getCampaignLeadId());
		target.setCampaignAdsetName(source.getCampaignAdsetName());
		target.setCampaignAdName(source.getCampaignAdName());
		target.setCampaignName(source.getCampaignName());
		target.setCampaignFormName(source.getCampaignFormName());
		target.setCampaignPlatform(source.getCampaignPlatform());
		target.setCampaignUrl(source.getCampaignUrl());
		target.setCampaignProduct(source.getCampaignProduct());
		target.setStatus(source.getStatus());
		target.setParentCompanyId(source.getParentCompanyId());
		target.setFxScheduling(source.getFxScheduling());
		target.setRazonSocial(source.getRazonSocial());

		target.setFxSendToOcm(source.getFxSendToOcm());
		target.setOcmLastCoding(source.getOcmLastCoding());
		target.setOcmMotor(source.getOcmMotor());
		target.setOcmLastAgent(source.getOcmLastAgent());
		target.setOcmId(source.getOcmId());

		target.setCoordinadorUuid(source.getCoordinadorUuid());
		target.setCoordinadorUserName(source.getCoordinadorUserName());
		target.setSupervisorUuid(source.getSupervisorUuid());
		target.setSupervisorUserName(source.getSupervisorUserName());
		target.setAgenteUuid(source.getAgenteUuid());
		target.setAgenteUsername(source.getAgenteUsername());

		target.setUuidProvider(source.getUuidProvider());

		target.setOrigin(source.getOrigin());
		target.setOriginUserUsername(source.getOriginUserUsername());
		target.setOriginUserUuid(source.getOriginUserUuid());
		target.setOriginGestoriaUuid(source.getOriginGestoriaUuid());
		target.setOriginIdAdquision(source.getOriginIdAdquision());

		target.setFxCreation(source.getFxCreation());
		target.setFxModification(source.getFxModification());

		target.setDateNextcall(source.getOcmFxNextCall());
		target.setDatefirstcall(source.getOcmFxFirstCall());
		target.setEndResultDesc(source.getOcmLastCoding());
		target.setEndResult(source.getEndResult());

		return target;
	}

	private AcquisitionEntity toAcquisitionEntity(AcquisitionVo source) {
		AcquisitionEntity target = new AcquisitionEntity();
		target.setIdAcquisition(source.getIdAcquisition());
		target.setNombreComercial(source.getNombreComercial());
		target.setCif(source.getCif());
		target.setName(source.getName());
		target.setSurname(source.getSurname());
		target.setSurname2(source.getSurname2());
		target.setFullname(source.getFullname());
		target.setEmail(source.getEmail());
		target.setPhone(source.getPhone());
		target.setDescription(source.getDescription());
		target.setCampaign(source.getCampaign());
		target.setCampaignLeadId(source.getCampaignLeadId());
		target.setCampaignAdsetName(source.getCampaignAdsetName());
		target.setCampaignAdName(source.getCampaignAdName());
		target.setCampaignName(source.getCampaignName());
		target.setCampaignFormName(source.getCampaignFormName());
		target.setCampaignPlatform(source.getCampaignPlatform());
		target.setCampaignUrl(source.getCampaignUrl());
		target.setCampaignProduct(source.getCampaignProduct());
		target.setStatus(source.getStatus());
		target.setParentCompanyId(source.getParentCompanyId());
		target.setFxScheduling(source.getFxScheduling());
		target.setRazonSocial(source.getRazonSocial());

		target.setFxSendToOcm(source.getFxSendToOcm());
		target.setOcmLastCoding(source.getOcmLastCoding());
		target.setOcmMotor(source.getOcmMotor());
		target.setOcmLastAgent(source.getOcmLastAgent());
		target.setOcmId(source.getOcmId());
		target.setOcmFxLastCall(source.getOcmFxLastCall());
		target.setFxInsertion(source.getFxInsertion());

		target.setCoordinadorUuid(source.getCoordinadorUuid());
		target.setCoordinadorUserName(source.getCoordinadorUserName());
		target.setSupervisorUuid(source.getSupervisorUuid());
		target.setSupervisorUserName(source.getSupervisorUserName());
		target.setAgenteUuid(source.getAgenteUuid());
		target.setAgenteUsername(source.getAgenteUsername());

		target.setUuidProvider(source.getUuidProvider());

		target.setOrigin(source.getOrigin());
		target.setOriginUserUsername(source.getOriginUserUsername());
		target.setOriginUserUuid(source.getOriginUserUuid());
		target.setOriginGestoriaUuid(source.getOriginGestoriaUuid());
		target.setOriginIdAdquision(source.getOriginIdAdquision());

		target.setOcmFxNextCall(source.getDateNextcall());
		target.setOcmFxFirstCall(source.getDatefirstcall());

		target.setEndResult(source.getEndResult());

		return target;
	}
}
