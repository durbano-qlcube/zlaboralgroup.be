package com.zap.security.service.jwt;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.zap.maintenance.service.settings.SettingsService;
import com.zap.security.entity.jwt.JwtEntity;
import com.zap.security.exception.authenticate.InvalidTokenException;
import com.zap.security.exception.jwt.JwtNotFoundException;
import com.zap.security.exception.jwt.JwtServiceException;
import com.zap.security.exception.jwt.TokenCipherException;
import com.zap.security.vo.authenticate.AuthUserVo;
import com.zap.security.vo.enumerates.RoleEnum;
import com.zap.security.vo.jwt.JwtInfoVo;
import com.zap.security.vo.jwt.JwtVo;


@Stateless
public class JwtService implements Serializable
{

	private static final long serialVersionUID = -8448538504088495308L;
	private static final Logger LOGGER = LoggerFactory.getLogger(JwtService.class.getName());
	private static String KEY;


	@PersistenceContext(unitName = "zapLaboralgrouPool")
	EntityManager em;
	
	private SecureRandom secureRandom = new SecureRandom();
	
	@Inject
	private SettingsService settingsService;
	
	@Inject
	TokenCipherService tokenCipherService;


	
	@PostConstruct
	private void initializes()
	{
			KEY = settingsService.getJWTKey();
	}

	
	public JwtVo create (JwtVo jwtVo)
	{
		String TAG ="[jwtService - create]";
		if (jwtVo == null)
			throw new IllegalArgumentException(TAG +" >> 'jwtVo' can not be null");
		
		try{
			JwtEntity entity = toJwtEntity(jwtVo);
			entity.setFxCreation(Calendar.getInstance());
			em.persist(entity);
			return toJwtVo(entity);
			
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
            throw new JwtServiceException(ex);
		}
	}
	
	public void delete (Long idjwt) throws JwtNotFoundException
	{
		String TAG ="[jwtService - delete idjwt:"+idjwt+"]";
		
		if (idjwt == null)
			throw new IllegalArgumentException(TAG +" >> 'idjwt' can not be null");

		try{
			JwtEntity entity = em.find(JwtEntity.class, idjwt);
			if(entity == null)
				throw new JwtNotFoundException();
			
			em.remove(entity);
			
		}catch (JwtNotFoundException ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw ex;
           
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
            throw new JwtServiceException(ex);
		}	
	}

	

	public JwtVo load (Long id) throws JwtNotFoundException
	{

		String TAG ="[jwtService - load id:"+id+"]";
		if (id == null)
			throw new IllegalArgumentException(TAG +" >> 'idjwt' can not be null");

		try{

			JwtEntity entity = em.find(JwtEntity.class, id);

			if(entity == null)
				return null;
			else
				return this.toJwtVo(entity);

		}catch (javax.persistence.NoResultException ex){
			return null;

		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw new JwtServiceException(ex);
		}	
	}

	@SuppressWarnings("unchecked")
	public JwtVo loadByEmail (String email) 
	{
		String TAG ="[jwtService - loadByEmail email:"+email+"]";
	
		if (email == null)
			throw new IllegalArgumentException(TAG +" >> 'email' can not be null");

		try {
			Query query = em.createNamedQuery("JwtEntity.loadByEmail");
			query.setParameter("email", email);
			
			JwtEntity entity = (JwtEntity)  query.getSingleResult();
			
			return this.toJwtVo(entity);
						
			
		}catch (javax.persistence.NoResultException ex){
			return null;
			
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw new JwtServiceException(ex);
		}
	}
	

	
//	@SuppressWarnings("unchecked")
//	public List<JwtVo> loadByJwt (String jwt) 
//	{
//		String TAG ="[jwtService - loadByCode jwt:"+jwt+"]";
//
//		if (jwt == null)
//			throw new IllegalArgumentException(TAG +" >> 'jwt' can not be null");
//
//		try {
//			Query query = em.createNamedQuery("JwtEntity.loadByCode");
//			query.setParameter("jwt", jwt);
//
//			List<JwtEntity> entityList = (List<JwtEntity>)  query.getResultList();
//			List<JwtVo> result = new ArrayList<JwtVo>();
//			if (entityList!=null && !entityList.isEmpty())
//			{	
//				for (JwtEntity entity: entityList)
//					result.add(this.toJwtVo(entity));
//			}
//			return result ;
//
//		}catch (Exception ex){
//			LOGGER.error(TAG + " - Error: ",ex.getMessage());
//			throw new JwtServiceException(ex);
//		}
//	}

