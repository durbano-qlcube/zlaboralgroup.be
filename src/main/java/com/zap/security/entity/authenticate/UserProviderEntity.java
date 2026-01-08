package com.zap.security.entity.authenticate;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

@Data
@Cacheable(true)
@Entity
@Table(name = "SEC_USER_PROVIDER")
@NamedQueries({
    @NamedQuery(name = "UserProviderEntity.loadByUserUuid", query = "SELECT up FROM UserProviderEntity AS up WHERE up.uuidUser = :uuidUser"),
    @NamedQuery(name = "UserProviderEntity.loadByProviderUuid", query = "SELECT up FROM UserProviderEntity AS up WHERE up.uuidProvider = :uuidProvider")
})
public class UserProviderEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_USER_PROVIDER", nullable = false, insertable = true, updatable = true)
    private Long idUserProvider;

    @Column(name = "UUID_USER", length = 200, insertable = true, updatable = true)
    private String uuidUser;

    @Column(name = "UUID_PROVIDER", length = 200, insertable = true, updatable = true)
    private String uuidProvider;

    @Column(name = "USERNAME_PROVIDER", length = 300, insertable = true, updatable = true)
    private String usernameProvider;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE_CREATION", insertable = true, updatable = true)
    private Calendar fxCreation;
}
