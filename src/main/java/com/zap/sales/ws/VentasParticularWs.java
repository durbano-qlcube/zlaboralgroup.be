package com.zap.sales.ws;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jboss.resteasy.annotations.GZIP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zap.maintenance.service.adapter.CalendarDeserializer;
import com.zap.maintenance.service.adapter.CalendarSerializer;
import com.zap.maintenance.service.settings.SettingsService;
import com.zap.sales.exception.formacion.FormacionNotFoundException;
import com.zap.sales.exception.persona.PersonaNotFoundException;
import com.zap.sales.exception.venta.VentaNotFoundException;
import com.zap.sales.service.DocPartService;
import com.zap.sales.service.PersonaService;
import com.zap.sales.service.VentasParticularService;
import com.zap.sales.vo.doc.DocPartVo;
import com.zap.sales.vo.particular.PersonaVo;
import com.zap.sales.vo.particular.VentaPartExtVo;
import com.zap.sales.vo.particular.VentaPartSearchRequestVo;
import com.zap.sales.vo.particular.VentaPartSearchResponseExtVo;
import com.zap.sales.vo.particular.VentaPartSearchResponseVo;
import com.zap.sales.vo.particular.VentaPartVo;
import com.zap.sales.vo.venta.StatusVentaEnum;
import com.zap.sales.vo.venta.VentaSearchResponseVo;
import com.zap.security.exception.authenticate.NotAuthException;
import com.zap.security.filter.JWTTokenNeeded;
import com.zap.security.service.authenticate.AuthService;
import com.zap.security.vo.authenticate.AuthUserVo;
import com.zap.security.vo.enumerates.RoleEnum;
import com.zap.stripe.vo.StatusStripeEnum;

@Path("/ventas-particular")
public class VentasParticularWs {

	private static final Logger LOGGER = LoggerFactory.getLogger(VentasParticularWs.class);
	private SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static String BASE_PATH = "";
	private static String URL = "";
	private Gson gson;

	@Context
	SecurityContext securityContext;

	@Inject
	PersonaService personaService;

	@Inject
	VentasParticularService ventasParticularService;

	@Inject
	AuthService authService;

	@Inject
	DocPartService docPartService;
	
	@Inject
	SettingsService settingsService;
	

	@PostConstruct
	public void initializes()
	{
		BASE_PATH =  settingsService.loadUrlSaveDoc();
//		BASE_PATH =	"C:\\Users\\Emanuel\\wildfly-21.0.2.Final\\standalone";
		URL=  settingsService.loadUrl();
	}
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

