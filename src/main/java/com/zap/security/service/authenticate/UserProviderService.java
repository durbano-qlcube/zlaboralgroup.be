package com.zap.security.service.authenticate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zap.security.entity.authenticate.UserProviderEntity;
import com.zap.security.exception.authenticate.AuthUserServiceException;
import com.zap.security.vo.authenticate.UserProviderVo;

@Stateless
public class UserProviderService implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserProviderService.class.getName());

    @PersistenceContext(unitName = "zapLaboralgrouPool")
    EntityManager em;

    public UserProviderVo create(UserProviderVo vo) {
        String TAG = "[userProviderService - create]";
        if (vo == null) {
            throw new IllegalArgumentException(TAG + " >> 'userProviderVo' can not be null");
        }
        try {
            UserProviderEntity entity = toEntity(vo);
            entity.setFxCreation(Calendar.getInstance());
            em.persist(entity);
            return toVo(entity);
        } catch (Exception ex) {
            LOGGER.error(TAG + " - Error: ", ex.getMessage());
            throw new AuthUserServiceException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public List<UserProviderVo> loadByUserUuid(String uuidUser) {
        String TAG = "[userProviderService - loadByUserUuid uuid:" + uuidUser + "]";
        if (uuidUser == null) {
            throw new IllegalArgumentException(TAG + " >> 'userUuid' can not be null");
        }
        try {
            Query query = em.createNamedQuery("UserProviderEntity.loadByUserUuid");
            query.setParameter("uuidUser", uuidUser);
            List<UserProviderEntity> entityList = (List<UserProviderEntity>) query.getResultList();
            List<UserProviderVo> result = new ArrayList<UserProviderVo>();
            if (entityList != null && !entityList.isEmpty()) {
                for (UserProviderEntity e : entityList) {
                    result.add(toVo(e));
                }
            }
            return result;
        } catch (Exception ex) {
            LOGGER.error(TAG + " - Error: ", ex.getMessage());
            throw new AuthUserServiceException(ex);
        }
    }
    

    @SuppressWarnings("unchecked")
    public List<UserProviderVo> loadByProviderUuid(String uuidProvider) {
        String TAG = "[userProviderService - loadByProviderUuid uuid:" + uuidProvider + "]";
        if (uuidProvider == null) {
            throw new IllegalArgumentException(TAG + " >> 'uuidProvider' can not be null");
        }
        try {
            Query query = em.createNamedQuery("UserProviderEntity.loadByProviderUuid");
            query.setParameter("uuidProvider", uuidProvider);
            List<UserProviderEntity> entityList = (List<UserProviderEntity>) query.getResultList();
            List<UserProviderVo> result = new ArrayList<UserProviderVo>();
            if (entityList != null && !entityList.isEmpty()) {
                for (UserProviderEntity e : entityList) {
                    result.add(toVo(e));
                }
            }
            return result;
        } catch (Exception ex) {
            LOGGER.error(TAG + " - Error: ", ex.getMessage());
            throw new AuthUserServiceException(ex);
        }
    }
    
    public void delete(Long idUserProvider) {
        String TAG = "[userProviderService - delete idUserProvider:" + idUserProvider + "]";
        if (idUserProvider == null) {
            throw new IllegalArgumentException(TAG + " >> 'idUserProvider' can not be null");
        }
        try {
            UserProviderEntity entity = em.find(UserProviderEntity.class, idUserProvider);
            if (entity != null) {
                em.remove(entity);
            }
        } catch (Exception ex) {
            LOGGER.error(TAG + " - Error: ", ex.getMessage());
            throw new AuthUserServiceException(ex);
        }
    }

    private UserProviderVo toVo(UserProviderEntity entity) {
        if (entity == null) {
            return null;
        }
        UserProviderVo vo = new UserProviderVo();
        vo.setIdUserProvider(entity.getIdUserProvider());
        vo.setUuidUser(entity.getUuidUser());
        vo.setUuidProvider(entity.getUuidProvider());
        vo.setUsernameProvider(entity.getUsernameProvider());
        vo.setFxCreation(entity.getFxCreation());
        return vo;
    }

    private UserProviderEntity toEntity(UserProviderVo vo) {
        if (vo == null) {
            return null;
        }
        UserProviderEntity entity = new UserProviderEntity();
        entity.setIdUserProvider(vo.getIdUserProvider());
        entity.setUuidUser(vo.getUuidUser());
        entity.setUuidProvider(vo.getUuidProvider());
        entity.setUsernameProvider(vo.getUsernameProvider());
        entity.setFxCreation(vo.getFxCreation());
        return entity;
    }
}
