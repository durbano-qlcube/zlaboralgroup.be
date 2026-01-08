package com.zap.maintenance.service.crypto;


import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import com.zap.maintenance.exception.crypto.TripleDesServiceException;

import org.apache.commons.codec.binary.Base64;





public class TripleDesMain implements Serializable
{

	private static final long serialVersionUID = -467469753175096173L;





	private static Cipher ENCRYPT;
	private static Cipher DECRYPT;
	private static String INIT_KEY1 = "4d89g13j4";
	private static String INIT_KEY2 = "hj91r23v5";
	private static String INIT_KEY3 = "76xjklp87";
	
	private static SecretKey KEY1;
	private static SecretKey KEY2;
	private static SecretKey KEY3;
	
	private final static  String CONTENT_TYPE= "UTF-8";
	
	

	
	public static void main(String[] args)
	{
	
		initializes();
		
		String token="UuSPePD7LK1BtotJXPB87EWj2RlFLRLB3QCzsb4l0YB1wvg2O2CLEYWVr7DA/BDVMGMvalgy5F2CopB1uipzf/0OhGeszLTTmeCE8CuC8bTEt9tHCc52rU3Fo4T5yLVcmfb0x2cqxS8=";
		String value1= decoding3Des(token);
		System.out.println(value1);

		
		String value2= "Aq9gon/DozjWTt9P7fOxxA6c0PdzSF4qHrBrwnAOD1yG";
		System.out.println(value2);
		String token2= encoding3Des(value2);
		System.out.println(token2);
		
	}
	
	
	private static void initializes()throws TripleDesServiceException
	{
		long T1=System.currentTimeMillis();
		try{
			ENCRYPT = Cipher.getInstance("DES/ECB/PKCS5Padding");
			DECRYPT = Cipher.getInstance("DES/ECB/PKCS5Padding");

			KeySpec ks1 = new DESKeySpec(INIT_KEY1.getBytes("UTF8"));
			SecretKeyFactory kf1 = SecretKeyFactory.getInstance("DES");
			KEY1 = kf1.generateSecret(ks1);

			KeySpec ks2 = new DESKeySpec(INIT_KEY2.getBytes("UTF8"));
			SecretKeyFactory kf2 = SecretKeyFactory.getInstance("DES");
			KEY2 = kf2.generateSecret(ks2);

			KeySpec ks3 = new DESKeySpec(INIT_KEY3.getBytes("UTF8"));
			SecretKeyFactory kf3 = SecretKeyFactory.getInstance("DES");
			KEY3 = kf3.generateSecret(ks3);


			System.out.println("[TripleDesService - initializes] - Finish Timing:"+(System.currentTimeMillis()-T1));
		}catch(Exception ex){
			throw new TripleDesServiceException(ex);
		}
	}

	private static String encoding(String input, SecretKey key)  throws TripleDesServiceException
	{

		try {
			ENCRYPT.init(Cipher.ENCRYPT_MODE, key);
			// Encode the string into bytes using utf-8
			byte[] utf8 = input.getBytes(CONTENT_TYPE);
			byte[] enc = ENCRYPT.doFinal(utf8);
			
			// Encode bytes to base64 to get a string
			String inputEncrypted=new String(Base64.encodeBase64(enc));
			return inputEncrypted;
			
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new TripleDesServiceException(ex);
		}
	}



	private static String decoding(String input, SecretKey key)   throws TripleDesServiceException
	{
		try {
			DECRYPT.init(Cipher.DECRYPT_MODE, key);
			// Decode base64 to get bytes
			byte[] dec = Base64.decodeBase64(input.getBytes());
			// Decrypt
			byte[] utf8 = DECRYPT.doFinal(dec);
			// Decode using utf-8
			return new String(utf8, CONTENT_TYPE);
			
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new TripleDesServiceException(ex);
		}
	}
	
	public static String encoding3DesUrlEncode(String input)   throws TripleDesServiceException
	{
		long T1=System.currentTimeMillis();
		try{
			String enc1=encoding(input, KEY1);
			String enc2=encoding(enc1, KEY3);
			String enc3=encoding(enc2, KEY2);
			String enc3UrlEncode=URLEncoder.encode(enc3,"UTF8");
			return enc3UrlEncode;
		
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
			throw new TripleDesServiceException(ex);
		
		}finally{
			System.out.println("[TripleDesService - encoding3Des] - Finish Timing:"+(System.currentTimeMillis()-T1));
		}
	}
	
	public static String decoding3DesUrlEncode(String input)   throws TripleDesServiceException
	{
		long T1=System.currentTimeMillis();
		try{
			input=URLDecoder.decode(input, "UTF8");
			String enc1=decoding(input, KEY2);
			String enc2=decoding(enc1, KEY3);
			String enc3=decoding(enc2, KEY1);
			return enc3;
			
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
			throw new TripleDesServiceException(ex);
		
		}finally{
			System.out.println("[TripleDesService - decoding3Des] - Finish Timing:"+(System.currentTimeMillis()-T1));
		}

	}
	
	
	public static String encoding3Des (String input)   throws TripleDesServiceException
	{
		long T1=System.currentTimeMillis();
		String enc1=encoding(input, KEY1);
		String enc2=encoding(enc1, KEY3);
		String enc3=encoding(enc2, KEY2);
		System.out.println("[TripleDesService - encoding3Des] - Finish Timing:"+(System.currentTimeMillis()-T1));
		return enc3;

	}
	
	
	public static String decoding3Des(String input)   throws TripleDesServiceException
	{
		long T1=System.currentTimeMillis();
		String enc1=decoding(input, KEY2);
		String enc2=decoding(enc1, KEY3);
		String enc3=decoding(enc2, KEY1);
		System.out.println("[TripleDesService - decoding3Des] - Finish Timing:"+(System.currentTimeMillis()-T1));
		return enc3;

	}
	
	
}
