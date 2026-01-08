package com.zap.maintenance.entity.geo;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;


@Entity
@Table(name = "MNT_CITY")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "city.loadAll", query = "SELECT m FROM CityEntity m"),
    @NamedQuery(name = "city.loadByIdProvince", query = "SELECT m FROM CityEntity m WHERE m.idProvince = :idProvince"),
//    @NamedQuery(name = "city.findByMunicipio", query = "SELECT m FROM v m WHERE m.municipio = :municipio"),
//    @NamedQuery(name = "city.findById", query = "SELECT m FROM MunicipioEntity m WHERE m.id = :id"),
//    @NamedQuery(name = "city.findBySlug", query = "SELECT m FROM MunicipioEntity m WHERE m.slug = :slug"),
//    @NamedQuery(name = "city.findByLatitud", query = "SELECT m FROM MunicipioEntity m WHERE m.latitud = :latitud"),
//    @NamedQuery(name = "city.findByLongitud", query = "SELECT m FROM MunicipioEntity m WHERE m.longitud = :longitud")
    
})

@Data
public class CityEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    
	@javax.persistence.Id
	@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
	@javax.persistence.Column(name = "ID_CITY", nullable = false, insertable = true, updatable = true, length = 19)
    private Integer idCity;
    

	@javax.persistence.Column(name = "NAME", insertable = true, updatable = true, length = 300)
    private String name;
    
    
	@javax.persistence.Column(name = "ID_PROVINCE", insertable = true, updatable = true)
    private Integer idProvince;
    
    
   
    
}
