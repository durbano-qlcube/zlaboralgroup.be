package com.zap.maintenance.service.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.ejb.Singleton;

import com.zap.maintenance.exception.crypto.PasswordEncryptionServiceException;

import org.apache.commons.codec.binary.Base64;


@Singleton
public class PasswordEncryptionService {

	 private static String ALGORITHM = "PBKDF2WithHmacSHA1";
	 private static String SHA1_SALT = "SHA1PRNG";
	 
	 private static boolean authenticate(String attemptedPassword, byte[] encryptedPassword, byte[] salt) 
	 { 
		 
		 // throws NoSuchAlgorithmException, InvalidKeySpecException {
		 
		 // Encrypt the clear-text password using the same salt that was used to
		  // encrypt the original password
		  byte[] encryptedAttemptedPassword = getEncryptedPassword(attemptedPassword, salt);
		
		  // Authentication succeeds if encrypted password that the user entered
		  // is equal to the stored hash
		  return Arrays.equals(encryptedPassword, encryptedAttemptedPassword);
	 }
	
	 public static byte[] getEncryptedPassword(String password, byte[] salt)
	 {
		  // PBKDF2 with SHA-1 as the hashing algorithm. Note that the NIST
		  // specifically names SHA-1 as an acceptable hashing algorithm for PBKDF2
		  String algorithm = ALGORITHM;
		  // SHA-1 generates 160 bit hashes, so that's what makes sense here
		  int derivedKeyLength = 160;
		  // Pick an iteration count that works for you. The NIST recommends at
		  // least 1,000 iterations:
		  // http://csrc.nist.gov/publications/nistpubs/800-132/nist-sp800-132.pdf
		  // iOS 4.x reportedly uses 10,000:
		  // http://blog.crackpassword.com/2010/09/smartphone-forensics-cracking-blackberry-backup-passwords/
		  int iterations = 20000;
		
		  KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, derivedKeyLength);
		
		  SecretKeyFactory f;
		try {
			
			f = SecretKeyFactory.getInstance(algorithm);
			return f.generateSecret(spec).getEncoded();
			
		} catch (NoSuchAlgorithmException e) {
			throw new PasswordEncryptionServiceException();
			
		} catch (InvalidKeySpecException e) {
			throw new PasswordEncryptionServiceException();
		
		} catch (Exception e) {
			throw new PasswordEncryptionServiceException();
		}
		
		  
		  
		  
	 }
	
	 public static byte[] generateSalt()throws NoSuchAlgorithmException
	 {
		  // VERY important to use SecureRandom instead of just Random
		  SecureRandom random = SecureRandom.getInstance(SHA1_SALT);
		
		  // Generate a 8 byte (64 bit) salt as recommended by RSA PKCS5
		  byte[] salt = new byte[8];
		  random.nextBytes(salt);
		
		  return salt;
	 }
	 
	 public static boolean authenticate(String attemptedPassword, String bdPassword)
	 {
		
		 byte[] encryptedPassword = Base64.decodeBase64(bdPassword.substring(0,bdPassword.length()-22)+"=");
		 //System.out.println("passEncrypted: "+Base64.encodeBase64String(encryptedPassword));
		 byte[] pepper= Base64.decodeBase64(bdPassword.substring(bdPassword.length()-22,bdPassword.length()-11)+"="); 
		 //System.out.println("pepper: "+Base64.encodeBase64String(pepper));
		 byte[] salt= Base64.decodeBase64(bdPassword.substring(bdPassword.length()-11,bdPassword.length())+"=");
		 //System.out.println("salt: "+Base64.encodeBase64String(salt));
		 
		 String pass = attemptedPassword + Base64.encodeBase64String(pepper);
		 
		 return PasswordEncryptionService.authenticate(pass, encryptedPassword, salt);
	 }
	 
	 public static String joinEncryptedPassword(byte[] a, byte[] b, byte[] c)
	 {
		 
		 String result = Base64.encodeBase64String(a)+ Base64.encodeBase64String(b)+Base64.encodeBase64String(c);
		 String[] splitPass = result.split("=");
		 result = splitPass[0]+splitPass[1]+splitPass[2];
		 return result;
	 }
	 
	 public static void main(String[] args)
	 {

		 String password = "aaaa";

		 //PasswordEncryptionService encryptionService = new PasswordEncryptionService();
		// HashService hashService = new HashService();

		 try{
			 //Generamos salt y pepper:
			 byte[] salt = PasswordEncryptionService.generateSalt();
			 System.out.println("salt: "+Base64.encodeBase64String(salt));
			 byte[] pepper = PasswordEncryptionService.generateSalt();
			 System.out.println("pepper: "+Base64.encodeBase64String(pepper));

			 //Unimos password y pepper:
			 String password2 = password + Base64.encodeBase64String(pepper);

			 //Generamos password encriptada:
			 byte[] passEncrypted = PasswordEncryptionService.getEncryptedPassword(password2, salt);
			 System.out.println("passEncrypted: "+Base64.encodeBase64String(passEncrypted));

			 String bdpass = PasswordEncryptionService.joinEncryptedPassword(passEncrypted, pepper, salt);
			 System.out.println("Result passEncrypted: "+bdpass);

			 System.out.println("Recuperar\n=========");

			 System.out.println(PasswordEncryptionService.authenticate(password, bdpass));

		 } catch (NoSuchAlgorithmException e) {
			 throw new PasswordEncryptionServiceException();

		 } catch (Exception e) {
			 throw new PasswordEncryptionServiceException();
		 }
	 }


}
