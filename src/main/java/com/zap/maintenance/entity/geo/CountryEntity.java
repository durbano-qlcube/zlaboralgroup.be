package com.zap.maintenance.entity.geo;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;


@Entity
@Table(name = "MNT_COUNTRY")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "country.loadAll", query = "SELECT m FROM CountryEntity m")
    
})

@Data
public class CountryEntity implements Serializable {

    private static final long serialVersionUID = 1L;


    
	@javax.persistence.Id
	@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
	@javax.persistence.Column(name = "ID_COUNTRY", nullable = false, insertable = true, updatable = true, length = 19)
	private Long idCountry;

	@javax.persistence.Column(name = "ALFA_2", insertable = true, updatable = true, length = 2)
	private String alfa2;

	@javax.persistence.Column(name = "ALFA_3", insertable = true, updatable = true, length = 3)
	private String alfa3;

	@javax.persistence.Column(name = "NAME", insertable = true, updatable = true, length = 300)
	private String name;


    
    
  
    
}
