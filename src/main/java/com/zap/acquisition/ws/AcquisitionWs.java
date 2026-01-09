package com.zap.acquisition.ws;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import javax.inject.Inject;
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

import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.annotations.GZIP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zap.acquisition.exception.AdquisitionDuplicatePhoneException;
import com.zap.acquisition.service.AcquisitionService;
import com.zap.acquisition.vo.AcquisitionSearchRequestVo;
import com.zap.acquisition.vo.AcquisitionSearchResponseVo;
import com.zap.acquisition.vo.AcquisitionVo;
import com.zap.acquisition.vo.StatusAcquisitionEnum;
import com.zap.maintenance.service.adapter.CalendarDeserializer;
import com.zap.maintenance.service.adapter.CalendarSerializer;
import com.zap.security.exception.authenticate.NotAuthException;
import com.zap.security.filter.JWTTokenNeeded;
import com.zap.security.service.authenticate.AuthService;
import com.zap.security.vo.authenticate.AuthUserVo;
import com.zap.security.vo.authenticate.UserProviderVo;
import com.zap.security.vo.enumerates.RoleEnum;
import com.zap.scheduling.job.SincroOcmToZcubeJob;

@Path("/acquisition")
public class AcquisitionWs {

	private static final Logger LOGGER = LoggerFactory.getLogger(AcquisitionWs.class);
	private SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static Gson gson;
	
	@Context
	SecurityContext securityContext;
	
	@Inject
	AuthService authService;
	
	@Inject
	AcquisitionService acquisitionService;
	
    @Inject
    SincroOcmToZcubeJob sincroOcmToZcubeJob;	
	
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

	
	
	private AuthUserVo getSecurityAdmAuthUserVo() throws NotAuthException
	{
		Principal principal = securityContext.getUserPrincipal();
		String uuid = principal.getName();


		if (uuid == null)
			throw new NotAuthException("getSecurityIdUser - 'id' can not be null");

		AuthUserVo user = authService.checkRoleIsAdmin(uuid);
		
		return user;
	}
	
        private AuthUserVo getSecurityIdUser() throws NotAuthException
        {
                Principal principal = securityContext.getUserPrincipal();
                String uuid = principal.getName();


		if (uuid == null)
			throw new NotAuthException("getSecurityIdUser - 'id' can not be null");

                AuthUserVo user = authService.loadByUuid(uuid);

                return user;
        }

        private String resolveMainProviderUuid(AuthUserVo user, String currentUuidProvider) {
                if (user == null || !RoleEnum.AGENTE.equals(user.getRole())) {
                        return currentUuidProvider;
                }

                if (!StringUtils.isBlank(currentUuidProvider)) {
                        return currentUuidProvider;
                }

                String mainProviderUuid = null;

                try {
                        List<AuthUserVo> providers = authService.loadProvidersByUserUuid(user.getUuid());
                        if (providers != null) {
                                for (AuthUserVo provider : providers) {
                                        if (provider == null || StringUtils.isBlank(provider.getUuid())) {
                                                continue;
                                        }

                                        if (Boolean.TRUE.equals(provider.getIsMainProvider())) {
                                                return provider.getUuid();
                                        }

                                        if (mainProviderUuid == null) {
                                                mainProviderUuid = provider.getUuid();
                                        }
                                }
                        }
                } catch (Exception ex) {
                        LOGGER.error("[AcquisitionWs - resolveMainProviderUuid] - Error loading providers for user:{}", user.getUuid(), ex);
                }

                if (StringUtils.isBlank(mainProviderUuid)) {
                        AuthUserVo parentProvider = authService.findParentProviderBySubProviderUsername(user.getUsername());
                        if (parentProvider != null && !StringUtils.isBlank(parentProvider.getUuid())) {
                                mainProviderUuid = parentProvider.getUuid();
                        }
                }

                return mainProviderUuid;
        }

