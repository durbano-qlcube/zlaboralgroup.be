package com.zap.security.entity.parentcompany;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Embeddable
public class UserParentCompanyId implements Serializable {

    private static final long serialVersionUID = -5019867601785144966L;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "parent_company_id", nullable = false)
    private Long parentCompanyId;

    public UserParentCompanyId(Long userId, Long parentCompanyId) {
        this.userId = userId;
        this.parentCompanyId = parentCompanyId;
    }
}
