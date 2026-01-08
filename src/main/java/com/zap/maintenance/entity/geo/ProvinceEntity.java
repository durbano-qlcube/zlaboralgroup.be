package com.zap.maintenance.entity.geo;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;


@Entity
@Table(name = "MNT_PROVINCE")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ProvinciaEntity.loadAll", query = "SELECT p FROM ProvinceEntity p"),
//    @NamedQuery(name = "Province.findById", query = "SELECT p FROM Provincias p WHERE p.id = :id"),
//    @NamedQuery(name = "Province.findBySlug", query = "SELECT p FROM Provincias p WHERE p.slug = :slug"),
//    @NamedQuery(name = "Province.findByProvincia", query = "SELECT p FROM Provincias p WHERE p.provincia = :provincia"),
//    @NamedQuery(name = "Province.findByComunidadId", query = "SELECT p FROM Provincias p WHERE p.comunidadId = :comunidadId"),
//    @NamedQuery(name = "Province.findByCapitalId", query = "SELECT p FROM Provincias p WHERE p.capitalId = :capitalId")
    
})

@Data
public class ProvinceEntity implements Serializable {

    private static final long serialVersionUID = 1L;

	@javax.persistence.Id
	@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
	@javax.persistence.Column(name = "ID_PROVINCE", nullable = false, insertable = true, updatable = true, length = 19)
    private Integer idProvince;
    
    
	@javax.persistence.Column(name = "NAME", insertable = true, updatable = true, length = 400)
    private String name;
    
    
    
}
