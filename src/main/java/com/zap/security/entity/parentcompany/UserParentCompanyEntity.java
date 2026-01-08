package com.zap.security.entity.parentcompany;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.zap.security.entity.authenticate.AuthUserEntity;

import lombok.Data;

@Data
@Cacheable(true)
@Entity
@Table(name = "USUARIO_EMPRESA_PADRE")
@NamedQueries({
        @NamedQuery(name = "UserParentCompanyEntity.findParentCompaniesByUserId",
                query = "SELECT upc.parentCompany FROM UserParentCompanyEntity upc WHERE upc.user.id = :userId ORDER BY upc.parentCompany.name ASC")
})
public class UserParentCompanyEntity implements Serializable {

    private static final long serialVersionUID = 3734226789794944771L;

    @EmbeddedId
    private UserParentCompanyId id;

    @MapsId("userId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "ID_AUTHUSER", nullable = false)
    private AuthUserEntity user;

    @MapsId("parentCompanyId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "parent_company_id", referencedColumnName = "id", nullable = false)
    private ParentCompanyEntity parentCompany;
}