        private String resolveUuidProvider(AuthUserVo user) {
                if (user == null) {
                        return null;
                }

                if (RoleEnum.PROVIDER.equals(user.getRole())) {
                        return user.getUuid();
                }

                String mainProviderUuid = null;
                String subProviderUuid = null;

                List<UserProviderVo> relations = user.getProviders();
                if (relations != null) {
                        for (UserProviderVo relation : relations) {
                                if (relation == null || StringUtils.isBlank(relation.getUuidProvider())) {
                                        continue;
                                }

                                try {
                                        AuthUserVo provider = authService.loadByUuid(relation.getUuidProvider());
                                        if (provider == null) {
                                                continue;
                                        }

                                        if (Boolean.TRUE.equals(provider.getIsMainProvider())) {
                                                mainProviderUuid = provider.getUuid();
                                                break;
                                        }

                                        if (subProviderUuid == null) {
                                                subProviderUuid = provider.getUuid();
                                        }
                                } catch (Exception ex) {
                                        LOGGER.error("[AcquisitionWs - resolveUuidProvider] - Error loading provider:{}", relation.getUuidProvider(), ex);
                                }
                        }
                }

                if (!StringUtils.isBlank(mainProviderUuid)) {
                        return mainProviderUuid;
                }

                if (!StringUtils.isBlank(subProviderUuid)) {
                        return subProviderUuid;
                }

                if (user.getUuidProviders() != null) {
                        for (String providerUuid : user.getUuidProviders()) {
                                if (!StringUtils.isBlank(providerUuid)) {
                                        return providerUuid;
                                }
                        }
                }

                return null;
        }




        @JWTTokenNeeded
        @GZIP
	@POST
	@Path("/register")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response register(AcquisitionVo acquisitionVo)
	{
		String TAG="[AcquisitionWs - register]";
		try{
			
                        AuthUserVo user = getSecurityIdUser();
                        TAG="[AcquisitionWs - uuidAdm:"+user.getUuid()+" >>  register]";

                        if (acquisitionVo == null)
                                throw new IllegalArgumentException(TAG +" >> 'AuthUserVo' can not be null");

                        if (acquisitionVo.getPhone() == null || "".equals(acquisitionVo.getPhone()))
                                throw new IllegalArgumentException(TAG +" >> 'acquisitionVo.getPhone' can not be null or empty");

                        String uuidProvider = resolveUuidProvider(user);
                        uuidProvider = resolveMainProviderUuid(user, uuidProvider);
                        acquisitionVo.setUuidProvider(uuidProvider);


                        acquisitionVo.setAgenteUsername(user.getUsername());
                        acquisitionVo.setAgenteUuid(user.getUuid());
			acquisitionVo.setOrigin("LABORALGROUP");
			acquisitionVo.setOriginUserUsername(user.getUsername());
			if (user.getRole().equals(RoleEnum.CORDINADOR)) {
				acquisitionVo.setCoordinadorUserName(user.getUsername());
				acquisitionVo.setCoordinadorUuid(user.getUuid());
			}
			if (acquisitionVo.getCoordinadorUserName() == null) {
				acquisitionVo.setCoordinadorUserName(user.getCordinadorUsername());
			}
			if (user.getRole().equals(RoleEnum.AGENTE) || user.getRole().equals(RoleEnum.CAPTADOR)) {
				AuthUserVo coordinador = authService.loadByCoordinadorUuid(user.getUuidCordinador());
				if (coordinador != null) {
				    acquisitionVo.setSupervisorUserName(coordinador.getSupervisorUsername());
				    acquisitionVo.setSupervisorUuid(coordinador.getUuidSupervisor());
				    acquisitionVo.setCoordinadorUserName(user.getCordinadorUsername());
				    acquisitionVo.setCoordinadorUuid(user.getUuidCordinador());
				}
			}
			if (user.getRole().equals(RoleEnum.SUPERVISOR)) {
				acquisitionVo.setSupervisorUserName(user.getUsername());
				acquisitionVo.setSupervisorUuid(user.getUuid());
			}
			if (acquisitionVo.getSupervisorUserName() == null) {
				acquisitionVo.setSupervisorUserName(user.getSupervisorUsername());
			}
			if (acquisitionVo.getStatus()==null)
				acquisitionVo.setStatus(StatusAcquisitionEnum.ENVIAR_OCM);

			AcquisitionVo result = acquisitionService.create(acquisitionVo);

			Gson gson = initializesGson();
			return Response.ok(gson.toJson(result),MediaType.APPLICATION_JSON).build();

		}catch (IllegalArgumentException ex){
			LOGGER.error(TAG + " - Error: {}",ex);
			return Response.serverError().status(Status.BAD_REQUEST).build();

		}catch (AdquisitionDuplicatePhoneException ex){
			LOGGER.error(TAG + " - Error: {}",ex);
			return Response.serverError().status(Status.CONFLICT).build();

		}catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}",ex);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	
	@JWTTokenNeeded
	@GZIP
	@POST
	@Path("/loadAll")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response loadAll ()
	{
		String TAG="[AcquisitionWs - loadAll]";
		try{
			
			AuthUserVo user = getSecurityIdUser();
			TAG="[AcquisitionWs - uuidAdm:"+user.getUuid()+" >>  register]";
			
			 List<AcquisitionVo> result = acquisitionService.loadAll(user);

			Gson gson = initializesGson();
			return Response.ok(gson.toJson(result),MediaType.APPLICATION_JSON).build();

		}catch (IllegalArgumentException ex){
			LOGGER.error(TAG + " - Error: {}",ex);
			return Response.serverError().status(Status.BAD_REQUEST).build();


		}catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}",ex);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	

	
	@JWTTokenNeeded
	@GZIP
	@POST
	@Path("/load")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response loadById (AcquisitionVo acquisitionVo)
	{
		String TAG="[AcquisitionWs - loadById]";
		try{
			
			AuthUserVo user = getSecurityIdUser();
			TAG="[AcquisitionWs - uuidAdm:"+user.getUuid()+" >>  loadById]";
			
			if (acquisitionVo == null)
				throw new IllegalArgumentException(TAG +" >> 'AuthUserVo' can not be null");
			
			if (acquisitionVo.getIdAcquisition() == null)
				throw new IllegalArgumentException(TAG +" >> 'getIdAcquisition' can not be null");
			
			
			LOGGER.info(TAG + "user:{} - Role: {}",user.getUsername(), user.getRole().toString());
			AcquisitionVo result = acquisitionService.load(acquisitionVo.getIdAcquisition());

			Gson gson = initializesGson();
			return Response.ok(gson.toJson(result),MediaType.APPLICATION_JSON).build();

		}catch (IllegalArgumentException ex){
			LOGGER.error(TAG + " - Error: {}",ex);
			return Response.serverError().status(Status.BAD_REQUEST).build();


		}catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}",ex);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	
	
