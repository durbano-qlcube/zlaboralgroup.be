package com.zap.security.service.jwt;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.xml.bind.DatatypeConverter;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.JsonKeysetWriter;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadKeyTemplates;

public class TEST {

	private static KeysetHandle keysetHandle;
	private static String ASSOCIATED_DATA ="luna Lun_era se";
	private static String KEYSET_PATHFILE = "/opt/share/attendis/config/my_keyset.json";
	
	public static void main(String[] args) throws Exception
	{

		
		String JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
		
		String[] d = JWT.split("\\.");
		Integer i = d.length;
		
		
		AeadConfig.register();
		//generateKeySet();
		loadKeySet();
		
		String cipherJWT = cipherToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
		System.out.println(cipherJWT);
		
		
		String decipherToken = decipherToken("0113180CEAC04F08AD3FDD35E92139AAACCA243D4829078FE49AA46DF480A544767F81339B2437C6D411C56E0788C9CCA33B4584A5BC0F88B686437DF8506DB5ACB5688D6C2B3DB25337684C65CC069405BB376049932975C91239BE71D26A900FAA7B55F75104E05784BC64F9E63953EE5688B595CAEB2217222E9D0D9686FA609D238B5A76E35DA2F4B0DA06396D2B36271A5FDBB6591E31EA0C9D2AFEB7FB3A62B9E6D68C8DA56E70EEE4C4E7FBC5F9D387597009DF716BBD61F9");
		System.out.println(decipherToken);
	}



	public static void generateKeySet() throws IOException, GeneralSecurityException
	{
		// Generate the key material...
		KeysetHandle keysetHandle = KeysetHandle.generateNew(AeadKeyTemplates.AES128_GCM);

		// and write it to a file.
		CleartextKeysetHandle.write(keysetHandle, JsonKeysetWriter.withFile(new File(KEYSET_PATHFILE)));
	}


	public static void loadKeySet() throws GeneralSecurityException, IOException
	{
		
		File file = new File(KEYSET_PATHFILE);
		if(!file.exists())
		{
			generateKeySet();
		}
		
		keysetHandle = CleartextKeysetHandle.read(JsonKeysetReader.withFile(new File(KEYSET_PATHFILE)));
		
	}
	

    public static String cipherToken(String jwt) throws Exception {
        //Verify parameters
        if (jwt == null || jwt.isEmpty())
        {
            throw new IllegalArgumentException("Both parameters must be specified !");
        }

       // 1. Generate the key material.
        if (keysetHandle==null)
        	loadKeySet();
    

       // 2. Get the primitive.
       Aead aead = keysetHandle.getPrimitive(Aead.class);

       // 3. Use the primitive.
       byte[] cipheredToken = aead.encrypt(jwt.getBytes(), ASSOCIATED_DATA.getBytes());
       
        return DatatypeConverter.printHexBinary(cipheredToken);
    }


    public static String decipherToken(String jwtInHex) throws Exception {
        //Verify parameters
        if (jwtInHex == null || jwtInHex.isEmpty())
        {
            throw new IllegalArgumentException("Both parameters must be specified !");
        }

        //Decode the ciphered token
        byte[] cipheredToken = DatatypeConverter.parseHexBinary(jwtInHex);

        //Get the primitive
     // 1. Generate the key material.
        if (keysetHandle==null)
        	loadKeySet();
        
        // 2. Get the primitive.
        Aead aead = keysetHandle.getPrimitive(Aead.class);
        
        //Decipher the token
        byte[] decipheredToken = aead.decrypt(cipheredToken, ASSOCIATED_DATA.getBytes());

        return new String(decipheredToken);
    }
}
