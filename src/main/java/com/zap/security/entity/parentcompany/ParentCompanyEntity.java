package com.zap.security.entity.parentcompany;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import lombok.Data;

@Data
@Cacheable(true)
@Entity
@Table(name = "EMPRESA_PADRE")
@NamedQueries({
        @NamedQuery(name = "ParentCompanyEntity.findAll", query = "SELECT pc FROM ParentCompanyEntity pc ORDER BY pc.name ASC")
})
public class ParentCompanyEntity implements Serializable {

    private static final long serialVersionUID = 7377875596763017503L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;
}
