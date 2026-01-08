package com.zap.lleida.ws;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.zap.lleida.service.SmsService;
import com.zap.lleida.vo.SmsRequestVo;
import com.zap.lleida.vo.SmsResponseVo;

@Path("/sms")
public class SmsWs {

    @Inject
    SmsService smsService ;

    @POST
    @Path("/send")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendSms(SmsRequestVo smsRequest) {
        SmsResponseVo response = smsService.sendSms(smsRequest);
        return Response.status(Response.Status.OK).entity(response).build();
    }
}
