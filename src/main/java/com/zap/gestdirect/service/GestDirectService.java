package com.zap.gestdirect.service;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zap.acquisition.vo.AcquisitionVo;
import com.zap.acquisition.vo.StatusAcquisitionEnum;
import com.zap.gestdirect.exception.GestDirectServiceException;
import com.zap.gestdirect.vo.GestdirectAcquisitionVo;
import com.zap.gestdirect.vo.GestdirectAlumnoVo;
import com.zap.gestdirect.vo.GestdirectEmpresaVo;
import com.zap.gestdirect.vo.GestdirectFormacionVo;
import com.zap.gestdirect.vo.GestdirectStatusAcquisitionEnum;
import com.zap.gestdirect.vo.GestdirectStatusVentaEnum;
import com.zap.gestdirect.vo.GestdirectVentaExtVo;
import com.zap.gestdirect.vo.GestdirectVentaVo;
import com.zap.gestdirect.vo.ResponseGestdirectVo;
import com.zap.maintenance.service.settings.SettingsService;
import com.zap.sales.vo.alumno.AlumnoVo;
import com.zap.sales.vo.empresa.EmpresaVo;
import com.zap.sales.vo.formacion.FormacionVo;
import com.zap.sales.vo.venta.StatusVentaEnum;
import com.zap.sales.vo.venta.VentaExtVo;
import com.zap.sales.vo.venta.VentaVo;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


@Stateless
public class GestDirectService implements Serializable
{

	private static final long serialVersionUID = -8448538504088495308L;
	private static final Logger LOGGER = LoggerFactory.getLogger(GestDirectService.class.getName());
	private static final MediaType JSON_MEDIATYPE = MediaType.parse("application/json; charset=utf-8");
	private static String URL ="";

	@PersistenceContext(unitName = "zapLaboralgrouPool")
	private EntityManager emZapCube;

	@Inject
	SettingsService settingsService;


	@PostConstruct
	public void initializes()
	{

		URL= settingsService.loadUrlGestdirect();
	}

	



