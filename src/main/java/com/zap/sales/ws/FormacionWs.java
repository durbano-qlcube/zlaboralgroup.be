package com.zap.sales.ws;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.annotations.GZIP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zap.sales.service.FormacionService;
import com.zap.sales.vo.formacion.FormacionVo;
import com.zap.security.filter.JWTTokenNeeded;

@Path("/formacion")  
public class FormacionWs {

    private static final Logger LOGGER = LoggerFactory.getLogger(FormacionWs.class);
    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    @Inject
    FormacionService formacionService;

    
    @JWTTokenNeeded
	@GZIP
    @POST
    @Path("/create")  
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(FormacionVo formacionVo) {
        String TAG = "[FormacionWs - create]";
        LOGGER.debug(TAG + " - init");

        try {
            FormacionVo createdFormacion = formacionService.create(formacionVo);
            return Response.ok(gson.toJson(createdFormacion)).build();
        } catch (Exception ex) {
            LOGGER.error(TAG + " - Error: {}", ex.getMessage());
            return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @JWTTokenNeeded
	@GZIP
    @GET
    @Path("/{idFormacion}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response load(@PathParam("idFormacion") Integer idFormacion) {
        String TAG = "[FormacionWs - load]";
        LOGGER.debug(TAG + " - init");

        try {
            FormacionVo formacionVo = formacionService.load(idFormacion);
            if (formacionVo == null) {
                return Response.status(Status.NOT_FOUND).build();
            } else {
                return Response.ok(gson.toJson(formacionVo)).build();
            }
        } catch (Exception ex) {
            LOGGER.error(TAG + " - Error: {}", ex.getMessage());
            return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @JWTTokenNeeded
	@GZIP
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadAll() {
        String TAG = "[FormacionWs - loadAll]";
        LOGGER.debug(TAG + " - init");

        try {
            List<FormacionVo> formaciones = formacionService.loadAll();
            return Response.ok(gson.toJson(formaciones)).build();
        } catch (Exception ex) {
            LOGGER.error(TAG + " - Error: {}", ex.getMessage());
            return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @JWTTokenNeeded
	@GZIP
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(FormacionVo formacionVo) {
        String TAG = "[FormacionWs - update]";
        LOGGER.debug(TAG + " - init");

        try {
            formacionService.update(formacionVo,false);
            return Response.ok().build();
        } catch (Exception ex) {
            LOGGER.error(TAG + " - Error: {}", ex.getMessage());
            return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @JWTTokenNeeded
	@GZIP
    @DELETE
    @Path("/{idFormacion}")
    public Response delete(@PathParam("idFormacion") Integer idFormacion) {
        String TAG = "[FormacionWs - delete]";
        LOGGER.debug(TAG + " - init");

        try {
            formacionService.delete(idFormacion);
            return Response.ok().build();
        } catch (Exception ex) {
            LOGGER.error(TAG + " - Error: {}", ex.getMessage());
            return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
