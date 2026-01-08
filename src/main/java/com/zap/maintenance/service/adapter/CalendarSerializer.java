package com.zap.maintenance.service.adapter;

import java.lang.reflect.Type;
import java.util.GregorianCalendar;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class CalendarSerializer implements JsonSerializer<GregorianCalendar>{

	@Override
	public JsonElement serialize(GregorianCalendar src, Type typeOfSrc, JsonSerializationContext context)
	{
		//System.out.println("-------------------------------INIT --------------------------------");
		//System.out.println("----------------------------------------------------------------");
		//SimpleDateFormat sf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		//System.out.println("CalendarSerializer >> XXXXXXX Calendar src: "+sf.format(src.getTime()));
		//
		//Date tx =new Date();
		//tx.setTime(src.getTimeInMillis());
		//		
		//System.out.println("CalendarSerializer >> XXXXXXX Date tx.getTime: "+tx.getTime());
		//System.out.println("CalendarSerializer >> XXXXXXX Date tx: "+sf.format(tx.getTime()));
		//		
		//System.out.println("----------------------------------------------------------------");
		//System.out.println("--------------------------------FIN--------------------------------");
		return src == null ? null : new JsonPrimitive(src.getTimeInMillis());


	}

}