	public ResponseGestdirectVo updateAdquisition (AcquisitionVo acquisitionVo)
	{
		long t = System.currentTimeMillis();
		String TAG ="[GestDirectService - "+t+" updateAdquisition]";
		ResponseGestdirectVo result = null;

		try {
			Gson gson = new Gson();
			GestdirectAcquisitionVo acquisitionGestDirectVo=this.toAcquisitionGestDirect(acquisitionVo);
			

			String buffer = gson.toJson(acquisitionGestDirectVo);
			String jwtToken ="xxx"; //authService.loadPfsJwt(program);

			LOGGER.info("{} >> buffer {}", TAG, buffer);

			RequestBody body = RequestBody.create(JSON_MEDIATYPE,buffer);

			Request request = new Request.Builder().url(URL + "/api/v1.0/zapCube/updateAdquisition")
					//.addHeader("Authorization", "Bearer " + jwtToken)
					.addHeader("Content-Type", "application/json")
					.post(body).build();

			OkHttpClient client = new OkHttpClient.Builder()
					.connectTimeout(10, TimeUnit.SECONDS)
					.writeTimeout(10, TimeUnit.SECONDS)
					.readTimeout(30, TimeUnit.SECONDS)
					.build();

			try (Response response = client.newCall(request).execute())
			{
				String jsonLine = response.body().string();
				int httpCode = response.code();
				if (httpCode == HttpURLConnection.HTTP_OK || httpCode == HttpURLConnection.HTTP_CREATED) {

					result = gson.fromJson (jsonLine, ResponseGestdirectVo.class);
				} else {
					LOGGER.error("{} - response {} ", TAG, jsonLine);
				}

				return result;
			} catch (Exception ex) {
				LOGGER.error("{} - response Message:{} {}", TAG, ex.getMessage());
				throw ex;
			}


		} catch (Exception ex) {
			LOGGER.error("{} - response Message:{} {}", TAG, ex.getMessage());
			throw new GestDirectServiceException(ex);
		}
	}

	
	private GestdirectAcquisitionVo toAcquisitionGestDirect (AcquisitionVo src)
	{

		GestdirectAcquisitionVo dest = new GestdirectAcquisitionVo();

		dest.setIdAcquisition(src.getIdAcquisition());
		
		
		String mappedStatusString = this.toGestdirectStatusAcquisition(src.getStatus(), src.getOcmLastCoding());
		GestdirectStatusAcquisitionEnum mappedStatus;

		try {
			mappedStatus = GestdirectStatusAcquisitionEnum.valueOf(mappedStatusString);
			LOGGER.info("Estado mapeado correctamente: " + mappedStatusString);
			
		} catch (IllegalArgumentException e) {
			mappedStatus = GestdirectStatusAcquisitionEnum.ERROR;
			LOGGER.info("Asignando estado 'ERROR'");
		}

		dest.setStatus(mappedStatus);

		String sourceDescription = src.getObservaciones();
		if (GestdirectStatusAcquisitionEnum.CLIENTE_EXISTENTE.toString().equals(mappedStatus.toString()))
		{
			String newDescription = sourceDescription + " CLIENTE YA EXISTENTE";
			dest.setDescription(newDescription);
			LOGGER.info("Descripción actualizada: " + newDescription);
		} else {
			dest.setDescription(sourceDescription);
		}

		return dest;	

	}
	
	
    private String toGestdirectStatusAcquisition(StatusAcquisitionEnum status, String ocmLastCoding)
    {
    	switch (status) {
    		case CODIFICADO:
    			if (ocmLastCoding != null && ocmLastCoding.contains("VENTA")) {
    				return "VENTA"; 
    			} else {
    				return "NO_VENTA";
    			}
    		default:
    			return "ERROR"; 
    	}
    }
	
	
	
	
	public GestdirectVentaVo saveOrUpdateVenta (VentaExtVo ventaExtVo)
	{
		long t = System.currentTimeMillis();
		String TAG ="[GestDirectService - "+t+" saveOrUpdateVenta]";
		GestdirectVentaVo result = null;

		try {
			Gson gson = new Gson();
			GestdirectVentaExtVo gestdirectVentaExtVo=this.toVentaGesdirect(ventaExtVo);


			String buffer = gson.toJson(gestdirectVentaExtVo);
			String jwtToken ="xxx"; //authService.loadPfsJwt(program);

			LOGGER.info("{} >> buffer {}", TAG, buffer);

			RequestBody body = RequestBody.create(JSON_MEDIATYPE,buffer);

			Request request = new Request.Builder().url(URL + "api/v1.0/zapCube/saveOrUpdateVenta")
					//.addHeader("Authorization", "Bearer " + jwtToken)
					.addHeader("Content-Type", "application/json")
					.post(body).build();

			OkHttpClient client = new OkHttpClient.Builder()
					.connectTimeout(10, TimeUnit.SECONDS)
					.writeTimeout(10, TimeUnit.SECONDS)
					.readTimeout(30, TimeUnit.SECONDS)
					.build();

			try (Response response = client.newCall(request).execute())
			{
				String jsonLine = response.body().string();
				int httpCode = response.code();
				if (httpCode == HttpURLConnection.HTTP_OK || httpCode == HttpURLConnection.HTTP_CREATED) {

					
					

					JsonObject jsonObject = JsonParser.parseString(jsonLine).getAsJsonObject();
	                JsonObject ventaObject = jsonObject.getAsJsonObject("venta");

	                result = gson.fromJson(ventaObject, GestdirectVentaVo.class);
					
				} else {
					LOGGER.error("{} - response {} ", TAG, jsonLine);
				}

				return result;
			} catch (Exception ex) {
				LOGGER.error("{} - response Message:{} {}", TAG, ex.getMessage());
				throw ex;
			}


		} catch (Exception ex) {
			LOGGER.error("{} - response Message:{} {}", TAG, ex.getMessage());
			throw new GestDirectServiceException(ex);
		}
	}

	
	private GestdirectVentaExtVo toVentaGesdirect (VentaExtVo ventaExtVo)
	{
		GestdirectVentaExtVo ext = new GestdirectVentaExtVo();
		ext.setVenta(this.copyVentaFields(ventaExtVo.getVenta()));
		ext.setFormacion(this.copyFormacionFields(ventaExtVo.getFormacion()));
		ext.setEmpresa(this.copyEmpresaFields(ventaExtVo.getEmpresa()));
		
		 List<AlumnoVo> alumnos = ventaExtVo.getAlumnos();
		    List<GestdirectAlumnoVo> alumnosGestdirect = Optional.ofNullable(alumnos)
		        .orElseGet(Collections::emptyList) 
		        .stream()
		        .map(this::copyAlumnoFields)
		        .collect(Collectors.toList());

		    ext.setAlumnos(alumnosGestdirect);		
		return ext;
	}
	
	
	private GestdirectFormacionVo copyFormacionFields(FormacionVo source)
	{
		GestdirectFormacionVo destination = new GestdirectFormacionVo();
		destination.setNombre(source.getNombre());
		destination.setHoras(source.getHoras());
		destination.setAreaProfesional(source.getAreaProfesional());
		destination.setNumeroAlumnos(source.getNumeroAlumnos());
		
		if (source.getFechaInicio() != null) {
			destination.setFechaInicio(source.getFechaInicio().getTimeInMillis()); 
	    }
		
		
		if (source.getFechaFin() != null) {
			destination.setFechaFin(source.getFechaFin().getTimeInMillis()); 
	    }
		
		if (source.getFechaNotificacionInicioFundae() != null) {
			destination.setFechaNotificacionInicioFundae(source.getFechaNotificacionInicioFundae().getTimeInMillis()); 
	    }
		
		if (source.getFechaNotificacionFinFundae() != null) {
			destination.setFechaNotificacionFinFundae(source.getFechaNotificacionFinFundae().getTimeInMillis()); 
	    }

		
		
		return destination;
	}

