package com.zap.sales.ws;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
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
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.zap.maintenance.service.adapter.CalendarDeserializer;
import com.zap.maintenance.service.adapter.CalendarSerializer;
import com.zap.maintenance.service.crypto.TripleDesService;
import com.zap.maintenance.service.settings.SettingsService;
import com.zap.sales.exception.venta.VentaNotFoundException;
import com.zap.sales.service.AlumnoService;
import com.zap.sales.service.DocPartService;
import com.zap.sales.service.EmpresaService;
import com.zap.sales.service.FormacionService;
import com.zap.sales.service.VentasParticularService;
import com.zap.sales.service.VentasService;
import com.zap.sales.vo.alumno.AlumnoVo;
import com.zap.sales.vo.doc.DocPartVo;
import com.zap.sales.vo.empresa.EmpresaVo;
import com.zap.sales.vo.formacion.FormacionVo;
import com.zap.sales.vo.particular.VentaPartVo;
import com.zap.security.exception.authenticate.NotAuthException;
import com.zap.security.filter.JWTTokenNeeded;
import com.zap.security.service.authenticate.AuthService;
import com.zap.security.vo.authenticate.AuthUserVo;



@Path("/doc-particular")
public class DocPartWs {

	private static final Logger LOGGER = LoggerFactory.getLogger(VentasWs.class);
	private Gson gson;
	
//	private static final String  PATCH_TEMPLATE = "/opt/share/zapcube/template/template.pdf";
//	private static final String PATH = "/opt/share/zapcube/temp/";
//	private static final String  PATCH_TEMPLATE = "\\Users\\Emanuel\\wildfly-21.0.2.Final\\standalone\\tmp\\template.pdf";
//	private static final String PATH = "C:\\Users\\Emanuel\\wildfly-21.0.2.Final\\standalone\\tmp";
	private static String  PATCH_TEMPLATE = "";
	private static String PATH = "";
	private static String URL = "";
	
    @Context
	SecurityContext securityContext;
    
    @Inject
    DocPartService docPartService;
    
    
	@Inject
	AuthService authService;
	
	@Inject
	FormacionService formacionService;
	
		
	@Inject
	SettingsService settingsService;
	
	@Inject
	TripleDesService tripleDesService;
	
	@Inject
	VentasParticularService ventasParticularService;
	
	@Inject
	AlumnoService alumnoService;
	
