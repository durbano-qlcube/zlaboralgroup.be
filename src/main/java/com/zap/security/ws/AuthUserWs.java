package com.zap.security.ws;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
import com.google.gson.JsonSyntaxException;
import com.zap.maintenance.service.adapter.CalendarDeserializer;
import com.zap.maintenance.service.adapter.CalendarSerializer;
import com.zap.maintenance.vo.settings.ComboBoxVo;
import com.zap.security.exception.authenticate.AuthUserAlreadyRegisterException;
import com.zap.security.exception.authenticate.AuthUserNotFoundException;
import com.zap.security.exception.authenticate.NotAuthException;
import com.zap.security.filter.JWTTokenNeeded;
import com.zap.security.service.authenticate.AuthService;
import com.zap.security.service.authenticate.UserProviderService;
import com.zap.security.vo.authenticate.AuthUserVo;
import com.zap.security.vo.authenticate.ChangePassAdmVo;
import com.zap.security.vo.authenticate.ChangePassVo;
import com.zap.security.vo.authenticate.UserProviderVo;
import com.zap.security.vo.enumerates.RoleEnum;

@Path("/authuser")
public class AuthUserWs {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthUserWs.class);
	private SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static Gson gson;
	
	@Context
	SecurityContext securityContext;

	@Inject
	AuthService authService;
	
    @Inject
    UserProviderService userProviderService;
	
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


	
	private AuthUserVo getSecurityAuthUserVo() throws NotAuthException
	{
		Principal principal = securityContext.getUserPrincipal();
		String uuid = principal.getName();


		if (uuid == null)
			throw new NotAuthException("getSecurityIdUser - 'id' can not be null");

		AuthUserVo user = authService.checkRoleIsAdmin(uuid);
		
		return user;
	}
	
	
	private String getSecurityIdUser() throws NotAuthException
	{
		Principal principal = securityContext.getUserPrincipal();
		String uuid = principal.getName();


		if (uuid == null)
			throw new NotAuthException("getSecurityIdUser - 'id' can not be null");

//		authService.checkRoleIsAdmin(uuid);
		
		return uuid;
	}
	
	@JWTTokenNeeded
	@GZIP
	@POST
	@Path("/register")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response register (AuthUserVo authUserVo)
	{
		String TAG="[AuthUserWs - register]";
		try{
			String uuidAdm = getSecurityIdUser();
			TAG="[AuthUserWs - uuidAdm:"+uuidAdm+" >>  registerUser]";

			if (authUserVo == null)
				throw new IllegalArgumentException(TAG +" >> 'AuthUserVo' can not be null");
			
			if (authUserVo.getUsername() == null || "".equals(authUserVo.getUsername()))
				throw new IllegalArgumentException(TAG +" >> 'authUserVo.getUsername' can not be null or empty");
		
			if (authUserVo.getPassword() == null || "".equals(authUserVo.getPassword()))
				throw new IllegalArgumentException(TAG +" >> 'authUserVo.getPassword' can not be null or empty");
		


			authUserVo = authService.register(authUserVo);
			authUserVo.setPassword(null);
			authUserVo.setId(null);
			
			Gson gson = initializesGson();
			return Response.ok(gson.toJson(authUserVo),MediaType.APPLICATION_JSON).build();

		}catch (IllegalArgumentException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.BAD_REQUEST).build();

		}catch (AuthUserAlreadyRegisterException ex) {
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.CONFLICT).build();
					
		}catch (NotAuthException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.UNAUTHORIZED).build();

		}catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}",ex);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	
	
	
	
	
	@JWTTokenNeeded
	@GZIP
        @PUT
        @Path("/")
        @Consumes(MediaType.APPLICATION_JSON)
        public Response update(String authUserJson)
        {
                String TAG="[AuthUserWs - update]";
                try{
                        String uuidAdm = getSecurityIdUser();
                        TAG="[AuthUserWs - uuidAdm:"+uuidAdm+" >>  update]";

                        if (authUserJson == null || authUserJson.trim().isEmpty()) {
                                throw new IllegalArgumentException(TAG + " >> 'authUserJson' can not be null or empty");
                        }

                        Gson gson = initializesGson();
                        AuthUserVo authUserVo = gson.fromJson(authUserJson, AuthUserVo.class);

                        if (authUserVo == null) {
                                throw new IllegalArgumentException(TAG + " >> 'authUserVo' can not be null");
                        }

                        authUserVo.setPassword(null);
                        authService.updateUuid(authUserVo,false);

                        return Response.ok().build();

                }catch (JsonSyntaxException ex){
                        LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
                        return Response.serverError().status(Status.BAD_REQUEST).build();

                }catch (IllegalArgumentException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.BAD_REQUEST).build();

		}catch (NotAuthException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.UNAUTHORIZED).build();

		}catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	
	@JWTTokenNeeded
	@GZIP
	@POST
	@Path("/changePassword")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changePassword (ChangePassVo changePassVo)
	{
		String TAG="[AuthUserWs - changePassword]";
		try{
			if (changePassVo == null)
				throw new IllegalArgumentException(TAG +" >> 'changePassVo' can not be null");
			
			if (changePassVo.getEmail() == null || "".equals(changePassVo.getEmail()))
				throw new IllegalArgumentException(TAG +" >> 'changePassVo.getEmail' can not be null or empty");
		
			if (changePassVo.getPassword() == null || "".equals(changePassVo.getPassword()))
				throw new IllegalArgumentException(TAG +" >> 'changePassVo.getPassword' can not be null or empty");
		
			
			TAG="[AuthUserWs - email:"+changePassVo.getEmail()+" >>  changePassword]";
			
			
		
			AuthUserVo user = authService.loadByEmail(changePassVo.getEmail());
			if(user==null)
				throw new AuthUserNotFoundException();
			
			LOGGER.info(TAG + " - loaded user email:"+ user.getEmail());
			
			this.checkPatternPass ( TAG,  changePassVo.getPassword());
			LOGGER.info(TAG + " - Checking Pattern Pass....");
			
			if(changePassVo.getTemporal())
			{
				if(Calendar.getInstance().after(user.getFxTemporal()))
				{
					throw new NotAuthException(TAG +" >> 'Temporal Pass pass more than 4 hours");
				}
				
				LOGGER.info(TAG + " - Checking temporal less 4 hours....");

				user.setIsTemporalPassword(false);
				user.setFxTemporal(null);
			}
		
			
			user.setPassword(authService.encrypPassword(changePassVo.getPassword()));
			
			
			
			if (changePassVo.getHowManyMonthsPassExpires()==null || changePassVo.getHowManyMonthsPassExpires().intValue()==0) {
				user.setHowManyMonthsPassExpires(0);
				user.setFxExpiration(null);
			
			}else if (changePassVo.getHowManyMonthsPassExpires().intValue()>=1){
				
				user.setHowManyMonthsPassExpires(changePassVo.getHowManyMonthsPassExpires());
				Calendar fx = Calendar.getInstance();
				fx.add(Calendar.MONTH, changePassVo.getHowManyMonthsPassExpires());
				user.setFxExpiration(fx);
			}
			
			
			
			
			authService.updateUuid(user,false);

			return Response.ok().build();

		}catch (IllegalArgumentException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.BAD_REQUEST).build();

		}catch (NotAuthException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.UNAUTHORIZED).build();

		}catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	private void checkPatternPass (String tag, String pass)
	{
		String TAG=tag + " >> checkPatternPass >>";
		String regex = "^(?=.*[0-9])"
                + "(?=.*[a-z])(?=.*[A-Z])"
                + "(?=.*[@#$%^&+=.-_])"
                + "(?=\\S+$).{6,}$";
		
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(pass);
		
		if (!m.matches())
		{
			throw new IllegalArgumentException(TAG + " Password not matches with minimum requirements");
		}
		LOGGER.info(TAG + " Pass matches with the requirements");
		
	}
	
	@JWTTokenNeeded
	@GZIP
	@POST
	@Path("/changePasswordAdm")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changePasswordAdm (ChangePassAdmVo changePassVo)
	{
		String TAG="[AuthUserWs - changePasswordAdm]";
		try{
			
			AuthUserVo userAdm = getSecurityAuthUserVo();
			TAG="[AuthUserWs - uuidAdm:"+userAdm.getUuid()+" >>  changePasswordAdm]";
			
			if (changePassVo == null)
				throw new IllegalArgumentException(TAG +" >> 'AuthUserVo' can not be null");
			
			if (changePassVo.getUuid() == null || "".equals(changePassVo.getUuid()))
				throw new IllegalArgumentException(TAG +" >> 'authUserVo.getUuid' can not be null or empty");
		
			
			if (changePassVo.getPassword() == null || "".equals(changePassVo.getPassword()))
				throw new IllegalArgumentException(TAG +" >> 'authUserVo.getPassword' can not be null or empty");
		
			AuthUserVo user = authService.loadByUuid(changePassVo.getUuid());
			if(user==null)
				throw new AuthUserNotFoundException();
			
			
			//this.checkPatternPass ( TAG,  changePassVo.getPassword());
//			LOGGER.info("Es temporal?" + changePassVo.getTemporal());
			user.setPassword(authService.encrypPassword(changePassVo.getPassword()));
			user.setIsTemporalPassword(changePassVo.getTemporal());
			
			Calendar fxTemporal = Calendar.getInstance();
			fxTemporal.add(Calendar.HOUR_OF_DAY, 4);
			user.setFxTemporal(fxTemporal);
			user.setHasToCheckHistoryPass(changePassVo.getHasToCheckHistoryPass());
			
			if (changePassVo.getHowManyMonthsPassExpires()==null) {
				user.setHowManyMonthsPassExpires(0);
				
			}else {
				user.setHowManyMonthsPassExpires(changePassVo.getHowManyMonthsPassExpires());
			}
			
			authService.updateUuid(user,true);

			return Response.ok().build();

		}catch (IllegalArgumentException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.BAD_REQUEST).build();

		}catch (NotAuthException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.UNAUTHORIZED).build();

		}catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	
	

	@JWTTokenNeeded
	@GZIP
	@POST
	@Path("/loadCordinadores")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response loadCordinadores() {
		String TAG = "[AuthUserWs - loadCordinadores]";
		try {
			String uuidAdm = getSecurityIdUser();
			AuthUserVo userVo = authService.loadByUuid(uuidAdm);

			TAG = "[AuthUserWs - uuidAdm:" + uuidAdm + " >>  loadCordinadores]";

			List<AuthUserVo> result = null;

			if (userVo.getRole().equals(RoleEnum.SUPER_ADMNISTRADOR) || userVo.getRole().equals(RoleEnum.ADMINISTRADOR) || userVo.getRole().equals(RoleEnum.BACKOFFICE)) {
				result = authService.loadByRole(RoleEnum.CORDINADOR);
			} else if (userVo.getRole().equals(RoleEnum.SUPERVISOR)) {
				result = authService.loadByRole(RoleEnum.CORDINADOR);
				result = result.stream().filter(authUserVo -> uuidAdm.equals(authUserVo.getUuidSupervisor()))
						.collect(Collectors.toList());
			} else if (userVo.getRole().equals(RoleEnum.CORDINADOR)) {
				result = authService.loadByRole(RoleEnum.CORDINADOR);
				result = result.stream().filter(authUserVo -> uuidAdm.equals(authUserVo.getUuidCordinador()))
						.collect(Collectors.toList());
			} else {
				LOGGER.warn(TAG + " - Rol no autorizado para cargar agentes: " + userVo.getRole());
				result = new ArrayList<>();
			}

			List<ComboBoxVo> resultx = new ArrayList<ComboBoxVo>();
			for (AuthUserVo authUserVo : result) {
				ComboBoxVo cm1 = new ComboBoxVo();
				cm1.setCode(authUserVo.getUuid());
				cm1.setVal(authUserVo.getUsername());

				resultx.add(cm1);
			}

			Gson gson = initializesGson();
			return Response.ok(gson.toJson(resultx), MediaType.APPLICATION_JSON).build();

		} catch (IllegalArgumentException ex) {
			LOGGER.error(TAG + " - Error: {}", ex.getLocalizedMessage());
			return Response.serverError().status(Status.BAD_REQUEST).build();

		} catch (NotAuthException ex) {
			LOGGER.error(TAG + " - Error: {}", ex.getLocalizedMessage());
			return Response.serverError().status(Status.UNAUTHORIZED).build();

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	
	
	@JWTTokenNeeded
	@GZIP
	@POST
	@Path("/loadSupervisores")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response loadSupervisor ()
	{
		String TAG="[AuthUserWs - loadAgentes]";
		try{
			String uuidAdm = getSecurityIdUser();
			TAG="[AuthUserWs - uuidAdm:"+uuidAdm+" >>  loadAgentes]";

			List<AuthUserVo> result = authService.loadByRole(RoleEnum.SUPERVISOR);

			List<ComboBoxVo> resultx = new ArrayList<ComboBoxVo>();
			for (AuthUserVo authUserVo : result)
			{
				ComboBoxVo  cm1= new ComboBoxVo ();
				cm1.setCode(authUserVo.getUuid());
				cm1.setVal(authUserVo.getUsername());
				
				resultx.add(cm1);
			}
			
			
			Gson gson = initializesGson();
			return Response.ok(gson.toJson(resultx),MediaType.APPLICATION_JSON).build();

		}catch (IllegalArgumentException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.BAD_REQUEST).build();

		}catch (NotAuthException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.UNAUTHORIZED).build();

		}catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@JWTTokenNeeded
	@GZIP
	@POST
	@Path("/loadAgentes")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response loadAgentes() {
		String TAG = "[AuthUserWs - loadAgentes]";
		try {
			String uuidAdm = getSecurityIdUser();
			TAG = "[AuthUserWs - uuidAdm:" + uuidAdm + " >>  loadAgentes]";

			AuthUserVo userVo = authService.loadByUuid(uuidAdm);
			List<AuthUserVo> result = null;
			if (userVo.getRole().equals(RoleEnum.SUPER_ADMNISTRADOR) ||userVo.getRole().equals(RoleEnum.ADMINISTRADOR) || userVo.getRole().equals(RoleEnum.BACKOFFICE)) {
				result = authService.loadByRole(RoleEnum.AGENTE);
			} else if (userVo.getRole().equals(RoleEnum.SUPERVISOR)) {
				result = authService.loadByRole(RoleEnum.AGENTE);
				result = result.stream().filter(authUserVo -> uuidAdm.equals(authUserVo.getUuidSupervisor()))
						.collect(Collectors.toList());
			} else if (userVo.getRole().equals(RoleEnum.CORDINADOR)) {
				result = authService.loadByRole(RoleEnum.AGENTE);
				result = result.stream().filter(authUserVo -> uuidAdm.equals(authUserVo.getUuidCordinador()))
						.collect(Collectors.toList());
			} else {
				LOGGER.warn(TAG + " - Rol no autorizado para cargar agentes: " + userVo.getRole());
				result = new ArrayList<>();
			}

			List<ComboBoxVo> resultx = new ArrayList<ComboBoxVo>();
			for (AuthUserVo authUserVo : result) {
				ComboBoxVo cm1 = new ComboBoxVo();
				cm1.setCode(authUserVo.getUuid());
				cm1.setVal(authUserVo.getUsername());

				resultx.add(cm1);
			}

			Gson gson = initializesGson();
			return Response.ok(gson.toJson(resultx), MediaType.APPLICATION_JSON).build();

		} catch (IllegalArgumentException ex) {
			LOGGER.error(TAG + " - Error: {}", ex.getLocalizedMessage());
			return Response.serverError().status(Status.BAD_REQUEST).build();

		} catch (NotAuthException ex) {
			LOGGER.error(TAG + " - Error: {}", ex.getLocalizedMessage());
			return Response.serverError().status(Status.UNAUTHORIZED).build();

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@JWTTokenNeeded
	@GZIP
	@POST
	@Path("/loadCaptador")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response loadCaptador ()
	{
		String TAG="[AuthUserWs - loadCaptador]";
		try{
			String uuidAdm = getSecurityIdUser();
			TAG = "[AuthUserWs - uuidAdm:" + uuidAdm + " >>  loadAgentes]";

			AuthUserVo userVo = authService.loadByUuid(uuidAdm);
			List<AuthUserVo> result = null;
			if (userVo.getRole().equals(RoleEnum.SUPER_ADMNISTRADOR) || userVo.getRole().equals(RoleEnum.ADMINISTRADOR)) {
				result = authService.loadByRole(RoleEnum.CAPTADOR);
			} else if (userVo.getRole().equals(RoleEnum.SUPERVISOR)) {
				result = authService.loadByRole(RoleEnum.CAPTADOR);
				result = result.stream().filter(authUserVo -> uuidAdm.equals(authUserVo.getUuidSupervisor()))
						.collect(Collectors.toList());
			} else if (userVo.getRole().equals(RoleEnum.CORDINADOR)) {
				result = authService.loadByRole(RoleEnum.CAPTADOR);
				result = result.stream().filter(authUserVo -> uuidAdm.equals(authUserVo.getUuidCordinador()))
						.collect(Collectors.toList());
			} else {
				LOGGER.warn(TAG + " - Rol no autorizado para cargar agentes: " + userVo.getRole());
				result = new ArrayList<>();
			}

			List<ComboBoxVo> resultx = new ArrayList<ComboBoxVo>();
			for (AuthUserVo authUserVo : result) {
				ComboBoxVo cm1 = new ComboBoxVo();
				cm1.setCode(authUserVo.getUuid());
				cm1.setVal(authUserVo.getUsername());

				resultx.add(cm1);
			}

			Gson gson = initializesGson();
			return Response.ok(gson.toJson(resultx), MediaType.APPLICATION_JSON).build();

		}catch (IllegalArgumentException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.BAD_REQUEST).build();

		}catch (NotAuthException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.UNAUTHORIZED).build();

		}catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}",ex);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@JWTTokenNeeded
	@GZIP
	@POST
	@Path("/loadActive")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response loadActive() {
		String TAG = "[AuthUserWs - loadActive]";
		try {
			String uuidAdm = getSecurityIdUser();
			TAG = "[AuthUserWs - uuidAdm:" + uuidAdm + " >> loadActive]";

			List<AuthUserVo> result = new ArrayList<>();
			AuthUserVo userVo = authService.loadByUuid(uuidAdm);

			if (userVo.getRole().equals(RoleEnum.SUPER_ADMNISTRADOR) || userVo.getRole().equals(RoleEnum.ADMINISTRADOR)) {
				result = authService.loadByRole(RoleEnum.CORDINADOR);
				result.addAll(authService.loadByRole(RoleEnum.ADMINISTRADOR));
				result.addAll(authService.loadByRole(RoleEnum.SUPERVISOR));
				result.addAll(authService.loadByRole(RoleEnum.AGENTE));
				result.addAll(authService.loadByRole(RoleEnum.BACKOFFICE));
				result.addAll(authService.loadByRole(RoleEnum.CAPTADOR));

			} else if (userVo.getRole().equals(RoleEnum.SUPERVISOR)) {

				List<AuthUserVo> supervisor = authService.loadByRole(RoleEnum.SUPERVISOR);
				List<AuthUserVo> filteredSupervisor = supervisor.stream()
						.filter(authUserVo -> uuidAdm.equals(authUserVo.getUuid())).collect(Collectors.toList());

				List<AuthUserVo> coordinators = authService.loadByRole(RoleEnum.CORDINADOR);
				List<AuthUserVo> filteredCoordinators = coordinators.stream()
						.filter(authUserVo -> uuidAdm.equals(authUserVo.getUuidSupervisor()))
						.collect(Collectors.toList());

				List<AuthUserVo> agents = authService.loadByRole(RoleEnum.AGENTE);
				List<AuthUserVo> filteredAgents = agents.stream()
						.filter(authUserVo -> filteredCoordinators.stream()
								.anyMatch(coord -> uuidAdm.equals(authUserVo.getUuidSupervisor())))
						.collect(Collectors.toList());
				result.addAll(filteredSupervisor);
				result.addAll(filteredCoordinators);
				result.addAll(filteredAgents);
			} else if (userVo.getRole().equals(RoleEnum.CORDINADOR)) {
				List<AuthUserVo> coordinators = authService.loadByRole(RoleEnum.CORDINADOR);
				List<AuthUserVo> filteredCoordinators = coordinators.stream()
						.filter(authUserVo -> uuidAdm.equals(authUserVo.getUuid())).collect(Collectors.toList());

				List<AuthUserVo> agents = authService.loadByRole(RoleEnum.AGENTE);
				List<AuthUserVo> filteredAgents = agents.stream()
						.filter(authUserVo -> uuidAdm.equals(authUserVo.getUuidCordinador()))
						.collect(Collectors.toList());

				result.addAll(filteredCoordinators);
				result.addAll(filteredAgents);
			} else if (userVo.getRole().equals(RoleEnum.AGENTE)) {
				AuthUserVo agent = authService.loadByUuid(uuidAdm);
				result = new ArrayList<>();
				result.add(agent);
			} else if (userVo.getRole().equals(RoleEnum.PROVIDER)) {
				result = new ArrayList<>();
				result.add(userVo);
				List<UserProviderVo> relations = userProviderService.loadByProviderUuid(uuidAdm);
				if (relations != null && !relations.isEmpty()) {
					for (UserProviderVo rel : relations) {
						try {
							AuthUserVo agent = authService.loadByUuid(rel.getUuidUser());
							if (agent != null) {
								result.add(agent);
							}
						} catch (Exception e) {
							LOGGER.error(TAG + " - Error loading agent:{}", rel.getUuidUser());
						}
					}
				}
			} else if (userVo.getRole().equals(RoleEnum.PARTNER)) {
				result = new ArrayList<>();
				List<AuthUserVo> assignedProviders = authService.loadProvidersByUserUuid(uuidAdm);
				if (assignedProviders != null) {
					for (AuthUserVo prov : assignedProviders) {
						result.add(prov);
						try {
							List<UserProviderVo> rels = userProviderService.loadByProviderUuid(prov.getUuid());
							if (rels != null) {
								for (UserProviderVo rel : rels) {
									AuthUserVo agent = authService.loadByUuid(rel.getUuidUser());
									if (agent != null) {
										result.add(agent);
									}
								}
							}
						} catch (Exception e) {
							LOGGER.error(TAG + " - Error loading agent:{}", prov.getUuid());
						}
					}
				}
			}

			List<ComboBoxVo> resultx = new ArrayList<>();
			for (AuthUserVo authUserVo : result) {
				ComboBoxVo cm1 = new ComboBoxVo();
				cm1.setCode(authUserVo.getUuid());
				cm1.setVal(authUserVo.getUsername());
				resultx.add(cm1);
			}

			Gson gson = initializesGson();
			return Response.ok(gson.toJson(resultx), MediaType.APPLICATION_JSON).build();

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
		
	
	@JWTTokenNeeded
	@GZIP
	@GET
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response loadAllUser ()
	{
		String TAG="[AuthUserWs - loadAllUser]";
		try{
			String uuidAdm = getSecurityIdUser();
			TAG="[AuthUserWs - uuidAdm:"+uuidAdm+" >>  loadAllUser]";

			List<AuthUserVo> result = authService.loadAll();
			Gson gson = initializesGson();
			return Response.ok(gson.toJson(result),MediaType.APPLICATION_JSON).build();

		}catch (IllegalArgumentException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.BAD_REQUEST).build();

		}catch (NotAuthException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.UNAUTHORIZED).build();

		}catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	
	@JWTTokenNeeded
	@GZIP
	@GET
	@Path("/loadByEmail/{email}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getUserByEmail (@PathParam("email") String email)
	{
		String TAG="[AuthUserWs - getUserByEmail]";
		try{
			String uuidAdm = getSecurityIdUser();
			TAG="[AuthUserWs - uuidAdm:"+uuidAdm+" >>  loadAllUser]";

			AuthUserVo authUserVo = authService.loadByEmail(email);
			
			Gson gson = initializesGson();
			return Response.ok(gson.toJson(authUserVo),MediaType.APPLICATION_JSON).build();

		}catch (IllegalArgumentException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.BAD_REQUEST).build();

		}catch (NotAuthException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.UNAUTHORIZED).build();

		}catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	
	@JWTTokenNeeded
	@GZIP
	@GET
	@Path("/{uuid}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response loadByUuid (@PathParam("uuid") String uuid)
	{
		String TAG="[AuthUserWs - loadByUuid]";
		try{
			String uuidAdm = getSecurityIdUser();
			TAG="[AuthUserWs - uuidAdm:"+uuidAdm+" >>  loadUserbyUuid uuid:"+uuid+"]";

			AuthUserVo authUserVo = authService.loadByUuid(uuid);
			authUserVo.setPassword(null);
			authUserVo.setId(null);
			Gson gson = initializesGson();
			return Response.ok(gson.toJson(authUserVo),MediaType.APPLICATION_JSON).build();

		}catch (IllegalArgumentException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.BAD_REQUEST).build();

		}catch (NotAuthException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.UNAUTHORIZED).build();

		}catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}",ex);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@JWTTokenNeeded
	@GZIP
	@DELETE
	@Path("/{uuid}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response delete (@PathParam("uuid") String uuid)
	{
		String TAG="[AuthUserWs - delete]";
		try{
			String uuidAdm = getSecurityIdUser();
			TAG="[AuthUserWs - uuidAdm:"+uuidAdm+" >>  delete uuid:"+uuid+"]";

			AuthUserVo authUserVo = authService.loadByUuid(uuid);
			authService.delete(authUserVo.getId());
			
			Gson gson = initializesGson();
			return Response.ok(gson.toJson(authUserVo),MediaType.APPLICATION_JSON).build();

		}catch (IllegalArgumentException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.BAD_REQUEST).build();

		}catch (NotAuthException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.UNAUTHORIZED).build();

		}catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}",ex);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@JWTTokenNeeded
	@GZIP
	@POST
	@Path("/enable/{uuid}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response enable (@PathParam("uuid") String uuid)
	{
		String TAG="[AuthUserWs - enable]";
		try{
			String uuidAdm = getSecurityIdUser();
			TAG="[AuthUserWs - uuidAdm:"+uuidAdm+" >>  enable]";
			AuthUserVo authUserVo= new AuthUserVo();
			authUserVo.setUuid(uuid);
			authUserVo.setIsActive(true);
			authUserVo.setPassword(null);
			authService.updateUuid(authUserVo,false);

			return Response.ok().build();

		}catch (IllegalArgumentException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.BAD_REQUEST).build();

		}catch (NotAuthException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.UNAUTHORIZED).build();

		}catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	
	@JWTTokenNeeded
	@GZIP
	@GET
	@Path("/disable/{uuid}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response disable (@PathParam("uuid") String uuid)
	{
		String TAG="[AuthUserWs - enable]";
		try{
			String uuidAdm = getSecurityIdUser();
			TAG="[AuthUserWs - uuidAdm:"+uuidAdm+" >>  enable]";
			AuthUserVo authUserVo= new AuthUserVo();
			authUserVo.setUuid(uuid);
			authUserVo.setIsActive(false);
			authUserVo.setPassword(null);
			authService.updateUuid(authUserVo,false);

			return Response.ok().build();

		}catch (IllegalArgumentException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.BAD_REQUEST).build();

		}catch (NotAuthException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.UNAUTHORIZED).build();

		}catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
}