	private GestdirectAlumnoVo copyAlumnoFields(AlumnoVo source)
	{
		GestdirectAlumnoVo destination = new GestdirectAlumnoVo();
		destination.setNombreCompleto(source.getNombreCompleto());
		destination.setDni(source.getDni());
		
		if (source.getFechaNacimiento() != null) {
			destination.setFechaNacimiento(source.getFechaNacimiento().getTimeInMillis()); 
	    }
		destination.setSexo(source.getSexo());
		destination.setNacionalidad(source.getNacionalidad());
		destination.setTelefonoContacto(source.getTelefonoContacto());
		destination.setEmail(source.getEmail());
		destination.setHorarioLaboral(source.getHorarioLaboral());
		destination.setNivelEstudios(source.getNivelEstudios());
		destination.setPuesto(source.getPuesto());

		
		return destination;
	}

		private GestdirectVentaVo copyVentaFields(VentaVo source)
		{
			GestdirectVentaVo destination=new GestdirectVentaVo();
			destination.setIdVenta(source.getIdVentaGestdirect());
			
			
			destination.setUuId(source.getUuId());
//			destination.setUsernameAgente(source.getUsernameAgente());
			destination.setPrice(source.getPrice());
			destination.setPriceDeductedExpenses(source.getPriceDeductedExpenses());
			destination.setPriceWithIva(source.getPriceWithIva());
			destination.setIva(source.getIva());
			destination.setCommission(source.getCommission());
			destination.setCharged(source.getCharged());
			destination.setPdteFirma(source.getPdteFirma());
			destination.setOrigin(source.getOrigin());
//			destination.setOriginUserUsername(source.getOriginUserUsername());
			
			//destination.setOriginGestoriaUuid(empresa.getOriginGestoriaUuid());
//			destination.setOriginUserUsername(source.getOriginUserUsername());
		
			destination.setParentCompanyId(source.getParentCompanyId());
		
			if (source.getFxVenta() != null) {
				destination.setFxVenta(source.getFxVenta().getTimeInMillis()); 
		    }
		
			//destination.setOriginUserUuid(source.getOriginUserUuid());
			StatusVentaEnum sourceStatus = source.getStatus();
			//GestdirectStatusAcquisitionEnum desStatus = null;


			if (sourceStatus == StatusVentaEnum.PDTE_DOC || sourceStatus == StatusVentaEnum.PDTE_INICIO_CURSO) {
				destination.setStatus(GestdirectStatusVentaEnum.valueOf("EN_EJECUCION"));
			
			
			} else if (StatusVentaEnum.EJECUCION_CURSO.toString().equals(sourceStatus.toString()) ||
					StatusVentaEnum.CURSO_FINALIZADO.toString().equals(sourceStatus.toString()) ||
					StatusVentaEnum.NOTIFICADO_FUNDAE.toString().equals(sourceStatus.toString()))
			{
				destination.setStatus(GestdirectStatusVentaEnum.valueOf("VENTA"));
			
			
			} else if (sourceStatus == StatusVentaEnum.CANCELADO) {
				destination.setStatus(GestdirectStatusVentaEnum.valueOf("ERROR"));
				
//			} else {
//				destination.setStatus(sourceStatus); 
			}

			return destination;
		}


