package com.zap.security.service.authenticate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zap.security.entity.authenticate.AuthUserEntity;
import com.zap.security.entity.authenticate.ProviderConfigEntity;
import com.zap.security.exception.authenticate.AuthUserServiceException;
import com.zap.security.vo.authenticate.ProviderConfigVo;
import com.zap.security.vo.enumerates.RoleEnum;

@Stateless
public class ProviderConfigService implements Serializable {

    private static final long serialVersionUID = 6879965080388083829L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderConfigService.class.getName());

    @PersistenceContext(unitName = "zapLaboralgrouPool")
    EntityManager em;

    public List<ProviderConfigVo> saveOrUpdate(Long userId, String userUuid, List<ProviderConfigVo> vos) {
        String TAG = "[providerConfigService - saveOrUpdate]";
        if (userId == null && (userUuid == null || "".equals(userUuid))) {
            throw new IllegalArgumentException(TAG + " >> 'userId' or 'userUuid' can not be null");
        }

        try {
            AuthUserEntity user = resolveUser(userId, userUuid);
            if (user == null) {
                throw new IllegalArgumentException(TAG + " >> 'user' can not be null");
            }
            if (!RoleEnum.PROVIDER.equals(user.getRole())) {
                throw new IllegalArgumentException(TAG + " >> Provider configuration is only allowed for provider users");
            }

            List<ProviderConfigEntity> existing = findEntitiesByUserId(user.getId());
            Map<Long, ProviderConfigEntity> existingById = new HashMap<>();
            Map<String, ProviderConfigEntity> existingByRange = new HashMap<>();
            if (existing != null) {
                for (ProviderConfigEntity entity : existing) {
                    if (entity.getIdProviderConfig() != null) {
                        existingById.put(entity.getIdProviderConfig(), entity);
                    }
                    existingByRange.put(buildUniqueKey(entity), entity);
                }
            }

            List<ProviderConfigVo> result = new ArrayList<>();
            List<ProviderConfigVo> createVos = new ArrayList<>();
            if (vos != null && !vos.isEmpty()) {
                for (ProviderConfigVo vo : vos) {
                    if (vo == null) {
                        continue;
                    }
                    if (vo.getIdProviderConfig() != null) {
                        ProviderConfigEntity entity = existingById.remove(vo.getIdProviderConfig());
                        if (entity == null) {
                            LOGGER.warn(TAG + " - Provider config with id {} not found for user {}", vo.getIdProviderConfig(),
                                    user.getId());
                            continue;
                        }

                        entity.setCostoLead(vo.getCostoLead());
                        entity.setInvalidacion(vo.getInvalidacion());
                        entity.setInvalidacionDesde(vo.getInvalidacionDesde());
                        entity.setInvalidacionHasta(vo.getInvalidacionHasta());
                        entity.setTotalInsertados(vo.getTotalInsertados());
                        entity.setFxModification(Calendar.getInstance());

                        entity = em.merge(entity);
                        result.add(toVo(entity));
                    } else {
                        ProviderConfigEntity entity = existingByRange.remove(buildUniqueKey(vo));
                        if (entity != null) {
                            if (entity.getIdProviderConfig() != null) {
                                existingById.remove(entity.getIdProviderConfig());
                            }
                            entity.setCostoLead(vo.getCostoLead());
                            entity.setInvalidacion(vo.getInvalidacion());
                            entity.setInvalidacionDesde(vo.getInvalidacionDesde());
                            entity.setInvalidacionHasta(vo.getInvalidacionHasta());
                            entity.setTotalInsertados(vo.getTotalInsertados());
                            entity.setFxModification(Calendar.getInstance());

                            entity = em.merge(entity);
                            result.add(toVo(entity));
                        } else {
                            createVos.add(vo);
                        }
                    }
                }
            }

            if (!existingById.isEmpty()) {
                for (ProviderConfigEntity entity : existingById.values()) {
                    ProviderConfigEntity managed = em.contains(entity) ? entity : em.merge(entity);
                    em.remove(managed);
                }
            }

            for (ProviderConfigVo vo : createVos) {
                ProviderConfigEntity entity = new ProviderConfigEntity();
                entity.setUser(user);
                entity.setCostoLead(vo.getCostoLead());
                entity.setInvalidacion(vo.getInvalidacion());
                entity.setInvalidacionDesde(vo.getInvalidacionDesde());
                entity.setInvalidacionHasta(vo.getInvalidacionHasta());
                entity.setTotalInsertados(vo.getTotalInsertados());
                entity.setFxCreation(Calendar.getInstance());
                entity.setFxModification(Calendar.getInstance());

                em.persist(entity);
                result.add(toVo(entity));
            }

            return result;
        } catch (AuthUserServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            LOGGER.error(TAG + " - Error: {}", ex.getMessage());
            throw new AuthUserServiceException(ex);
        }
    }

    public List<ProviderConfigVo> loadByUserUuid(String userUuid) {
        String TAG = "[providerConfigService - loadByUserUuid uuid:" + userUuid + "]";
        if (userUuid == null || "".equals(userUuid)) {
            throw new IllegalArgumentException(TAG + " >> 'userUuid' can not be null");
        }
        try {
            List<ProviderConfigEntity> entities = findEntitiesByUserUuid(userUuid);
            return toVoList(entities);
        } catch (AuthUserServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            LOGGER.error(TAG + " - Error: {}", ex.getMessage());
            throw new AuthUserServiceException(ex);
        }
    }

    public void deleteByUserUuid(String userUuid) {
        String TAG = "[providerConfigService - deleteByUserUuid uuid:" + userUuid + "]";
        if (userUuid == null || "".equals(userUuid)) {
            throw new IllegalArgumentException(TAG + " >> 'userUuid' can not be null");
        }
        try {
            deleteByUserUuidInternal(userUuid);
        } catch (AuthUserServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            LOGGER.error(TAG + " - Error: {}", ex.getMessage());
            throw new AuthUserServiceException(ex);
        }
    }

    private void deleteByUserUuidInternal(String userUuid) {
        List<ProviderConfigEntity> entities = findEntitiesByUserUuid(userUuid);
        if (entities != null && !entities.isEmpty()) {
            for (ProviderConfigEntity entity : entities) {
                ProviderConfigEntity managed = em.contains(entity) ? entity : em.merge(entity);
                em.remove(managed);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<ProviderConfigEntity> findEntitiesByUserId(Long userId) {
        Query query = em.createNamedQuery("ProviderConfigEntity.loadByUserId");
        query.setParameter("userId", userId);
        return (List<ProviderConfigEntity>) query.getResultList();
    }

    @SuppressWarnings("unchecked")
    private List<ProviderConfigEntity> findEntitiesByUserUuid(String userUuid) {
        Query query = em.createNamedQuery("ProviderConfigEntity.loadByUserUuid");
        query.setParameter("uuid", userUuid);
        return (List<ProviderConfigEntity>) query.getResultList();
    }

    private AuthUserEntity resolveUser(Long userId, String userUuid) {
        if (userId != null) {
            return em.find(AuthUserEntity.class, userId);
        }
        if (userUuid != null && !"".equals(userUuid)) {
            Query query = em.createNamedQuery("AuthUserEntity.loadByUuid");
            query.setParameter("uuid", userUuid);
            try {
                return (AuthUserEntity) query.getSingleResult();
            } catch (NoResultException ex) {
                return null;
            }
        }
        return null;
    }

    private ProviderConfigVo toVo(ProviderConfigEntity entity) {
        if (entity == null) {
            return null;
        }
        ProviderConfigVo vo = new ProviderConfigVo();
        vo.setIdProviderConfig(entity.getIdProviderConfig());
        if (entity.getUser() != null) {
            vo.setUserId(entity.getUser().getId());
            vo.setUserUuid(entity.getUser().getUuid());
        }
        vo.setCostoLead(entity.getCostoLead());
        vo.setInvalidacion(entity.getInvalidacion());
        vo.setInvalidacionDesde(entity.getInvalidacionDesde());
        vo.setInvalidacionHasta(entity.getInvalidacionHasta());
        vo.setTotalInsertados(entity.getTotalInsertados());
        vo.setFxCreation(entity.getFxCreation());
        vo.setFxModification(entity.getFxModification());
        return vo;
    }

    private List<ProviderConfigVo> toVoList(List<ProviderConfigEntity> entities) {
        List<ProviderConfigVo> result = new ArrayList<>();
        if (entities != null) {
            for (ProviderConfigEntity entity : entities) {
                ProviderConfigVo vo = toVo(entity);
                if (vo != null) {
                    result.add(vo);
                }
            }
        }
        return result;
    }

    private String buildUniqueKey(ProviderConfigVo vo) {
        if (vo == null) {
            return null;
        }
        return buildUniqueKey(vo.getInvalidacionDesde(), vo.getInvalidacionHasta());
    }

    private String buildUniqueKey(ProviderConfigEntity entity) {
        if (entity == null) {
            return null;
        }
        return buildUniqueKey(entity.getInvalidacionDesde(), entity.getInvalidacionHasta());
    }

    private String buildUniqueKey(Calendar invalidacionDesde, Calendar invalidacionHasta) {
        long desde = invalidacionDesde != null ? invalidacionDesde.getTimeInMillis() : -1L;
        long hasta = invalidacionHasta != null ? invalidacionHasta.getTimeInMillis() : -1L;
        return desde + "#" + hasta;
    }
}