	@PostConstruct
	public void initializes()
	{
		PATH =  settingsService.getAppPath()+"temp/";
		PATCH_TEMPLATE=  settingsService.loadPathTemplateContrato();
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
	
	@GET
    @Path("/fields")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPdfFields() {
        String pdfPath = "encomienda.pdf";
        StringBuilder result = new StringBuilder();

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(pdfPath);

        if (inputStream == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("File not found").build();
        }

        try {
            PdfReader reader = new PdfReader(inputStream);
            AcroFields fields = reader.getAcroFields();
            Map<String, AcroFields.Item> formFields = fields.getFields();

            for (Map.Entry<String, AcroFields.Item> entry : formFields.entrySet()) {
                String fieldName = entry.getKey();
                String fieldValue = fields.getField(fieldName);
                result.append(fieldName).append(": ").append(fieldValue).append("\n");
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error reading PDF").build();
        }

        return Response.ok(result.toString()).build();
    }
	
	
	@JWTTokenNeeded
	@GZIP
	@GET
	@Path("/loadDocumentsByUuidVenta/{uuIdVenta}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response loadDocumentsByuuIdVenta(@PathParam("uuIdVenta") String uuIdVenta) {
		String TAG = "[VentasWs - loadDocuments uuId:" + uuIdVenta + "]";
		LOGGER.debug(TAG + " - init");

		try {

			AuthUserVo user = getSecurityIdUser();
			TAG = "[SaleWs - uuidAdm:" + user.getUuid() + " >>  register]";

			VentaPartVo ventaPartVo = ventasParticularService.loadByuuid(uuIdVenta);
			if(ventaPartVo==null)
				throw new VentaNotFoundException();
			
			List<DocPartVo> docPartVo = docPartService.loadDocByIdVenta(ventaPartVo.getIdVenta());
			Gson gson = initializesGson();
			
			
			return Response.ok(gson.toJson(docPartVo)).build();
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
	
	


	private String generateDownloadUrl(DocPartVo fileVo) throws Exception {
	    String TAG = "[DocWs - generateDownloadUrl]";
	    LOGGER.info(TAG + " - input " + fileVo.toString());

	    String TOKENIZER = settingsService.getTokenizer();
	    StringBuilder token = new StringBuilder();
	    token.append(fileVo.getFilepath()).append(TOKENIZER);
	    token.append(fileVo.getFilename()).append(TOKENIZER);
	    token.append(fileVo.getPath()).append(TOKENIZER);
	    token.append(System.currentTimeMillis());

	    LOGGER.info(TAG + " - Build Token: " + token.toString());

	    LOGGER.info(TAG + " - Encrypting Token....");
	    String encToken = tripleDesService.encoding3Des(token.toString());

	    String encTokenStr = URLEncoder.encode(encToken, "UTF-8");
	    LOGGER.info(TAG + " - Encoding to URL: " + encTokenStr);

	    return URL+"/api/v1.0/DownloadFileServlet?t=" + encTokenStr;
//	    return "http://localhost:1024/zapcube.be/api/v1.0/DownloadFileServlet?t=" + encTokenStr;
	}



//	@JWTTokenNeeded
//	@GZIP
//    @POST
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response loadByIdVenta() {
//        String TAG = "[AlumnoWs - loadAll]";
//        LOGGER.debug(TAG + " - init");
//
//        try {
//        	
//        	AuthUserVo user = getSecurityIdUser();
//	        TAG = "[SaleWs - uuidAdm:" + user.getUuid() + " >> register]";
//	        
//        	
//            List<AlumnoVo> alumnos = alumnoService.loadAll();
//            Gson gson = initializesGson();
//            return Response.ok(gson.toJson(alumnos)).build();
//        }catch (IllegalArgumentException ex) {
//	        LOGGER.error(TAG + " - Error: {}", ex);
//	        return Response.serverError().status(Status.BAD_REQUEST).build();
//
//	    } catch (NotAuthException ex) {
//	        LOGGER.error(TAG + " - Error: {}", ex);
//	        return Response.serverError().status(Status.UNAUTHORIZED).build();
//
//	    }  catch (Exception ex) {
//            LOGGER.error(TAG + " - Error: {}", ex.getMessage());
//            return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
//        }
//    }

//    @JWTTokenNeeded
//	@GZIP
//	@DELETE
//	@Path("/{idDoc}")
//	public Response delete(@PathParam("idDoc") Integer idAlumno) {
//	    String TAG = "[AlumnoWs - delete]";
//	    LOGGER.debug(TAG + " - init");
//
//	    try {
//	    	 AuthUserVo user = getSecurityIdUser();
//		     TAG = "[SaleWs - uuidAdm:" + user.getUuid() + " >> register]";
//	        docService.delete(idDoc);
//	        
//	        return Response.ok().build();
//	    } catch (AlumnoNotFoundException ex) {
//	        LOGGER.error(TAG + " - Error: ", ex.getMessage());
//	        return Response.status(Status.NOT_FOUND).build();
//	    } catch (NotAuthException ex) {
//	        LOGGER.error(TAG + " - Error: {}", ex);
//	        return Response.serverError().status(Status.UNAUTHORIZED).build();
//	    } catch (AlumnoAsociadoACursoException ex) {
//	        LOGGER.error(TAG + " - Error: ", ex.getMessage());
//	        return Response.status(Status.CONFLICT).entity(ex.getMessage()).build();
//	    } catch (Exception ex) {
//	        LOGGER.error(TAG + " - Error: ", ex.getMessage());
//	        return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
//	    }
//	}
}
