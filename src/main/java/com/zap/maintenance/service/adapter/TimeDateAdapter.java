package com.zap.maintenance.service.adapter;

import java.util.Calendar;
import java.util.TimeZone;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class TimeDateAdapter extends XmlAdapter<Long, Calendar>
{

	@Override
	public Long marshal(Calendar v) throws Exception {
		return v.getTimeInMillis();
	}

	@Override
	public Calendar unmarshal(Long v) throws Exception
	{

		//SimpleDateFormat sf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

//				 System.out.println("-------------------------------INIT --------------------------------");
//				System.out.println("----------------------------------------------------------------");
//				System.out.println("unmarshal >> XXXXXXX Calendar v: "+v);

		//Calendar s= Calendar.getInstance();
		Calendar s = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));

		s.setTimeInMillis(v);
		//    	System.out.println("unmarshal >> XXXXXXX Calendar src: "+sf.format(s.getTime()));
		//    	
		//		System.out.println("----------------------------------------------------------------");
		//		System.out.println("--------------------------------FIN--------------------------------");
		return s;
	}

}