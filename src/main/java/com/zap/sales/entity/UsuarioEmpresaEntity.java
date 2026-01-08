package com.zap.sales.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Objects;

import com.zap.security.entity.authenticate.AuthUserEntity;
import lombok.Data;

@Entity
@Table(name = "ORG_USUARIO_EMPRESA")
@Data
public class UsuarioEmpresaEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_USUARIO_EMPRESA")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_AUTHUSER", nullable = false)
    private AuthUserEntity usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_EMPRESA", nullable = false)
    private EmpresaEntity empresa;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_ASIGNACION")
    private Calendar fechaAsignacion = Calendar.getInstance();

    @Column(name = "ACTIVO")
    private Boolean activo = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UsuarioEmpresaEntity)) return false;
        UsuarioEmpresaEntity that = (UsuarioEmpresaEntity) o;
        return Objects.equals(usuario, that.usuario) &&
               Objects.equals(empresa, that.empresa);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuario, empresa);
    }
}
