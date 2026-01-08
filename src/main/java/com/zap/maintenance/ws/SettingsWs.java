package com.zap.maintenance.ws;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
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
import com.zap.maintenance.service.geo.CityService;
import com.zap.maintenance.service.geo.CountryService;
import com.zap.maintenance.service.geo.ProvinceService;
import com.zap.maintenance.service.rate.RateService;
import com.zap.maintenance.service.rate.SegmentService;
import com.zap.maintenance.service.settings.SettingsService;
import com.zap.maintenance.vo.geo.CityVo;
import com.zap.maintenance.vo.geo.CountryVo;
import com.zap.maintenance.vo.geo.ProvinceVo;
import com.zap.maintenance.vo.rate.RateExtVo;
import com.zap.maintenance.vo.rate.RateVo;
import com.zap.maintenance.vo.settings.ComboBoxVo;
import com.zap.maintenance.vo.settings.SettingsVo;
import com.zap.sales.vo.venta.StatusVentaEnum;
import com.zap.security.exception.authenticate.NotAuthException;
import com.zap.security.filter.JWTTokenNeeded;
import com.zap.security.service.authenticate.AuthService;
import com.zap.security.vo.authenticate.AuthUserVo;
import com.zap.security.vo.enumerates.RoleEnum;
import com.zap.security.vo.parentcompany.ParentCompanyVo;

@Path("/settings")
public class SettingsWs {

	private static final Logger LOGGER = LoggerFactory.getLogger(SettingsWs.class);
	private SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static Gson gson;
	
	@Context
	SecurityContext securityContext;
	
	@Inject
	AuthService authService;
	
	@Inject
	SettingsService settingsService;
	
	@Inject
	CityService cityService;
	
	@Inject
	ProvinceService provinceService;
	
	@Inject
	CountryService countryService;
	
	@Inject
	SegmentService segmentService;
	
