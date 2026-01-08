package com.zap.stripe.ws;


import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.model.checkout.SessionCollection;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionListParams;
import com.zap.sales.exception.venta.VentaNotFoundException;
import com.zap.sales.service.VentasParticularService;
import com.zap.sales.vo.particular.VentaPartVo;
import com.zap.stripe.service.StripeEventService;
import com.zap.stripe.vo.StatusStripeEnum;
import com.zap.stripe.vo.StripeEventVo;

@Path("/stripe")
public class WebhookStripeWs {

    private static final String ENDPOINT_SECRET = "STRIPE_SECRET_KEY";
    private static final String STRIPE_KEY = "STRIPE_SECRET_KEY"; 
//    private static final String STRIPE_KEY = "STRIPE_SECRET_KEY";

    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookStripeWs.class);
	
	
    @Inject
    private StripeEventService stripeEventService;
    
    @Inject
    private VentasParticularService ventasParticularService;
    
    @POST
    @Path("/webhook")
    @Consumes("application/json")
    @Produces("application/json")
    public Response handleWebhook(@HeaderParam("Stripe-Signature") String sigHeader, String payload) throws VentaNotFoundException {
    	
    	long t =System.currentTimeMillis();
    	String TAG ="[WebhookStripeWs - "+t+"]";
    	Stripe.apiKey = STRIPE_KEY;

    	
        LOGGER.info(TAG+ "Recepción de webhook Stripe iniciada...");
       // LOGGER.info(TAG+ "Payload recibido: " + payload);

        try {
            Event event = this.verifySignature(payload, sigHeader);
            LOGGER.info(TAG+ "Firma verificada correctamente");

            StripeObject stripeObject = extractStripeObject(event);
            LOGGER.info(TAG+ "Objeto deserializado: " + stripeObject.getClass().getSimpleName());	

            this.processStripeEvent(TAG, event, stripeObject);
            LOGGER.info(TAG+ "Evento procesado: " + event.getType());
            
            return Response.status(Response.Status.OK).entity("{\"status\":\"success\"}").build();
            
        } catch (SignatureVerificationException e) {
            LOGGER.error("Firma inválida");
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Invalid signature\"}").build();

        } catch (JsonSyntaxException e) {
            LOGGER.error("Payload JSON inválido");
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Invalid JSON payload\"}").build();
        } catch (IllegalStateException e) {
            LOGGER.error("Fallo en deserialización del objeto Stripe");
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Deserialization failed\"}").build();
        }

        
    }

    private Event verifySignature(String payload, String sigHeader) throws SignatureVerificationException, JsonSyntaxException {
        return Webhook.constructEvent(payload, sigHeader, ENDPOINT_SECRET);
    }

    private StripeObject extractStripeObject(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        return dataObjectDeserializer.getObject().orElseThrow(() -> new IllegalStateException("StripeObject not present"));
    }

    private void processStripeEvent(String TAG, Event event, StripeObject stripeObject) throws VentaNotFoundException {
        String eventType = event.getType();

        switch (eventType) {
            case "payment_intent.succeeded":
            case "payment_intent.payment_failed": {
                PaymentIntent intent = (PaymentIntent) stripeObject;
                String orderId = this.recuperarOrderIdDesdeSession(TAG, intent.getId());
                LOGGER.info(TAG+ " >>>>> Evento de PaymentIntent recibido: " + eventType + ", order_id: " + orderId);
                handlePaymentIntentEvent(TAG, eventType,event, orderId, intent);
                break;
            }

            case "checkout.session.completed":
           // case "checkout.session.async_payment_succeeded":
            case "checkout.session.expired": {
                Session session = (Session) stripeObject;
                String orderId = session.getMetadata().get("order_id");
                LOGGER.info(TAG+ " ****** Evento de Checkout Session recibido: " + eventType + ", order_id: " + orderId);
                this.handleCheckoutSessionEvent(TAG, eventType,event, orderId, session);
                break;
            }


            default:
                LOGGER.info("Evento no manejado: " + eventType);
        }
    }

    private String recuperarOrderIdDesdeSession(String TAG, String paymentIntentId) {
        try {
            SessionListParams params = SessionListParams.builder()
                    .setLimit(1L)
                    .setPaymentIntent(paymentIntentId)
                    .build();
            LOGGER.info(TAG+ ">  SessionList params...");
            SessionCollection sessions = Session.list(params);
            if (!sessions.getData().isEmpty())
            {
                Session session = sessions.getData().get(0);
                LOGGER.info(TAG+ ">  Session loaded...");
                
                String orderId = session.getMetadata().get("order_id");
                LOGGER.info(TAG+ ">Recuperado order_id desde Session: " + orderId);
                return orderId;
            } else {
                LOGGER.error(TAG+ ">No se encontró Session para el PaymentIntent: " + paymentIntentId);
            }
        } catch (Exception e) {
            LOGGER.error(TAG+ ">Error al recuperar Session desde PaymentIntent: " + e.getMessage());
        }
        return null;
    }

    private void handlePaymentIntentEvent(String TAG, String eventType,Event event, String orderId, PaymentIntent intent) throws VentaNotFoundException {
        
    	if ("payment_intent.succeeded".equals(eventType)) {
            LOGGER.info(TAG+ " > Payment Sucessfully for order_id: " + orderId);
            // Lógica para pago exitoso
            StripeEventVo eventVo = createEventVoFromEvent(event);
            stripeEventService.create(eventVo);
          	
            //RECUPERAMOS LA VENTA POR ORDER ID
            VentaPartVo venta = ventasParticularService.loadVentasByUuidOrderId(orderId);
            LOGGER.info(TAG + " >Venta recuperada para order_id: " + orderId);

            int charged = calcularPorcentajePagado(intent, venta, TAG);

            venta.setStripePaymentStatus(StatusStripeEnum.EXITOSO);
            Integer chargedPrincipal = venta.getCharged();
            venta.setCharged(charged + chargedPrincipal);

            //ACTULIZAMOS EL RESULTADO
            venta = ventasParticularService.update(venta, false);
            LOGGER.info(TAG+ " > Updated Ventas order_id:{} : StripePaymentStatus:{}", venta.getStripeUuidOrderId(), venta.getStripePaymentStatus().toString());

   
        } else if ("payment_intent.payment_failed".equals(eventType)) {
            LOGGER.info(TAG+ " > Payment FAILED for order_id: " + orderId);

            StripeEventVo eventVo = createEventVoFromEvent(event);
            stripeEventService.create(eventVo);
          
            //RECUPERAMOS LA VENTA POR ORDER ID
            VentaPartVo venta = ventasParticularService.loadVentasByUuidOrderId(orderId);
            LOGGER.info("Venta recuperada para order_id: " + orderId);

            
            //ACTULIZAMOS EL RESULTADO
            venta = ventasParticularService.update(venta, false);
            venta.setStripePaymentStatus(StatusStripeEnum.FALLIDO);
            LOGGER.info(TAG+ " > Updated Ventas order_id:{} : StripePaymentStatus:{}", venta.getStripeUuidOrderId(), venta.getStripePaymentStatus().toString());


        }
    }

    private void handleCheckoutSessionEvent(String TAG, String eventType, Event event, String orderId, Session session) {
        switch (eventType) {
            case "checkout.session.completed":
            case "checkout.session.async_payment_succeeded":
                fulfillCheckout(orderId);
                break;
            case "checkout.session.expired":
                LOGGER.info(TAG + "Sesión expirada para order_id: " + orderId);
                break;
        }
    }
    
