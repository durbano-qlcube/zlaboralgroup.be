package com.zap.maintenance.service.crypto;


import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.spec.KeySpec;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zap.maintenance.exception.crypto.TripleDesServiceException;
import com.zap.maintenance.service.settings.SettingsService;
import com.zap.maintenance.vo.crypto.TripleDesSettingsVo;


@Singleton
public class TripleDesService implements Serializable
{

	private static final long serialVersionUID = -467469753175096173L;
	private static final Logger LOGGER = LoggerFactory.getLogger(TripleDesService.class);

	@Inject
	private SettingsService settingsService;

	private Cipher ENCRYPT;
	private Cipher DECRYPT;
	private SecretKey KEY1;
	private SecretKey KEY2;
	private SecretKey KEY3;
	
	private final static  String CONTENT_TYPE= "UTF-8";
	
	
	/**
	 * Default constructor. 
	 */
	public TripleDesService()throws TripleDesServiceException
	{
		
	}
	
	@PostConstruct
	private void initializes()throws TripleDesServiceException
	{

		//LOGGER.debug("[TripleDesService - initializes] - Init");
		long T1=System.currentTimeMillis();
		
		
		try{
			
			TripleDesSettingsVo TripleDesSettingsVo = settingsService.getTripleDesSettings();
			
			ENCRYPT = Cipher.getInstance("DES/ECB/PKCS5Padding");
			DECRYPT = Cipher.getInstance("DES/ECB/PKCS5Padding");

			if(TripleDesSettingsVo==null)
			{
				throw new TripleDesServiceException("Clave no recuperada");
				//SecretKey key = KeyGenerator.getInstance("DES").generateKey();//Genera Clave autom�tica
				//encrypt.init(Cipher.ENCRYPT_MODE, key);//Con clave aleatoria
				//decrypt.init(Cipher.DECRYPT_MODE, key);//Con clave aleatoria
			}else{
				KeySpec ks1 = new DESKeySpec(TripleDesSettingsVo.getKey1().getBytes("UTF8"));
				SecretKeyFactory kf1 = SecretKeyFactory.getInstance("DES");
				KEY1 = kf1.generateSecret(ks1);
				
				KeySpec ks2 = new DESKeySpec(TripleDesSettingsVo.getKey2().getBytes("UTF8"));
				SecretKeyFactory kf2 = SecretKeyFactory.getInstance("DES");
				KEY2 = kf2.generateSecret(ks2);
				
				KeySpec ks3 = new DESKeySpec(TripleDesSettingsVo.getKey3().getBytes("UTF8"));
				SecretKeyFactory kf3 = SecretKeyFactory.getInstance("DES");
				KEY3 = kf3.generateSecret(ks3);
				
			}

			LOGGER.debug("[TripleDesService - initializes] - Finish Timing:"+(System.currentTimeMillis()-T1));
		}catch(Exception ex){
			LOGGER.error("[TripleDesService - initializes] - Error: ",ex);
			throw new TripleDesServiceException(ex);
		}
	}

	private String encoding(String input, SecretKey key)  throws TripleDesServiceException
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
			LOGGER.error("[TripleDesService - encoding] - Error: ",ex);
			throw new TripleDesServiceException(ex);
		}
	}



	private String decoding(String input, SecretKey key)   throws TripleDesServiceException
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
			LOGGER.error("[TripleDesService - decoding] - Error: ",ex);
			throw new TripleDesServiceException(ex);
		}
	}
	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String encoding3DesUrlEncode(String input)   throws TripleDesServiceException
	{
		long T1=System.currentTimeMillis();
		try{
			String enc1=this.encoding(input, KEY1);
			String enc2=this.encoding(enc1, KEY3);
			String enc3=this.encoding(enc2, KEY2);
			String enc3UrlEncode=URLEncoder.encode(enc3,"UTF8");
			return enc3UrlEncode;
		
		} catch (UnsupportedEncodingException ex) {
			LOGGER.error("[TripleDesService - decoding] - Error: ",ex);
			throw new TripleDesServiceException(ex);
		
		}finally{
			LOGGER.debug("[TripleDesService - encoding3Des] - Finish Timing:"+(System.currentTimeMillis()-T1));
		}
	}
	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String decoding3DesUrlEncode(String input)   throws TripleDesServiceException
	{
		long T1=System.currentTimeMillis();
		try{
			input=URLDecoder.decode(input, "UTF8");
			String enc1=this.decoding(input, KEY2);
			String enc2=this.decoding(enc1, KEY3);
			String enc3=this.decoding(enc2, KEY1);
			return enc3;
			
		} catch (UnsupportedEncodingException ex) {
			LOGGER.error("[TripleDesService - decoding] - Error: ",ex);
			throw new TripleDesServiceException(ex);
		
		}finally{
			LOGGER.debug("[TripleDesService - decoding3Des] - Finish Timing:"+(System.currentTimeMillis()-T1));
		}

	}
	
	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String encoding3Des (String input)   throws TripleDesServiceException
	{
		long T1=System.currentTimeMillis();
		String enc1=this.encoding(input, KEY1);
		String enc2=this.encoding(enc1, KEY3);
		String enc3=this.encoding(enc2, KEY2);
		LOGGER.debug("[TripleDesService - encoding3Des] - Finish Timing:"+(System.currentTimeMillis()-T1));
		return enc3;

	}
	
	
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String decoding3Des(String input)   throws TripleDesServiceException
	{
		long T1=System.currentTimeMillis();
		String enc1=this.decoding(input, KEY2);
		String enc2=this.decoding(enc1, KEY3);
		String enc3=this.decoding(enc2, KEY1);
		LOGGER.debug("[TripleDesService - decoding3Des] - Finish Timing:"+(System.currentTimeMillis()-T1));
		return enc3;

	}
	
	
}
