package com.zap.maintenance.ws;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
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
import com.zap.maintenance.service.notifications.EmailService;
import com.zap.maintenance.vo.notifications.EmailVo;

@Path("/emailTest")
public class EmailTestWs {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailTestWs.class);
	private SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static Gson gson;
	
	@Context
	SecurityContext securityContext;
	

	@Inject
	EmailService emailService;
	
	
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

	
	
	@GZIP
	@GET
	@Path("/xx")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response xxx ()
	{
		//long t= System.currentTimeMillis();
		String TAG="[NotificacionesWs - xx]";
		try{
			
			EmailVo emailVo = new EmailVo();
			emailVo.setTo("dugt13@gmail.com");
			emailVo.setSubject("TEST EMAIL");
			emailVo.setBody("Esto es una prueba");
			emailService.sendEmail(emailVo);
			
			return Response.ok().build();

		}catch (IllegalArgumentException ex){
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.BAD_REQUEST).build();

		}catch (Exception ex) {
			LOGGER.error(TAG + " - Error: {}",ex.getLocalizedMessage());
			return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}



}
