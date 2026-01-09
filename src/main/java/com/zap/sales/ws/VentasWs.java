package com.zap.sales.ws;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.transaction.Transactional;
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
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jboss.resteasy.annotations.GZIP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stripe.exception.StripeException;
import com.zap.maintenance.service.adapter.CalendarDeserializer;
import com.zap.maintenance.service.adapter.CalendarSerializer;
import com.zap.maintenance.service.settings.SettingsService;
import com.zap.sales.exception.alumno.AlumnoNotFoundException;
import com.zap.sales.exception.doc.DocNotFoundException;
import com.zap.sales.exception.empresa.EmpresaNotFoundException;
import com.zap.sales.exception.formacion.FormacionNotFoundException;
import com.zap.sales.exception.venta.VentaNotFoundException;
import com.zap.sales.service.AlumnoService;
import com.zap.sales.service.DocService;
import com.zap.sales.service.EmpresaService;
import com.zap.sales.service.FormacionService;
import com.zap.sales.service.PersonaService;
import com.zap.sales.service.VentasParticularService;
import com.zap.sales.service.VentasService;
import com.zap.sales.vo.alumno.AlumnoVo;
import com.zap.sales.vo.doc.DocVo;
import com.zap.sales.vo.empresa.EmpresaVo;
import com.zap.sales.vo.formacion.FormacionVo;
import com.zap.sales.vo.particular.PersonaVo;
import com.zap.sales.vo.particular.VentaPartVo;
import com.zap.sales.vo.venta.StatusVentaEnum;
import com.zap.sales.vo.venta.VentaExtVo;
import com.zap.sales.vo.venta.VentaSearchRequestVo;
import com.zap.sales.vo.venta.VentaSearchResponseExtVo;
import com.zap.sales.vo.venta.VentaSearchResponseVo;
import com.zap.sales.vo.venta.VentaVo;
import com.zap.security.exception.authenticate.NotAuthException;
import com.zap.security.filter.JWTTokenNeeded;
import com.zap.security.service.authenticate.AuthService;
import com.zap.security.vo.authenticate.AuthUserVo;
import com.zap.security.vo.enumerates.RoleEnum;
import com.zap.stripe.exception.CustomStripeException;
import com.zap.stripe.exception.ProductStripeException;
import com.zap.stripe.service.CustomerService;
import com.zap.stripe.service.PaymentLinkService;
import com.zap.stripe.service.PriceService;
import com.zap.stripe.service.ProductService;
import com.zap.stripe.vo.CustomerVo;
import com.zap.stripe.vo.PaymentLinkVo;
import com.zap.stripe.vo.PriceVo;
import com.zap.stripe.vo.ProductVo;
import com.zap.stripe.vo.StatusStripeEnum;

@Path("/ventas")
public class VentasWs {

	private static final Logger LOGGER = LoggerFactory.getLogger(VentasWs.class);
	private SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static String BASE_PATH = "";
	private static String URL = "";
	private Gson gson;

	@Context
	SecurityContext securityContext;

	@Inject
	EmpresaService empresaService;

	@Inject
	VentasService ventasService;

	@Inject
	FormacionService formacionService;

	@Inject
	AlumnoService alumnoService;

	@Inject
	AuthService authService;

	@Inject
	DocService docService;

	@Inject
	CustomerService stripeService;

	@Inject
	ProductService productService;

	@Inject
	PriceService priceService;

	@Inject
	PaymentLinkService paymentLinkService;

	@Inject
	VentasParticularService ventasParticularService;

	@Inject
	PersonaService personaService;
	
