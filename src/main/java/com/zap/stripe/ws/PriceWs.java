package com.zap.stripe.ws;

import com.stripe.exception.StripeException;
import com.zap.stripe.service.PriceService;
import com.zap.stripe.vo.PriceVo;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/prices")
public class PriceWs {

	@Inject
    PriceService priceService; 

    @POST
    @Path("/create")
    @Consumes("application/json")
    @Produces("application/json")
    public Response createPrice(PriceVo priceRequest) {
        try {
            PriceVo priceVo = priceService.createPrice(
                    priceRequest.getProductId(),
                    priceRequest.getUnitAmount(),
                    priceRequest.getCurrency(),
                    priceRequest.getInterval()
            );

            return Response.status(Response.Status.CREATED).entity(priceVo).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error al crear el precio: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @POST
    @Path("/{id}")
    @Consumes("application/json")
    @Produces("application/json")
    public Response updatePrice(@PathParam("id") String id, String orderId) {
        try {
            PriceVo updatedPrice = priceService.updatePrice(id, orderId);
            return Response.status(Response.Status.OK).entity(updatedPrice).build();
        } catch (StripeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error al actualizar el precio: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    @Produces("application/json")
    public Response getPriceById(@PathParam("id") String priceId) {
        try {
            PriceVo priceVo = priceService.getPriceById(priceId);
            return Response.status(Response.Status.OK).entity(priceVo).build();
        } catch (StripeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error al obtener el precio: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Produces("application/json")
    public Response listPrices(@QueryParam("limit") @DefaultValue("1") long limit) {
        try {
            List<PriceVo> priceList = priceService.listPrices(limit);
            return Response.status(Response.Status.OK).entity(priceList).build();
        } catch (StripeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error al listar los precios: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}
