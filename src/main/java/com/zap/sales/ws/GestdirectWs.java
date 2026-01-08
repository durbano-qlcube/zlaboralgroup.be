package com.zap.sales.ws;

import java.security.Principal;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.jboss.resteasy.annotations.GZIP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zap.acquisition.service.AcquisitionService;
import com.zap.acquisition.vo.AcquisitionVo;
import com.zap.acquisition.vo.StatusAcquisitionEnum;
import com.zap.maintenance.service.adapter.CalendarDeserializer;
import com.zap.maintenance.service.adapter.CalendarSerializer;
import com.zap.sales.exception.empresa.EmpresaNotFoundException;
import com.zap.sales.service.AlumnoService;
import com.zap.sales.service.EmpresaService;
import com.zap.sales.service.FormacionService;
import com.zap.sales.service.VentasService;
import com.zap.sales.vo.alumno.AlumnoVo;
import com.zap.sales.vo.empresa.EmpresaVo;
import com.zap.sales.vo.formacion.FormacionVo;
import com.zap.sales.vo.gestdirect.ResponseGestDirectVo;
import com.zap.sales.vo.venta.StatusVentaEnum;
import com.zap.sales.vo.venta.VentaExtVo;
import com.zap.sales.vo.venta.VentaVo;
import com.zap.security.exception.authenticate.NotAuthException;
import com.zap.security.service.authenticate.AuthService;
import com.zap.security.vo.authenticate.AuthUserVo;

@Path("/gestDirect")
public class GestdirectWs {

	private static final Logger LOGGER = LoggerFactory.getLogger(GestdirectWs.class);
	private Gson gson;

	@Context
	SecurityContext securityContext;

	@Inject
	EmpresaService empresaService;

	@Inject
	FormacionService formacionService;

	@Inject
	AlumnoService alumnoService;
	
	@Inject
	private VentasService ventasService;

	@Inject
	AcquisitionService acquisitionService;
	
	
	@Inject
	AuthService authService;

	private Gson initializesGson() {
		if (gson == null) {
			gson = new GsonBuilder().registerTypeAdapter(Calendar.class, new CalendarSerializer())
					.registerTypeAdapter(Calendar.class, new CalendarDeserializer())
					.registerTypeAdapter(GregorianCalendar.class, new CalendarSerializer()).create();
		}
		return gson;
	}