	@Inject
	RateService rateService;
	
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


//	@JWTTokenNeeded
//	@GZIP
//	@POST
//	@Path("/loadStatus")
//	@Consumes(MediaType.APPLICATION_JSON)
//	public Response loadStatus()
//	{
//		String TAG="[SettingsWs - loadStatus]";
//		try{
//			
//			List<ComboBoxVo> result= new ArrayList<ComboBoxVo>();
//			
//			List<SettingsVo> settings = settingsService.loadSettingsByCategory("COMBO","STATUS");
//			if(settings!=null && !settings.isEmpty())
//			{
//				for (SettingsVo settingsVo : settings)
//				{
//					ComboBoxVo  cm= new ComboBoxVo ();
//					cm.setCode(settingsVo.getCode());
//					cm.setVal(settingsVo.getValue());
//					result.add(cm);
//				}
//			}
//			//public enum TareasEstadoEnum implements java.io.Serializable
//			//	PENDIENTE,PLANIFICADO,RESUELTO, NO_RESUELTO;
//			
//			Gson gson = initializesGson();
//			return Response.ok(gson.toJson(result),MediaType.APPLICATION_JSON).build();
//
//		}catch (IllegalArgumentException ex){
//			LOGGER.error(TAG + " - Error: {}",ex);
//			return Response.serverError().status(Status.BAD_REQUEST).build();
//
//			
//		}catch (Exception ex) {
//			LOGGER.error(TAG + " - Error: {}",ex);
//			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
//		}
//	}
	
	


//	@JWTTokenNeeded
//	@GZIP
//	@POST
//	@Path("/loadZonas")
//	@Consumes(MediaType.APPLICATION_JSON)
//	public Response loadAllZonas()
//	{
//		String TAG="[SettingsWs - loadZonas]";
//		try{
////			String userSession = this.getSecurityUuidUser();
////			LOGGER.info(TAG + " - userSession: {} procesing...",userSession);			
//			
//			List<ComboBoxVo> result= new ArrayList<ComboBoxVo>();
//			
//			List<SettingsVo> settings = settingsService.loadSettingsByCategory("COMBO","ZONAS");
//			if(settings!=null && !settings.isEmpty())
//			{
//				for (SettingsVo settingsVo : settings)
//				{
//					ComboBoxVo  cm= new ComboBoxVo ();
//					cm.setCode(settingsVo.getCode());
//					cm.setVal(settingsVo.getValue());
//					result.add(cm);
//				}
//				
//			}
//			
//			Gson gson = initializesGson();
//			return Response.ok(gson.toJson(result),MediaType.APPLICATION_JSON).build();
//
//		}catch (IllegalArgumentException ex){
//			LOGGER.error(TAG + " - Error: {}",ex);
//			return Response.serverError().status(Status.BAD_REQUEST).build();
//
//			
//		}catch (Exception ex) {
//			LOGGER.error(TAG + " - Error: {}",ex);
//			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
//		}
//	}
	

	
	@JWTTokenNeeded
	@GZIP
	@POST
	@Path("/loadNacionalidad")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response loadNacionalidad()
	{
		String TAG="[SettingsWs - loadNacionalidad]";
		try{
//			String userSession = this.getSecurityUuidUser();
//			LOGGER.info(TAG + " - userSession: {} procesing...",userSession);			
			
			List<ComboBoxVo> result= new ArrayList<ComboBoxVo>();
			
			List<CountryVo> list = countryService.loadAll();
			for (CountryVo countryVo : list)
			{
				ComboBoxVo  cm= new ComboBoxVo ();
				cm.setCode(countryVo.getAlfa2());
				cm.setVal(countryVo.getName());
				result.add(cm);
			}
			
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
	@Path("/loadProvincia")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response loadProvincia()
	{
		String TAG="[SettingsWs - loadProvincia]";
		try{
			//			String userSession = this.getSecurityUuidUser();
			//			LOGGER.info(TAG + " - userSession: {} procesing...",userSession);			

			List<ComboBoxVo> result= new ArrayList<ComboBoxVo>();

			List<ProvinceVo> list = provinceService.loadAll();
			for (ProvinceVo provinceVo : list)
			{
				ComboBoxVo  cm= new ComboBoxVo ();
				cm.setCode(provinceVo.getIdProvince().toString());
				cm.setVal(provinceVo.getName());
				result.add(cm);
			}
			


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
	@Path("/{idProvincia}/loadMunicipio")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response loadMunicipio(@PathParam("idProvincia") Integer idProvincia)
	{
		String TAG="[SettingsWs - loadMunicipio]";
		try{
			//			String userSession = this.getSecurityUuidUser();
			//			LOGGER.info(TAG + " - userSession: {} procesing...",userSession);			

			List<ComboBoxVo> result= new ArrayList<ComboBoxVo>();


			List<CityVo> list = cityService.loadByIdProvince(idProvincia);
			for (CityVo cityVo : list)
			{
				ComboBoxVo  cm= new ComboBoxVo ();
				cm.setCode(cityVo.getName());
				cm.setVal(cityVo.getName());
				result.add(cm);
			}



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
	@Path("/loadRol")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response loadRole()
	{
		String TAG="[SettingsWs - loadRole]";
		try{
			//			String userSession = this.getSecurityUuidUser();
			//			LOGGER.info(TAG + " - userSession: {} procesing...",userSession);			

			List<ComboBoxVo> result= new ArrayList<ComboBoxVo>();
			Collection<String> list = RoleEnum.literals();

			for (String code : list)
			{
				ComboBoxVo  cm1= new ComboBoxVo ();
				cm1.setCode(code);
				cm1.setVal(code.replace("_", " "));
				result.add(cm1);
			}

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
	@Path("{idSegmento}/loadRates")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response loadRates(@PathParam("idSegmento") Integer idSegmento)
	{
		String TAG="[SettingsWs - loadRates]";
		try{
			//			String userSession = this.getSecurityUuidUser();
			//			LOGGER.info(TAG + " - userSession: {} procesing...",userSession);			

			List<RateExtVo> result= new ArrayList<RateExtVo>();

			List<RateVo> list = rateService.loadByIdSegment(idSegmento);
			if(list!=null && !list.isEmpty())
			{
				for (RateVo rateVo : list)
				{
					RateExtVo  cm1= new RateExtVo ();
					cm1.setIdSegment(idSegmento);
					cm1.setCode(rateVo.getIdRate());
					cm1.setValue(rateVo.getName());

					if (rateVo.getDiscounts()!=null && !rateVo.getDiscounts().equals(""))
					{
						String[] discounts = rateVo.getDiscounts().split(";");
						List<ComboBoxVo> discountList = new ArrayList<ComboBoxVo>();

						for (int i = 0; i < discounts.length; i++)
						{
							String ratefull = discounts[i];
							String[] rate = ratefull.split(":");
							
							ComboBoxVo  cmAux= new ComboBoxVo ();
							cmAux.setCode(rate[0]);
							cmAux.setVal(rate[1]);
							
							discountList.add(cmAux);
						}
						cm1.setDiscounts(discountList);
					}
					result.add(cm1);
				}
			}
			
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
	@Path("/loadStatusSale")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response loadStatusSale() {
	    String TAG = "[SettingsWs - loadStatusSale]";
	    try {
	        List<ComboBoxVo> result = new ArrayList<>();

	        List<String> orderedStatuses = Arrays.asList(
	            "PDTE_DOC", 
	            "PDTE_PAGO", 
	            "PDTE_INICIO_CURSO", 
	            "EJECUCION_CURSO", 
	            "CURSO_FINALIZADO", 
	            "NOTIFICADO_FUNDAE", 
	            "CANCELADO"
	        );

	        Set<String> allowedStatuses = new HashSet<>(orderedStatuses);

	        Collection<String> allStatuses = StatusVentaEnum.literals();

	        for (String code : allStatuses) {
	            if (allowedStatuses.contains(code)) {
	                ComboBoxVo cm1 = new ComboBoxVo();
	                cm1.setCode(code);
	                cm1.setVal(code.replace("_", " "));
	                result.add(cm1);
	            }
	        }

	        result.sort((cb1, cb2) -> {
	            int index1 = orderedStatuses.indexOf(cb1.getCode());
	            int index2 = orderedStatuses.indexOf(cb2.getCode());
	            return Integer.compare(index1, index2);
	        });

	        Gson gson = initializesGson();
	        return Response.ok(gson.toJson(result), MediaType.APPLICATION_JSON).build();

	    } catch (IllegalArgumentException ex) {
	        LOGGER.error(TAG + " - Error: {}", ex);
	        return Response.serverError().status(Status.BAD_REQUEST).build();
	    } catch (Exception ex) {
	        LOGGER.error(TAG + " - Error: {}", ex);
	        return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
	    }
	}


	
	
	@JWTTokenNeeded
	@GZIP
	@POST
	@Path("/loadStatusAdquisition")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response loadStatusAdquisition() {
	    String TAG = "[SettingsWs - loadStatusAdquisition]";
	    try {
	        List<ComboBoxVo> result = new ArrayList<>();

	        List<String> filteredStatuses = Arrays.asList("ENVIAR_OCM", "PROCESADO", "CODIFICADO", "ERROR");

	        for (String code : filteredStatuses) {
	            ComboBoxVo cm = new ComboBoxVo();
	            cm.setCode(code);
	            cm.setVal(code.replace("_", " "));
	            result.add(cm);
	        }

	        Gson gson = initializesGson();
	        return Response.ok(gson.toJson(result), MediaType.APPLICATION_JSON).build();

	    } catch (IllegalArgumentException ex) {
	        LOGGER.error(TAG + " - Error: {}", ex);
	        return Response.serverError().status(Status.BAD_REQUEST).build();

	    } catch (Exception ex) {
	        LOGGER.error(TAG + " - Error: {}", ex);
	        return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
	    }
	}
	
	
	@JWTTokenNeeded
	@GZIP
	@POST
	@Path("/loadEnergyCompanies")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response loadEnergyCompanies()
	{
		String TAG="[SettingsWs - loadEnergyCompanies]";
		try{

			List<ComboBoxVo> result= new ArrayList<ComboBoxVo>();
			List<SettingsVo> list = settingsService.loadSettingsByCategory("COMPANY", "ENERGY");

			for (SettingsVo settingsVo : list)
			{
				ComboBoxVo  cm1= new ComboBoxVo ();
				cm1.setCode(settingsVo.getCode());
				cm1.setVal(settingsVo.getValue());
				result.add(cm1);
			}

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
    @Path("/loadProviders")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response loadProviders(@QueryParam("onlyMain") Boolean onlyMain,
                                  @QueryParam("mainUsername") String mainUsername)
    {
            String TAG="[SettingsWs - loadProviders]";
            try{
                        AuthUserVo user = getSecurityIdUser();
                    TAG="[SettingsWs - loadProviders Username:"+user.getUsername()+" >>]";

//                    if (RoleEnum.CAPTADOR.equals(user.getRole()) || RoleEnum.AGENTE.equals(user.getRole())) {
//                            return Response.status(Status.UNAUTHORIZED).build();
//                    }

                    List<AuthUserVo> providers = new ArrayList<AuthUserVo>();
                    java.util.Set<String> added = new java.util.HashSet<String>();

                    if (RoleEnum.PARTNER.equals(user.getRole())) {
                        providers = authService.loadProvidersByUserUuid(user.getUuid());
                    } else if (Boolean.TRUE.equals(onlyMain)
                            && RoleEnum.SUPER_ADMNISTRADOR.equals(user.getRole())) {
                    	providers = authService.loadMainProviders();
                    } else if (RoleEnum.SUPER_ADMNISTRADOR.equals(user.getRole())) {
                        providers = authService.loadByRole(RoleEnum.PROVIDER);
                    } else if (RoleEnum.ADMINISTRADOR.equals(user.getRole())
                    		|| (RoleEnum.AGENTE.equals(user.getRole()) 
                    		|| RoleEnum.CAPTADOR.equals(user.getRole()))) {
                        List<String> parentCompanyNames = normalizeParentCompanyNames(user.getParentCompanies());
                        if (parentCompanyNames.isEmpty()) {
                                providers = authService.loadProvidersByUserUuid(user.getUuid());
                        } else {
                                List<AuthUserVo> availableProviders = authService.loadByRole(RoleEnum.PROVIDER);
                                if (availableProviders != null) {
                                        for (AuthUserVo provider : availableProviders) {
                                                if (matchesParentCompany(provider, parentCompanyNames)
                                                                && provider != null
                                                                && added.add(provider.getUuid())) {
                                                        providers.add(provider);
                                                }
                                        }
                                }
                        }
                    } else if (RoleEnum.PROVIDER.equals(user.getRole())) {
                        providers.add(authService.loadByUuid(user.getUuid()));
                    } 
//                    else if (RoleEnum.AGENTE.equals(user.getRole()) || RoleEnum.CAPTADOR.equals(user.getRole())) {
//                        collectProviders(user, added, providers);
//                    }
                    else if (RoleEnum.CORDINADOR.equals(user.getRole())) {
                        collectProviders(user, added, providers);

                        List<AuthUserVo> agents = authService.loadByRole(RoleEnum.AGENTE, RoleEnum.CAPTADOR);
                        if (agents != null) {
                            for (AuthUserVo agent : agents) {
                                if (user.getUuid().equals(agent.getUuidCordinador())) {
                                    collectProviders(agent, added, providers);
                                }
                            }
                        }
                    } else if (RoleEnum.SUPERVISOR.equals(user.getRole())) {
                        collectProviders(user, added, providers);

                        List<AuthUserVo> coordinators = authService.loadByRole(RoleEnum.CORDINADOR);
                        List<AuthUserVo> agents = authService.loadByRole(RoleEnum.AGENTE, RoleEnum.CAPTADOR);

                        if (coordinators != null) {
                            for (AuthUserVo coord : coordinators) {
                                if (user.getUuid().equals(coord.getUuidSupervisor())) {
                                    collectProviders(coord, added, providers);

                                    if (agents != null) {
                                        for (AuthUserVo agent : agents) {
                                            if (coord.getUuid().equals(agent.getUuidCordinador())) {
                                                collectProviders(agent, added, providers);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        providers = authService.loadProvidersByUserUuid(user.getUuid());
                    }

                    if (providers != null) {
                        java.util.Set<String> uuids = new java.util.HashSet<String>();
                        List<AuthUserVo> mainProviders = new ArrayList<AuthUserVo>();

                        for (AuthUserVo provider : providers) {
                            if (provider == null || !RoleEnum.PROVIDER.equals(provider.getRole())) {
                                continue;
                            }

                            if (Boolean.TRUE.equals(provider.getIsMainProvider())) {
                                if (provider.getUuid() != null && uuids.add(provider.getUuid())) {
                                    mainProviders.add(provider);
                                }
                            } else {
                                AuthUserVo parent = authService.findParentProviderBySubProviderUsername(provider.getUsername());
                                if (parent != null && Boolean.TRUE.equals(parent.getIsMainProvider()) && parent.getUuid() != null
                                        && RoleEnum.PROVIDER.equals(parent.getRole()) && uuids.add(parent.getUuid())) {
                                    mainProviders.add(parent);
                                }
                            }
                        }

                        providers = mainProviders;
                    }

                    Map<String, Integer> priority = new HashMap<>();
                    priority.put("CAPTA", 1);
                    priority.put("ZAP_CAPTA", 2);
                    priority.put("PIN_CAPTA", 3);
                    priority.put("APEX", 4);
                    priority.put("ZAP_APEX", 5);
                    priority.put("PIN_APEX", 6);
                    priority.put("ZAP_STARTEND", 7);
                    priority.put("UNIDAD_DIGITAL", 8);

                    providers.sort(new Comparator<AuthUserVo>() {
                        @Override
                        public int compare(AuthUserVo a, AuthUserVo b) {
                            String nameA = a.getUsername() != null ? a.getUsername().toUpperCase() : "";
                            String nameB = b.getUsername() != null ? b.getUsername().toUpperCase() : "";
                            int prioA = priority.getOrDefault(nameA, Integer.MAX_VALUE);
                            int prioB = priority.getOrDefault(nameB, Integer.MAX_VALUE);
                            if (prioA != prioB) {
                                return Integer.compare(prioA, prioB);
                            }
                            return nameA.compareTo(nameB);
                        }
                    });

                    for (AuthUserVo p : providers) {
                            p.setPassword(null);
                            p.setId(null);
                    }

                    Gson gson = initializesGson();
                    return Response.ok(gson.toJson(providers),MediaType.APPLICATION_JSON).build();

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
	    @Path("/loadProvidersAuth")
	    @Consumes(MediaType.APPLICATION_JSON)
	    public Response loadProvidersAuth()
	    {
	            String TAG="[SettingsWs - loadProvidersAuth]";
	            try{
	                    AuthUserVo user = getSecurityIdUser();
	                    TAG = "[SettingsWs - loadProvidersAuth Username:" + (user != null ? user.getUsername() : "null") + " >>]";

	                    List<AuthUserVo> providers = authService.loadByRole(RoleEnum.PROVIDER);

	                    if (providers != null) {
	                            for (AuthUserVo provider : providers) {
	                                    if (provider != null) {
	                                            provider.setPassword(null);
	                                            provider.setId(null);
	                                    }
	                            }
	                    }

	                    Gson gson = initializesGson();
	                    return Response.ok(gson.toJson(providers),MediaType.APPLICATION_JSON).build();

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
	    @Path("/loadSubProviders")
	    @Consumes(MediaType.APPLICATION_JSON)
	    public Response loadSubProviders(@QueryParam("mainUsername") String mainUsername)
	    {
	            String TAG="[SettingsWs - loadSubProviders]";
	            try{
	                        AuthUserVo user = getSecurityIdUser();
	                        TAG="[SettingsWs - loadSubProviders Username:"+user.getUsername()+" >>]";
	                        String normalizedMainUsername = mainUsername != null ? mainUsername.trim() : null;
	                        AuthUserVo parentProvider = null;
	                        boolean filterByParent = normalizedMainUsername != null && !"".equals(normalizedMainUsername);
	                        if (filterByParent) {
	                                AuthUserVo requestedProvider = authService.loadByUsername(normalizedMainUsername);
	                                if (requestedProvider != null && Boolean.TRUE.equals(requestedProvider.getIsMainProvider())) {
	                                        parentProvider = requestedProvider;
	                                } else {
	                                        parentProvider = authService.findParentProviderBySubProviderUsername(normalizedMainUsername);
	                                }
	                        }

	                        List<AuthUserVo> relatedProviders;
	                        if (RoleEnum.SUPER_ADMNISTRADOR.equals(user.getRole())
	                                || RoleEnum.ADMINISTRADOR.equals(user.getRole())
	                                || RoleEnum.AGENTE.equals(user.getRole())
	                                || RoleEnum.BACKOFFICE.equals(user.getRole())
	                                || RoleEnum.CAPTADOR.equals(user.getRole())
	                                || RoleEnum.COLABORADOR.equals(user.getRole())
	                                || RoleEnum.PARTNER.equals(user.getRole())) {
	                        	relatedProviders = authService.loadSubProvidersByUsernameLike("");
	                        } else {
	                                relatedProviders = authService.loadProvidersByUserUuid(user.getUuid());
	                        }
	                        List<AuthUserVo> providers = new ArrayList<AuthUserVo>();
	                        java.util.Set<String> uuids = new java.util.HashSet<String>();

	                        if (relatedProviders != null) {
	                                for (AuthUserVo provider : relatedProviders) {
	                                        if (provider == null || provider.getUuid() == null) {
	                                                continue;
	                                        }

	                                        if (!RoleEnum.PROVIDER.equals(provider.getRole()) || Boolean.TRUE.equals(provider.getIsMainProvider())) {
	                                                continue;
	                                        }

	                                        if (filterByParent) {
	                                                if (parentProvider == null) {
	                                                        continue;
	                                                }
	                                                AuthUserVo parent = authService.findParentProviderBySubProviderUsername(provider.getUsername());
	                                                if (parent == null || parent.getUsername() == null || !parent.getUsername().equalsIgnoreCase(parentProvider.getUsername())) {
	                                                        continue;
	                                                }
	                                        }

	                                        if (uuids.add(provider.getUuid())) {
	                                                providers.add(provider);
	                                        }
	                                }
	                        }

	                        Map<String, Integer> priority = new HashMap<>();
	                        priority.put("CAPTA", 1);
	                        priority.put("ZAP_CAPTA", 2);
	                        priority.put("PIN_CAPTA", 3);
	                        priority.put("APEX", 4);
	                        priority.put("ZAP_APEX", 5);
	                        priority.put("PIN_APEX", 6);
	                        priority.put("ZAP_STARTEND", 7);
	                        priority.put("UNIDAD_DIGITAL", 8);

	                        if (providers != null) {
	                                providers.sort(new Comparator<AuthUserVo>() {
	                                        @Override
	                                        public int compare(AuthUserVo a, AuthUserVo b) {
	                                                String nameA = a.getUsername() != null ? a.getUsername().toUpperCase() : "";
	                                                String nameB = b.getUsername() != null ? b.getUsername().toUpperCase() : "";
	                                                int prioA = priority.getOrDefault(nameA, Integer.MAX_VALUE);
	                                                int prioB = priority.getOrDefault(nameB, Integer.MAX_VALUE);
	                                                if (prioA != prioB) {
	                                                        return Integer.compare(prioA, prioB);
	                                                }
	                                                return nameA.compareTo(nameB);
	                                        }
	                                });

	                                for (AuthUserVo p : providers) {
	                                        p.setPassword(null);
	                                        p.setId(null);
	                                }
	                        }

	                        Gson gson = initializesGson();
	                        return Response.ok(gson.toJson(providers),MediaType.APPLICATION_JSON).build();

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
	    
	    
	private void collectProviders(AuthUserVo user, java.util.Set<String> uuids, java.util.List<AuthUserVo> providers) {
        if (user == null) {
                return;
        }
        java.util.List<AuthUserVo> direct = authService.loadProvidersByUserUuid(user.getUuid());
        if (direct != null) {
                for (AuthUserVo p : direct) {
                        if (p != null && uuids.add(p.getUuid())) {
                                providers.add(p);
                        }
                }
        }
}

    private List<String> normalizeParentCompanyNames(List<ParentCompanyVo> parentCompanies) {
            List<String> normalized = new ArrayList<String>();
            if (parentCompanies == null) {
                    return normalized;
            }

            for (ParentCompanyVo parentCompany : parentCompanies) {
                    if (parentCompany == null || parentCompany.getName() == null) {
                            continue;
                    }

                    String normalizedName = normalizeIdentifier(parentCompany.getName());
                    if (!normalizedName.isEmpty()) {
                            normalized.add(normalizedName);
                    }
            }

            return normalized;
    }

    private boolean matchesParentCompany(AuthUserVo provider, List<String> normalizedParentCompanyNames) {
            if (provider == null || normalizedParentCompanyNames == null || normalizedParentCompanyNames.isEmpty()) {
                    return false;
            }

            String normalizedUsername = normalizeIdentifier(provider.getUsername());
            if (normalizedUsername.isEmpty()) {
                    return false;
            }

            for (String parentCompanyName : normalizedParentCompanyNames) {
                    if (parentCompanyName.contains(normalizedUsername) || normalizedUsername.contains(parentCompanyName)) {
                            return true;
                    }
            }

            return false;
    }

    private String normalizeIdentifier(String value) {
            if (value == null) {
                    return "";
            }

            return value.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }
}
