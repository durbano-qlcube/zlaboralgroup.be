package com.zap.sales.ws;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import javax.ws.rs.core.StreamingOutput;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.jboss.resteasy.annotations.GZIP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zap.maintenance.service.adapter.CalendarDeserializer;
import com.zap.maintenance.service.adapter.CalendarSerializer;
import com.zap.maintenance.service.settings.SettingsService;
import com.zap.sales.exception.empresa.EmpresaConVentasException;
import com.zap.sales.exception.empresa.EmpresaNotFoundException;
import com.zap.sales.service.AlumnoService;
import com.zap.sales.service.EmpresaService;
import com.zap.sales.service.FormacionService;
import com.zap.sales.service.VentasService;
import com.zap.sales.vo.empresa.EmpresaSearchRequestVo;
import com.zap.sales.vo.empresa.EmpresaSearchResponseVo;
import com.zap.sales.vo.empresa.EmpresaVo;
import com.zap.security.exception.authenticate.NotAuthException;
import com.zap.security.filter.JWTTokenNeeded;
import com.zap.security.service.authenticate.AuthService;
import com.zap.security.vo.authenticate.AuthUserVo;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


@Path("/empresa")
public class EmpresaWs {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmpresaWs.class);
	private Gson gson;
	private static String BASE_PATH = "";
	private static String URL = "";

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
	AuthService authService;
	
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
	@POST
	@Path("/saveOrUpdate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveOrUpdate(EmpresaVo empresaVo) {
		String TAG = "[EmpresaWs - saveOrUpdate]";
		LOGGER.debug(TAG + " - init");

		try {

			AuthUserVo user = getSecurityIdUser(); /// Poner error 401 cuando no ingrese el usuario o se vencio el token
			TAG = "[SaleWs - uuidAdm:" + user.getUuid() + " >>  register]";

			if (empresaVo.getOrigin() == null || empresaVo.getOrigin().isEmpty()) {
				empresaVo.setOrigin("LABORALGROUP");
				empresaVo.setOriginUserUsername(user.getUsername());

			}
			
			if (empresaVo.getIdEmpresa() == null ) {
				empresaVo.setOriginUserUsername(user.getUsername());
				empresaVo.setOriginUserUuid(user.getUuid());
			}


			if (empresaVo.getEstado() == null || empresaVo.getEstado().isEmpty()) {
				empresaVo.setEstado("NUEVA");
			}

			if (empresaVo.getBonificacion() == null) {
				empresaVo.setBonificacion(BigDecimal.ZERO);
			}

			if (empresaVo.getCreditosDisponibles() == null) {
				empresaVo.setCreditosDisponibles(BigDecimal.ZERO);
			}

			if (empresaVo.getCreditosGastados() == null) {
				empresaVo.setCreditosGastados(BigDecimal.ZERO);
			}

			EmpresaVo savedEmpresa = empresaService.saveOrUpdate(empresaVo);

			Gson gson = initializesGson();
			return Response.ok(gson.toJson(savedEmpresa), MediaType.APPLICATION_JSON).build();
		} catch (IllegalArgumentException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.BAD_REQUEST).build();

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
	@GET
	@Path("/{uuIdEmpresa}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response load(@PathParam("uuIdEmpresa") String uuIdEmpresa) {
		String TAG = "[EmpresaWs - load]";
		LOGGER.debug(TAG + " - init");

		try {
			AuthUserVo user = getSecurityIdUser();
			TAG = "[SaleWs - uuidAdm:" + user.getUuid() + " >> register]";

			if (uuIdEmpresa == null)
				throw new IllegalArgumentException(TAG + " >> 'uuid' can not be null");

			EmpresaVo empresaVo = empresaService.loadByUuId(uuIdEmpresa);
			if (empresaVo == null)
				throw new EmpresaNotFoundException();

			Gson gson = initializesGson();
			return Response.ok(gson.toJson(empresaVo)).build();

		} catch (EmpresaNotFoundException ex) {
			LOGGER.error(TAG + " - Error: {}", ex.getMessage());
			return Response.status(Status.NOT_FOUND).build();

		} catch (IllegalArgumentException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.BAD_REQUEST).build();

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
	@DELETE
	@Path("/{uuIdEmpresa}")
	public Response delete(@PathParam("uuIdEmpresa") String uuIdEmpresa) {
		String TAG = "[EmpresaWs - delete]";
		LOGGER.debug(TAG + " - init");

		try {
			AuthUserVo user = getSecurityIdUser();
			TAG = "[SaleWs - uuidAdm:" + user.getUuid() + " >>  register]";

			// TODO: CHECK

			EmpresaVo empresa = empresaService.loadByUuId(uuIdEmpresa);
			empresaService.delete(empresa.getIdEmpresa());
			return Response.ok().build();

//		} catch (EmpresaConVentasException ex) {
//			LOGGER.error(TAG + " - Error: {}", ex);
//			return Response.serverError().status(Status.CONFLICT).build();

		} catch (EmpresaNotFoundException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.NOT_FOUND).build();

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
	@Path("/search")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response search(EmpresaSearchRequestVo request) {
		String TAG = "[EmpresaWs - search]";
		try {

			AuthUserVo user = getSecurityIdUser();
			TAG = "[EmpresaWs - uuidAdm:" + user.getUuid() + " >> search]";

			List<EmpresaSearchResponseVo> data = empresaService.search(request, user);
			if (data == null) {
				data = Collections.emptyList();
			}

			Gson gson = initializesGson();
			return Response.ok(gson.toJson(data), MediaType.APPLICATION_JSON).build();

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
	@Path("/download")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response searchExcel(EmpresaSearchRequestVo request) {
	    long t = System.currentTimeMillis();
	    String TAG = "[EmpresaWs -" + t + " download]";
	    LOGGER.debug(TAG + " - init");

//	    BASE_PATH = "C:\\Users\\Emanuel\\wildfly-21.0.2.Final\\standalone\\";
	    File dir = new File(BASE_PATH);
	    if (!dir.exists()) {
	        dir.mkdirs();
	    }

	    String filename = "empresas_" + System.currentTimeMillis() + ".xlsx";
	    String filePath = BASE_PATH + filename;

	    try {
	        AuthUserVo user = getSecurityIdUser();
	        TAG = "[EmpresaWs - uuidAdm:" + user.getUuid() + " >> download]";

	        List<EmpresaSearchResponseVo> data = empresaService.search(request, user);

	        try (Workbook workbook = new XSSFWorkbook()) {
	            Sheet sheet = workbook.createSheet("Empresas");

	            Row headerRow = sheet.createRow(0);
	            String[] headers = { "Cif", "Nombre Empresa", "Estado", "Origen", 
	                                 "Usuario origen", "Creditos", "Disponibles", "Gastados" };

	            for (int i = 0; i < headers.length; i++) {
	                headerRow.createCell(i).setCellValue(headers[i]);
	            }

	            int rowNum = 1;
	            for (EmpresaSearchResponseVo item : data) {
	                Row row = sheet.createRow(rowNum++);
	                row.createCell(0).setCellValue(item.getCif());
	                row.createCell(1).setCellValue(item.getNombreComercial());
	                row.createCell(2).setCellValue(item.getEstado());
	                row.createCell(3).setCellValue(item.getOrigin());
	                row.createCell(4).setCellValue(item.getOriginUserUsername());
	                row.createCell(5).setCellValue(item.getBonificacion() != null ? item.getBonificacion().doubleValue() : 0.0);
	                row.createCell(6).setCellValue(item.getCreditosDisponibles() != null ? item.getCreditosDisponibles().doubleValue() : 0.0);
	                row.createCell(7).setCellValue(item.getCreditosGastados() != null ? item.getCreditosGastados().doubleValue() : 0.0);

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
	        String baseUrl = URL +"api/v1.0/empresa/downloads/"; 
	        return baseUrl + filename; 
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
	
	@JWTTokenNeeded
	@GZIP
	@GET
	@Path("/loadByCif/{cif}")
	public Response validateCIF(@PathParam("cif") String cif) {
		String TAG = "[CIFValidationEndpoint - validateCIF cif:" + cif + "]";
		LOGGER.debug(TAG + " - init");

		try {

			AuthUserVo user = getSecurityIdUser();
			TAG = "[SaleWs - uuidAdm:" + user.getUuid() + " >> register]";

			EmpresaVo empresa = empresaService.loadByCif(cif);

			if (empresa == null) {
				return Response.status(Status.NOT_FOUND).entity("{\"message\": \"CIF no existe en la base de datos.\"}")
						.type(MediaType.APPLICATION_JSON).build();
			} else if (empresa.getCif().isEmpty()) {
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
				return Response.status(Status.CONFLICT).entity("{\"message\": \"La empresa no existe en el sistema\"}")
						.type(MediaType.APPLICATION_JSON).build();
			}

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR)
					.entity("{\"error\":\"Error interno del servidor.\"}").build();
		}
	}

}
