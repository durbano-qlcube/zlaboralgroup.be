package com.zap.security.service.parentcompany;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.zap.security.entity.authenticate.AuthUserEntity;
import com.zap.security.entity.parentcompany.ParentCompanyEntity;
import com.zap.security.entity.parentcompany.UserParentCompanyEntity;
import com.zap.security.entity.parentcompany.UserParentCompanyId;
import com.zap.security.vo.parentcompany.ParentCompanyVo;

@Stateless
public class ParentCompanyService implements Serializable {

    private static final long serialVersionUID = 3309347035739603547L;

    @PersistenceContext(unitName = "zapLaboralgrouPool")
    EntityManager em;

    public ParentCompanyVo create(ParentCompanyVo parentCompanyVo) {
        if (parentCompanyVo == null) {
            throw new IllegalArgumentException("ParentCompanyVo can not be null");
        }
        if (parentCompanyVo.getName() == null || parentCompanyVo.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Parent company name can not be null or empty");
        }

        ParentCompanyEntity entity = new ParentCompanyEntity();
        entity.setName(parentCompanyVo.getName().trim());
        em.persist(entity);
        em.flush();

        parentCompanyVo.setId(entity.getId());
        return parentCompanyVo;
    }

    @SuppressWarnings("unchecked")
    public List<ParentCompanyVo> findAll() {
        Query query = em.createNamedQuery("ParentCompanyEntity.findAll");
        List<ParentCompanyEntity> result = query.getResultList();
        List<ParentCompanyVo> parentCompanies = new ArrayList<ParentCompanyVo>();
        if (result != null && !result.isEmpty()) {
            for (ParentCompanyEntity entity : result) {
                parentCompanies.add(toVo(entity));
            }
        }
        return parentCompanies;
    }

    @SuppressWarnings("unchecked")
    public List<ParentCompanyVo> findByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId can not be null");
        }
        Query query = em.createNamedQuery("UserParentCompanyEntity.findParentCompaniesByUserId");
        query.setParameter("userId", userId);
        List<ParentCompanyEntity> result = query.getResultList();
        List<ParentCompanyVo> parentCompanies = new ArrayList<ParentCompanyVo>();
        if (result != null && !result.isEmpty()) {
            for (ParentCompanyEntity entity : result) {
                parentCompanies.add(toVo(entity));
            }
        }
        return parentCompanies;
    }

    public void linkUserToParentCompany(Long userId, Long parentCompanyId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId can not be null");
        }
        if (parentCompanyId == null) {
            throw new IllegalArgumentException("parentCompanyId can not be null");
        }

        AuthUserEntity user = em.find(AuthUserEntity.class, userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        ParentCompanyEntity parentCompany = em.find(ParentCompanyEntity.class, parentCompanyId);
        if (parentCompany == null) {
            throw new IllegalArgumentException("Parent company not found");
        }

        UserParentCompanyId id = new UserParentCompanyId(userId, parentCompanyId);
        UserParentCompanyEntity existing = em.find(UserParentCompanyEntity.class, id);
        if (existing != null) {
            return;
        }

        UserParentCompanyEntity entity = new UserParentCompanyEntity();
        entity.setId(id);
        entity.setUser(user);
        entity.setParentCompany(parentCompany);
        em.persist(entity);
    }

    public void replaceUserParentCompanies(Long userId, List<Long> parentCompanyIds) {
        if (userId == null) {
            throw new IllegalArgumentException("userId can not be null");
        }

        Set<Long> desiredParentCompanyIds = new LinkedHashSet<Long>();
        if (parentCompanyIds != null) {
            for (Long parentCompanyId : parentCompanyIds) {
                if (parentCompanyId != null) {
                    desiredParentCompanyIds.add(parentCompanyId);
                }
            }
        }

        TypedQuery<UserParentCompanyEntity> query = em.createQuery(
                "SELECT upc FROM UserParentCompanyEntity upc WHERE upc.user.id = :userId",
                UserParentCompanyEntity.class);
        query.setParameter("userId", userId);
        List<UserParentCompanyEntity> existingRelations = query.getResultList();

        for (UserParentCompanyEntity relation : existingRelations) {
            Long existingParentCompanyId = relation.getParentCompany().getId();
            if (!desiredParentCompanyIds.contains(existingParentCompanyId)) {
                em.remove(relation);
            } else {
                desiredParentCompanyIds.remove(existingParentCompanyId);
            }
        }

        for (Long parentCompanyId : desiredParentCompanyIds) {
            linkUserToParentCompany(userId, parentCompanyId);
        }
    }

    private ParentCompanyVo toVo(ParentCompanyEntity entity) {
        ParentCompanyVo vo = new ParentCompanyVo();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        return vo;
    }
}
