package com.zap.security.filter;

import java.io.IOException;
import java.security.Principal;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import com.zap.security.service.jwt.JwtService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@JWTTokenNeeded
@Priority(Priorities.AUTHENTICATION)
public class JWTTokenNeededFilter implements ContainerRequestFilter
{
 
	private static final Logger LOGGER = LoggerFactory.getLogger(JWTTokenNeededFilter.class.getName());
	
//	@Inject
//	UserIcoService userIcoService;

//	@Inject
//	private SettingsService settingsService;
 
	@Inject
	private JwtService jwtService;
	

	
	private static final String AUTHENTICATION_SCHEME = "Bearer";
	
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
 
    	Long tx= System.currentTimeMillis();
		String TAG="[JWTTokenNeededFilter "+tx+"]";
		
        // Get the HTTP Authorization header from the request
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
 
        // Check if the HTTP Authorization header is present and formatted correctly 
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer "))
        {
            throw new NotAuthorizedException("Authorization header must be provided");
        }
        
        // Extract the token from the HTTP Authorization header
//        LOGGER.info(TAG + " - authorizationHeader : " + authorizationHeader);
        String token = authorizationHeader.substring("Bearer".length()).trim();
//        LOGGER.info(TAG + " - jwt : " + token);
        
        
        
        if (jwtService.isTokenRevoked(token))
        {
            throw new NotAuthorizedException("Authorization header must be provided");
        }
        
        
        
        try {
        	
//            Algorithm algorithm = Algorithm.HMAC256(settingsService.getJWTKey());
//            JWTVerifier verifier = JWT.require(algorithm)
//    	        .withIssuer("BFC-SERVER")
// 		        .withAudience("WEB-CLIENTS")
//                .build(); //Reusable verifier instance
//            DecodedJWT jwt = verifier.verify(token);
//            String userId = jwt.getClaim("id").asString();
            
           // Validate the token
           // Key key = keyGenerator.generateKey();
           // Jwts.parser().setSigningKey(key).parseClaimsJws(token);
           // LOGGER.info("#### valid token : " + token);
        	
        	
            String userId = jwtService.validateToken(token);
//            LOGGER.info(TAG + " - id : " + userId);
			//userIcoService.loadById(userId );
			
//			LOGGER.info(TAG + " - id : " + userId);
			
			final SecurityContext currentSecurityContext = requestContext.getSecurityContext();
			requestContext.setSecurityContext(new SecurityContext() {

				@Override
				public Principal getUserPrincipal() {
					return () -> userId;
				}

			    @Override
			    public boolean isUserInRole(String role) {
			        return true;
			    }

			    @Override
			    public boolean isSecure() {
			        return currentSecurityContext.isSecure();
			    }

			    @Override
			    public String getAuthenticationScheme() {
			        return AUTHENTICATION_SCHEME;
			    }
			    
			    
			});
			
			
			
			
 
        } catch (Exception e) {
        	LOGGER.error("#### invalid token : " + token);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }
}