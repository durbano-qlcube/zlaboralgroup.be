package com.zap.scheduling.ws;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
import com.zap.scheduling.job.SincroZapCubeToOcmJob;
import com.zap.scheduling.job.SincroOcmToZlaboralJob;
import com.zap.scheduling.job.ZapCubeToGestDirectLeadsJob;
import com.zap.scheduling.job.ZapCubeToGestDirectVenta;
import com.zap.scheduling.job.EmpresaCreditoUpdateJob;
import com.zap.scheduling.job.OrgVentaToMongoJob;

@Path("/caller")
public class JobWs {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobWs.class);
	private SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static Gson gson;
	
	@Context
	SecurityContext securityContext;
	

	@Inject
	SincroZapCubeToOcmJob sincroCapturadorToOcmJob;
	
	@Inject
	SincroOcmToZlaboralJob sincroOcmToCapJob;
	
    @Inject
    ZapCubeToGestDirectLeadsJob acquisitionTransferJob;
   
    @Inject
    ZapCubeToGestDirectVenta ventaTransferJob;
    
    @Inject
    EmpresaCreditoUpdateJob empresaCreditoUpdateJob;
    	
	@Inject
	OrgVentaToMongoJob orgventatomongojob;
	
	private Gson initializesGson()
	{
		if (gson == null)
		{
			gson = new GsonBuilder()
			.registerTypeAdapter(Calendar.class, new CalendarSerializer())
			.registerTypeAdapter(Calendar.class, new CalendarDeserializer())
			.registerTypeAdapter(GregorianCalendar.class,
			new CalendarSerializer()).create();
		}
		return gson;
	}


	

	
	
//	private String getSecurityIdUser() throws NotAuthException
//	{
//		Principal principal = securityContext.getUserPrincipal();
//		String id = principal.getName();
//
//
//		if (id == null)
//			throw new NotAuthException(" - 'id' can not be null");
//
//		return id;
//	}
	
	
	//@JWTTokenNeeded
	@GZIP
	@GET
	@Path("/sincroCapturadorToOcmJob")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sincroCapturadorToOcmJob ()
	{
		String TAG="[CallerWs - sincroCapturadorToOcmJob]";
		try{
			sincroCapturadorToOcmJob.doExecute();
			
			Gson gson = initializesGson();
			return Response.ok(MediaType.APPLICATION_JSON).build();

		}catch (IllegalArgumentException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.BAD_REQUEST).build();
//
//		}catch (NotAuthException ex){
//			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
//			return Response.serverError().status(Status.UNAUTHORIZED).build();

		}catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@GZIP
	@GET
	@Path("/sincroOcmToCapJob")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response SincroOcmToCapJob ()
	{
		String TAG="[CallerWs - SincroOcmToCapJob]";
		try{
			sincroOcmToCapJob.doExecute();
			
			Gson gson = initializesGson();
			return Response.ok(MediaType.APPLICATION_JSON).build();

		}catch (IllegalArgumentException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.BAD_REQUEST).build();
//
//		}catch (NotAuthException ex){
//			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
//			return Response.serverError().status(Status.UNAUTHORIZED).build();

		}catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	
	@POST
    @Path("/runSincro")
    public Response runTransferJob() {
        try {
            LOGGER.info("EJECUTANDO Acquisition Transfer Job...");
            acquisitionTransferJob.doExecute();
            LOGGER.info("Acquisition Transfer Job executed successfully.");
            return Response.ok("Acquisition Transfer Job EJECUTADO EXITOSAMENTE").build();
        } catch (Exception e) {
            LOGGER.error("Error executing Acquisition Transfer Job: ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error executing Acquisition Transfer Job: " + e.getMessage())
                    .build();
        }
    }
	
	
	@POST
    @Path("/runSincroVenta")
    public Response runTransferVentaJob() {
        try {
            LOGGER.info("EJECUTANDO Venta Transfer Job..."); 
            ventaTransferJob.doExecute();
            LOGGER.info("Venta Transfer Job executed successfully.");
            return Response.ok("Venta Transfer Job EJECUTADO EXITOSAMENTE").build();
        } catch (Exception e) {
            LOGGER.error("Error executing Venta Transfer Job: ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error executing Venta Transfer Job: " + e.getMessage())
                    .build();
        }
    }
	
	
	@POST
    @Path("/creditoUpdateJob")
    public Response EmpresaCreditoUpdateJob() {
        try {
            LOGGER.info("EJECUTANDO CreditoUpdate Transfer Job..."); 
            empresaCreditoUpdateJob.doExecute();
            LOGGER.info("Transfer Job executed successfully.");
            return Response.ok("Transfer Job EJECUTADO EXITOSAMENTE").build();
        } catch (Exception e) {
            LOGGER.error("Error executing Transfer Job: ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error executing Transfer Job: " + e.getMessage())
                    .build();
        }
    }

	
	
	@GZIP
    @GET
    @Path("/orgVentaToMongoJob")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response TrasladoVentasMongo() {
        String TAG = "[CallerWs - JobDataInserter]"; 
        try {
            LOGGER.info(TAG + " - Iniciando la ejecución del Job OrgVentaToMongoJob.");

            orgventatomongojob.process();

            LOGGER.info(TAG + " - Job OrgVentaToMongoJob completado con éxito.");
            
            return Response.status(Response.Status.OK)
                           .entity("El proceso OrgVentaToMongoJob se ejecutó exitosamente.")
                           .build();
        } catch (Exception ex) {
            LOGGER.error(TAG + " - Error: {}", ex.getLocalizedMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Error interno del servidor: " + ex.getMessage())
                           .build();
        }
    }





}
