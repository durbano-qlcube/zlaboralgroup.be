package com.zap.ocm.vo;

import java.io.Serializable;
import java.util.Calendar;

import lombok.Data;




@Data
public class MotorLeadsVo  implements Serializable
{

    private static final long serialVersionUID = -2109966671878671439L;

    private Long idDkvMotorLeads;
    
    private Long idOcm;
   // private String active;
    private Integer status;
    private Integer priority;
    //private String number;
    
    private String number1;
    private Integer attempt;
    private Calendar dateInsert;
    private Calendar dateFirstcall;
    private Calendar dateLastcall;
    private Calendar dateNextcall;
    private String scheduledagent;
    private String lastagent;
    
    private Integer endresult;
    private String endresultdesc;
    private String endresultgroup;
    
    private String ProveedorDb;
    private String supplierKeyword;
    private String supplierSweepstake;
    private String campaignExternalId;
    private String leadsOriginCode;
    private String leadsOrigin;
    private String leadsTemp;
    private String externalId;
    private String url;

    private Integer endtype;
    private Integer bloq;
    private String forceani;
    
    
    
    private String nombre;
    private String apellidos;
    private String email;
    private String sexo;
    private String FechaNacimiento;
    private String cp;
    private String Provincia;
    private Integer numeroFamiliares;
    private String observaciones;
    
    private String tipoCliente;
    private String typeReg;
    
    private String NumeroPolizaDKV;
    private String NumeroAseguradosPoliza;
    private String primaAnual;
    private String productoVendido;
    
    private Integer consentimientoLegal;
    private Calendar fechaConsentimiento;
    private String fuenteConsentimiento;
    
    
    private Integer mascota;
    private String tipoMascota;
    
    private String decesosInteresa;
    private String decesosTieneSeguro;
    private String decesosCompaniaActual;
    private Calendar decesosFechaRenovacion;
    private String decesosLlamarVencimiento;
    
    private Integer endresultDkv;
    private String endresultdescDkv;
    private Integer pagoDkvLeads;
    private String tipoProductoVendido;
    
    
    private Integer idload;
    private String loadCode;
    private String loadDesc;
    private String skillDef;
	
	 
//	private String hubspotFMessageId;
//	private Integer hubspotHttpCode;
//	private HubSpotStatusEnum hubspotStatus;
//	private String hubspotStatusResult;
//	private String hubspotDescription;
//	private Calendar fxSendToHubspot;

    
}