		private GestdirectEmpresaVo copyEmpresaFields(EmpresaVo source)
		{
			
			GestdirectEmpresaVo destination= new GestdirectEmpresaVo();
			
			destination.setCif(source.getCif());
			destination.setRazonSocial(source.getRazonSocial());
			destination.setNombreComercial(source.getNombreComercial());
			destination.setActividadPrincipal(source.getActividadPrincipal());
			destination.setPlantillaMedia(source.getPlantillaMedia());
			destination.setExisteRlt(source.getExisteRlt());
			destination.setEsPyme(source.getEsPyme());
			destination.setCnae(source.getCnae());
			destination.setDomicilioFiscal(source.getDomicilioFiscal());
			destination.setTamanoEmpresa(source.getTamanoEmpresa());
			destination.setBonificacion(source.getBonificacion());
                        destination.setCreditosDisponibles(source.getCreditosDisponibles());
                        destination.setCreditosGastados(source.getCreditosGastados());
                        destination.setEstado(source.getEstado());
                        destination.setParentCompanyId(source.getParentCompanyId());
                        destination.setParentCompanyName(source.getParentCompanyName());
                        destination.setIban(source.getIban());
			destination.setRepreLegalNombreCom(source.getRepreLegalNombreCom());
			destination.setRepreLegalNif(source.getRepreLegalNif());
			destination.setRepreLegalTelefono(source.getRepreLegalTelefono());
			destination.setRepreLegalEmail(source.getRepreLegalEmail());
			destination.setAsesoriaNombre(source.getAsesoriaNombre());
			destination.setAsesorNombreCompleto(source.getAsesorNombreCompleto());
			destination.setAsesorTelefono(source.getAsesorTelefono());
			destination.setAsesorEmail(source.getAsesorEmail());
			destination.setUuIdEmpresa(source.getUuIdEmpresa());
			destination.setOriginUsername(source.getOriginUserUsername());
			
			destination.setPersonaContacto(source.getPersonaContacto());
			destination.setEmailContacto(source.getEmailContacto());
			destination.setPuestoContacto(source.getPuestoContacto());
			destination.setTelefonoContacto(source.getTelefonoContacto());
			destination.setObservaciones(source.getObservaciones());

			
			if (source.getFechaAlta() != null) {
				destination.setFechaAlta(source.getFechaAlta().getTimeInMillis()); 
		    }

//			//destination.setColaboradorUuid(source.getColaboradorUuid());
//			destination.setOrigin(source.getOrigin());
			
			return destination;
		}


}