//    private int calcularPorcentajePagado(PaymentIntent intent, VentaPartVo venta, String TAG)
//    {
//        Long amountReceived = intent.getAmountReceived(); 
//        BigDecimal amountReceivedDecimal = BigDecimal.valueOf(amountReceived).divide(BigDecimal.valueOf(100)); 
//
//        BigDecimal ventaPrice = venta.getPriceWithIva(); 
//        BigDecimal porcentajePagado = BigDecimal.ZERO;
//
//        if (ventaPrice != null && ventaPrice.compareTo(BigDecimal.ZERO) > 0) {
//            porcentajePagado = amountReceivedDecimal
//                                .divide(ventaPrice, 2, RoundingMode.HALF_UP)
//                                .multiply(BigDecimal.valueOf(100)); 
//        }
//
//        LOGGER.info(TAG + " > Amount received: " + amountReceivedDecimal + " / Precio venta: " + ventaPrice + " = " + porcentajePagado + "%");
//
//        int charged = porcentajePagado.setScale(0, RoundingMode.DOWN).intValue();
//        return Math.min(charged, 100); 
//    }

    
    
    private int calcularPorcentajePagado(PaymentIntent intent, VentaPartVo venta, String tag) {
        if (intent == null || venta == null) {
            LOGGER.warn("{} > PaymentIntent o VentaPartVo son nulos. No se puede calcular el porcentaje pagado.", tag);
            return 0;
        }

        Long amountReceivedCents = intent.getAmountReceived();
        if (amountReceivedCents == null) amountReceivedCents = 0L;

        BigDecimal ventaTotalCents = venta.getPriceWithIva() != null
                ? venta.getPriceWithIva().multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        BigDecimal porcentajePagado = BigDecimal.ZERO;

        if (ventaTotalCents.compareTo(BigDecimal.ZERO) > 0) {
            porcentajePagado = BigDecimal.valueOf(amountReceivedCents)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(ventaTotalCents, 2, RoundingMode.DOWN);
        }

        int porcentajeFinal = porcentajePagado.setScale(0, RoundingMode.DOWN).intValue();
        porcentajeFinal = Math.min(porcentajeFinal, 100);

        LOGGER.info("{} > Pagado: {} céntimos / Total venta: {} céntimos → {}%",
                tag, amountReceivedCents, ventaTotalCents.toBigInteger(), porcentajeFinal);

        return porcentajeFinal;
    }

    
    
    
    
    
    
    
    private void handlePaymentMethodAttached(PaymentMethod paymentMethod) {
        LOGGER.info("Método de pago adjuntado: " + paymentMethod.getId());
    }

    private void fulfillCheckout(String orderId) {
        LOGGER.info("Checkout completado para order_id: " + orderId);
        // Aquí puedes incluir la lógica de negocio para cerrar el pedido, actualizar la base de datos, etc.
    }
    
    private StripeEventVo createEventVoFromEvent(Event event) {
        StripeEventVo eventVo = new StripeEventVo();
        eventVo.setEventId(event.getId());
        eventVo.setEventType(event.getType());
        eventVo.setData(event.getData().toJson()); 

        return eventVo;
    }

}