	@Inject
	SettingsService settingsService;
	
	
	@PostConstruct
	public void initializes()
	{
		BASE_PATH =  settingsService.loadUrlSaveDoc();
//		BASE_PATH =	"C:\\Users\\Emanuel\\wildfly-21.0.2.Final\\standalone";
		URL=  settingsService.loadUrl();
	}

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
	@Path("/empresa/{uuIdEmpresa}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response loadVentasByUuidEmpresa(@PathParam("uuIdEmpresa") String uuIdEmpresa) {
		String TAG = "[VentasWs - loadVentasByUuidEmpresa uuId:" + uuIdEmpresa + "]";
		LOGGER.debug(TAG + " - init");

		try {

			AuthUserVo user = getSecurityIdUser();

			List<VentaExtVo> ventasConDetalles = new ArrayList<VentaExtVo>();

			EmpresaVo empresaVo = empresaService.loadByUuId(uuIdEmpresa);

			List<VentaVo> ventas = ventasService.loadVentasByIdEmpresa(empresaVo.getIdEmpresa());
			if (ventas == null || ventas.isEmpty())
				ventasConDetalles = new ArrayList<VentaExtVo>();

			for (VentaVo ventaVo : ventas) {
				FormacionVo formacionVo = formacionService.loadByIdVenta(ventaVo.getIdVenta());

				VentaExtVo ventaExtVo = new VentaExtVo();
				ventaExtVo.setVenta(ventaVo);
				ventaExtVo.setFormacion(formacionVo);

				List<AlumnoVo> alumnosVo = alumnoService.loadByIdFormacion(formacionVo.getIdFormacion());
				ventaExtVo.setAlumnos(alumnosVo);

				List<DocVo> documentos = docService.loadDocByIdVenta(ventaVo.getIdVenta());
				ventaExtVo.setDocumentos(documentos);

				ventasConDetalles.add(ventaExtVo);
			}

			Gson gson = initializesGson();
			return Response.ok(gson.toJson(ventasConDetalles)).build();

//		} catch (VentaNotFoundException ex) {
//			LOGGER.error(TAG + " - Error: {}", ex);
//			return Response.serverError().status(Status.NOT_FOUND).build();

		} catch (IllegalArgumentException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.BAD_REQUEST).build();

		} catch (NotAuthException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.UNAUTHORIZED).build();

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@JWTTokenNeeded
	@GZIP
	@GET
	@Path("/{uuidVenta}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response loadVentasByUuid(@PathParam("uuidVenta") String uuidVenta) {
		String TAG = "[VentasWs - cargarVentaExt uuidVenta:" + uuidVenta + "]";
		LOGGER.debug(TAG + " - init");

		try {

			AuthUserVo user = getSecurityIdUser();

			VentaVo ventaVo = ventasService.loadByuuid(uuidVenta);
			if (ventaVo == null)
				throw new VentaNotFoundException();

			if (!StringUtils.isBlank(ventaVo.getUuidProvider())) {
				try {
					AuthUserVo provider = authService.loadByUuid(ventaVo.getUuidProvider());
					if (provider != null) {
						String providerName = StringUtils.firstNonBlank(provider.getUsername(), provider.getName(),
								provider.getFullname(), provider.getEmail());
						ventaVo.setProviderName(providerName);
					}
				} catch (Exception ex) {
					LOGGER.error(TAG + " - Error loading provider: {}", ventaVo.getUuidProvider(), ex);
				}
			}

			EmpresaVo empresaVo = empresaService.load(ventaVo.getIdEmpresa());

			FormacionVo formacionVo = formacionService.loadByIdVenta(ventaVo.getIdVenta());

			VentaExtVo ventaExtVo = new VentaExtVo();
			ventaExtVo.setVenta(ventaVo);
			ventaExtVo.setFormacion(formacionVo);
			ventaExtVo.setEmpresa(empresaVo);

			List<AlumnoVo> alumnosVo = alumnoService.loadByIdFormacion(formacionVo.getIdFormacion());
			ventaExtVo.setAlumnos(alumnosVo);

			List<DocVo> documentos = docService.loadDocByIdVenta(ventaVo.getIdVenta());
			ventaExtVo.setDocumentos(documentos);

			Gson gson = initializesGson();
			return Response.ok(gson.toJson(ventaExtVo)).build();

		} catch (VentaNotFoundException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.NOT_FOUND).build();

		} catch (IllegalArgumentException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.BAD_REQUEST).build();

		} catch (NotAuthException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.UNAUTHORIZED).build();

		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@JWTTokenNeeded
	@GZIP
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response search(@QueryParam("pageLimit") int pageLimit, @QueryParam("pageNumber") int pageNumber,
			VentaSearchRequestVo request) {
		String TAG = "[VentasWs - loadAll]";
		LOGGER.debug(TAG + " - init");

		try {
			AuthUserVo user = getSecurityIdUser();
			TAG = "[VentasWs - uuidAdm:" + user.getUuid() + " >> loadAll]";

			if (request == null)
				throw new IllegalArgumentException(TAG + " >> 'request' can not be null");

			if (pageLimit < 0) {
				throw new IllegalArgumentException(TAG + " >> 'pageLimit' debe ser mayor a 0");
			}

			if (pageNumber < 0) {
				throw new IllegalArgumentException(TAG + " >> 'pageNumber' debe ser mayor a 0");
			}

//			if (request.getFxInitCurso() != null) {
//				Calendar fxVentaInitStartOfDayCurso = this.setStartOfDay(request.getFxInitCurso());
//				request.setFxInitCurso(fxVentaInitStartOfDayCurso);
//				LOGGER.info(TAG + ">> FxVentaInit: {} ", FORMAT_DATE.format(fxVentaInitStartOfDayCurso.getTime()));
//			}
//			if (request.getFxFinCurso() != null) {
//				Calendar fxVentaFinEndOfDayCurso = this.setEndOfDay(request.getFxFinCurso());
//				request.setFxFinCurso(fxVentaFinEndOfDayCurso);
//				LOGGER.info(TAG + ">> FxVentaFin: {} ", FORMAT_DATE.format(fxVentaFinEndOfDayCurso.getTime()));
//
//			}
                        if (!hasParentCompanyFilter(request)) {
                                VentaSearchResponseExtVo emptyResult = buildEmptyVentaSearchResponse();
                                Gson gson = initializesGson();
                                return Response.ok(gson.toJson(emptyResult)).build();
                        }

                        if (request.getFxVentaInit() != null) {
				Calendar fxVentaInitStartOfDay = this.setStartOfDay(request.getFxVentaInit());
				request.setFxVentaInit(fxVentaInitStartOfDay);
				LOGGER.info(TAG + ">> FxVentaInit: {} ", FORMAT_DATE.format(fxVentaInitStartOfDay.getTime()));
			}

			if (request.getFxVentaFin() != null) {
				Calendar fxVentaFinEndOfDay = this.setEndOfDay(request.getFxVentaFin());
				request.setFxVentaFin(fxVentaFinEndOfDay);
				LOGGER.info(TAG + ">> FxVentaFin: {} ", FORMAT_DATE.format(fxVentaFinEndOfDay.getTime()));

			}

			if (request.getFxVentaInit() != null && request.getFxVentaFin() == null) {
				Calendar fxVentaInitStartOfDay = this.setStartOfDay(request.getFxVentaInit());
				request.setFxVentaInit(fxVentaInitStartOfDay);
				LOGGER.info(TAG + ">> FxVentaInit: {} ", FORMAT_DATE.format(fxVentaInitStartOfDay.getTime()));

				Calendar fxVentaFinEndOfDay = this.setEndOfDay(request.getFxVentaInit());
				request.setFxVentaFin(fxVentaFinEndOfDay);
				LOGGER.info(TAG + ">> FxVentaFin: {} ", FORMAT_DATE.format(fxVentaFinEndOfDay.getTime()));
			}
			
//			if (request.getFxInitCurso() != null && request.getFxFinCurso() == null) {
//				Calendar fxCursoInitStartOfDay = this.setStartOfDay(request.getFxInitCurso());
//				request.setFxFinCurso(fxCursoInitStartOfDay);
//				LOGGER.info(TAG + ">> FxCursoInit: {} ", FORMAT_DATE.format(fxCursoInitStartOfDay.getTime()));
//
//				Calendar fxCursoFinEndOfDay = this.setEndOfDay(request.getFxInitCurso());
//				request.setFxFinCurso(fxCursoFinEndOfDay);
//				LOGGER.info(TAG + ">> FxCursoFin: {} ", FORMAT_DATE.format(fxCursoFinEndOfDay.getTime()));
//			}

			if (request.getFxVentaInit() == null && request.getFxVentaFin() != null) {
				Calendar fxVentaInitStartOfDay = this.setStartOfDay(request.getFxVentaFin());
				request.setFxVentaInit(fxVentaInitStartOfDay);
				LOGGER.info(TAG + ">> FxVentaInit: {} ", FORMAT_DATE.format(fxVentaInitStartOfDay.getTime()));

				Calendar fxVentaFinEndOfDay = this.setEndOfDay(request.getFxVentaFin());
				request.setFxVentaFin(fxVentaFinEndOfDay);
				LOGGER.info(TAG + ">> FxVentaFin: {} ", FORMAT_DATE.format(fxVentaFinEndOfDay.getTime()));
			}
//			if (request.getFxInitCurso() == null && request.getFxFinCurso() != null) {
//				Calendar fxCursoInitStartOfDay = this.setStartOfDay(request.getFxFinCurso());
//				request.setFxFinCurso(fxCursoInitStartOfDay);
//				LOGGER.info(TAG + ">> FxCursoInit: {} ", FORMAT_DATE.format(fxCursoInitStartOfDay.getTime()));
//
//				Calendar fxCursoFinEndOfDay = this.setEndOfDay(request.getFxFinCurso());
//				request.setFxFinCurso(fxCursoFinEndOfDay);
//				LOGGER.info(TAG + ">> FxCursoFin: {} ", FORMAT_DATE.format(fxCursoFinEndOfDay.getTime()));
//			}

                        VentaSearchResponseExtVo result = ventasService.search(request, user, pageLimit, pageNumber);

                        Gson gson = initializesGson();
                        return Response.ok(gson.toJson(result)).build();

		} catch (IllegalArgumentException ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			return Response.status(Status.BAD_REQUEST).build();
		} catch (NotAuthException ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			return Response.status(Status.UNAUTHORIZED).build();
		} catch (Exception ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@JWTTokenNeeded
	@GZIP
	@POST
	@Path("/saveOrUpdate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveOrUpdate(VentaExtVo ventaExtVo) {
		String TAG = "[VentasWs - update]";
		LOGGER.debug(TAG + " - init");

		try {
			AuthUserVo user = getSecurityIdUser();
			TAG = "[SaleWs - uuidAdm:" + user.getUuid() + " >> saveOrUpdate]";

			VentaVo ventaVo = ventaExtVo.getVenta();
			if (ventaVo == null)
				throw new IllegalArgumentException(TAG + " >> ventaVo is null or empty...");

			VentaExtVo result = new VentaExtVo();
			if (ventaVo.getIdVenta() != null) {
				LOGGER.info(TAG + " - processing update....");
				result = this.updateVentaExt(TAG, user, ventaExtVo);

			} else {
				LOGGER.info(TAG + " - processing create....");
				result = this.createVentaExt(TAG, user, ventaExtVo);
			}

			Gson gson = initializesGson();
			return Response.ok(gson.toJson(result)).build();

		} catch (IllegalArgumentException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.BAD_REQUEST).build();

		} catch (NotAuthException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.UNAUTHORIZED).build();

		} catch (Exception ex) {
			LOGGER.error(TAG + " - ", ex);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@JWTTokenNeeded
	@GZIP
	@POST
	@Path("/createPaymentLink")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createPaymentLink(VentaVo ventaVo) {
		String TAG = "[VentasWs - createPaymentLink]";
		LOGGER.debug(TAG + " - init");

		try {
			AuthUserVo user = getSecurityIdUser();
			TAG = "[SaleWs - uuidAdm:" + user.getUuid() + " >> saveOrUpdate]";

			if (ventaVo == null || ventaVo.getIdVenta() == null) {
				throw new IllegalArgumentException("El objeto ventaVo o su ID es nulo.");
			}

			VentaVo venta = null;
			VentaPartVo ventaPart = null;
			ProductVo createdProduct;
			long unitAmount;

			if ("empresa".equals(ventaVo.getVentaSegment())) {
				venta = validarVentaEmpresa(ventaVo);
				createdProduct = crearProductoEnStripe(formacionService.loadByIdVenta(ventaVo.getIdVenta()));
				unitAmount = calcularPrecioConIVA(venta.getPrice());
				venta.setStripeProductId(createdProduct.getId());
			} else if ("privada".equals(ventaVo.getVentaSegment())) {
				ventaPart = validarVentaPrivada(ventaVo);
				createdProduct = crearProductoPrivadaEnStripe(ventaPart);
				unitAmount = calcularPrecioConIVA(ventaPart.getPrice());
				
				long originalQuantity = unitAmount;
	            int percentageToPay = ventaVo.getPercentageToPay();

	            if (percentageToPay > 0 && percentageToPay <= 100) {
	                long adjustedQuantity = (originalQuantity * percentageToPay) / 100;
	                unitAmount = adjustedQuantity;
	            }
				ventaPart.setStripeProductId(createdProduct.getId());
				ventaPart.setPercentageToPay(percentageToPay);


			} else {
				throw new IllegalArgumentException("Segmento de venta no válido.");
			}

			String uuidOrderId = null;

			PriceVo createdPrice = priceService.createPrice(createdProduct.getId(), unitAmount, "eur", "month");
			if ("empresa".equals(ventaVo.getVentaSegment())) {
				venta.setStripePrecioId(createdPrice.getId());
				uuidOrderId = ventasService.getUuidOrderId();
				venta.setStripeUuidOrderId(uuidOrderId);
			} else if ("privada".equals(ventaVo.getVentaSegment())) {
				ventaPart.setStripePrecioId(createdPrice.getId());
				uuidOrderId = ventasParticularService.getUuidOrderId();
				ventaPart.setStripeUuidOrderId(uuidOrderId);

			}

			PaymentLinkVo paymentLink = paymentLinkService.createPaymentLink(createdPrice.getId(), 1, uuidOrderId);

			if ("empresa".equals(ventaVo.getVentaSegment())) {
				venta.setStripePaymentLink(paymentLink.getUrl());
				venta.setStripePaymentId(paymentLink.getId());
				venta.setStripePaymentStatus(StatusStripeEnum.PENDIENTE);
			} else if ("privada".equals(ventaVo.getVentaSegment())) {
				ventaPart.setStripePaymentLink(paymentLink.getUrl());
				ventaPart.setStripePaymentId(paymentLink.getId());
				ventaPart.setStripePaymentStatus(StatusStripeEnum.PENDIENTE);
			}

//			HashMap<String, String> result = new HashMap<>();
			HashMap<String, Object> result = new HashMap<>();

			if ("empresa".equals(ventaVo.getVentaSegment())) {
				ventasService.update(venta, false);

				result.put("stripePaymentLink", venta.getStripePaymentLink());
				result.put("stripePaymentStatus", StatusStripeEnum.PENDIENTE.toString());
				result.put("stripePaymentId", venta.getStripePaymentId());

			} else if ("privada".equals(ventaVo.getVentaSegment())) {
				ventasParticularService.update(ventaPart, false);

				result.put("stripePaymentLink", ventaPart.getStripePaymentLink());
				result.put("stripePaymentStatus", StatusStripeEnum.PENDIENTE.toString());
				result.put("stripePaymentId", ventaPart.getStripePaymentId());
				result.put("percentageToPay", ventaPart.getPercentageToPay());

			}

			Gson gson = initializesGson();
			return Response.ok(gson.toJson(result)).build();

		} catch (IllegalArgumentException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.status(Status.BAD_REQUEST).entity("{\"error\":\"" + ex.getMessage() + "\"}").build();
		} catch (NotAuthException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.status(Status.UNAUTHORIZED).entity("{\"error\":\"No autorizado\"}").build();
		} catch (Exception ex) {
			LOGGER.error(TAG + " - ", ex);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Error interno del servidor\"}")
					.build();
		}
	}

	@JWTTokenNeeded
	@GZIP
	@POST
	@Path("/getPaymentInfo")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPaymentInfo(VentaVo ventaVo) {
		String TAG = "[VentasWs - webhookPaymentLink]";
		LOGGER.debug(TAG + " - init");

		try {
			AuthUserVo user = getSecurityIdUser();
			TAG = "[SaleWs - uuidAdm:" + user.getUuid() + " >> getPaymentInfo]";

			VentaVo venta = null;
			VentaPartVo ventaPart = null;
			HashMap<String, Object> result = new HashMap<>();

			if ("empresa".equals(ventaVo.getVentaSegment())) {
				venta = ventasService.load(ventaVo.getIdVenta());

				result.put("stripePaymentLink", venta.getStripePaymentLink());
				result.put("stripePaymentStatus", venta.getStripePaymentStatus().toString());
				result.put("stripePaymentId", venta.getStripePaymentId());
				result.put("charged", String.valueOf(venta.getCharged()));

			} else if ("privada".equals(ventaVo.getVentaSegment())) {
				ventaPart = ventasParticularService.load(ventaVo.getIdVenta());

				result.put("stripePaymentLink", ventaPart.getStripePaymentLink());
				result.put("stripePaymentStatus", ventaPart.getStripePaymentStatus().toString());
				result.put("stripePaymentId", ventaPart.getStripePaymentId());
	            result.put("charged", ventaPart.getCharged());

			}

			Gson gson = initializesGson();
			return Response.ok(gson.toJson(result)).build();

		} catch (IllegalArgumentException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.BAD_REQUEST).build();

		} catch (NotAuthException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.UNAUTHORIZED).build();

		} catch (Exception ex) {
			LOGGER.error(TAG + " - ", ex);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@JWTTokenNeeded
	@GZIP
	@POST
	@Path("/webhookPaymentLink")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response webhookPaymentLink(VentaVo ventaVo) {
		String TAG = "[VentasWs - webhookPaymentLink]";
		LOGGER.debug(TAG + " - init");

		try {
			AuthUserVo user = getSecurityIdUser();
			TAG = "[SaleWs - uuidAdm:" + user.getUuid() + " >> webhookPaymentLink]";

			// RECIBO DE STRIPE LA INFO PARA UNA VENTA DADA PAYME_ID >RECUPERA DE LA VENTA
			// RECUPERA LA VENTA y CREA LOS DATOS RECIBIDAS ASOCIADO A LA VENTA
			// ANALIZAR EL PAGO
			// SI ES EXPIRADO >> SE ACTULIA STRIPE _STATUS
			// SI ES FAILED >> SE ACTULIA STRIPE _STATUS
			// SI ES OK >> SE ACTULIA STRIPE _STATUS Y el flag cobrado = true

			return Response.ok().build();

		} catch (IllegalArgumentException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.BAD_REQUEST).build();

		} catch (NotAuthException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.UNAUTHORIZED).build();

		} catch (Exception ex) {
			LOGGER.error(TAG + " - ", ex);
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	private VentaExtVo updateVentaExt(String TAG, AuthUserVo user, VentaExtVo ventaExtVo)
			throws EmpresaNotFoundException, FormacionNotFoundException, VentaNotFoundException,
			AlumnoNotFoundException {

                EmpresaVo empresaVo = ventaExtVo.getEmpresa();
                VentaVo ventaVo = ventaExtVo.getVenta();
                Long parentCompanyId = ventaVo != null ? ventaVo.getParentCompanyId() : null;

                if (empresaVo.getOrigin() == null) {
                        empresaVo.setOrigin("LABORALGROUP");
                }
                empresaService.update(empresaVo, false);

                FormacionVo formacionVo = ventaExtVo.getFormacion();
                formacionVo.setIdEmpresa(empresaVo.getIdEmpresa());
                formacionService.update(formacionVo, false);

                saveOrUpdateAlumnos(ventaExtVo, empresaVo, formacionVo);

                if (ventaVo != null) {
                        ventaVo.setParentCompanyId(parentCompanyId);
                }

		if (ventaVo.getUuIdAgente() != null) {
			AuthUserVo usuario = authService.loadByUuid(ventaVo.getUuIdAgente());

			if (usuario != null) {
				ventaVo.setUsernameAgente(usuario.getUsername());
				ventaVo.setUuIdAgente(usuario.getUuid());
			}

			if (usuario.getRole().equals(RoleEnum.AGENTE)) {
				ventaVo.setUsernameCoordinador(usuario.getCordinadorUsername());
				ventaVo.setUuIdCoordinador(usuario.getUuidCordinador());

				AuthUserVo usuarioSupervisor = authService.loadByCoordinadorUuid(usuario.getUuidCordinador());
				
				if (usuarioSupervisor != null) {
					ventaVo.setUsernameSupervisor(usuarioSupervisor.getSupervisorUsername());
					ventaVo.setUuIdSupervisor(usuarioSupervisor.getUuidSupervisor());
				}
				
			} else if (usuario.getRole().equals(RoleEnum.CORDINADOR)) {
				ventaVo.setUsernameCoordinador(usuario.getUsername());
				ventaVo.setUuIdCoordinador(usuario.getUuid());
				AuthUserVo usuarioSupervisor = authService.loadByCoordinadorUuid(usuario.getUuidCordinador());
				
				if (usuarioSupervisor != null) {
					ventaVo.setUsernameSupervisor(usuarioSupervisor.getSupervisorUsername());
					ventaVo.setUuIdSupervisor(usuarioSupervisor.getUuidSupervisor());
				}

			} else if (usuario.getRole().equals(RoleEnum.SUPERVISOR) || usuario.getRole().equals(RoleEnum.ADMINISTRADOR)
					|| usuario.getRole().equals(RoleEnum.BACKOFFICE)) {
				ventaVo.setUsernameCoordinador(usuario.getUsername());
				ventaVo.setUuIdCoordinador(usuario.getUuid());
				ventaVo.setUsernameSupervisor(usuario.getUsername());
				ventaVo.setUuIdSupervisor(usuario.getUuid());
				
			}
		}

		ventasService.update(ventaVo, false);

		VentaExtVo result = new VentaExtVo();
		result.setVenta(ventaVo);
		result.setEmpresa(empresaVo);
		result.setFormacion(formacionVo);
		return result;

	}

	private VentaExtVo createVentaExt(String TAG, AuthUserVo user, VentaExtVo ventaExtVo) {
		TAG = TAG + " >> createVentaExt - ";
		LOGGER.info(TAG + " -  processing....");
                EmpresaVo empresaVo = ventaExtVo.getEmpresa();
                VentaVo ventaVo = ventaExtVo.getVenta();
                Long parentCompanyId = ventaVo != null ? ventaVo.getParentCompanyId() : null;

                EmpresaVo empresaVoExist = empresaService.loadByCif(empresaVo.getCif());
                if (empresaVoExist != null) {
                        LOGGER.info(TAG + " -  Empresa existente con CIF:{}....", empresaVo.getCif());
                        empresaVo.setIdEmpresa(empresaVoExist.getIdEmpresa());
                }
                if (empresaVo.getOriginUserUsername() == null) {
			empresaVo.setOriginUserUuid(user.getUuid());
			empresaVo.setOriginUserUsername(user.getUsername());
		}
		if (empresaVo.getOrigin() == null) {
			empresaVo.setOrigin("GESFORMA");
		}

                if (empresaVo.getEstado() == null || empresaVo.getEstado().isEmpty()) {
                        empresaVo.setEstado("NUEVA");
                }

                empresaVo = empresaService.saveOrUpdate(empresaVo);
                LOGGER.info(TAG + " -  Empresa saved or updated con CIF:{} idEmpresa:{}....", empresaVo.getCif(),
                                empresaVo.getIdEmpresa());

                ventaVo.setIdEmpresa(empresaVo.getIdEmpresa());
                ventaVo.setParentCompanyId(parentCompanyId);

		if (ventaExtVo.getVenta().getFxVenta() == null)
			ventaExtVo.getVenta().setFxVenta(Calendar.getInstance());

		ventaVo.setUsernameAgente(user.getUsername());
		ventaVo.setUuIdAgente(user.getUuid());
		ventaVo.setOrigin(empresaVo.getOrigin());
		ventaVo.setOriginUserUsername(user.getUsername());
		ventaVo.setOriginUserUuid(user.getUuid());
		ventaVo.setStatus(StatusVentaEnum.PDTE_DOC);
		ventaVo.setStripePaymentStatus(StatusStripeEnum.PENDIENTE);

		if ((RoleEnum.AGENTE.equals(user.getRole()) || RoleEnum.PARTNER.equals(user.getRole()))
				&& (ventaVo.getUuidProvider() == null || ventaVo.getUuidProvider().trim().isEmpty())) {
			String providerUuid = resolveProviderUuid(user);
			if (providerUuid != null) {
				ventaVo.setUuidProvider(providerUuid);
			}
		}


		if (user.getRole().toString().equals(RoleEnum.CORDINADOR.toString())) {
			ventaVo.setUsernameAgente(user.getUsername());
			ventaVo.setUuIdAgente(user.getUuid());
			ventaVo.setUsernameCoordinador(user.getUsername());
			ventaVo.setUuIdCoordinador(user.getUuid());
			ventaVo.setUuIdSupervisor(user.getUuidSupervisor());
			ventaVo.setUsernameSupervisor(user.getSupervisorUsername());

		} else if (user.getRole().toString().equals(RoleEnum.AGENTE.toString())) {
			ventaVo.setUsernameAgente(user.getUsername());
			ventaVo.setUuIdAgente(user.getUuid());
			ventaVo.setUuIdCoordinador(user.getUuidCordinador());
			ventaVo.setUsernameCoordinador(user.getCordinadorUsername());

			AuthUserVo coordinador = authService.loadByCoordinadorUuid(user.getUuidCordinador());
			if (coordinador != null) {
				ventaVo.setUsernameSupervisor(coordinador.getSupervisorUsername());
				ventaVo.setUuIdSupervisor(coordinador.getUuidSupervisor());
			}
		} else if (user.getRole().toString().equals(RoleEnum.SUPERVISOR.toString())
				|| user.getRole().toString().equals(RoleEnum.BACKOFFICE.toString())
				|| user.getRole().toString().equals(RoleEnum.ADMINISTRADOR.toString())) {
			ventaVo.setUsernameSupervisor(user.getUsername());
			ventaVo.setUuIdSupervisor(user.getUuid());

			ventaVo.setUsernameCoordinador(user.getUsername());
			ventaVo.setUuIdCoordinador(user.getUuid());

			ventaVo.setUsernameAgente(user.getUsername());
			ventaVo.setUuIdAgente(user.getUuid());
		}

		FormacionVo formacionVo = ventaExtVo.getFormacion();
		formacionVo.setIdEmpresa(empresaVo.getIdEmpresa());
		LOGGER.info(TAG + " -  Creating Formacion with IdEmpresa:{} IdVenta:{} IdFormacion:{}....",
				empresaVo.getIdEmpresa(), ventaVo.getIdVenta(), formacionVo.getIdFormacion());
		formacionVo = formacionService.create(formacionVo);
		ventaVo.setIdFormacion(formacionVo.getIdFormacion());
		LOGGER.info(TAG + " -  Formacion created with id:{}....", formacionVo.getIdFormacion());

		List<AlumnoVo> incomingAlumnos = ventaExtVo.getAlumnos();
		if (incomingAlumnos != null && !incomingAlumnos.isEmpty()) {
			for (AlumnoVo alumnoVo : incomingAlumnos) {
				alumnoVo.setIdFormacion(formacionVo.getIdFormacion());
				alumnoVo.setIdEmpresa(empresaVo.getIdEmpresa());
				alumnoService.saveOrUpdate(alumnoVo);
				LOGGER.info(TAG + " -  Alumno saved or updated with  id:{}....", alumnoVo.getIdAlumno());

			}
		}

		ventaVo.setUuidProvider(ensureMainProviderUuid(user, ventaVo.getUuidProvider()));
		ventaVo = ventasService.create(ventaVo);
		LOGGER.info(TAG + " -  Venta created with  id:{}....", ventaVo.getIdVenta());

		VentaExtVo result = new VentaExtVo();
		result.setVenta(ventaVo);
		result.setEmpresa(empresaVo);
		result.setFormacion(formacionVo);
		return result;

	}


	private String resolveProviderUuid(AuthUserVo user) {
		if (user == null) {
			return null;
		}

		if (user.getUuidProviders() != null) {
			for (String providerUuid : user.getUuidProviders()) {
				if (providerUuid != null && !providerUuid.trim().isEmpty()) {
					return providerUuid;
				}
			}
		}

		if (user.getUuid() == null) {
			return null;
		}

		try {
			List<AuthUserVo> providers = authService.loadProvidersByUserUuid(user.getUuid());
			if (providers != null) {
				for (AuthUserVo provider : providers) {
					if (provider != null && provider.getUuid() != null
						&& !provider.getUuid().trim().isEmpty()) {
						return provider.getUuid();
					}
				}
			}
		} catch (Exception ex) {
			LOGGER.warn("[VentasWs - resolveProviderUuid] - Error resolving provider for user {}: {}", user.getUuid(),
							ex.getMessage());
		}

		return null;
	}

	private String ensureMainProviderUuid(AuthUserVo user, String currentUuidProvider) {
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
			LOGGER.warn("[VentasWs - ensureMainProviderUuid] - Error resolving providers for user {}: {}", user.getUuid(),
					ex.getMessage());
		}

		if (StringUtils.isBlank(mainProviderUuid)) {
			AuthUserVo parentProvider = authService.findParentProviderBySubProviderUsername(user.getUsername());
			if (parentProvider != null && !StringUtils.isBlank(parentProvider.getUuid())) {
				mainProviderUuid = parentProvider.getUuid();
			}
		}

		return mainProviderUuid;
	}

	private void saveOrUpdateAlumnos(VentaExtVo ventaExtVo, EmpresaVo empresaVo, FormacionVo formacionVo)
			throws AlumnoNotFoundException {
		String TAG = "[gestionarAlumnos]";
		LOGGER.info(TAG + " - init");

		List<AlumnoVo> incomingAlumnos = ventaExtVo.getAlumnos();
		List<AlumnoVo> alumnosToUpdate = new ArrayList<>();
		List<AlumnoVo> alumnosToCreate = new ArrayList<>();
		List<Integer> incomingAlumnoIds = new ArrayList<>();

		if (incomingAlumnos != null && !incomingAlumnos.isEmpty()) {
			LOGGER.info(TAG + " - Processing incoming alumnos: {}", incomingAlumnos.size());
			for (AlumnoVo alumnoVo : incomingAlumnos) {
				alumnoVo.setIdFormacion(formacionVo.getIdFormacion());
				alumnoVo.setIdEmpresa(empresaVo.getIdEmpresa());
				if (alumnoVo.getIdAlumno() != null) {
					alumnosToUpdate.add(alumnoVo);
					LOGGER.info(TAG + " - Adding to update: {}", alumnoVo);
				} else {
					alumnosToCreate.add(alumnoVo);
					LOGGER.info(TAG + " - Adding to create: {}", alumnoVo);
				}
				incomingAlumnoIds.add(alumnoVo.getIdAlumno());
			}
			LOGGER.info(TAG + " - No incoming alumnos to process");
		}

		if (formacionVo.getIdFormacion() != null) {
			List<AlumnoVo> existingAlumnos = alumnoService.loadByIdFormacion(formacionVo.getIdFormacion());
			LOGGER.info(TAG + " - Existing alumnos found: {}", existingAlumnos.size());

			for (AlumnoVo existingAlumno : existingAlumnos) {
				if (!incomingAlumnoIds.contains(existingAlumno.getIdAlumno())) {
					alumnoService.removeAlumnoFromFormacion(existingAlumno.getIdAlumno(), formacionVo.getIdFormacion());
					LOGGER.info(TAG + " - Removed alumno from formacion: {}", existingAlumno);
				}
			}
		}

		for (AlumnoVo alumnoVo : alumnosToUpdate) {
			alumnoVo.setIdFormacion(formacionVo.getIdFormacion());
			alumnoService.update(alumnoVo, false);
			LOGGER.info(TAG + " - Updated alumno: {}", alumnoVo);

		}
		for (AlumnoVo alumnoVo : alumnosToCreate) {
			alumnoService.create(alumnoVo);
			LOGGER.info(TAG + " - Created alumno: {}", alumnoVo);
		}
		LOGGER.info(TAG + " - Completed gestion de alumnos");
	}

	@Deprecated
	@JWTTokenNeeded
	@GZIP
	@POST
	@Path("/loadVenta/{idEmpresa}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response loadVenta(@PathParam("idEmpresa") Integer idEmpresa) {
		String TAG = "[EmpresaWs - load]";
		LOGGER.debug(TAG + " - init");

		try {
			AuthUserVo user = getSecurityIdUser();
			TAG = "[SaleWs - uuidAdm:" + user.getUuid() + " >> register]";

			if (idEmpresa == null)
				throw new IllegalArgumentException(TAG + " >> 'uuid' can not be null");

			List<VentaExtVo> ventasConDetalles = new ArrayList<VentaExtVo>();

//			EmpresaVo empresaVo = empresaService.load(idEmpresa);

			List<VentaVo> ventas = ventasService.loadVentasByIdEmpresa(idEmpresa);
			if (ventas == null || ventas.isEmpty())
				throw new VentaNotFoundException();

			for (VentaVo ventaVo : ventas) {
				FormacionVo formacionVo = formacionService.loadByIdVenta(ventaVo.getIdVenta());

				VentaExtVo ventaExtVo = new VentaExtVo();
				ventaExtVo.setVenta(ventaVo);
				ventaExtVo.setFormacion(formacionVo);

				List<AlumnoVo> alumnosVo = alumnoService.loadByIdFormacion(formacionVo.getIdFormacion());
				ventaExtVo.setAlumnos(alumnosVo);

				List<DocVo> documentos = docService.loadDocByIdVenta(ventaVo.getIdVenta());
				ventaExtVo.setDocumentos(documentos);

				ventasConDetalles.add(ventaExtVo);
			}

			Gson gson = initializesGson();
			return Response.ok(gson.toJson(ventasConDetalles)).build();

		} catch (IllegalArgumentException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.BAD_REQUEST).build();

		} catch (VentaNotFoundException ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			return Response.serverError().status(Status.NOT_FOUND).build();

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
	@Path("/{uuidVenta}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteVenta(@PathParam("uuidVenta") String uuidVenta) {
		String TAG = "[EmpresaWs - deleteVenta]";
		LOGGER.debug(TAG + " - init");

		try {
			AuthUserVo user = getSecurityIdUser();
			TAG = "[SaleWs - uuidAdm:" + user.getUuid() + " >> register]";

			if (uuidVenta == null)
				throw new IllegalArgumentException(TAG + " >> 'uuidVenta' can not be null");

			VentaVo ventas = ventasService.loadByuuid(uuidVenta);
			if (ventas == null)
				throw new VentaNotFoundException();

			ventasService.delete(uuidVenta);

			return Response.ok().build();

		} catch (IllegalArgumentException ex) {
			LOGGER.error(TAG + " - Error: {}", ex);
			return Response.serverError().status(Status.BAD_REQUEST).build();

		} catch (VentaNotFoundException ex) {
			LOGGER.error(TAG + " - Error: ", ex.getMessage());
			return Response.serverError().status(Status.NOT_FOUND).build();

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
    @POST
    @Path("/download")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response searchExcel(VentaSearchRequestVo request) {
        long t = System.currentTimeMillis();
        String TAG = "[VentasWs -" + t + " search]";
        LOGGER.debug(TAG + " - init");


        File dir = new File(BASE_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filename = "resultados_" + System.currentTimeMillis() + ".xlsx"; 
        String filePath = BASE_PATH + filename;

        try {
            AuthUserVo user = getSecurityIdUser();
            TAG = "[VentasWs - uuidAdm:" + user.getUuid() + " >> search]";

            if (request == null) {
                throw new IllegalArgumentException(TAG + " >> 'request' can not be null");
            }


            LOGGER.info(TAG + ">> {} ", request.toString());

            if (!hasParentCompanyFilter(request)) {
                throw new IllegalArgumentException(TAG + " >> 'parentCompanyId' es requerido");
            }

            if (request.getFxVentaInit() != null) {
                Calendar fxVentaInitStartOfDay = this.setStartOfDay(request.getFxVentaInit());
                request.setFxVentaInit(fxVentaInitStartOfDay);
                LOGGER.info(TAG + ">> FxVentaInit: {} ", FORMAT_DATE.format(fxVentaInitStartOfDay.getTime()));
            }

            if (request.getFxVentaFin() != null) {
                Calendar fxVentaFinEndOfDay = this.setEndOfDay(request.getFxVentaFin());
                request.setFxVentaFin(fxVentaFinEndOfDay);
                LOGGER.info(TAG + ">> FxVentaFin: {} ", FORMAT_DATE.format(fxVentaFinEndOfDay.getTime()));
            }

            
            VentaSearchResponseExtVo result = ventasService.search(request, user, null, null);

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Resultados");

                Row headerRow = sheet.createRow(0);
                String[] headers = { "Fecha de Venta", "Nombre Comercial", "Cif",
                                    "Estado de Venta", "Agente","Nombre Formacion","Cobrado"};

                for (int i = 0; i < headers.length; i++) {
                    headerRow.createCell(i).setCellValue(headers[i]);
                }

                int rowNum = 1;
                for (VentaSearchResponseVo item : result.getData()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(FORMAT_DATE.format(item.getFxVenta().getTime()));
                    row.createCell(1).setCellValue(item.getNombreComercial());
                    row.createCell(2).setCellValue(item.getCif());
                    row.createCell(3).setCellValue(item.getStatus() != null ? item.getStatus().name() : "");
                    row.createCell(4).setCellValue(item.getUsernameAgente());
                    row.createCell(5).setCellValue(item.getNombreFormacion());
                    row.createCell(6).setCellValue(item.getCharged());



                }

                try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                    workbook.write(fileOut);
                }
            }

            String downloadUrl = generateDownloadUrl(filename);
           
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("url", downloadUrl);
            LOGGER.info(TAG + " - Download URL: " + downloadUrl);

            return Response.ok(resultMap, MediaType.APPLICATION_JSON).build();

        } catch (IllegalArgumentException ex) {
            LOGGER.error(TAG + " - Error:{} ", ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();

        } catch (NotAuthException ex) {
            LOGGER.error(TAG + " - Error:{} ", ex.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();

        } catch (Exception ex) {
            LOGGER.error(TAG + " - Error ", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error interno del servidor.").build();
        }
    }

    private VentaSearchResponseExtVo buildEmptyVentaSearchResponse() {
        VentaSearchResponseExtVo response = new VentaSearchResponseExtVo();
        response.setTOTAL_RECORDS(0L);
        response.setTOTAL_PAGES(0L);
        response.setTOTAL_CHARGED(0.0);
        response.setTOTAL_PENDING_CHARGED(0.0);
        response.setData(Collections.emptyList());
        return response;
    }

    private String generateDownloadUrl(String filename) {
        String baseUrl = URL +"api/v1.0/ventas/downloads/";
        return baseUrl + filename;
    }

    private boolean hasParentCompanyFilter(VentaSearchRequestVo request) {
        if (request == null) {
            return false;
        }
        if (request.getParentCompanyId() != null) {
            return true;
        }
        return request.getParentCompanyIds() != null
                && request.getParentCompanyIds().stream().anyMatch(Objects::nonNull);
    }

    @GET
    @Path("/downloads/{filename}")
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public Response downloadFile(@PathParam("filename") String filename) {
    	
        String filePath = BASE_PATH + filename; 

        File file = new File(filePath);
        if (!file.exists()) {
            return Response.status(Response.Status.NOT_FOUND).entity("File not found").build();
        }

        StreamingOutput fileStream = new StreamingOutput() {
            @Override
            public void write(OutputStream out) throws IOException {
                try (FileInputStream input = new FileInputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = input.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
            }
        };

        return Response.ok(fileStream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .build();
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

	private VentaVo validarVentaEmpresa(VentaVo ventaVo)
			throws DocNotFoundException, EmpresaNotFoundException, StripeException, CustomStripeException {
		VentaVo venta = ventasService.load(ventaVo.getIdVenta());
		if (venta == null || venta.getUuId() == null) {
			throw new IllegalArgumentException("El objeto venta empresa es nulo.");
		}

		EmpresaVo empresa = empresaService.load(venta.getIdEmpresa());
		if (empresa == null) {
			throw new IllegalArgumentException("La empresa asociada a la venta es nula.");
		}

		CustomerVo customer = obtenerOCrearCliente(venta, empresa);
		venta.setStripeCustomerId(customer.getId());

		return venta;
	}

	private VentaPartVo validarVentaPrivada(VentaVo ventaVo)
			throws DocNotFoundException, StripeException, CustomStripeException {
		VentaPartVo venta = ventasParticularService.load(ventaVo.getIdVenta());
		if (venta == null || venta.getUuId() == null) {
			throw new IllegalArgumentException("El objeto venta privada es nulo.");
		}

		PersonaVo persona = personaService.loadByIdPersona(venta.getIdPersona());
		if (persona == null) {
			throw new IllegalArgumentException("La persona asociada a la venta es nula.");
		}

		CustomerVo customer = obtenerOCrearClientePrivada(venta, persona);
		venta.setStripeCustomerId(customer.getId());

		return venta;
	}

	private CustomerVo obtenerOCrearCliente(VentaVo venta, EmpresaVo empresa)
			throws StripeException, CustomStripeException {
		if (venta.getStripeCustomerId() == null) {
			CustomerVo customerVo = new CustomerVo();
			customerVo.setEmail(empresa.getEmailContacto());
			customerVo.setName(empresa.getNombreComercial());
			return stripeService.createCustomer(customerVo);
		} else {
			String query = String.format("name:'%s' AND email:'%s'", empresa.getNombreComercial(),
					empresa.getEmailContacto());
			List<CustomerVo> customers = stripeService.searchCustomers(query);
			if (customers.isEmpty()) {
				throw new IllegalArgumentException("Cliente no encontrado en Stripe.");
			}
			return customers.get(0);
		}
	}

	private CustomerVo obtenerOCrearClientePrivada(VentaPartVo venta, PersonaVo persona)
			throws StripeException, CustomStripeException {
		if (venta.getStripeCustomerId() == null) {
			CustomerVo customerVo = new CustomerVo();
			customerVo.setEmail(persona.getEmail());
			customerVo.setName(persona.getNombre());
			return stripeService.createCustomer(customerVo);
		} else {
			String query = String.format("name:'%s' AND email:'%s'", persona.getNombre(), persona.getEmail());
			List<CustomerVo> customers = stripeService.searchCustomers(query);
			if (customers.isEmpty()) {
				throw new IllegalArgumentException("Cliente no encontrado en Stripe.");
			}
			return customers.get(0);
		}
	}

	private ProductVo crearProductoEnStripe(FormacionVo formacionVo) throws StripeException, ProductStripeException {
		ProductVo productVo = new ProductVo();
		productVo.setName(formacionVo.getNombre() != null ? formacionVo.getNombre() : "Nombre defecto");
		String descripcion = (formacionVo.getAreaProfesional() != null
				&& !formacionVo.getAreaProfesional().trim().isEmpty()) ? formacionVo.getAreaProfesional()
						: "Descripcion no disponible";
		productVo.setDescription(descripcion);
		return productService.createProduct(productVo);
	}

	private ProductVo crearProductoPrivadaEnStripe(VentaPartVo venta) throws StripeException, ProductStripeException {
		ProductVo productVo = new ProductVo();
		productVo.setName(venta.getNombre() != null ? venta.getNombre() : "Nombre defecto");
		String descripcion = (venta.getAreaProfesional() != null && !venta.getAreaProfesional().trim().isEmpty())
				? venta.getAreaProfesional()
				: "Descripcion no disponible";
		productVo.setDescription(descripcion);

		return productService.createProduct(productVo);
	}

	private long calcularPrecioConIVA(BigDecimal price) {
	    BigDecimal priceWithTax = price.multiply(new BigDecimal("1.21"));
	    BigDecimal rounded = priceWithTax.setScale(2, RoundingMode.HALF_UP);
	    long unitAmount = rounded.multiply(new BigDecimal("100")).longValue();

	    if (unitAmount > 99999999) {
	        throw new IllegalArgumentException("El monto con IVA excede el limite permitido por Stripe.");
	    }
	    return unitAmount;
	}


}
