package com.zap.sales.ws;

import java.security.Principal;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.inject.Inject;
import javax.ws.rs.GET;
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
import com.zap.sales.service.PersonaService;
import com.zap.sales.vo.particular.PersonaVo;
import com.zap.security.exception.authenticate.NotAuthException;
import com.zap.security.filter.JWTTokenNeeded;
import com.zap.security.service.authenticate.AuthService;
import com.zap.security.vo.authenticate.AuthUserVo;

@Path("/particular")
public class PersonaWs {

	private static final Logger LOGGER = LoggerFactory.getLogger(PersonaWs.class);
	private Gson gson;

	@Context
	SecurityContext securityContext;

	@Inject
	PersonaService personaService;

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
	@GET
	@Path("/loadByDni/{Dni}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response load(@PathParam("Dni") String Dni) {
		String TAG = "[EmpresaWs - load]";
		LOGGER.debug(TAG + " - init");

		try {
			AuthUserVo user = getSecurityIdUser();
			TAG = "[SaleWs - uuidAdm:" + user.getUuid() + " >> register]";

			if (Dni == null)
				throw new IllegalArgumentException(TAG + " >> 'uuid' can not be null");

			PersonaVo personaVo = personaService.loadByDni(Dni);
			if (personaVo == null) {
				return Response.status(Status.NOT_FOUND).entity("{\"message\": \"DNI no existe en la base de datos.\"}")
						.type(MediaType.APPLICATION_JSON).build();
			}

			Gson gson = initializesGson();
			return Response.ok(gson.toJson(personaVo)).build();

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

}
