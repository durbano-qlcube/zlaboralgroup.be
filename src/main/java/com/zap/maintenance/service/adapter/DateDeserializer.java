package com.zap.maintenance.service.adapter;

import java.lang.reflect.Type;
import java.util.Date;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class DateDeserializer implements JsonDeserializer<Date>{

	@Override
	public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		if(json == null)
			return null;
		else{
			
//			SimpleDateFormat sf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
//			
//			System.out.println("-------------------------------INIT --------------------------------");
//			System.out.println("----------------------------------------------------------------");
//			System.out.println("CalendarDeserializer >>> YYYYYYY json: "+json.getAsJsonPrimitive().getAsLong());
//			
//			Date tx =new Date();
//			tx.setTime(json.getAsJsonPrimitive().getAsLong());
//			System.out.println("CalendarDeserializer >>> YYYYYYY Date tx : "+sf.format(tx));
//			
			Date calendar = new Date();
			calendar.setTime(json.getAsJsonPrimitive().getAsLong());
//			System.out.println("CalendarDeserializer >>> YYYYYYY calendar : "+calendar);
//			System.out.println("----------------------------------------------------------------");
//			System.out.println("--------------------------------FIN--------------------------------");
			
			return calendar;
		}
	}

}
