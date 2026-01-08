package com.zap.security.entity.authenticate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

@Data
@Cacheable(true)
@Entity
@Table(name = "SEC_PROVIDER_CONFIG")
@NamedQueries({
    @NamedQuery(name = "ProviderConfigEntity.loadByUserId", query = "SELECT pc FROM ProviderConfigEntity pc WHERE pc.user.id = :userId"),
    @NamedQuery(name = "ProviderConfigEntity.loadByUserUuid", query = "SELECT pc FROM ProviderConfigEntity pc WHERE pc.user.uuid = :uuid")
})
public class ProviderConfigEntity implements Serializable {

    private static final long serialVersionUID = 2707860728742462180L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PROVIDER_CONFIG", nullable = false, insertable = true, updatable = true)
    private Long idProviderConfig;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_AUTHUSER", nullable = false)
    private AuthUserEntity user;

    @Column(name = "COSTO_LEAD", precision = 10, scale = 2, insertable = true, updatable = true)
    private BigDecimal costoLead;

    @Column(name = "INVALIDACION", precision = 5, scale = 2, insertable = true, updatable = true)
    private BigDecimal invalidacion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "INVALIDACION_DESDE", insertable = true, updatable = true)
    private Calendar invalidacionDesde;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "INVALIDACION_HASTA", insertable = true, updatable = true)
    private Calendar invalidacionHasta;

    @Column(name = "TOTAL_INSERTADOS", insertable = true, updatable = true)
    private Integer totalInsertados;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE_CREATION", insertable = true, updatable = true)
    private Calendar fxCreation;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE_MODIFICATION", insertable = true, updatable = true)
    private Calendar fxModification;
}