	@SuppressWarnings("unchecked")
	public JwtVo loadByJwt (String jwt) 
	{
		String TAG ="[jwtService - loadByJwt jwt:"+jwt+"]";

		if (jwt == null)
			throw new IllegalArgumentException(TAG +" >> 'jwt' can not be null");

		try {
			Query query = em.createNamedQuery("JwtEntity.loadByToken");
			query.setParameter("token", jwt);

			JwtEntity entity = (JwtEntity)  query.getSingleResult();
			return this.toJwtVo(entity) ;

		}catch (javax.persistence.NoResultException ex){
			LOGGER.error(TAG + " - Error:{} ",ex.getMessage());
			return null;
			
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw new JwtServiceException(ex);
		}
	}
	
	
	public void update (JwtVo JwtVo, Boolean copyIfNull) throws JwtNotFoundException
	{
		String TAG ="[JwtService - update]";
		
		if (JwtVo == null)
			throw new IllegalArgumentException(TAG +" >> 'JwtVo' can not be null");
		
		if (JwtVo.getId() == null)
			throw new IllegalArgumentException(TAG +" >> 'JwtVo.getId()' can not be null");

		try{
			
			JwtEntity entity = em.find(JwtEntity.class, JwtVo.getId());
			
			if(entity == null)
				throw new JwtNotFoundException();
			
			this.toJwtEntity(JwtVo, entity, copyIfNull);
			entity.setFxModification(Calendar.getInstance());
			em.merge(entity);
			
		}catch (JwtNotFoundException ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw ex;
           
		}catch (Exception ex){
			LOGGER.error("[JwtService - updateJwt] - Error: ",ex);
            throw new JwtServiceException(ex);
		
		}
	}
	
	
	
	
	
	/**
	  * Verify if a digest encoded in HEX of the ciphered token is present 
	  * in the revokation table
	  *
	  * @param jwtInHex Token encoded in HEX
	  * @return Presence flag
	  * @throws Exception If any issue occur during communication with DB
	  */
	public boolean isTokenRevoked(String jwtInHex)
	{
		boolean isTokenRevoked = true;
		if (jwtInHex != null && !jwtInHex.trim().isEmpty())
		{
			JwtVo jwtVo = this.loadByJwt(jwtInHex);
			if(jwtVo!=null && jwtVo.getFxRevokation()==null)
			{
				isTokenRevoked = false;
			}
		}

		return isTokenRevoked;
	}


	 /**
	  * Add a digest encoded in HEX of the ciphered token to the revokation token table
	  *
	  * @param jwtInHex Token encoded in HEX
	  * @throws Exception If any issue occur during communication with DB
	  */
	 public void revokeToken(String jwtInHex) throws Exception
	 {
		 if (jwtInHex != null && !jwtInHex.trim().isEmpty())
	     {
	    	 JwtVo jwtVo = this.loadByJwt(jwtInHex);
	    	 if(jwtVo!=null)
	    	 {
	    		 jwtVo.setFxRevokation(Calendar.getInstance());
	    		 this.update(jwtVo, false);
	    	 }
	     }
	 }
	
	
	
	 private String genUserFingerprint () throws Exception
	 {
		 byte[] randomFgp = new byte[50];
		 secureRandom.nextBytes(randomFgp);
		 String userFingerprint = DatatypeConverter.printHexBinary(randomFgp);
		 return userFingerprint;
	 }


	

