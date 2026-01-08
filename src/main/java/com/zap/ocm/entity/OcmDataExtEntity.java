package com.zap.ocm.entity;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;

import lombok.Data;





@Cacheable(true)
@javax.persistence.Entity
@javax.persistence.Table(name = "skill_formacionleadsmotor_dataexten")
@javax.persistence.NamedQueries({
//	@javax.persistence.NamedQuery(name = "DkvMotorLeadsNotEntity.loadAll", query = "select AuthUser from AuthUserEntity AS AuthUser order by AuthUser.id desc"),
//	@javax.persistence.NamedQuery(name = "DkvMotorLeadsNotEntity.load", query = "select AuthUser from AuthUserEntity AS AuthUser WHERE AuthUser.id=:id"),
//	@javax.persistence.NamedQuery(name = "DkvMotorLeadsNotEntity.loadByIdOcm", query="SELECT  lead FROM DkvMotorLeadsEntity AS lead WHERE lead.idOcm=:idOcm"),
//	@javax.persistence.NamedQuery(name = "DkvMotorLeadsNotEntity.loadByHubspotStatus", query="SELECT lead FROM DkvMotorLeadsEntity AS lead WHERE lead.hubspotStatus=:hubspotStatus order by lead.dateInsert desc"),

	
	})
@Data
public class OcmDataExtEntity  implements Serializable
{

    private static final long serialVersionUID = -2109966671878671439L;

    @Id
   	//@GeneratedValue(strategy=GenerationType.IDENTITY)
   	@Column(name="id", unique=true, insertable=true, updatable=true, nullable=false)
    private Integer id;
    
    @javax.persistence.Column(name = "NOMBRE", insertable = true, updatable = true, length = 300)
    private String nombre;
    
    @javax.persistence.Column(name = "APELLIDOS", insertable = true, updatable = true, length = 300)
    private String apellidos;
    
    @javax.persistence.Column(name = "CIF", insertable = true, updatable = true, length = 15)
    private String cif;
    
    @javax.persistence.Column(name = "TIPO_CLIENTE", insertable = true, updatable = true, length = 15)
    private String tipoCliente;

    @javax.persistence.Column(name = "NTRABAJADORES", insertable = true, updatable = true, length = 25)
    private String ntrabajadores;
    
    
    @javax.persistence.Column(name = "SECTOR_PRODUCTIVO", insertable = true, updatable = true, length = 25)
    private String sectorProductivo;
    
    @javax.persistence.Column(name = "CP", insertable = true, updatable = true, length = 15)
    private String cp;

    @javax.persistence.Column(name = "PROVINCIA", insertable = true, updatable = true, length = 150)
    private String provincia;
    
    
    
    @javax.persistence.Column(name = "CREDITOS_DISPONIBLES", insertable = true, updatable = true, length = 25)
    private String creditosDisponibles;
    
    @javax.persistence.Column(name = "CIUDAD", insertable = true, updatable = true, length = 150)
    private String ciudad;
    
    
    @javax.persistence.Column(name = "EMAIL", insertable = true, updatable = true, length = 300)
    private String email;
    