	private AuthUserVo getSecurityIdUser() throws NotAuthException {
		Principal principal = securityContext.getUserPrincipal();
		String uuid = principal.getName();

		if (uuid == null)
			throw new NotAuthException("getSecurityIdUser - 'id' can not be null");

		AuthUserVo user = authService.loadByUuid(uuid);

		return user;
	}

	
//	@JWTTokenNeeded
	@GZIP
	@POST
	@Path("/saveAdquition")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveAdquition(AcquisitionVo acquisitionVo)
	{
		long t=System.currentTimeMillis();
		String TAG = "[GestdirectWs - "+t+"saveAdquition]";
		LOGGER.debug(TAG + " - init");

		try {

			if (acquisitionVo == null)
				throw new IllegalArgumentException(TAG +" >> 'AuthUserVo' can not be null");

			if (acquisitionVo.getPhone() == null || "".equals(acquisitionVo.getPhone()))
				throw new IllegalArgumentException(TAG +" >> 'acquisitionVo.getPhone' can not be null or empty");


			AuthUserVo user = authService.loadByUsername(acquisitionVo.getAgenteUsername());

			acquisitionVo.setAgenteUsername(user.getUsername());
			acquisitionVo.setAgenteUuid(user.getUuid());
			acquisitionVo.setCoordinadorUuid(user.getUuidCordinador());
			
			AuthUserVo coordinador = authService.loadByCoordinadorUuid(user.getUuidCordinador());
			if (coordinador != null) {
				acquisitionVo.setCoordinadorUserName(coordinador.getUsername());
				acquisitionVo.setSupervisorUserName(coordinador.getSupervisorUsername());
				acquisitionVo.setSupervisorUuid(coordinador.getUuidSupervisor());
			}

			if (acquisitionVo.getStatus()==null)
				acquisitionVo.setStatus(StatusAcquisitionEnum.ENVIAR_OCM);

			AcquisitionVo result = acquisitionService.create(acquisitionVo);

			Gson gson = initializesGson();
			return Response.ok(gson.toJson(result),MediaType.APPLICATION_JSON).build();

		} catch (IllegalArgumentException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.BAD_REQUEST).build();

//		} catch (NotAuthException ex) {
//			LOGGER.error(TAG + " - Error: {}", ex);
//			return Response.serverError().status(Status.UNAUTHORIZED).build();

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}", ex.getMessage());
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

//	@JWTTokenNeeded
	@GZIP
	@POST
	@Path("/saveVenta")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveVenta(VentaExtVo ventaExtVo)
	{
		long t=System.currentTimeMillis();
		String TAG = "[GestdirectWs - "+t+"saveVenta]";
		LOGGER.debug(TAG + " - init");

		try {

//			AuthUserVo user = getSecurityIdUser(); /// Poner error 401 cuando no ingrese el usuario o se vencio el token
//			TAG = "[SaleWs - uuidAdm:" + user.getUuid() + " >>  register]";

			TAG= TAG + " >> createVentaExt - ";
			LOGGER.info(TAG + " -  processing....");
			EmpresaVo empresaVo = ventaExtVo.getEmpresa();
			EmpresaVo empresaVoExist = empresaService.loadByCif(empresaVo.getCif());
			if(empresaVoExist!=null)
			{
				LOGGER.info(TAG + " -  Empresa existente con CIF:{}....",empresaVo.getCif() );
				empresaVo.setIdEmpresa(empresaVoExist.getIdEmpresa());
			}else {
				throw new EmpresaNotFoundException(TAG+ "La empresa con CIF "+empresaVo.getCif()+" no existe...");
			}

			LOGGER.info(TAG + " -  Empresa loaded with CIF:{} and idEmpresa:{}....",empresaVo.getCif(),empresaVo.getIdEmpresa());
			
			VentaVo ventaVo = ventaExtVo.getVenta();
			ventaVo.setStatus(StatusVentaEnum.PDTE_DOC);
			ventaVo.setIdEmpresa(empresaVo.getIdEmpresa());
			
			if (ventaVo.getFxVenta() == null )
				ventaVo.setFxVenta(Calendar.getInstance());
			

			AuthUserVo user = authService.loadByUsername(ventaVo.getUsernameAgente());

			ventaVo.setUsernameAgente(user.getUsername());
			ventaVo.setUuIdAgente(user.getUuid());
			ventaVo.setUuIdCoordinador(user.getUuidCordinador());
			
			AuthUserVo coordinador = authService.loadByCoordinadorUuid(user.getUuidCordinador());
			if (coordinador != null) {
				ventaVo.setUsernameCoordinador(coordinador.getUsername());
				ventaVo.setUsernameSupervisor(coordinador.getSupervisorUsername());
				ventaVo.setUuIdSupervisor(coordinador.getUuidSupervisor());
			}

			
			FormacionVo formacionVo = ventaExtVo.getFormacion();
			formacionVo.setIdEmpresa(empresaVo.getIdEmpresa());
			LOGGER.info(TAG + " -  Creating Formacion with IdEmpresa:{} IdVenta:{} IdFormacion:{}....",empresaVo.getIdEmpresa(),ventaVo.getIdVenta(),formacionVo.getIdFormacion() );
			formacionVo = formacionService.create(formacionVo);
			ventaVo.setIdFormacion(formacionVo.getIdFormacion());
			LOGGER.info(TAG + " -  Formacion created with id:{}....",formacionVo.getIdFormacion() );
			
			
			List<AlumnoVo> incomingAlumnos = ventaExtVo.getAlumnos();
			if (incomingAlumnos != null && !incomingAlumnos.isEmpty())
			{
				for (AlumnoVo alumnoVo : incomingAlumnos)
				{
					alumnoVo.setIdFormacion(formacionVo.getIdFormacion());
					alumnoVo.setIdEmpresa(empresaVo.getIdEmpresa());
					alumnoService.saveOrUpdate(alumnoVo);
					LOGGER.info(TAG + " -  Alumno saved or updated with  id:{}....",alumnoVo.getIdAlumno() );

				}
			}
			
			
			ventaVo = ventasService.create(ventaVo);
			LOGGER.info(TAG + " -  Venta created with  id:{}....",ventaVo.getIdVenta() );
			
			VentaExtVo result = new VentaExtVo();
			result.setVenta(ventaVo);
			result.setEmpresa(empresaVo);
			result.setFormacion(formacionVo);
			
			Gson gson = initializesGson();
			return Response.ok(gson.toJson(result)).build();
			
			
		} catch (IllegalArgumentException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.BAD_REQUEST).build();

//		} catch (NotAuthException ex) {
//			LOGGER.error(TAG + " - Error: {}", ex);
//			return Response.serverError().status(Status.UNAUTHORIZED).build();

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}", ex.getMessage());
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
//	@JWTTokenNeeded
	@GZIP
	@POST
	@Path("/saveEmpresa")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveEmpresa(EmpresaVo empresaVo)
	{
		long t=System.currentTimeMillis();
		String TAG = "[GestdirectWs - "+t+"saveEmpresa]";
		LOGGER.debug(TAG + " - init");

		try {
//			AuthUserVo user = getSecurityIdUser(); /// Poner error 401 cuando no ingrese el usuario o se vencio el token
//			TAG = "[SaleWs - uuidAdm:" + user.getUuid() + " >>  register]";

			EmpresaVo savedEmpresa = empresaService.saveOrUpdate(empresaVo);

			Gson gson = initializesGson();
			return Response.ok(gson.toJson(savedEmpresa), MediaType.APPLICATION_JSON).build();
		} catch (IllegalArgumentException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.BAD_REQUEST).build();

//		} catch (NotAuthException ex) {
//			LOGGER.error(TAG + " - Error: {}", ex);
//			return Response.serverError().status(Status.UNAUTHORIZED).build();

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}", ex.getMessage());
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}


	@GZIP
	@POST
	@Path("/checkCif")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkCif(EmpresaVo empresaTocheck)
	{
		long t=System.currentTimeMillis();
		String TAG = "[GestdirectWs - "+t+"checkCif]";
		LOGGER.debug(TAG + " - init");

		try {

//			AuthUserVo user = getSecurityIdUser();
//			TAG = "[SaleWs - uuidAdm:" + user.getUuid() + " >>  register]";

			if (empresaTocheck == null)
				throw new IllegalArgumentException(TAG + " >> 'empresaVo' can not be null");
			
			if (empresaTocheck.getCif() == null)
				throw new IllegalArgumentException(TAG + " >> 'Cif' can not be null");

					
			if (empresaTocheck.getOriginGestoriaUuid() == null)
				throw new IllegalArgumentException(TAG + " >> 'OriginUserUsername' can not be null");
			
			ResponseGestDirectVo result = new ResponseGestDirectVo();
			EmpresaVo empresa = empresaService.loadByCif(empresaTocheck.getCif());
			if  (empresa==null)
			{
				result.setCode(0);
				
			}else {
				if (empresa.getOriginGestoriaUuid().equalsIgnoreCase(empresaTocheck.getOriginGestoriaUuid()))
					result.setCode(0);
				else
					result.setCode(1);
			}
			
			Gson gson = initializesGson();
			return Response.ok(gson.toJson(result), MediaType.APPLICATION_JSON).build();
		} catch (IllegalArgumentException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.BAD_REQUEST).build();

//		} catch (NotAuthException ex) {
//			LOGGER.error(TAG + " - Error: {}", ex);
//			return Response.serverError().status(Status.UNAUTHORIZED).build();

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}", ex.getMessage());
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	
//	@JWTTokenNeeded
	@GZIP
	@GET
	@Path("/loadByCif/{cif}")
	public Response validateCIF(@PathParam("cif") String cif) {
		String TAG = "[CIFValidationEndpoint - validateCIF cif:" + cif + "]";
		LOGGER.debug(TAG + " - init");

		try {

//			AuthUserVo user = getSecurityIdUser();
//			TAG = "[SaleWs - uuidAdm:" + user.getUuid() + " >> register]";

			EmpresaVo empresa = empresaService.loadByCif(cif);

			if (empresa == null) {
				return Response.status(Status.NOT_FOUND).entity("{\"message\": \"CIF no existe en la base de datos.\"}")
						.type(MediaType.APPLICATION_JSON).build();
			}
			else if (empresa.getCif().isEmpty()) {
				Map<String, Object> responseMap = new HashMap<>();

				responseMap.put("empresa", empresa);
			

				Gson gson = initializesGson();
				return Response.ok(gson.toJson(responseMap)).build();
			} else if (empresa != null) {
				Map<String, Object> responseMap = new HashMap<>();
				responseMap.put("empresa", empresa);


				Gson gson = initializesGson();
				return Response.ok(gson.toJson(responseMap)).build();
			} 

			else {
				return Response.status(Status.CONFLICT).entity(
						"{\"message\": \"La empresa no existe en el sistema\"}")
						.type(MediaType.APPLICATION_JSON).build();
			}

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
					.entity("{\"error\":\"Error interno del servidor.\"}").build();
		}
	}
	
	
}
