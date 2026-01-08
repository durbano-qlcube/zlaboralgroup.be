package com.zap.security.ws;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.annotations.GZIP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zap.maintenance.service.adapter.CalendarDeserializer;
import com.zap.maintenance.service.adapter.CalendarSerializer;
import com.zap.security.exception.authenticate.AuthUserNotFoundException;
import com.zap.security.exception.authenticate.InvalidAccessException;
import com.zap.security.exception.authenticate.InvalidTokenException;
import com.zap.security.exception.authenticate.PasswordException;
import com.zap.security.exception.authenticate.UserNotActiveException;
import com.zap.security.filter.JWTTokenNeeded;
import com.zap.security.service.authenticate.AuthService;
import com.zap.security.vo.authenticate.LoginVo;
import com.zap.security.vo.authenticate.SessionVo;

@Path("/authenticate")
public class AuthenticateWs {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticateWs.class);
	private SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static Gson gson;
	
	

	@Inject
	AuthService authService;
	
	
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


	
//	private String getSecurityIdUser()
//	{
//		Principal principal = securityContext.getUserPrincipal();
//		String id = principal.getName();
//
//
//		if (id == null)
//			throw new IllegalArgumentException(" - 'id' can not be null");
//
//		if (!ObjectId.isValid(id))
//			throw new IllegalArgumentException(" - 'id' is not valid");
//		
//		return id;
//	}
	
	
	@GZIP
	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response login (@Context HttpServletRequest ctx, LoginVo loginVo)
	{

		String TAG="[AuthenticateWs - login]";
		LOGGER.debug(TAG + "- init");
		long currentSystemTime = System.currentTimeMillis();
		Gson gson = initializesGson();

		try{
			if (loginVo == null)
				throw new IllegalArgumentException(TAG + " - 'loginVo' can not be null");

			if (loginVo.getUsername() == null || "".equals(loginVo.getUsername()) )
				throw new IllegalArgumentException(TAG + " - 'Username' can not be null");

			if (loginVo.getSignature() == null || "".equals(loginVo.getSignature()) )
				throw new IllegalArgumentException(TAG + " - 'signature' can not be null");

			TAG="[AuthenticateWs - login Username:"+loginVo.getUsername()+"]";
			
			SessionVo sessionVo = authService.login(loginVo);
			
			
			return Response.ok(gson.toJson(sessionVo)).header(HttpHeaders.AUTHORIZATION, "Bearer " + sessionVo.getToken()).build();

			
		}catch (PasswordException ex){
			LOGGER.error(TAG + "- Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
			
		}catch (IllegalArgumentException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.BAD_REQUEST).build();

		}catch (AuthUserNotFoundException ex){
			LOGGER.error(TAG + "- Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.UNAUTHORIZED).build();

		} catch (InvalidAccessException ex) {
			LOGGER.error(TAG + "- Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.UNAUTHORIZED).build();
			
			
		} catch (UserNotActiveException ex) {
			LOGGER.error(TAG + "- Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.PRECONDITION_FAILED).build();
			
		}catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();

		}finally{
			LOGGER.info(TAG + " - Finish Timing:"+(System.currentTimeMillis()-currentSystemTime));
		}
	}


	@JWTTokenNeeded
	@GZIP
	@POST
	@Path("/logout")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response logout (@Context HttpServletRequest requestContext)
	{
		String TAG="[AuthenticateWs - logout]";
		LOGGER.debug(TAG + "- init");
		long currentSystemTime = System.currentTimeMillis();
		try{
	        String authorizationHeader = requestContext.getHeader(HttpHeaders.AUTHORIZATION);
	        
	        // Check if the HTTP Authorization header is present and formatted correctly 
	        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer "))
	            throw new NotAuthorizedException("Authorization header must be provided");
	        
	        // Extract the token from the HTTP Authorization header
	        String token = authorizationHeader.substring("Bearer".length()).trim();
	        LOGGER.info(TAG + " - jwt : " + token);
			authService.logout(token);
			return Response.ok().build();

		}catch (IllegalArgumentException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.BAD_REQUEST).build();

		}catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();

		}finally{
			LOGGER.info(TAG + " - Finish Timing:"+(System.currentTimeMillis()-currentSystemTime));
		}
	}

	
	@GZIP
	@POST
	@Path("/refresh")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response refresh (@Context HttpServletRequest requestContext)
	{
		String TAG="[AuthenticateWs - refresh]";
		LOGGER.debug(TAG + "- init");
		long currentSystemTime = System.currentTimeMillis();
		try{
	        String authorizationHeader = requestContext.getHeader(HttpHeaders.AUTHORIZATION);
	        
	        // Check if the HTTP Authorization header is present and formatted correctly 
	        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer "))
	            throw new NotAuthorizedException("Authorization header must be provided");
	        
	        // Extract the token from the HTTP Authorization header
	        String token = authorizationHeader.substring("Bearer".length()).trim();
	        LOGGER.info(TAG + " - jwt : " + token);
			
	        SessionVo sessionVo = authService.refreshJwt(token);
	        Gson gson = initializesGson();
			return Response.ok(gson.toJson(sessionVo)).header(HttpHeaders.AUTHORIZATION, "Bearer " + sessionVo.getToken()).build();

		}catch (IllegalArgumentException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.BAD_REQUEST).build();

		} catch (NotAuthorizedException ex) {
			LOGGER.error(TAG + "- Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.UNAUTHORIZED).build();
			
		} catch (InvalidTokenException ex) {
			LOGGER.error(TAG + "- Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.UNAUTHORIZED).build();
			
		}catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();

		}finally{
			LOGGER.info(TAG + " - Finish Timing:"+(System.currentTimeMillis()-currentSystemTime));
		}
	}
}
