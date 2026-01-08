package com.zap.security.ws;

import java.security.Principal;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

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
import com.zap.security.exception.authenticate.NotAuthException;
import com.zap.security.filter.JWTTokenNeeded;
import com.zap.security.service.authenticate.AuthService;
import com.zap.security.service.parentcompany.ParentCompanyService;
import com.zap.security.vo.authenticate.AuthUserVo;
import com.zap.security.vo.enumerates.RoleEnum;
import com.zap.security.vo.parentcompany.ParentCompanyVo;
import com.zap.security.vo.parentcompany.UserParentCompanyVo;

@Path("/parent_company")
public class ParentCompanyWs {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParentCompanyWs.class);
    private static Gson gson;

    @Context
    SecurityContext securityContext;

    @Inject
    ParentCompanyService parentCompanyService;

    @Inject
    AuthService authService;

    private Gson initializesGson() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .registerTypeAdapter(Calendar.class, new CalendarSerializer())
                    .registerTypeAdapter(Calendar.class, new CalendarDeserializer())
                    .registerTypeAdapter(GregorianCalendar.class, new CalendarSerializer()).create();
        }
        return gson;
    }

    private String getSecurityUuid() throws NotAuthException {
        Principal principal = securityContext.getUserPrincipal();
        if (principal == null) {
            throw new NotAuthException("User principal can not be null");
        }
        String uuid = principal.getName();
        if (uuid == null) {
            throw new NotAuthException("UUID can not be null");
        }
        return uuid;
    }

    private AuthUserVo getCurrentUser() throws NotAuthException {
        String uuid = getSecurityUuid();
        AuthUserVo user = authService.loadByUuid(uuid);
        if (user == null) {
            throw new NotAuthException("User not found");
        }
        return user;
    }

    @JWTTokenNeeded
    @GZIP
    @GET
    @Path("/my-parent-companies")
    public Response loadMyParentCompanies() {
        String TAG = "[ParentCompanyWs - loadMyParentCompanies]";
        try {
            AuthUserVo user = getCurrentUser();
            List<ParentCompanyVo> parentCompanies;
            if (RoleEnum.SUPER_ADMNISTRADOR.equals(user.getRole())) {
                parentCompanies = parentCompanyService.findAll();
            } else {
                parentCompanies = parentCompanyService.findByUserId(user.getId());
            }
            Gson gson = initializesGson();
            return Response.ok(gson.toJson(parentCompanies), MediaType.APPLICATION_JSON).build();
        } catch (NotAuthException ex) {
            LOGGER.error(TAG + " - Error: {}", ex.getLocalizedMessage());
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        } catch (IllegalArgumentException ex) {
            LOGGER.error(TAG + " - Error: {}", ex.getLocalizedMessage());
            return Response.serverError().status(Status.BAD_REQUEST).build();
        } catch (Exception ex) {
            LOGGER.error(TAG + " - Error: {}", ex.getLocalizedMessage());
            return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @JWTTokenNeeded
    @GZIP
    @GET
    @Path("/parent-companies")
    public Response loadAllParentCompanies() {
        String TAG = "[ParentCompanyWs - loadAllParentCompanies]";
        try {
        	 AuthUserVo user = getCurrentUser();
             List<ParentCompanyVo> parentCompanies;
             if (RoleEnum.SUPER_ADMNISTRADOR.equals(user.getRole())) {
                 parentCompanies = parentCompanyService.findAll();
             } else {
                 parentCompanies = parentCompanyService.findByUserId(user.getId());
             }
             Gson gson = initializesGson();
            return Response.ok(gson.toJson(parentCompanies), MediaType.APPLICATION_JSON).build();
        } catch (NotAuthException ex) {
            LOGGER.error(TAG + " - Error: {}", ex.getLocalizedMessage());
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        } catch (Exception ex) {
            LOGGER.error(TAG + " - Error: {}", ex.getLocalizedMessage());
            return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @JWTTokenNeeded
    @GZIP
    @POST
    @Path("/parent-companies")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createParentCompany(ParentCompanyVo parentCompanyVo) {
        String TAG = "[ParentCompanyWs - createParentCompany]";
        try {
            if (parentCompanyVo == null) {
                throw new IllegalArgumentException("ParentCompanyVo can not be null");
            }
            ParentCompanyVo created = parentCompanyService.create(parentCompanyVo);
            Gson gson = initializesGson();
            return Response.ok(gson.toJson(created), MediaType.APPLICATION_JSON).build();
        } catch (IllegalArgumentException ex) {
            LOGGER.error(TAG + " - Error: {}", ex.getLocalizedMessage());
            return Response.serverError().status(Status.BAD_REQUEST).build();
        } catch (Exception ex) {
            LOGGER.error(TAG + " - Error: {}", ex.getLocalizedMessage());
            return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @JWTTokenNeeded
    @GZIP
    @POST
    @Path("/user-parent-companies")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response linkUserToParentCompany(UserParentCompanyVo userParentCompanyVo) {
        String TAG = "[ParentCompanyWs - linkUserToParentCompany]";
        try {
            if (userParentCompanyVo == null) {
                throw new IllegalArgumentException("UserParentCompanyVo can not be null");
            }
            Long userId = userParentCompanyVo.getUserId();
            if (userId == null) {
                AuthUserVo currentUser = getCurrentUser();
                userId = currentUser.getId();
            }
            parentCompanyService.linkUserToParentCompany(userId,
                    userParentCompanyVo.getParentCompanyId());
            return Response.ok().build();
        } catch (IllegalArgumentException ex) {
            LOGGER.error(TAG + " - Error: {}", ex.getLocalizedMessage());
            return Response.serverError().status(Status.BAD_REQUEST).build();
        } catch (NotAuthException ex) {
            LOGGER.error(TAG + " - Error: {}", ex.getLocalizedMessage());
            return Response.serverError().status(Status.UNAUTHORIZED).build();
        } catch (Exception ex) {
            LOGGER.error(TAG + " - Error: {}", ex.getLocalizedMessage());
            return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
