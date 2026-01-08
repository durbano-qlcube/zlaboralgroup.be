package com.zap.maintenance.service.crypto;


import java.io.Serializable;
import java.nio.charset.Charset;
import java.security.MessageDigest;

import javax.ejb.Singleton;

import com.zap.maintenance.exception.crypto.HashServiceException;
import com.zap.maintenance.vo.enumerates.HashEnum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Session Bean implementation class MD5Service
 */
@Singleton
public class HashService implements Serializable{

	private static final long serialVersionUID = 5597629607641669217L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HashService.class);
	private final static String CONTENT_TYPE= "UTF-8";
	private final static String MD5= "MD5";
	private final static String SHA1 = "SHA-1";
	private final static String SHA256 = "SHA-256";
	private final static String SHA384 = "SHA-384";
	private final static String SHA512 = "SHA-512";

//	private final static String PREFIX_MD5="$1$";
//	private final static String PREFIX_BLOWFISH="";//$2a$ lo implementa el propio algoritmo
//	private final static String PREFIX_SHA1="$3$";
//	private final static String PREFIX_SHA384="$4$";
//	private final static String PREFIX_SHA256="$5$";
//	private final static String PREFIX_SHA512="$6$";
	

	

	
	private String getHash(String input,String method)  throws HashServiceException
	{
		long T1=System.currentTimeMillis();
		String hashtext="";
		
		try{
			MessageDigest m = MessageDigest.getInstance(method);
			m.reset();
			m.update(input.getBytes(Charset.forName(CONTENT_TYPE)));
			byte[] digest = m.digest();
			
			for (byte aux : digest)
			{
				int b = aux & 0xff;
				if (Integer.toHexString(b).length() == 1) {
					hashtext += "0";
				}
				hashtext += Integer.toHexString(b);
			} 

		}catch (Exception e) {
			throw new HashServiceException(e);
		
		}finally{
			LOGGER.debug("[HashService - getHash] - Finish Timing:"+(System.currentTimeMillis()-T1));
		}
		return hashtext;
		

	}
	
	private Boolean validate(String hashed, String input,String method)   throws HashServiceException
	{
		long T1=System.currentTimeMillis();
		Boolean result=false;
		LOGGER.debug("[HashService - validate] - init..");
		try{
			String hashtext=this.getHash(input, method);
			result = hashed.equalsIgnoreCase(hashtext);
			
			LOGGER.debug("[HashService - validate] - hashed:"+hashed);
			LOGGER.debug("[HashService - validate] - hashtext:"+hashtext);
			LOGGER.debug("[HashService - validate] - result:"+result);
			
		}catch (Exception e) {
			throw new HashServiceException(e);
		
		}finally{
			LOGGER.debug("[HashService - validate] - Finish Timing:"+(System.currentTimeMillis()-T1));
		}
		
		return result;
	}
	
	public String getHash(String input,HashEnum method)  throws HashServiceException
	{
		String result="";
		if (HashEnum.MD5.equals(method))
			result=this.getHash(input,MD5);
		
		else if (HashEnum.SHA1.equals(method))
			result= this.getHash(input,SHA1);
		
		else if (HashEnum.SHA256.equals(method))
			result= this.getHash(input,SHA256);
		
		else if (HashEnum.SHA384.equals(method))
			result= this.getHash(input,SHA384);
		
		else if (HashEnum.SHA512.equals(method))
			result= this.getHash(input,SHA512);
		
		else if (HashEnum.BLOWFISH.equals(method))
			result = BCrypt.hashpw(input, BCrypt.gensalt());

		return result;
		

	}
	
	public Boolean validate(String hashed, String input,HashEnum method) throws HashServiceException
	{
		Boolean result=false;
		//String hashedWitoutPrefix=hashed.substring(3);
		
		if (HashEnum.MD5.equals(method)){
			result=this.validate(hashed,input,MD5);
		
		}else if (HashEnum.SHA1.equals(method)){
			result=this.validate(hashed,input,SHA1);
		
		}else if (HashEnum.SHA256.equals(method)){
			result=this.validate(hashed,input,SHA256);
		
		}else if (HashEnum.SHA384.equals(method)){
			result=this.validate(hashed,input,SHA384);
		
		}else if (HashEnum.SHA512.equals(method)){
			result=this.validate(hashed,input,SHA512);
		
		}else if (HashEnum.BLOWFISH.equals(method)){
			result=BCrypt.checkpw(input, hashed);// el prefijo lo implementa el mismo metodo

		}
		return result;
	}
	

}
