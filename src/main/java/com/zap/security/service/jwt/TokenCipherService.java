package com.zap.security.service.jwt;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.GeneralSecurityException;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.xml.bind.DatatypeConverter;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.JsonKeysetWriter;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import com.zap.security.exception.jwt.TokenCipherException;


@Stateless
public class TokenCipherService  implements Serializable
{

	private static final long serialVersionUID = 3507382994521802382L;
	private static KeysetHandle keysetHandle;
	private static String ASSOCIATED_DATA ="luna Lun_era eeeel3507382994521802382L";
	private static String KEYSET_PATHFILE = "/opt/share/gruasAlhambra/config/my_keyset.json";
	
	
    
	
	@PostConstruct
	private void initializes()
	{
		try {
			AeadConfig.register();
			
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
	}
	
	
    public void generateKeySet() throws IOException, GeneralSecurityException
    {
    	// Generate the key material...
    	KeysetHandle keysetHandle = KeysetHandle.generateNew(AeadKeyTemplates.AES128_GCM);

    	// and write it to a file.
    	CleartextKeysetHandle.write(keysetHandle, JsonKeysetWriter.withFile(new File(KEYSET_PATHFILE)));
    }
    
    
    public void loadKeySet() throws GeneralSecurityException, IOException
    {
    	File file = new File(KEYSET_PATHFILE);
		if(!file.exists())
		{
			this.generateKeySet();
		}
		
		keysetHandle = CleartextKeysetHandle.read(JsonKeysetReader.withFile(new File(KEYSET_PATHFILE)));
    }
    
    
    /**
     * Cipher a JWT
     *
     * @param jwt          Token to cipher
     * @param keysetHandle Pointer to the keyset handle
     * @return The ciphered version of the token encoded in HEX
     * @throws Exception If any issue occur during token ciphering operation
     */
    public String cipherToken(String jwt)
    {
    	try{
    		//Verify parameters
    		if (jwt == null || jwt.isEmpty())
    		{
    			throw new IllegalArgumentException("Both parameters must be specified !");
    		}

    		// 1. Generate the key material.
    		if (keysetHandle==null)
    			this.loadKeySet();


    		// 2. Get the primitive.
    		Aead aead = keysetHandle.getPrimitive(Aead.class);

    		// 3. Use the primitive.
    		byte[] cipheredToken = aead.encrypt(jwt.getBytes(), ASSOCIATED_DATA.getBytes());

    		return DatatypeConverter.printHexBinary(cipheredToken);


    	}catch (Exception ex){
    		throw new TokenCipherException();
    	}
    }

    /**
     * Decipher a JWT
     *
     * @param jwtInHex     Token to decipher encoded in HEX
     * @param keysetHandle Pointer to the keyset handle
     * @return The token in clear text
     * @throws Exception If any issue occur during token deciphering operation
     */
    public String decipherToken(String jwtInHex)
    {

    	try{
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
    			this.loadKeySet();

    		// 2. Get the primitive.
    		Aead aead = keysetHandle.getPrimitive(Aead.class);

    		//Decipher the token
    		byte[] decipheredToken = aead.decrypt(cipheredToken, ASSOCIATED_DATA.getBytes());

    		return new String(decipheredToken);
    	
    	}catch (Exception ex){
    		throw new TokenCipherException();
    	}
    }
}