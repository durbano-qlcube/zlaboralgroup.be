package com.zap.stripe.ws;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.zap.stripe.service.CustomerService;
import com.zap.stripe.vo.CustomerVo;
import com.stripe.exception.StripeException;
import com.zap.stripe.exception.CustomStripeException;

@Path("/customers")
public class CustomerWs {

    @Inject
    CustomerService stripeService;

 


    @POST
    @Path("/create")
    @Consumes("application/json")
    @Produces("application/json")
    public Response createCustomer(CustomerVo customerVo) {
        try {
            CustomerVo createdCustomer = stripeService.createCustomer(customerVo);
            
            return Response.status(Response.Status.CREATED).entity(createdCustomer).build();
        } catch (CustomStripeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error al crear cliente en Stripe: " + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Ocurrio un error inesperado: " + e.getMessage() + "\"}")
                    .build();
        }
    }
    
    @POST
    @Path("/update/{id}")
    @Consumes("application/json")
    @Produces("application/json")
    public Response updateCustomer(@PathParam("id") String customerId, CustomerVo customerVo) {
        try {
            CustomerVo updatedCustomer = stripeService.updateCustomer(customerId, customerVo);
            return Response.ok(updatedCustomer).build();
        } catch (StripeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error al actualizar cliente en Stripe: " + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Ocurrio un error inesperado: " + e.getMessage() + "\"}")
                    .build();
        }
    }
    
    @GET
    @Path("/listById/{id}")
    @Produces("application/json")
    public Response getCustomer(@PathParam("id") String customerId) {
        try {
            CustomerVo customer = stripeService.getCustomerById(customerId);
            
            return Response.ok(customer).build();
        } catch (CustomStripeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error al recuperar cliente desde Stripe: " + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Ocurrio un error inesperado: " + e.getMessage() + "\"}")
                    .build();
        }
    }
    
    
    @GET
    @Path("/listCustomers")
    @Produces("application/json")
    public Response listCustomers(@QueryParam("limit") @DefaultValue("3") int limit) {
        try {
            List<CustomerVo> customers = stripeService.listCustomers(limit);
            return Response.ok(customers).build();
        } catch (StripeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error al recuperar clientes desde Stripe: " + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Ocurrio un error inesperado: " + e.getMessage() + "\"}")
                    .build();
        }
    }
    
    
    
    @DELETE
    @Path("/delete/{id}")
    @Produces("application/json")
    public Response deleteCustomer(@PathParam("id") String customerId) {
        try {
            boolean deleted = stripeService.deleteCustomer(customerId);
            if (deleted) {
                return Response.ok("{\"status\":\"success\", \"message\":\"Cliente eliminado exitosamente\"}").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Cliente no encontrado o no se pudo eliminar\"}").build();
            }
        } catch (CustomStripeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error al eliminar cliente desde Stripe: " + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Ocurrio un error inesperado: " + e.getMessage() + "\"}")
                    .build();
        }
    }
    
    
    @GET
    @Path("/search")
    @Produces("application/json")
    public Response searchCustomers(@QueryParam("query") String query) {
        if (query == null || query.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"El parametro 'query' es obligatorio para realizar la busqueda\"}")
                    .build();
        }
        
        try {
            List<CustomerVo> customers = stripeService.searchCustomers(query);

            if (customers.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\":\"No se encontraron clientes que coincidan con los criterios de busqueda\"}")
                        .build();
            }

            return Response.ok(customers).build();
        } catch (CustomStripeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error al realizar la busqueda de clientes en Stripe: " + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Ocurrió un error inesperado: " + e.getMessage() + "\"}")
                    .build();
        }
    }

}