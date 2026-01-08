package com.zap.ocm.vo;

import java.util.Calendar;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.zap.maintenance.service.adapter.TimeDateAdapter;

import lombok.Data;

@Data
public class LeadsMotorVo {

    private Long idOcm;
    private Integer status;
    private Integer priority;
    private String number1;

    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar dateInsert;
    
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar dateFirstcall;
    
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar dateLastcall;
    
    @XmlJavaTypeAdapter(TimeDateAdapter.class)
    private Calendar dateNextcall;

    private Integer attempt;
    private String lastAgent;
    private Integer endResult;
    private String endResultDesc;
    private String endResultGroup;

    private String nombre;
    private String apellidos;
    private String email;
    private String observaciones;
    private String campaignId;
    private String campaignProvider;
    private String provedorBdd;
    private String provider;
    private Integer active;

 // ====== CAMPOS DATAEXTEN ======
    private String cif;
    private String tipoCliente;
    private String sectorProductivo;
    private String cp;
    private String provincia;
    private String creditosDisponibles;
    private String nombreEmpresa;
    private String gestoria;
    private String ciudad;
    private String direccion;
    private String acceptancePolicy;
    private String acceptance3Party;
    private String campaignLeadId;
    private String campaignAdsetName;
    private String campaignAdName;
    private String campaignName;
    private String campaignFormName;
    private String campaignPlatform;
    private String campaignUrl;
    private String campaignProduct;
    private String haUsadoBonificacion;
    private String formacionInteresada;
    private String gestoriaTelefono;
    private String nempleados;
    private String fullname;
    private String phone;
  
}
