package com.zap.sales.ws;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.jboss.resteasy.annotations.GZIP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zap.maintenance.service.adapter.CalendarDeserializer;
import com.zap.maintenance.service.adapter.CalendarSerializer;
import com.zap.sales.service.GeneralReportService;
import com.zap.security.exception.authenticate.NotAuthException;
import com.zap.security.filter.JWTTokenNeeded;
import com.zap.security.service.authenticate.AuthService;
import com.zap.security.vo.authenticate.AuthUserVo;
import com.zap.security.vo.enumerates.RoleEnum;

@Path("/reportes")
public class ReportesWs {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportesWs.class);
    private Gson gson;

    @Context
    SecurityContext securityContext;

    @Inject
    AuthService authService;

    @Inject
    GeneralReportService reportService;

    private Gson initializesGson() {
        if (gson == null) {
            gson = new GsonBuilder().registerTypeAdapter(java.util.Calendar.class, new CalendarSerializer())
                    .registerTypeAdapter(java.util.Calendar.class, new CalendarDeserializer())
                    .registerTypeAdapter(java.util.GregorianCalendar.class, new CalendarSerializer()).create();
        }
        return gson;
    }

    private AuthUserVo getSecurityIdUser() throws NotAuthException {
        Principal principal = securityContext.getUserPrincipal();
        String uuid = principal.getName();

        if (uuid == null)
            throw new NotAuthException("getSecurityIdUser - 'id' can not be null");

        return authService.loadByUuid(uuid);
    }

    private Calendar setStartOfDay(Calendar c) {
        Calendar fx = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
        fx.setTimeInMillis(c.getTimeInMillis());
        fx.set(Calendar.HOUR_OF_DAY, 0);
        fx.set(Calendar.MINUTE, 0);
        fx.set(Calendar.SECOND, 0);
        fx.set(Calendar.MILLISECOND, 0);
        return fx;
    }

    private Calendar setEndOfDay(Calendar c) {
        Calendar fx = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
        fx.setTimeInMillis(c.getTimeInMillis());
        fx.set(Calendar.HOUR_OF_DAY, 23);
        fx.set(Calendar.MINUTE, 59);
        fx.set(Calendar.SECOND, 59);
        fx.set(Calendar.MILLISECOND, 999);
        return fx;
    }

    @JWTTokenNeeded
    @GZIP
    @GET
    @Path("/general")
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadGeneral(@QueryParam("year") Integer yearParam,
                                @QueryParam("uuidProvider") String uuidProvider,
                                @QueryParam("reference") String reference) {
        String TAG = "[ReportesWs - loadGeneral]";
        LOGGER.debug(TAG + " - init");
        try {
            AuthUserVo currentUser = getSecurityIdUser();
            if (yearParam == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("year is required").build();
            }

            if (reference == null || reference.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("reference is required").build();
            }

            if (uuidProvider != null && uuidProvider.trim().isEmpty()) {
                uuidProvider = null;
            }

            if (uuidProvider == null && currentUser != null && RoleEnum.PARTNER.equals(currentUser.getRole())) {
                uuidProvider = currentUser.getUuid();
            }

            String uuidProviderForCpl = uuidProvider;

            List<Map<String, Object>> data;
            String providerName = null;
            if (uuidProvider != null) {
                AuthUserVo provider = authService.loadByUuid(uuidProvider);
                providerName = provider != null ? provider.getUsername() : null;

                if (provider != null && RoleEnum.PROVIDER.equals(provider.getRole())
                        && Boolean.TRUE.equals(provider.getIsMainProvider())) {
                    List<AuthUserVo> providers = authService.loadSubProvidersByUsernameLike(provider.getUsername());
                    if (providers != null && !providers.isEmpty()) {
                        List<String> ids = new java.util.ArrayList<>();
                        ids.add(provider.getUuid());
                        for (AuthUserVo p : providers) {
                            ids.add(p.getUuid());
                        }
                        data = reportService.load(yearParam.intValue(), ids, reference, providerName, provider.getUuid());
                    } else {
                        data = reportService.load(yearParam.intValue(), uuidProvider, reference);
                    }
                } else {
                    data = reportService.load(yearParam.intValue(), uuidProvider, reference);
                }

                if (providerName != null) {
                    for (Map<String, Object> row : data) {
                        row.put("proveedor", providerName);
                    }
                }
            } else {
                data = reportService.load(yearParam.intValue(), (String) null, reference);
            }

            Map<String, Object> responsePayload = new LinkedHashMap<>();
            responsePayload.put("data", data);
            responsePayload.put("cpl",
                    uuidProviderForCpl != null ? reportService.loadCplByMonth(yearParam.intValue(), uuidProviderForCpl)
                            : Collections.emptyMap());

            Gson g = initializesGson();
            return Response.ok(g.toJson(responsePayload)).build();

        } catch (NotAuthException ex) {
            LOGGER.error(TAG + " - Error: {}", ex.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (Exception ex) {
            LOGGER.error(TAG + " - Error: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

  
    @JWTTokenNeeded
    @GZIP
    @GET
    @Path("/diario")
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadDiario(@QueryParam("dateFrom") String dateFrom,
                               @QueryParam("dateUntil") String dateUntil,
                               @QueryParam("uuidProvider") String uuidProvider,
                               @QueryParam("reference") String reference) {
        String TAG = "[ReportesWs - loadDiario]";
        LOGGER.debug(TAG + " - init");
        try {
            getSecurityIdUser();
            if (dateFrom == null || dateUntil == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("dateFrom and dateUntil are required").build();
            }

            if (reference == null || reference.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("reference is required").build();
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            sdf.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
            Calendar start = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
            start.setTime(sdf.parse(dateFrom));
            start = setStartOfDay(start);
            Calendar end = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
            end.setTime(sdf.parse(dateUntil));
            end = setEndOfDay(end);

            if (uuidProvider != null && uuidProvider.trim().isEmpty()) {
                uuidProvider = null;
            }

            String uuidProviderForCpl = uuidProvider;

            List<Map<String, Object>> data;
            if (uuidProvider != null) {
                AuthUserVo provider = authService.loadByUuid(uuidProvider);
                if (provider != null && RoleEnum.PROVIDER.equals(provider.getRole())
                        && Boolean.TRUE.equals(provider.getIsMainProvider())) {
                    List<AuthUserVo> providers = authService.loadSubProvidersByUsernameLike(provider.getUsername());
                    if (providers != null && !providers.isEmpty()) {
                        List<String> ids = new java.util.ArrayList<>();
                        for (AuthUserVo p : providers) {
                            ids.add(p.getUuid());
                        }
                        data = reportService.loadDaily(start, end, ids, reference, provider.getUuid());
                    } else {
                        data = reportService.loadDaily(start, end, uuidProvider, reference);
                    }
                    if (provider != null) {
                        String providerName = provider.getUsername();
                        for (Map<String, Object> row : data) {
                            row.put("proveedor", providerName);
                        }
                    }
                } else {
                    data = reportService.loadDaily(start, end, uuidProvider, reference);
                    if (provider != null) {
                        String providerName = provider.getUsername();
                        for (Map<String, Object> row : data) {
                            row.put("proveedor", providerName);
                        }
                    }
                }
            } else {
                data = reportService.loadDaily(start, end, (String) null, reference);
            }


            Map<String, Object> responsePayload = new LinkedHashMap<>();
            responsePayload.put("data", data);
            Map<String, Object> insertadosConInvalidacion = extractRowValues(data, "Total Inse. - invalidacion");
            if (insertadosConInvalidacion != null && !insertadosConInvalidacion.isEmpty()) {
                responsePayload.put("totalInsertadosInvalidacion", insertadosConInvalidacion);
            }

            Map<String, Object> cpaBruta = extractRowValues(data, "CPA Bruta");
            Map<String, Object> cpaNeta = extractRowValues(data, "CPA Neta");
            if ((cpaBruta != null && !cpaBruta.isEmpty()) || (cpaNeta != null && !cpaNeta.isEmpty())) {
                Map<String, Object> cpa = new LinkedHashMap<>();
                if (cpaBruta != null && !cpaBruta.isEmpty()) {
                    cpa.put("bruta", cpaBruta);
                }
                if (cpaNeta != null && !cpaNeta.isEmpty()) {
                    cpa.put("neta", cpaNeta);
                }
                responsePayload.put("cpa", cpa);
            }
            responsePayload.put("cpl",
                    uuidProviderForCpl != null ? reportService.loadCplByDay(start, end, uuidProviderForCpl)
                            : Collections.emptyMap());

            Gson g = initializesGson();
            return Response.ok(g.toJson(responsePayload)).build();

        } catch (NotAuthException ex) {
            LOGGER.error(TAG + " - Error: {}", ex.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (Exception ex) {
            LOGGER.error(TAG + " - Error: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @JWTTokenNeeded
    @GZIP
    @GET
    @Path("/conversionPorProveedores")
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadConversionPorProveedores(@QueryParam("year") Integer year,
                                                 @QueryParam("reference") String reference) {
        String TAG = "[ReportesWs - loadConversionPorProveedores]";
        LOGGER.debug(TAG + " - init");
        try {
            getSecurityIdUser();
            if (year == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("year is required").build();
            }
            if (reference == null || reference.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("reference is required").build();
            }

            List<Map<String, Object>> data = reportService.loadConversionByProvider(year.intValue(), reference);

            if (data != null) {
                java.util.Iterator<Map<String, Object>> it = data.iterator();
                while (it.hasNext()) {
                    Map<String, Object> row = it.next();
                    Object provider = row.get("proveedor");
                    if (provider != null) {
                        String name = provider.toString();
                        if ("TOTAL".equalsIgnoreCase(name) || "WEB_CAPTA".equalsIgnoreCase(name)) {
                            it.remove();
                        }
                    }
                }
            }

            Gson g = initializesGson();
            return Response.ok(g.toJson(data)).build();

        } catch (NotAuthException ex) {
            LOGGER.error(TAG + " - Error: {}", ex.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (Exception ex) {
            LOGGER.error(TAG + " - Error: ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    private Map<String, Object> extractRowValues(List<Map<String, Object>> data, String targetType) {
        if (data == null || targetType == null) {
            return null;
        }
        for (Map<String, Object> row : data) {
            if (row == null) {
                continue;
            }
            Object type = row.get("tipo");
            if (type != null && targetType.equalsIgnoreCase(type.toString())) {
                Map<String, Object> values = new LinkedHashMap<>();
                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    String key = entry.getKey();
                    if ("id".equals(key) || "tipo".equals(key) || "proveedor".equals(key)) {
                        continue;
                    }
                    values.put(key, entry.getValue());
                }
                return values;
            }
        }
        return null;
    }
}