	public String gentTokenSession (AuthUserVo authUserVo, String app, String signature, Calendar expiresAt) throws Exception
	{
		String uuid = authUserVo.getUuid();
		//String email =  authUserVo.getEmail();
		String role = authUserVo.getRole().toString();
		String userFingerprint = this.genUserFingerprint();
		
		
		
		String jwt = this.gentTokenSessionWeb(uuid,role,app, userFingerprint, expiresAt);
		//String token = tokenCipherService.cipherToken(jwt);
		
		JwtVo jwtVo = new JwtVo();
	    jwtVo.setEmail(authUserVo.getEmail());
	    jwtVo.setFingerprint(userFingerprint);
	    if (authUserVo.getRole()!=null)
	    	jwtVo.setRole(authUserVo.getRole().toString());
	    
	    jwtVo.setUuid(authUserVo.getUuid());
	    jwtVo.setApp(app);
	    jwtVo.setAppSignature(signature);
		jwtVo.setJwt(jwt);
		jwtVo.setToken(jwt);
		this.create (jwtVo);
		 
		return jwt;
		
	}

	
	private String gentTokenSessionWeb (String uuid,String role, String app, String userFingerprint, Calendar expiresAt) throws Exception
	{
		Algorithm algorithm = Algorithm.HMAC256(KEY);
		String token = JWT.create()
				.withIssuer("ZAP")
				.withSubject(app)
				.withAudience("WEB")
				.withIssuedAt(new Date())
				.withExpiresAt(new Date(expiresAt.getTimeInMillis()))
				.withClaim("uuid",  uuid)
				.withClaim("role",  role)
				.withClaim("userFingerprint",  userFingerprint)
				.sign(algorithm);
		return token;

	}
	
	

	
	

	
	

	public JwtInfoVo decode (String jwt) throws InvalidTokenException
	{
		try{
			JWT token = JWT.decode(jwt);

			JwtInfoVo result = new JwtInfoVo();
			result.setIssuer(token.getIssuer());		
			result.setSubject(token.getSubject());
			List<String> audienceList = token.getAudience();
			result.setAudience(audienceList.get(0));
			result.setUuid(token.getClaim("uuid").asString());
			result.setApp(token.getClaim("app").asString());
			result.setSignature(token.getClaim("signature").asString());
//			if (RoleEnum.jefeTaller.toString().equalsIgnoreCase(token.getClaim("role").asString()))
//			{
//				result.setRole(RoleEnum.jefeTaller);
//			
//			}else if (RoleEnum.mecanico.toString().equalsIgnoreCase(token.getClaim("role").asString())) {
//			
//				result.setRole(RoleEnum.mecanico);
//				
//			}else {
//				result.setRole(RoleEnum.admin);
//			}
			result.setUserFingerprint(token.getClaim("userFingerprint").asString());
			return result;

		}catch (Exception e){
			throw new InvalidTokenException(jwt);

		}
	}

	
	public String validateTokenWeb (String token) throws Exception
	{
		String userId=null;
        try {
            Algorithm algorithm = Algorithm.HMAC256(KEY);
            JWTVerifier verifier = JWT.require(algorithm)
            		.withIssuer("ZAP")
    				//.withSubject("member")
    				.withAudience("WEB")
    				
                .build(); //Reusable verifier instance
            DecodedJWT jwt = verifier.verify(token);
            userId = jwt.getClaim("uuid").asString();
 
        } catch (Exception e) {
//        	LOGGER.error("#### invalid token Web: " + token);
        }
        
		return userId;
	}
	
