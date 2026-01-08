package com.zap.stripe.ws;

import com.zap.stripe.vo.ProductVo;
import com.zap.stripe.service.ProductService;
import com.zap.stripe.exception.CustomStripeException;
import com.zap.stripe.exception.ProductStripeException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.PathParam;


@Path("/products")
public class ProductWs {

    @Inject
    ProductService productService;

    @POST
    @Path("/create")
    @Consumes("application/json")
    @Produces("application/json")
    public Response createProduct(ProductVo productVo) {
        try {
            ProductVo createdProduct = productService.createProduct(productVo);

            return Response.status(Response.Status.CREATED).entity(createdProduct).build();
        } catch (ProductStripeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error al crear producto en Stripe: " + e.getMessage() + "\"}")
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
    public Response updateProduct(@PathParam("id") String productId, ProductVo productVo) {
        try {
            ProductVo updatedProduct = productService.updateProduct(productId, productVo);

            return Response.status(Response.Status.OK).entity(updatedProduct).build();
        } catch (ProductStripeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error al actualizar producto en Stripe: " + e.getMessage() + "\"}")
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
    public Response getProduct(@PathParam("id") String productId) {
        try {
            ProductVo product = productService.getProduct(productId);

            return Response.status(Response.Status.OK).entity(product).build();
        } catch (ProductStripeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error al obtener el producto de Stripe: " + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Ocurrio un error inesperado: " + e.getMessage() + "\"}")
                    .build();
        }
    }
    
    
    @GET
    @Path("/listProducts")
    @Produces("application/json")
    public Response getProducts(@QueryParam("limit") Long limit) {
        try {
            if (limit == null) {
                limit = 3L;
            }
            
            List<ProductVo> products = productService.getProducts(limit);

            return Response.status(Response.Status.OK).entity(products).build();
        } catch (ProductStripeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error al obtener los productos de Stripe: " + e.getMessage() + "\"}")
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
    public Response deleteProduct(@PathParam("id") String productId) {
        try {
            boolean isDeleted = productService.deleteProduct(productId);

            if (isDeleted) {
                return Response.status(Response.Status.OK)
                        .entity("{\"id\":\"" + productId + "\", \"deleted\": true}")
                        .build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Producto no encontrado en Stripe\"}")
                        .build();
            }
        } catch (ProductStripeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Error al eliminar el producto de Stripe: " + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Ocurrio un error inesperado: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}
