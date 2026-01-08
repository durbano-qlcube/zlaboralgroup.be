package com.zap.stripe.ws;

import com.zap.stripe.service.PaymentLinkService;
import com.zap.stripe.vo.PaymentLinkRequest;
import com.zap.stripe.vo.PaymentLinkUpdateRequest;
import com.zap.stripe.vo.PaymentLinkVo;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/paymentLinks")
public class PaymentLinkWs {

    @Inject
    PaymentLinkService paymentLinkService;

    @POST
    @Path("/create")
    @Consumes("application/json")
    @Produces("application/json")
    public Response createPaymentLink(PaymentLinkRequest paymentLinkRequest) {
        try {
            String orderId = paymentLinkRequest.getOrderId(); 
            
            
            PaymentLinkVo paymentLink = paymentLinkService.createPaymentLink(
                    paymentLinkRequest.getPriceId(),
                    paymentLinkRequest.getQuantity(),
                    orderId
            );

            return Response.status(Response.Status.CREATED).entity(paymentLink).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error al crear el enlace de pago: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @POST
    @Path("/{id}")
    @Consumes("application/json")
    @Produces("application/json")
    public Response updatePaymentLink(@PathParam("id") String id, PaymentLinkUpdateRequest request) {
        try {
            PaymentLinkVo updatedPaymentLink = paymentLinkService.updatePaymentLink(id, request.getOrderId());

            return Response.status(Response.Status.OK).entity(updatedPaymentLink).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error al actualizar el enlace de pago: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/list")
    @Produces("application/json")
    public Response listPaymentLinks(@QueryParam("limit") @DefaultValue("3") long limit) {
        try {
            List<PaymentLinkVo> paymentLinks = paymentLinkService.listPaymentLinks(limit);
            return Response.ok(paymentLinks).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error al obtener los enlaces de pago de Stripe: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}