	@JWTTokenNeeded
	@GZIP
	@GET
	@Path("/{uuidVenta}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response loadVentasByUuid(@PathParam("uuidVenta") String uuidVenta) {
		String TAG = "[VentasWs - cargarVentaExt uuidVenta:" + uuidVenta + "]";
		LOGGER.debug(TAG + " - init");

		try {

			AuthUserVo user = getSecurityIdUser();

			VentaPartVo ventaVo = ventasParticularService.loadByuuid(uuidVenta);
			if (ventaVo == null)
				throw new VentaNotFoundException();
			
			PersonaVo personaVo = personaService.loadByIdPersona(ventaVo.getIdPersona());
			
			VentaPartExtVo ventaPartExtVo = new VentaPartExtVo();
			ventaPartExtVo.setVenta(ventaVo);
			ventaPartExtVo.setPersona(personaVo);

			List<DocPartVo> documentos = docPartService.loadDocByIdVenta(ventaVo.getIdVenta());
			ventaPartExtVo.setDocumentos(documentos);

			Gson gson = initializesGson();
			return Response.ok(gson.toJson(ventaPartExtVo)).build();

		} catch (VentaNotFoundException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.NOT_FOUND).build();

		} catch (IllegalArgumentException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.BAD_REQUEST).build();

		} catch (NotAuthException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.UNAUTHORIZED).build();

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@JWTTokenNeeded
	@GZIP
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response search(@QueryParam("pageLimit") int pageLimit,
            @QueryParam("pageNumber") int pageNumber,VentaPartSearchRequestVo request) {
		String TAG = "[VentasWs - loadAll]";
		LOGGER.debug(TAG + " - init");

		try {
			AuthUserVo user = getSecurityIdUser();
			TAG = "[VentasWs - uuidAdm:" + user.getUuid() + " >> loadAll]";
			
			if (request==null)
				throw new IllegalArgumentException(TAG + " >> 'request' can not be null");
			
			 if (pageLimit < 0) {
		            throw new IllegalArgumentException(TAG + " >> 'pageLimit' debe ser mayor a 0");
		        }
			 
		     if (pageNumber < 0) {
		            throw new IllegalArgumentException(TAG + " >> 'pageNumber' debe ser mayor a 0");
		        }
		     

		     if (request.getFxInitCurso() != null) {
					Calendar fxVentaInitStartOfDayCurso = this.setStartOfDay(request.getFxInitCurso());
					request.setFxInitCurso(fxVentaInitStartOfDayCurso);
					LOGGER.info(TAG + ">> FxVentaInit: {} ", FORMAT_DATE.format(fxVentaInitStartOfDayCurso.getTime()));
				}		
		     
		     if (request.getFxFinCurso() != null) {
					Calendar fxVentaFinEndOfDayCurso = this.setEndOfDay(request.getFxFinCurso());
					request.setFxFinCurso(fxVentaFinEndOfDayCurso);
					LOGGER.info(TAG + ">> FxVentaFin: {} ", FORMAT_DATE.format(fxVentaFinEndOfDayCurso.getTime()));

				}
				
                                if (!hasParentCompanyFilter(request)) {
                                        VentaPartSearchResponseExtVo emptyResult = buildEmptyVentaPartSearchResponse();
                                        Gson gson = initializesGson();
                                        return Response.ok(gson.toJson(emptyResult)).build();
                                }

                                if (request.getFxVentaInit() != null) {
					Calendar fxVentaInitStartOfDay = this.setStartOfDay(request.getFxVentaInit());
					request.setFxVentaInit(fxVentaInitStartOfDay);
					LOGGER.info(TAG + ">> FxVentaInit: {} ", FORMAT_DATE.format(fxVentaInitStartOfDay.getTime()));
				}

				if (request.getFxVentaFin() != null) {
					Calendar fxVentaFinEndOfDay = this.setEndOfDay(request.getFxVentaFin());
					request.setFxVentaFin(fxVentaFinEndOfDay);
					LOGGER.info(TAG + ">> FxVentaFin: {} ", FORMAT_DATE.format(fxVentaFinEndOfDay.getTime()));

				}

				if (request.getFxVentaInit() != null && request.getFxVentaFin() == null) {
					Calendar fxVentaInitStartOfDay = this.setStartOfDay(request.getFxVentaInit());
					request.setFxVentaInit(fxVentaInitStartOfDay);
					LOGGER.info(TAG + ">> FxVentaInit: {} ", FORMAT_DATE.format(fxVentaInitStartOfDay.getTime()));

					Calendar fxVentaFinEndOfDay = this.setEndOfDay(request.getFxVentaInit());
					request.setFxVentaFin(fxVentaFinEndOfDay);
					LOGGER.info(TAG + ">> FxVentaFin: {} ", FORMAT_DATE.format(fxVentaFinEndOfDay.getTime()));
				}

				if (request.getFxVentaInit() == null && request.getFxVentaFin() != null) {
					Calendar fxVentaInitStartOfDay = this.setStartOfDay(request.getFxVentaFin());
					request.setFxVentaInit(fxVentaInitStartOfDay);
					LOGGER.info(TAG + ">> FxVentaInit: {} ", FORMAT_DATE.format(fxVentaInitStartOfDay.getTime()));

					Calendar fxVentaFinEndOfDay = this.setEndOfDay(request.getFxVentaFin());
					request.setFxVentaFin(fxVentaFinEndOfDay);
					LOGGER.info(TAG + ">> FxVentaFin: {} ", FORMAT_DATE.format(fxVentaFinEndOfDay.getTime()));
				}


                     VentaPartSearchResponseExtVo result = ventasParticularService.search(request, user, pageLimit, pageNumber);


                                Gson gson = initializesGson();
                                return Response.ok(gson.toJson(result)).build();
				
		} catch (IllegalArgumentException ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			return Response.status(Status.BAD_REQUEST).build();
		} catch (NotAuthException ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			return Response.status(Status.UNAUTHORIZED).build();
		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@JWTTokenNeeded
	@GZIP
	@POST
	@Path("/saveOrUpdate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveOrUpdate(VentaPartExtVo ventaPartExtVo) {
		String TAG = "[VentasWs - update]";
		LOGGER.debug(TAG + " - init");

		try {
			AuthUserVo user = getSecurityIdUser();
			TAG = "[SaleWs - uuidAdm:" + user.getUuid() + " >> saveOrUpdate]";

			VentaPartVo ventaVo = ventaPartExtVo.getVenta();
			if (ventaVo == null)
				throw new IllegalArgumentException(TAG + " >> ventaVo is null or empty...");

			VentaPartExtVo result = new VentaPartExtVo();
			if (ventaVo.getIdVenta() != null) {
				LOGGER.info(TAG + " - processing update....");
				result = this.updateVentaExt(TAG, user, ventaPartExtVo);

			} else {
				LOGGER.info(TAG + " - processing create....");
				result = this.createVentaExt(TAG, user, ventaPartExtVo);
			}

			Gson gson = initializesGson();
			return Response.ok(gson.toJson(result)).build();

		} catch (IllegalArgumentException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.BAD_REQUEST).build();

		} catch (NotAuthException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.UNAUTHORIZED).build();

		} catch (Exception ex) {
			LOGGER.error(TAG + " - ", ex);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	private VentaPartExtVo updateVentaExt(String TAG, AuthUserVo user, VentaPartExtVo ventaPartExtVo)
			throws VentaNotFoundException, PersonaNotFoundException, FormacionNotFoundException {

                PersonaVo personaVo = ventaPartExtVo.getPersona();
                personaService.update(personaVo, false);

                VentaPartVo ventaVo = ventaPartExtVo.getVenta();
                Long parentCompanyId = ventaVo != null ? ventaVo.getParentCompanyId() : null;

                if (ventaVo != null) {
                        ventaVo.setParentCompanyId(parentCompanyId);
                }


                if (ventaVo.getUuIdAgente() != null ) {
			AuthUserVo usuario = authService.loadByUuid(ventaVo.getUuIdAgente());
			
			if (usuario !=null ) {
				ventaVo.setUsernameAgente(usuario.getUsername());
				ventaVo.setUuIdAgente(usuario.getUuid());
			}
			
			if (usuario.getRole().equals(RoleEnum.AGENTE)) {
				ventaVo.setUsernameCoordinador(usuario.getCordinadorUsername());
				ventaVo.setUuIdCoordinador(usuario.getUuidCordinador());
				
				AuthUserVo usuarioSupervisor = authService.loadByCoordinadorUuid(usuario.getUuidCordinador());
				
				if (usuarioSupervisor != null) {
					ventaVo.setUsernameSupervisor(usuarioSupervisor.getSupervisorUsername());
					ventaVo.setUuIdSupervisor(usuarioSupervisor.getUuidSupervisor());
				}

			}else if (usuario.getRole().equals(RoleEnum.CORDINADOR)) {
				ventaVo.setUsernameCoordinador(usuario.getUsername());
				ventaVo.setUuIdCoordinador(usuario.getUuid());
				
				AuthUserVo usuarioSupervisor = authService.loadByCoordinadorUuid(usuario.getUuidCordinador());
				
				if (usuarioSupervisor != null) {
					ventaVo.setUsernameSupervisor(usuarioSupervisor.getSupervisorUsername());
					ventaVo.setUuIdSupervisor(usuarioSupervisor.getUuidSupervisor());
				}

				
			}else if (usuario.getRole().equals(RoleEnum.SUPERVISOR) || usuario.getRole().equals(RoleEnum.ADMINISTRADOR)
					|| usuario.getRole().equals(RoleEnum.BACKOFFICE)) {
		
				ventaVo.setUsernameCoordinador(usuario.getUsername());
				ventaVo.setUuIdCoordinador(usuario.getUuid());
				ventaVo.setUsernameSupervisor(usuario.getUsername());
				ventaVo.setUuIdSupervisor(usuario.getUuid());
				

			}	
		}
		ventasParticularService.update(ventaVo, false);

		VentaPartExtVo result = new VentaPartExtVo();
		result.setVenta(ventaVo);
		result.setPersona(personaVo);
		result.setDocumentos(ventaPartExtVo.getDocumentos());

		return result;
	}

	private VentaPartExtVo createVentaExt(String TAG, AuthUserVo user, VentaPartExtVo ventaPartExtVo) {

		TAG = TAG + " >> createVentaExt - ";
		LOGGER.info(TAG + " -  processing....");
                PersonaVo personaVo = ventaPartExtVo.getPersona();
                PersonaVo personaVoExist = personaService.loadByDni(personaVo.getDni());
                if (personaVoExist != null) {
                        LOGGER.info(TAG + " -  persona existente con DNI:{}....", personaVo.getDni());
                        personaVo.setIdPersona(personaVoExist.getIdPersona());
                }

		personaVo = personaService.saveOrUpdate(personaVo);
		LOGGER.info(TAG + " -  Empresa saved or updated con CIF:{} idEmpresa:{}....", personaVo.getDni(),
				personaVo.getIdPersona());

                VentaPartVo ventaVo = ventaPartExtVo.getVenta();
                Long parentCompanyId = ventaVo != null ? ventaVo.getParentCompanyId() : null;
                ventaVo.setIdPersona(personaVo.getIdPersona());

                ventaVo.setParentCompanyId(parentCompanyId);

                if (ventaVo.getFxVenta() == null) {
                        ventaVo.setFxVenta(Calendar.getInstance());
                }

		ventaVo.setUsernameAgente(user.getUsername());
		ventaVo.setUuIdAgente(user.getUuid());
		ventaVo.setStatus(StatusVentaEnum.PDTE_DOC);
		ventaVo.setStripePaymentStatus(StatusStripeEnum.PENDIENTE);

		if (user.getRole().toString().equals(RoleEnum.CORDINADOR.toString())) {
			ventaVo.setUsernameAgente(user.getUsername());
			ventaVo.setUuIdAgente(user.getUuid());
			ventaVo.setUsernameCoordinador(user.getUsername());
			ventaVo.setUuIdCoordinador(user.getUuid());
			ventaVo.setUuIdSupervisor(user.getUuidSupervisor());
			ventaVo.setUsernameSupervisor(user.getSupervisorUsername());

		} else if (user.getRole().toString().equals(RoleEnum.AGENTE.toString())) {
			ventaVo.setUsernameAgente(user.getUsername());
			ventaVo.setUuIdAgente(user.getUuid());
			ventaVo.setUuIdCoordinador(user.getUuidCordinador());
			ventaVo.setUsernameCoordinador(user.getCordinadorUsername());

			AuthUserVo coordinador = authService.loadByCoordinadorUuid(user.getUuidCordinador());
			if (coordinador != null) {
				ventaVo.setUsernameSupervisor(coordinador.getSupervisorUsername());
				ventaVo.setUuIdSupervisor(coordinador.getUuidSupervisor());
			}
		} else if (user.getRole().toString().equals(RoleEnum.SUPERVISOR.toString())
				|| user.getRole().toString().equals(RoleEnum.BACKOFFICE.toString())
				|| user.getRole().toString().equals(RoleEnum.ADMINISTRADOR.toString())) {
			ventaVo.setUsernameSupervisor(user.getUsername());
			ventaVo.setUuIdSupervisor(user.getUuid());

			ventaVo.setUsernameCoordinador(user.getUsername());
			ventaVo.setUuIdCoordinador(user.getUuid());

			ventaVo.setUsernameAgente(user.getUsername());
			ventaVo.setUuIdAgente(user.getUuid());
		}


		ventaVo = ventasParticularService.create(ventaVo);
		LOGGER.info(TAG + " - Venta created with id:{}....", ventaVo.getIdVenta());

		VentaPartExtVo result = new VentaPartExtVo();
		result.setVenta(ventaVo);
		result.setPersona(personaVo);
		result.setVentaPartSearchResponseExtVo(ventaPartExtVo.getVentaPartSearchResponseExtVo());
		result.setDocumentos(ventaPartExtVo.getDocumentos());

		return result;
	}

	@JWTTokenNeeded
	@GZIP
	@DELETE
	@Path("/{uuidVenta}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteVenta(@PathParam("uuidVenta") String uuidVenta) {
		String TAG = "[EmpresaWs - deleteVenta]";
		LOGGER.debug(TAG + " - init");

		try {
			AuthUserVo user = getSecurityIdUser();
			TAG = "[SaleWs - uuidAdm:" + user.getUuid() + " >> register]";

			if (uuidVenta == null)
				throw new IllegalArgumentException(TAG + " >> 'uuidVenta' can not be null");

			VentaPartVo ventas = ventasParticularService.loadByuuid(uuidVenta);
			if (ventas == null)
				throw new VentaNotFoundException();

			ventasParticularService.delete(uuidVenta);

			return Response.ok().build();

		} catch (IllegalArgumentException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.BAD_REQUEST).build();

		} catch (VentaNotFoundException ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			return Response.serverError().status(Status.NOT_FOUND).build();

		} catch (NotAuthException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.UNAUTHORIZED).build();

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}", ex.getMessage());
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	
	@JWTTokenNeeded
    @GZIP
    @POST
    @Path("/download")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response searchExcel(VentaPartSearchRequestVo request) {
        long t = System.currentTimeMillis();
        String TAG = "[VentasWs -" + t + " search]";
        LOGGER.debug(TAG + " - init");


        File dir = new File(BASE_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filename = "resultados_" + System.currentTimeMillis() + ".xlsx"; 
        String filePath = BASE_PATH + filename;

        try {
            AuthUserVo user = getSecurityIdUser();
            TAG = "[VentasWs - uuidAdm:" + user.getUuid() + " >> search]";

            if (request == null) {
                throw new IllegalArgumentException(TAG + " >> 'request' can not be null");
            }
           

            LOGGER.info(TAG + ">> {} ", request.toString());

            if (!hasParentCompanyFilter(request)) {
                throw new IllegalArgumentException(TAG + " >> 'parentCompanyId' es requerido");
            }

            if (request.getFxVentaInit() != null) {
                Calendar fxVentaInitStartOfDay = this.setStartOfDay(request.getFxVentaInit());
                request.setFxVentaInit(fxVentaInitStartOfDay);
                LOGGER.info(TAG + ">> FxVentaInit: {} ", FORMAT_DATE.format(fxVentaInitStartOfDay.getTime()));
            }

            if (request.getFxVentaFin() != null) {
                Calendar fxVentaFinEndOfDay = this.setEndOfDay(request.getFxVentaFin());
                request.setFxVentaFin(fxVentaFinEndOfDay);
                LOGGER.info(TAG + ">> FxVentaFin: {} ", FORMAT_DATE.format(fxVentaFinEndOfDay.getTime()));
            }

            
            VentaPartSearchResponseExtVo result = ventasParticularService.search(request, user, null, null);

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Resultados");

                Row headerRow = sheet.createRow(0);
                String[] headers = { "Fecha de Venta",  "Dni",
                                    "Estado de Venta", "Agente","Nombre Formacion","Cobrado"};

                for (int i = 0; i < headers.length; i++) {
                    headerRow.createCell(i).setCellValue(headers[i]);
                }

                int rowNum = 1;
                for (VentaPartSearchResponseVo item : result.getData()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(FORMAT_DATE.format(item.getFxVenta().getTime()));
                    row.createCell(1).setCellValue(item.getDni());
                    row.createCell(2).setCellValue(item.getStatus() != null ? item.getStatus().name() : "");
                    row.createCell(3).setCellValue(item.getUsernameAgente());
                    row.createCell(4).setCellValue(item.getNombreFormacion());
                    row.createCell(5).setCellValue(item.getCharged());



                }

                try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                    workbook.write(fileOut);
                }
            }

            String downloadUrl = generateDownloadUrl(filename);
           
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("url", downloadUrl);
            LOGGER.info(TAG + " - Download URL: " + downloadUrl);

            return Response.ok(resultMap, MediaType.APPLICATION_JSON).build();

        } catch (IllegalArgumentException ex) {
            LOGGER.error(TAG + " - Error:{} ", ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();

        } catch (NotAuthException ex) {
            LOGGER.error(TAG + " - Error:{} ", ex.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();

        } catch (Exception ex) {
            LOGGER.error(TAG + " - Error ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error interno del servidor.").build();
        }
    }

    private String generateDownloadUrl(String filename) {
        String baseUrl = URL +"api/v1.0/ventas/downloads/";
        return baseUrl + filename;
    }

    private VentaPartSearchResponseExtVo buildEmptyVentaPartSearchResponse() {
        VentaPartSearchResponseExtVo response = new VentaPartSearchResponseExtVo();
        response.setTOTAL_RECORDS(0L);
        response.setTOTAL_PAGES(0L);
        response.setTOTAL_CHARGED(0.0);
        response.setTOTAL_PENDING_CHARGED(0.0);
        response.setData(Collections.emptyList());
        return response;
    }

    private boolean hasParentCompanyFilter(VentaPartSearchRequestVo request) {
        if (request == null) {
            return false;
        }
        if (request.getParentCompanyId() != null) {
            return true;
        }
        return request.getParentCompanyIds() != null
                && request.getParentCompanyIds().stream().anyMatch(Objects::nonNull);
    }

    @GET
    @Path("/downloads/{filename}")
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public Response downloadFile(@PathParam("filename") String filename) {
        String filePath = BASE_PATH + filename; 

        File file = new File(filePath);
        if (!file.exists()) {
            return Response.status(Response.Status.NOT_FOUND).entity("File not found").build();
        }

        StreamingOutput fileStream = new StreamingOutput() {
            @Override
            public void write(OutputStream out) throws IOException {
                try (FileInputStream input = new FileInputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = input.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
            }
        };

        return Response.ok(fileStream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .build();
    }
    
    
	private Calendar setStartOfDay(Calendar c) {
		Calendar fx = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
		fx.setTimeInMillis(c.getTimeInMillis());
		fx.set(Calendar.HOUR_OF_DAY, 0);
		fx.set(Calendar.MINUTE, 0);
		fx.set(Calendar.SECOND, 0);
		fx.set(Calendar.MILLISECOND, 0);
		return fx;
	}

	private Calendar setEndOfDay(Calendar c) {
		Calendar fx = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
		fx.setTimeInMillis(c.getTimeInMillis());
		fx.set(Calendar.HOUR_OF_DAY, 23);
		fx.set(Calendar.MINUTE, 59);
		fx.set(Calendar.SECOND, 59);
		fx.set(Calendar.MILLISECOND, 999);
		return fx;
	}

}
