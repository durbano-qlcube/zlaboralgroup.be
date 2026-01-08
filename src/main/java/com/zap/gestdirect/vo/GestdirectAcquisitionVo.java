package com.zap.gestdirect.vo;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.Column;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.zap.maintenance.service.adapter.TimeDateAdapter;

import lombok.Data;


@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class GestdirectAcquisitionVo implements Serializable
{
	private static final long serialVersionUID = -7836406887983296193L;
	
	private Integer idAcquisition;
    private String nombreComercial;
    private String cif;
	private String name;
	private String surname;
	private String surname2;
	private String fullname;
	private String	email;
	private String	phone;
	private String	description;
	private String campaign;
	private GestdirectStatusAcquisitionEnum	status;
	
	@XmlJavaTypeAdapter(TimeDateAdapter.class)
	private Calendar fxScheduling;
	
	private String repreLegalNombreCom;
	private String repreLegalNif;
	private String repreLegalTelefono;
	private String repreLegalEmail;
	
	

	
	private String origin;
	private String gestoriaUuid; 
	private String gestoriaName;
	private String vendedorUuid;
	private String vendedorUsername;
	private String colaboradorUsername;
	private String colaboradorUuid;
	private Integer idAdquisionCube;
	
	

	
	
	@XmlJavaTypeAdapter(TimeDateAdapter.class)
	private Calendar fxCreation;
	
	@XmlJavaTypeAdapter(TimeDateAdapter.class)
	private Calendar fxModification;

}