	public String getJwtFromToken (String token) throws Exception
	{
		try {

			String jwt = token; //tokenCipherService.decipherToken(token);
			return jwt;


		} catch (TokenCipherException e) {
        	LOGGER.error("#### invalid token Can't decipherToken:" + token);
        	throw new Exception("Session Expirada. Token invalid...");
        
        	
        } catch (Exception e) {
        	LOGGER.error("#### invalid token :" + token);
        	throw new Exception("Session Expirada. Token invalid...");
        }
        
		
	}
	

	
	public String validateToken (String jwt) throws Exception
	{
		try {

			//String jwt = tokenCipherService.decipherToken(token);
			JwtInfoVo JwtInfo = this.decode(jwt);

			String userId = validateTokenWeb(jwt);
			return userId;


		} catch (TokenCipherException e) {
        	LOGGER.error("#### invalid token Can't decipherToken:" + jwt);
        	throw new Exception("Session Expirada. Token invalid...");
        
        	
        } catch (Exception e) {
        	LOGGER.error("#### invalid token :" + jwt);
        	throw new Exception("Session Expirada. Token invalid...");
        }
        
		
	}
	
	public String refreshToken (String token) throws Exception
	{
		String refreshJwt=null;
		String userId=null;
		try {

			Algorithm algorithm = Algorithm.HMAC256(KEY);
			JWTVerifier verifier = JWT.require(algorithm)
					.withIssuer("ZAP")
					//.withSubject("ZAP")
					.withAudience("WEB")
					.build(); //Reusable verifier instance
			DecodedJWT jwt = verifier.verify(token);
			userId = jwt.getClaim("code").asString();

			Calendar expiresAt= Calendar.getInstance();
			expiresAt.add(Calendar.MINUTE, 120);

			refreshJwt = JWT.create()
					.withIssuer("ZAP")
					//.withSubject("member")
					.withAudience("WEB")
					.withIssuedAt(new Date())
					.withExpiresAt(new Date(expiresAt.getTimeInMillis()))
					.withClaim("code",  userId)
					.sign(algorithm);

		} catch (Exception e) {
			LOGGER.error("#### invalid token : " + token);
		}

		return refreshJwt;
	}
	
	private void toJwtEntity(JwtVo source, JwtEntity target, Boolean copyIfNull)
	{
		if(copyIfNull || source.getId()!=null)
			target.setId(source.getId());

		if(copyIfNull || source.getEmail()!=null)
		target.setEmail(source.getEmail());
		

		if(copyIfNull || source.getUuid()!=null)
		target.setUuid(source.getUuid());
		
		if(copyIfNull || source.getFingerprint()!=null)
		target.setFingerprint(source.getFingerprint());
		
		if(copyIfNull || source.getRole()!=null)
		target.setRole(source.getRole());
		
		if(copyIfNull || source.getApp()!=null)
		target.setApp(source.getApp());
		
		if(copyIfNull || source.getAppSignature()!=null)
		target.setAppSignature(source.getAppSignature());
		
		if(copyIfNull || source.getJwt()!=null)
		target.setJwt(source.getJwt());
		
		if(copyIfNull || source.getToken()!=null)
		target.setToken(source.getToken());

		if(copyIfNull || source.getFxRevokation()!=null)
		target.setFxRevokation(source.getFxRevokation());
	}

	private JwtVo toJwtVo(JwtEntity source)
	{
		JwtVo target = new JwtVo();
		target.setId(source.getId());
		target.setEmail(source.getEmail());
		target.setUuid(source.getUuid());
		target.setFingerprint(source.getFingerprint());
		target.setRole(source.getRole());
		target.setApp(source.getApp());
		target.setAppSignature(source.getAppSignature());
		target.setJwt(source.getJwt());
		target.setToken(source.getToken());
		target.setFxRevokation(source.getFxRevokation());
		
		return target;
	}

	private JwtEntity toJwtEntity(JwtVo source)
	{
		JwtEntity target = new JwtEntity();
		target.setId(source.getId());
		target.setEmail(source.getEmail());
		target.setUuid(source.getUuid());
		target.setFingerprint(source.getFingerprint());
		target.setRole(source.getRole());
		target.setApp(source.getApp());
		target.setAppSignature(source.getAppSignature());
		target.setJwt(source.getJwt());
		target.setToken(source.getToken());
		target.setFxRevokation(source.getFxRevokation());
		
		return target;
	}
	
	
}
