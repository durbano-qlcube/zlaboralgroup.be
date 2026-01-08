package com.zap.security.service.jwt;

import java.util.Calendar;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

public class decodeMain {

	private static final String KEY = "243_&77AAs";


	public static void main(String[] args) throws Exception {
		
		
		
		//System.out.println(validateTokenMobile("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtZW1iZXIiLCJhdWQiOiJtb2JpbGUiLCJpc3MiOiJmcmlud28uaWNvIiwiaWQiOiI1YWQ4NzFlOGUzNzA0YTUzZWEwMDcwMWYiLCJpYXQiOjE1MjcyNTI5Mjd9.ItY4cRjc4AtJNOoXJoHRZaf7a3HgCNt5F1b8RIohXdE"));
//		System.out.println(validateTokenWeb("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtZW1iZXIiLCJhdWQiOiJXRUIiLCJpc3MiOiJmcmlud28uaWNvIiwiaWQiOiI1YjAwOGExZmUzNzA0YTVlZGUxZDMwYzkiLCJleHAiOjE1MjY4MTk5NjYsImlhdCI6MTUyNjgxODE2Nn0.cB8jcXX5yR5rNN00FEVSXb02p23JGP7lZVoreIJsFbY"));
//		System.out.println(validateTokenWeb("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtZW1iZXIiLCJhdWQiOiJXRUIiLCJpc3MiOiJmcmlud28uaWNvIiwiaWQiOiI1YjAwOGExZmUzNzA0YTVlZGUxZDMwYzkiLCJleHAiOjE1MjY4MTk5OTIsImlhdCI6MTUyNjgxODE5Mn0.MKTKFiWS4YeHQqcWrevMQs7c7Y2klFd23KH7g5tm4MA"));
//		System.out.println(validateTokenWeb("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtZW1iZXIiLCJhdWQiOiJXRUIiLCJpc3MiOiJmcmlud28uaWNvIiwiaWQiOiI1YjAwOGExZmUzNzA0YTVlZGUxZDMwYzkiLCJleHAiOjE1MjY4MjAzMDksImlhdCI6MTUyNjgxODUwOX0.G5uUGfTqQWfyLgfFiy_Fg9ckPaa6IHUPX5JVbHR3BXk"));

		//		System.out.println(validateTokenWeb("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtZW1iZXIiLCJhdWQiOiJXRUIiLCJpc3MiOiJmcmlud28uaWNvIiwiaWQiOiI1YWYyZmVmMWUzNzA0YTE2YjMzZTU2MTMiLCJleHAiOjE1MjY4MjA0ODcsImlhdCI6MTUyNjgxODY4N30.OPO36DuT8m1nSZSPVPxDSwJCns4Mn7Xz7C-EtB9xvq0"));
//		System.out.println(validateTokenWeb("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtZW1iZXIiLCJhdWQiOiJXRUIiLCJpc3MiOiJmcmlud28uaWNvIiwiaWQiOiI1YWYyZmVmMWUzNzA0YTE2YjMzZTU2MTMiLCJleHAiOjE1MjY4MjA1NDIsImlhdCI6MTUyNjgxODc0Mn0.oKqqtUZpZEkAa63HmObsc4cKUkHJCrMBzRXDUQ2AZ3w"));
//		System.out.println(validateTokenWeb("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtZW1iZXIiLCJhdWQiOiJXRUIiLCJpc3MiOiJmcmlud28uaWNvIiwiaWQiOiI1YWYyZmVmMWUzNzA0YTE2YjMzZTU2MTMiLCJleHAiOjE1MjY4MjI0MjksImlhdCI6MTUyNjgyMDYyOX0.pouMSh7Mjdo7nwvsATyU13pVrvYxk3CDtiaUsQHxMfY"));
//		System.out.println(validateTokenWeb("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtZW1iZXIiLCJhdWQiOiJXRUIiLCJpc3MiOiJmcmlud28uaWNvIiwiaWQiOiI1YjAxYTA2Y2UzNzA0YTdlOTM5NzYwNWIiLCJleHAiOjE1MjY4MzUwNjAsImlhdCI6MTUyNjgzMzI2MH0.B02o1dAUL1WQ0MYekcxSzfwitHT1R4aEGvvyRzqh3aI"));

		
		//		System.out.println(validateTokenWeb("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtZW1iZXIiLCJhdWQiOiJXRUIiLCJpc3MiOiJmcmlud28uaWNvIiwiaWQiOiI1YWYyZmVmMWUzNzA0YTE2YjMzZTU2MTMiLCJleHAiOjE1MjY4MzU2NzUsImlhdCI6MTUyNjgzMzg3NX0.3s7qhUtd_PVK_NvhFLQD_ss9VzoLq4W_3QfoXYI8aQM"));
//		System.out.println(validateTokenWeb("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtZW1iZXIiLCJhdWQiOiJXRUIiLCJpc3MiOiJmcmlud28uaWNvIiwiaWQiOiI1YWYyZmVmMWUzNzA0YTE2YjMzZTU2MTMiLCJleHAiOjE1MjY4MzU3MzUsImlhdCI6MTUyNjgzMzkzNX0.k9SbBSsaFQTROh5HGRQ-zbXddaDBhvgFVSujdMLu1d0"));
//		System.out.println(validateTokenWeb("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtZW1iZXIiLCJhdWQiOiJXRUIiLCJpc3MiOiJmcmlud28uaWNvIiwiaWQiOiI1YWYyZmVmMWUzNzA0YTE2YjMzZTU2MTMiLCJleHAiOjE1MjY4MzU3NjEsImlhdCI6MTUyNjgzMzk2MX0.1C9tvngV4_s9WZExGAsjxFblIM0U2CW8nc-xuuTv0w4"));


		System.out.println(gentTokenSessionMobile("5b43c4fee3704a2ff753fe4e"));
		

	}

	
	private static String gentTokenSessionMobile (String id) throws Exception
	{


		Algorithm algorithm = Algorithm.HMAC256(KEY);
		String token = JWT.create()
				.withIssuer("frinwo.ico")
				.withSubject("member")
				.withAudience("mobile")
				.withClaim("id",  id)
				
				.sign(algorithm);
		return token;

	}
	
	
	private static String validateTokenMobile (String token) throws Exception
	{
		String userId=null;
        try {
        	
            Algorithm algorithm = Algorithm.HMAC256(KEY);
            JWTVerifier verifier = JWT.require(algorithm)
            		.withIssuer("frinwo.ico")
    				.withSubject("member")
    				.withAudience("mobile")
                .build();
            DecodedJWT jwt = verifier.verify(token);
            userId = jwt.getClaim("id").asString();
 
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
		return userId;
	}
	
	private static String validateTokenWeb (String token) throws Exception
	{
		String userId=null;
        try {
        	
            Algorithm algorithm = Algorithm.HMAC256(KEY);
            JWTVerifier verifier = JWT.require(algorithm)
            		.withIssuer("frinwo.ico")
    				.withSubject("member")
    				.withAudience("WEB")
    				
                .build(); //Reusable verifier instance
            DecodedJWT jwt = verifier.verify(token);
            userId = jwt.getClaim("id").asString();
 
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
		return userId;
	}
	
}
