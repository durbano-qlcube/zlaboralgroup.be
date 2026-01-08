package com.zap.sales.ws;

import java.security.Principal;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import com.zap.maintenance.service.adapter.CalendarDeserializer;
import com.zap.maintenance.service.adapter.CalendarSerializer;
import com.zap.sales.exception.alumno.AlumnoAsociadoACursoException;
import com.zap.sales.exception.alumno.AlumnoNotFoundException;
import com.zap.sales.service.AlumnoService;
import com.zap.sales.service.EmpresaService;
import com.zap.sales.vo.alumno.AlumnoVo;
import com.zap.security.exception.authenticate.NotAuthException;
import com.zap.security.filter.JWTTokenNeeded;
import com.zap.security.service.authenticate.AuthService;
import com.zap.security.vo.authenticate.AuthUserVo;

@Path("/alumno")
public class AlumnoWs {

	private static final Logger LOGGER = LoggerFactory.getLogger(VentasWs.class);
	private Gson gson;

    
    @Context
	SecurityContext securityContext;
    
    @Inject
    AlumnoService alumnoService;
    
    @Inject
    EmpresaService empresaService;
    
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


	@JWTTokenNeeded
	@GZIP
    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(AlumnoVo alumnoVo) {
        String TAG = "[AlumnoWs - create]";
        LOGGER.debug(TAG + " - init");

        try {
        	AuthUserVo user = getSecurityIdUser();
	        TAG = "[AlumnoWs - alumno:" + user.getUuid() + " >> register]";
	        
        	
            AlumnoVo createdAlumno = alumnoService.create(alumnoVo);
            
            
            Gson gson = initializesGson();
            return Response.ok(gson.toJson(createdAlumno)).build();
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

//    @JWTTokenNeeded
//	@GZIP
//    @GET
//    @Path("/{idAlumno}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response load(@PathParam("idAlumno") Integer idAlumno) {
//        String TAG = "[AlumnoWs - load]";
//        LOGGER.debug(TAG + " - init");
//
//        try {
//        	
//        	AuthUserVo user = getSecurityIdUser();
//	        TAG = "[SaleWs - uuidAdm:" + user.getUuid() + " >> register]";
//	        
//	        
//	        
//        	  List<AlumnoVo> alumno = alumnoService.load(idAlumno);
//        	  if (alumno.isEmpty()) {
//                  return Response.status(Status.NOT_FOUND).build();
//              } else {
//            	  Gson gson = initializesGson();
//                  return Response.ok(gson.toJson(alumno)).build();
//              }
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
//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response loadAll() {
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

    @JWTTokenNeeded
	@GZIP
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/update")
    public Response update(AlumnoVo alumnoVo) {
        String TAG = "[AlumnoWs - update]";
        LOGGER.debug(TAG + " - init");

        try {
        	AuthUserVo user = getSecurityIdUser();
	        TAG = "[SaleWs - uuidAdm:" + user.getUuid() + " >> register]";
	        
            alumnoService.update(alumnoVo,false);
            

	        
            return Response.ok().build();
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
	@Path("/{idAlumno}")
	public Response delete(@PathParam("idAlumno") Integer idAlumno) {
	    String TAG = "[AlumnoWs - delete]";
	    LOGGER.debug(TAG + " - init");

	    try {
	    	 AuthUserVo user = getSecurityIdUser();
		     TAG = "[SaleWs - uuidAdm:" + user.getUuid() + " >> register]";
	        alumnoService.delete(idAlumno);
	        
	        return Response.ok().build();
	    } catch (AlumnoNotFoundException ex) {
	        LOGGER.error(TAG + " - Error: ", ex.getMessage());
	        return Response.status(Status.NOT_FOUND).build();
	    } catch (NotAuthException ex) {
	        LOGGER.error(TAG + " - Error: {}", ex);
	        return Response.serverError().status(Status.UNAUTHORIZED).build();

	        //	    } catch (AlumnoAsociadoACursoException ex) {
//	        LOGGER.error(TAG + " - Error: ", ex.getMessage());
//	        return Response.status(Status.CONFLICT).entity(ex.getMessage()).build();
	    
	    } catch (Exception ex) {
	        LOGGER.error(TAG + " - Error: ", ex.getMessage());
	        return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
	    }
	}
    
    
	@JWTTokenNeeded
	@GZIP
	@POST    
	@Path("/loadTrabajador/{idEmpresa}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response loadTrabajadoresByEmpresa(@PathParam("idEmpresa") Integer idEmpresa)
	{
	    String TAG = "[AlumnoWs - loadTrabajadoresByEmpresa]";
	    LOGGER.debug(TAG + " - init");

	    try {
	        AuthUserVo user = getSecurityIdUser();
	        TAG = "[SaleWs - uuidAdm:" + user.getUuid() + " >> register]";

	        if (idEmpresa == null)
	            throw new IllegalArgumentException(TAG + " >> 'uuid' can not be null");

	        List<AlumnoVo> trabajadores = alumnoService.loadByIdEmpresa(idEmpresa);

	        Gson gson = initializesGson();
	        return Response.ok(gson.toJson(trabajadores)).build();

	    }catch (IllegalArgumentException ex) {
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
}