    @javax.persistence.Column(name = "NOMBRE_EMPRESA", insertable = true, updatable = true, length = 300)
    private String nombreEmpresa;

    
    @javax.persistence.Column(name = "DIRECCION", insertable = true, updatable = true, length = 300)
    private String direccion;
    
    
    @javax.persistence.Column(name = "OBSERVACIONES", insertable = true, updatable = true, length = 2500)
    private String observaciones;
    
    
//    @javax.persistence.Column(name = "DNI", insertable = true, updatable = true, length = 300)
//    private String dni;
//    
    
    
//    @javax.persistence.Column(name = "Proveedor_bbdd", insertable = true, updatable = true)
//    private String ProveedorDb;
//    
//    @javax.persistence.Column(name = "supplier_keyword", insertable = true, updatable = true)
//    private String supplierKeyword;
//    
//    @javax.persistence.Column(name = "url", insertable = true, updatable = true, length = 1500)
//    private String url;
//    
//
//
//    @javax.persistence.Column(name = "campaign_external_id", insertable = true, updatable = true)
//    private String campaignExternalId;
//    
//    @javax.persistence.Column(name = "external_id", insertable = true, updatable = true)
//    private String externalId;
//    
//    
//    @javax.persistence.Column(name = "supplier_sweepstake", insertable = true, updatable = true)
//    private String supplierSweepstake;
//    
//    @javax.persistence.Column(name = "tipoCliente", insertable = true, updatable = true, length = 50)
//    private String tipoCliente;
//    
//    @javax.persistence.Column(name = "TYPE_REG", insertable = true, updatable = true, length = 50)
//    private String typeReg;
//    
//    @javax.persistence.Column(name = "leads_origin_code", insertable = true, updatable = true, length = 10)
//    private String leadsOriginCode;
//    
//    
//    @javax.persistence.Column(name = "leads_origin", insertable = true, updatable = true, length = 10)
//    private String leadsOrigin;
//    
//    
//    @javax.persistence.Column(name = "leads_temp", insertable = true, updatable = true, length = 10)
//    private String leadsTemp;
//    
//    
//
//
//  
//    @javax.persistence.Column(name = "Numero_Poliza_DKV", insertable = true, updatable = true, length = 50)
//    private String NumeroPolizaDKV;
//    
//  
//    @javax.persistence.Column(name = "Numero_asegurados_poliza", insertable = true, updatable = true, length = 4)
//    private String NumeroAseguradosPoliza;
//    
//  
//    
//    @javax.persistence.Column(name = "primaAnual", insertable = true, updatable = true, length = 50)
//    private String primaAnual;
//    
//    @javax.persistence.Column(name = "productoVendido", insertable = true, updatable = true, length = 150)
//    private String productoVendido;
//    
//  
//
//
//
//    @javax.persistence.Column(name = "consentimientoLegal", insertable = true, updatable = true, length = 10)
//    private Integer consentimientoLegal;
//    
//    @javax.persistence.Column(name = "fechaConsentimiento", insertable = true, updatable = true)
//    private Calendar fechaConsentimiento;
//    
//    @javax.persistence.Column(name = "fuenteConsentimiento", insertable = true, updatable = true, length = 20)
//    private String fuenteConsentimiento;
//    
//    
//    @javax.persistence.Column(name = "mascota", insertable = true, updatable = true, length = 2)
//    private Integer mascota;
//    
//    @javax.persistence.Column(name = "tipoMascota", insertable = true, updatable = true, length = 100)
//    private String tipoMascota;
//    
//    
//    @javax.persistence.Column(name = "decesosInteresa", insertable = true, updatable = true, length = 10)
//    private String decesosInteresa;
//    
//    @javax.persistence.Column(name = "decesosTieneSeguro", insertable = true, updatable = true, length = 10)
//    private String decesosTieneSeguro;
//    
//    @javax.persistence.Column(name = "decesosCompaniaActual", insertable = true, updatable = true, length = 100)
//    private String decesosCompaniaActual;
//    
//    @javax.persistence.Column(name = "decesosFechaRenovacion", insertable = true, updatable = true)
//    private Calendar decesosFechaRenovacion;
//    
//    @javax.persistence.Column(name = "decesosLlamarVencimiento", insertable = true, updatable = true, length = 10)
//    private String decesosLlamarVencimiento;
//    
//
//    
//    @javax.persistence.Column(name = "endresult_dkv", insertable = true, updatable = true)
//    private Integer endresultDkv;
//    
//    @javax.persistence.Column(name = "endresultdesc_dkv", insertable = true, updatable = true, length = 300)
//    private String endresultdescDkv;
//    
//    @javax.persistence.Column(name = "pago_leads_dkv", insertable = true, updatable = true)
//    private Integer pagoDkvLeads;
//    
//    @javax.persistence.Column(name = "tipo_producto_vendido", insertable = true, updatable = true, length = 50)
//    private String tipoProductoVendido;
//    
//    
//    
//    @javax.persistence.Column(name = "idload", insertable = true, updatable = true)
//    private Integer idload;
//    
//    @javax.persistence.Column(name = "loadCode", insertable = true, updatable = true, length = 150)
//    private String loadCode;
//    
//    @javax.persistence.Column(name = "loadDesc", insertable = true, updatable = true, length = 150)
//    private String loadDesc;
//    
//    @javax.persistence.Column(name = "skillDef", insertable = true, updatable = true, length = 150)
//    private String skillDef;
    
    
    
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private OcmDataEntity ocmDataEntity;
    
}