	@JWTTokenNeeded
	@GZIP
	@POST
	@Path("/search")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response search (AcquisitionSearchRequestVo request)
	{
		String TAG="[AcquisitionWs - search]";
		try{
			
			AuthUserVo user = getSecurityIdUser();
			TAG="[AcquisitionWs - uuidAdm:"+user.getUuid()+" >>  search]";
			
                        if (request == null) {
                                throw new IllegalArgumentException(TAG + " >> 'request' can not be null");
                        }

                        if (!hasParentCompanyFilter(request)) {
                                Gson gson = initializesGson();
                                return Response.ok(gson.toJson(Collections.emptyList()), MediaType.APPLICATION_JSON).build();
                        }

                        if (request.getFxInicio()!=null)
			{
				 Calendar fxVentaInitStartOfDay = this.setStartOfDay(request.getFxInicio());
				 request.setFxInicio(fxVentaInitStartOfDay);
				 LOGGER.info(TAG + ">> FxVentaInit: {} ", FORMAT_DATE.format(fxVentaInitStartOfDay.getTime()));
			}
			
			if (request.getFxFin()!=null)
			{
				Calendar fxVentaFinEndOfDay = this.setEndOfDay(request.getFxFin());
				request.setFxFin(fxVentaFinEndOfDay);
				 LOGGER.info(TAG + ">> FxVentaFin: {} ", FORMAT_DATE.format(fxVentaFinEndOfDay.getTime()));

			}
			
			if (request.getFxInicio()!=null && request.getFxFin()==null)
			{
				Calendar fxVentaInitStartOfDay = this.setStartOfDay(request.getFxInicio());
				request.setFxInicio(fxVentaInitStartOfDay);
				LOGGER.info(TAG + ">> FxVentaInit: {} ", FORMAT_DATE.format(fxVentaInitStartOfDay.getTime()));


				Calendar fxVentaFinEndOfDay = this.setEndOfDay(request.getFxInicio());
				request.setFxFin(fxVentaFinEndOfDay);
				LOGGER.info(TAG + ">> FxVentaFin: {} ", FORMAT_DATE.format(fxVentaFinEndOfDay.getTime()));
			}
	        
                        if (request.getFxInicio()==null && request.getFxFin()!=null)
                        {
                                Calendar fxVentaInitStartOfDay = this.setStartOfDay(request.getFxFin());
                                request.setFxInicio(fxVentaInitStartOfDay);
				LOGGER.info(TAG + ">> FxVentaInit: {} ", FORMAT_DATE.format(fxVentaInitStartOfDay.getTime()));


				Calendar fxVentaFinEndOfDay = this.setEndOfDay(request.getFxFin());
				request.setFxFin(fxVentaFinEndOfDay);
				LOGGER.info(TAG + ">> FxVentaFin: {} ", FORMAT_DATE.format(fxVentaFinEndOfDay.getTime()));
			}

                        if (request.getFxLastCallInit()!=null)
			{
				 Calendar fxLastCallInitStartOfDay = this.setStartOfDay(request.getFxLastCallInit());
				 request.setFxLastCallInit(fxLastCallInitStartOfDay);
				 LOGGER.info(TAG + ">> FxLastCallInit: {} ", FORMAT_DATE.format(fxLastCallInitStartOfDay.getTime()));
			}

			if (request.getFxLastCallFin()!=null)
			{
				Calendar fxLastCallFinEndOfDay = this.setEndOfDay(request.getFxLastCallFin());
				request.setFxLastCallFin(fxLastCallFinEndOfDay);
				 LOGGER.info(TAG + ">> FxLastCallFin: {} ", FORMAT_DATE.format(fxLastCallFinEndOfDay.getTime()));

			}

			if (request.getFxLastCallInit()!=null && request.getFxLastCallFin()==null)
			{
				Calendar fxLastCallInitStartOfDay = this.setStartOfDay(request.getFxLastCallInit());
				request.setFxLastCallInit(fxLastCallInitStartOfDay);
				LOGGER.info(TAG + ">> FxLastCallInit: {} ", FORMAT_DATE.format(fxLastCallInitStartOfDay.getTime()));


				Calendar fxLastCallFinEndOfDay = this.setEndOfDay(request.getFxLastCallInit());
				request.setFxLastCallFin(fxLastCallFinEndOfDay);
				LOGGER.info(TAG + ">> FxLastCallFin: {} ", FORMAT_DATE.format(fxLastCallFinEndOfDay.getTime()));
			}
	        
                        if (request.getFxLastCallInit()==null && request.getFxLastCallFin()!=null)
                        {
                                Calendar fxLastCallInitStartOfDay = this.setStartOfDay(request.getFxLastCallFin());
                                request.setFxLastCallInit(fxLastCallInitStartOfDay);
				LOGGER.info(TAG + ">> FxLastCallInit: {} ", FORMAT_DATE.format(fxLastCallInitStartOfDay.getTime()));


				Calendar fxLastCallFinEndOfDay = this.setEndOfDay(request.getFxLastCallFin());
				request.setFxLastCallFin(fxLastCallFinEndOfDay);
				LOGGER.info(TAG + ">> FxLastCallFin: {} ", FORMAT_DATE.format(fxLastCallFinEndOfDay.getTime()));
			}


			
			 List<AcquisitionSearchResponseVo> result = acquisitionService.search(request, user);

                        Gson gson = initializesGson();
                        return Response.ok(gson.toJson(result),MediaType.APPLICATION_JSON).build();

		}catch (IllegalArgumentException ex){
			LOGGER.error(TAG + " - Error: {}",ex);
			return Response.serverError().status(Status.BAD_REQUEST).build();


		}catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}",ex);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
                }
        }

        private boolean hasParentCompanyFilter(AcquisitionSearchRequestVo request) {
                if (request == null) {
                        return false;
                }

                if (request.getParentCompanyId() != null) {
                        return true;
                }

                return request.getParentCompanyIds() != null
                                && request.getParentCompanyIds().stream().anyMatch(Objects::nonNull);
        }


	
	@JWTTokenNeeded
	@GZIP
	@DELETE
	@Path("/{idAcquisition}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response delete (@PathParam("idAcquisition") Integer idAcquisition)
	{
		String TAG="[AcquisitionWs - delete]";
		try{

			AuthUserVo user = getSecurityIdUser();
			TAG="[SaleWs - uuidAdm:"+user.getUuid()+" >>  register]";

			if (idAcquisition == null)
				throw new IllegalArgumentException(TAG +" >> 'idAcquisition' can not be null");

		

			acquisitionService.delete(idAcquisition);


			return Response.ok().build();

		}catch (IllegalArgumentException ex){
			LOGGER.error(TAG + " - Error: {}",ex);
			return Response.serverError().status(Status.BAD_REQUEST).build();

		}catch (NotAuthException ex){
			LOGGER.error(TAG + " - Error: {}",ex);
			return Response.serverError().status(Status.UNAUTHORIZED).build();

		}catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}",ex);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	
	@JWTTokenNeeded
	@GZIP
	@POST
	@Path("/update")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(AcquisitionVo acquisitionVo)
	{
		String TAG="[AcquisitionWs - update]";
		try{
			
			AuthUserVo userLoging = getSecurityIdUser();
			TAG="[AcquisitionWs - uuidAdm:"+userLoging.getUuid()+" >>  update]";
			
			if (acquisitionVo == null)
				throw new IllegalArgumentException(TAG +" >> 'AuthUserVo' can not be null");
			
			if (acquisitionVo.getPhone() == null || "".equals(acquisitionVo.getPhone()))
				throw new IllegalArgumentException(TAG +" >> 'acquisitionVo.getPhone' can not be null or empty");
		
			AuthUserVo user = authService.loadByUuid(acquisitionVo.getAgenteUuid());
			acquisitionVo.setAgenteUsername(user.getUsername());
			acquisitionVo.setAgenteUuid(user.getUuid());
			
			acquisitionService.update(acquisitionVo, true);

			return Response.ok().build();

		}catch (IllegalArgumentException ex){
			LOGGER.error(TAG + " - Error: {}",ex);
			return Response.serverError().status(Status.BAD_REQUEST).build();

		}catch (AdquisitionDuplicatePhoneException ex){
			LOGGER.error(TAG + " - Error: {}",ex);
			return Response.serverError().status(Status.CONFLICT).build();

		}catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}",ex);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@JWTTokenNeeded
	@GZIP
	@POST
	@Path("/reload")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response reload()
	{
		String TAG="[AcquisitionWs - reload]";
		try{

			AuthUserVo user = getSecurityIdUser();
			TAG="[AcquisitionWs - uuidAdm:"+user.getUuid()+" >>  update]";
			
			sincroOcmToZcubeJob.doExecute();


			List<AcquisitionSearchResponseVo> result = acquisitionService.search(new AcquisitionSearchRequestVo(), user);

			Gson gson = initializesGson();
			return Response.ok(gson.toJson(result),MediaType.APPLICATION_JSON).build();


		}catch (IllegalArgumentException ex){
			LOGGER.error(TAG + " - Error: {}",ex);
			return Response.serverError().status(Status.BAD_REQUEST).build();


		}catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}",ex);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@JWTTokenNeeded
	@GZIP
	@GET
	@Path("/verifyProvider")
	@Produces(MediaType.APPLICATION_JSON)
        public Response verifyProvider(@QueryParam("phone") String phone) {
                String TAG = "[AcquisitionWs - verifyProvider]";
                try {
                        if (phone == null || "".equals(phone)) {
                                throw new IllegalArgumentException(TAG + " >> 'phone' can not be null or empty");
                        }

			AcquisitionVo acquisition = acquisitionService.loadByPhone(phone);
			if (acquisition == null) {
				Map<String, String> error = new HashMap<>();
				error.put("error", "PHONE_NOT_FOUND");
				error.put("message", "No se ha encontrado ning\u00fan registro con ese n\u00famero de tel\u00e9fono.");
				return Response.status(Status.NOT_FOUND).entity(error).build();
			}
			
			AuthUserVo authUser =  null;
			
			if (acquisition.getUuidProvider() != null) {
				
				authUser = authService.loadByUuid(acquisition.getUuidProvider());

			}
			

			Map<String, String> result = new HashMap<>();
			result.put("uuidProvider", acquisition.getUuidProvider());
			result.put("usernameProvider", authUser.getUsername());

			return Response.ok(result, MediaType.APPLICATION_JSON).build();

		} catch (IllegalArgumentException ex) {
			LOGGER.error(TAG + " - Error:{}", ex.getMessage());
			return Response.serverError().status(Status.BAD_REQUEST).build();

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error:{}", ex.getMessage());
                        return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
                }
        }
	
	
	
	private Calendar setStartOfDay(Calendar c)
	{
		Calendar fx = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
		fx.setTimeInMillis(c.getTimeInMillis());
		fx.set(Calendar.HOUR_OF_DAY, 0);
		fx.set(Calendar.MINUTE, 0);
		fx.set(Calendar.SECOND, 0);
		fx.set(Calendar.MILLISECOND, 0);
		return fx;
	}

	private Calendar setEndOfDay(Calendar c)
	{
		 Calendar fx = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
	     fx.setTimeInMillis(c.getTimeInMillis());
	     fx.set(Calendar.HOUR_OF_DAY, 23);
	     fx.set(Calendar.MINUTE, 59);
	     fx.set(Calendar.SECOND, 59);
	     fx.set(Calendar.MILLISECOND, 999);
	    return fx;
	}
	
	
}
