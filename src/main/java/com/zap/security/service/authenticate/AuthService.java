package com.zap.security.service.authenticate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.codec.binary.Base64;
import org.jboss.resteasy.microprofile.config.ResteasyConfig.SOURCE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zap.maintenance.service.crypto.PasswordEncryptionService;
import com.zap.maintenance.service.settings.SettingsService;
import com.zap.security.entity.authenticate.AuthUserEntity;
import com.zap.security.exception.authenticate.AuthUserAlreadyRegisterException;
import com.zap.security.exception.authenticate.AuthUserNotFoundException;
import com.zap.security.exception.authenticate.AuthUserServiceException;
import com.zap.security.exception.authenticate.InvalidAccessException;
import com.zap.security.exception.authenticate.InvalidTokenException;
import com.zap.security.exception.authenticate.NotAuthException;
import com.zap.security.exception.authenticate.PasswordException;
import com.zap.security.exception.authenticate.UserNotActiveException;
import com.zap.security.service.jwt.JwtService;
import com.zap.security.service.parentcompany.ParentCompanyService;
import com.zap.security.vo.authenticate.AuthUserVo;
import com.zap.security.vo.authenticate.LoginDataVo;
import com.zap.security.vo.authenticate.LoginVo;
import com.zap.security.vo.authenticate.ProviderConfigVo;
import com.zap.security.vo.authenticate.SessionVo;
import com.zap.security.vo.authenticate.UserProviderVo;
import com.zap.security.vo.enumerates.AppsEnum;
import com.zap.security.vo.enumerates.RoleEnum;
import com.zap.security.vo.jwt.JwtInfoVo;
import com.zap.security.vo.parentcompany.ParentCompanyVo;



@Stateless
public class AuthService implements Serializable
{

	private static final long serialVersionUID = -799844646085705211L;
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class.getName());
	

	private static final  String ZAP_ENDESA_SIGNATURE =      "d247bd00-be71-412b-a7bb-5f28b0130246";
	//private static final  String WEB_ADMIN_SIGNATURE=   "b1819c56-0ad3-4802-a20f-b58a6de1e479";
	private static Integer EXPIRES_IN_MINUTES_WEB = 480;

	@PersistenceContext(unitName = "zapLaboralgrouPool")
	EntityManager em;
	
	@Inject
	JwtService jwtService;
	
    @Inject
    SettingsService settingsService;

    @Inject
    PasswordEncryptionService passwordEncryptionService;

    @Inject
    ParentCompanyService parentCompanyService;

    @Inject
    UserProviderService userProviderService;

    @Inject
    ProviderConfigService providerConfigService;
	
	public SessionVo refreshJwt (String  token) throws InvalidTokenException 
	{
		String TAG="[AuthService refreshJwt - token:"+token+"]";
		
		if (token == null)
			throw new IllegalArgumentException(TAG + " - 'token' can not be null");
		
		try{
			
			
			//String jwt = jwtService.getJwtFromToken(token);
			String jwt = token;
			JwtInfoVo JwtInfo = jwtService.decode(jwt);
			LOGGER.info(TAG+ " - JwtInfo:"+JwtInfo.toString()+"...");
			
			String uuid = jwtService.validateTokenWeb(jwt);
			LOGGER.info(TAG+ " - UUID:"+uuid+"...");
			
			AuthUserVo authUserVo = this.loadByUuid(uuid);
			if (authUserVo==null)
			{
				LOGGER.info(TAG+ " - authUser NO FOUND..");
				throw new AuthUserNotFoundException();
			}
			

			
//			LOGGER.info(TAG+ " - Login OK. All Correct. Generating JWT...");
//			SessionVo sessionVo= new SessionVo();
//			sessionVo.setFirstname(authUserVo.getFirstName());
//			sessionVo.setLastname(authUserVo.getLastName());
//			sessionVo.setName(authUserVo.getName());
//			sessionVo.setFullname(authUserVo.getFullname());
//			sessionVo.setRole(authUserVo.getRole().toString());
//			sessionVo.setEmail(authUserVo.getEmail());
//			
//			Calendar expiresAt = Calendar.getInstance();
//			expiresAt.add(Calendar.MINUTE, EXPIRES_IN_MINUTES_WEB);
//			
//			sessionVo.setToken(jwtService.gentTokenSession(authUserVo, JwtInfo.getApp(), JwtInfo.getSignature(), expiresAt));
//			sessionVo.setExpiresAt(expiresAt);
			
			
			LOGGER.debug(TAG+ " - Login OK. All Correct. Generating Session...");
			SessionVo sessionVo= new SessionVo();
			
			sessionVo.setSurname(authUserVo.getSurname());
			sessionVo.setName(authUserVo.getName());
			
			sessionVo.setFullname(authUserVo.getFullname());
			sessionVo.setIsTemporalPassword(authUserVo.getIsTemporalPassword());
//			sessionVo.setExpiresAt(expiresAt);
			
			
			sessionVo.setName(authUserVo.getName());
			sessionVo.setRole(authUserVo.getRole().toString());
//			sessionVo.setRole("ADMIN");
			Calendar expiresAt = Calendar.getInstance();
			expiresAt.add(Calendar.MINUTE, EXPIRES_IN_MINUTES_WEB);
			sessionVo.setToken(jwtService.gentTokenSession(authUserVo, JwtInfo.getApp(), JwtInfo.getSignature(), expiresAt));

//			if (authUserVo.getRole().toString().equalsIgnoreCase(RoleEnum.jefeTaller.toString())){
//				sessionVo.setLoginRedirectUrl("/jefeTaller/tareas");
//			}else if (authUserVo.getRole().toString().equalsIgnoreCase(RoleEnum.mecanico.toString())){ 
//				sessionVo.setLoginRedirectUrl("/mecanico/tareas");
//			}else if (authUserVo.getRole().toString().equalsIgnoreCase(RoleEnum.administrativo.toString())){ 
//				sessionVo.setLoginRedirectUrl("/administrativo/tareas");
//			}

			LoginDataVo loginDataVo = new LoginDataVo();
			loginDataVo.setDisplayName(authUserVo.getFullname());
			loginDataVo.setEmail(authUserVo.getEmail());
			loginDataVo.setPhotoURL(null);
			sessionVo.setData(loginDataVo);
			
			return sessionVo;
			
			
			
		} catch (InvalidTokenException ex) {
			LOGGER.error(TAG + " - Error:{} ",ex);
			throw ex;
			
			
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error:{} ",ex);
			throw new AuthUserServiceException(ex);
		}
	}
	
	public JwtInfoVo getTokenSession (String  token) throws InvalidTokenException 
	{
		String TAG="[AuthService getTokenSession - token:"+token+"]";
		
		if (token == null)
			throw new IllegalArgumentException(TAG + " - 'token' can not be null");
		
		try{
			
			
			String jwt = jwtService.getJwtFromToken(token);
			JwtInfoVo JwtInfo = jwtService.decode(jwt);
			LOGGER.info(TAG+ " - JwtInfo:"+JwtInfo.toString()+"...");
			
//			String uuid = jwtService.validateTokenWeb(jwt);
//			LOGGER.info(TAG+ " - UUID:"+uuid+"...");
//			
//			AuthUserVo authUserVo = this.loadByUuid(uuid);
//			if (authUserVo==null)
//			{
//				LOGGER.info(TAG+ " - authUser NO FOUND..");
//				throw new AuthUserNotFoundException();
//			}
//			
//
//			
//			LOGGER.info(TAG+ " - Login OK. All Correct. Generating JWT...");
//			SessionVo sessionVo= new SessionVo();
//			sessionVo.setFirstname(authUserVo.getFirstName());
//			sessionVo.setLastname(authUserVo.getLastName());
//			sessionVo.setName(authUserVo.getName());
//			sessionVo.setFullname(authUserVo.getFullname());
//			sessionVo.setRole(authUserVo.getRole().toString());
//			sessionVo.setToken(jwtService.gentTokenSession(authUserVo, JwtInfo.getApp(), JwtInfo.getSignature()));
			return JwtInfo;
			
			
			
		} catch (InvalidTokenException ex) {
			LOGGER.error(TAG + " - Error:{} ",ex);
			throw ex;
			
			
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error:{} ",ex);
			throw new AuthUserServiceException(ex);
		}
	}
	
	
	public void logout (String  jwt) 
	{
		String TAG="[AuthService logout - jwt:"+jwt+"]";
		
		if (jwt == null)
			throw new IllegalArgumentException(TAG + " - 'jwt' can not be null");
		
		try{
			jwtService.revokeToken(jwt);
			
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error:{} ",ex);
			throw new AuthUserServiceException(ex);
		}
	}
	
	public String generateUuid() {
		String result = "";
		for (int i = 0; i < 50; i++) {
			UUID uuid = UUID.randomUUID();
			AuthUserVo user = this.loadByUuid(uuid.toString());
			if (user == null) {
				result = uuid.toString();
				break;
			}

		}

		return result;
	}
	
	public SessionVo login (LoginVo loginVo) throws PasswordException, AuthUserNotFoundException, UserNotActiveException, InvalidAccessException
	{
		long t = System.currentTimeMillis();
		String TAG = "[AuthService - login "+t+"]";
		
		
		if (loginVo == null)
			throw new IllegalArgumentException(TAG +" >> 'authUserVo' can not be null");
		
		if (loginVo.getUsername() == null || "".equals(loginVo.getUsername()))
			throw new IllegalArgumentException(TAG +" >> 'authUserVo.getUsername' can not be null or empty");
	
		if (loginVo.getPassword() == null || "".equals(loginVo.getPassword()))
			throw new IllegalArgumentException(TAG +" >> 'authUserVo.getPassword' can not be null or empty");
	
	
			
		TAG = "[AuthService - login "+t+"] >>"+loginVo.toString()+"]";
		

		try{
			
			LOGGER.info(TAG+ " - Checking Signature Apps... ");
			AppsEnum app = this.getAppsSignature(loginVo.getSignature());
			
			AuthUserVo authUserVo = this.loadByUsername(loginVo.getUsername());
			if (authUserVo==null)
			{
				LOGGER.info(TAG+ " - authUser NO FOUND..");
				throw new AuthUserNotFoundException();
			}
			
			LOGGER.info(TAG+ " - authUser FOUND. idAuthUser="+authUserVo.getId()+".. checking password...");
			if (!PasswordEncryptionService.authenticate(loginVo.getPassword(), authUserVo.getPassword()))
				throw new PasswordException(TAG + "- Password incorrect...");
	
			
			
			LOGGER.info(TAG+ " authUser idAuthUser="+authUserVo.getId()+".. checking IsActive:"+authUserVo.getIsActive());
			if(!authUserVo.getIsActive())
				throw new UserNotActiveException(TAG + "- User Not active...");

			
			LOGGER.info(TAG+ " - User Role: "+authUserVo.getRole().toString());
			

//			LOGGER.info(TAG+ " - Checking Role with APP: "+authUserVo.getRole().toString()+" app:"+app.toString());
//			if(!this.isValidRoleApp (authUserVo.getRole() , app))
//				throw new InvalidAccessException();

//			sessionVo.setFirstname(authUserVo.getFirstName());
//			sessionVo.setLastname(authUserVo.getLastName());
//			sessionVo.setName(authUserVo.getName());
			
//			sessionVo.setFullname(authUserVo.getFullname());
//			sessionVo.setIsTemporalPassword(authUserVo.getIsTemporalPassword());
//			sessionVo.setExpiresAt(expiresAt);
			
			
			LOGGER.debug(TAG+ " - Login OK. All Correct. Generating Session...");
			SessionVo sessionVo= new SessionVo();
			
			sessionVo.setSurname(authUserVo.getSurname());
			sessionVo.setName(authUserVo.getName());
			
			sessionVo.setFullname(authUserVo.getFullname());
			sessionVo.setIsTemporalPassword(authUserVo.getIsTemporalPassword());
//			sessionVo.setExpiresAt(expiresAt);
			
			
			sessionVo.setName(authUserVo.getName());
			sessionVo.setRole(authUserVo.getRole().toString());
			Calendar expiresAt = Calendar.getInstance();
			expiresAt.add(Calendar.MINUTE, EXPIRES_IN_MINUTES_WEB);
			sessionVo.setToken(jwtService.gentTokenSession(authUserVo, app.toString(), loginVo.getSignature(),expiresAt));


			LoginDataVo loginDataVo = new LoginDataVo();
			loginDataVo.setDisplayName(authUserVo.getFullname());
			loginDataVo.setEmail(authUserVo.getEmail());
			loginDataVo.setPhotoURL(null);
			sessionVo.setData(loginDataVo);
			
			
			LOGGER.info(TAG + "SESSION: {}", sessionVo);
			return sessionVo;

			

		} catch (InvalidAccessException ex) {
			LOGGER.error(TAG+ " - InvalidAccessException:{}",ex);
			throw ex;
			
			
		} catch (UserNotActiveException ex) {
			LOGGER.error(TAG+ " - UserNotActiveException:{}",ex);
			throw ex;
			
		} catch (PasswordException ex) {
			LOGGER.error(TAG+ " - PasswordException:{}",ex);
			throw ex;
			
		}catch (Exception ex){
			LOGGER.error(TAG+ " - LOGIN Exception:{}",ex);
			throw new AuthUserServiceException(ex);
		}
	}


	

	
	private AppsEnum getAppsSignature (String  signature) throws InvalidTokenException 
	{
		AppsEnum app =null;
		
		if (ZAP_ENDESA_SIGNATURE.equalsIgnoreCase(signature))
		{
			app = AppsEnum.ZAP;
			
		}else{
			throw new InvalidTokenException("Signature NOT valid ****************");
		}		

		return app;

	}
	
	private Boolean isValidRoleApp (RoleEnum  role , AppsEnum app) 
	{
		String TAG="[AuthService isValidRoleApp]";
		Boolean result = false;

		if (role == null)
			throw new IllegalArgumentException(TAG + " - 'role' can not be null");

		if (app == null)
			throw new IllegalArgumentException(TAG + " - 'app' can not be null");
		
		TAG="[AuthService isValidRoleApp - role:"+role.toString()+" app:"+app.toString()+"]";


//		if (RoleEnum.jefeTaller.toString().equalsIgnoreCase(role.toString())) {
//			result = true;
//
//		}else if (RoleEnum.mecanico.toString().equalsIgnoreCase(role.toString()) && AppsEnum.WEB_AGENT.toString().equalsIgnoreCase(app.toString())) {
//			result = true;
//			
//		}else if (RoleEnum.admin.toString().equalsIgnoreCase(role.toString()) && AppsEnum.WEB_AGENT.toString().equalsIgnoreCase(app.toString())) {
//			result = true;
//			
//		}else {
				result = false;
//		}

		return result;

	}
	
	public AuthUserVo checkRoleIsAdmin(String Uuid) throws NotAuthException
	{
		String TAG="[AuthUserWs - checkRoleIsAdmin >>id:"+Uuid+"]";

		try{
			if (Uuid== null)
				throw new NotAuthException(TAG + "id is null");

			
			AuthUserVo user = this.loadByUuid(Uuid);
			if(user==null)
				throw new AuthUserNotFoundException();
				
//			if (!RoleEnum.ADMIN.toString().equals(user.getRole().toString()))
//				throw new NotAuthException("User who make the operation is not admin");

			return user;
	
		}catch (NotAuthException ex){
			LOGGER.error(TAG + " - Error: ",ex);
			throw ex;
           
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex);
            throw new AuthUserServiceException(ex);
		}
	}
	
	public AuthUserVo register(AuthUserVo authUserVo) throws AuthUserAlreadyRegisterException {
	    String TAG = "[authUserService - create]";

	    if (authUserVo == null)
	        throw new IllegalArgumentException(TAG + " >> 'authUserVo' can not be null");

	    if (authUserVo.getUsername() == null || "".equals(authUserVo.getUsername()))
	        throw new IllegalArgumentException(TAG + " >> 'authUserVo.getUsername' can not be null or empty");

	    if (authUserVo.getPassword() == null || "".equals(authUserVo.getPassword()))
	        throw new IllegalArgumentException(TAG + " >> 'authUserVo.getPassword' can not be null or empty");

	    try {
	        LOGGER.info(TAG + " - check if exist a user with the same Username:{} ...", authUserVo.getUsername());
	        AuthUserVo user = this.loadByUsername(authUserVo.getUsername());

	        if (user != null)
	            throw new AuthUserAlreadyRegisterException();

	        LOGGER.info(TAG + " - Username Not found. Generating uuid for Username:{} ...", authUserVo.getUsername());

	        if (authUserVo.getEmail() != null)
	            authUserVo.setEmail(authUserVo.getEmail().toLowerCase().trim());

	        authUserVo.setIsActive(true);
	        authUserVo.setUuid(this.generateUuid());
	        authUserVo.setPassword(this.encrypPassword(authUserVo.getPassword()));
	        authUserVo.setFullname(this.generateFullname(authUserVo.getName(), authUserVo.getSurname(), ""));

	        List<ProviderConfigVo> providerConfig = authUserVo.getProviderConfig();
	        List<Long> parentCompanyIds = resolveParentCompanyIds(authUserVo);

	        if (authUserVo.getUuidProviders() != null) {
                for (String providerUuid : authUserVo.getUuidProviders()) {
                        try {
                                AuthUserVo provider = this.loadByUuid(providerUuid);
                                if (provider != null && RoleEnum.PROVIDER.equals(provider.getRole())) {
                                        UserProviderVo rel = new UserProviderVo();
                                        rel.setUuidUser(authUserVo.getUuid());
                                        rel.setUuidProvider(providerUuid);
                                        rel.setUsernameProvider(provider.getUsername());
                                        userProviderService.create(rel);
                                }
                        } catch (Exception e) {
                                LOGGER.error(TAG + " - Error assigning provider:{}", providerUuid);
                        }
                }
        }

	        authUserVo = this.create(authUserVo);
	        authUserVo.setProviderConfig(providerConfig);

	        synchronizeProviderConfig(authUserVo);

	        if (parentCompanyIds != null) {
	            parentCompanyService.replaceUserParentCompanies(authUserVo.getId(), parentCompanyIds);
	            authUserVo = this.loadByUuid(authUserVo.getUuid());
	        }

	        LOGGER.info(TAG + " - User created email:{} and id:{} uuid:{}...",
	                authUserVo.getEmail() != null ? authUserVo.getEmail().toLowerCase().trim() : "null",
	                authUserVo.getId(),
	                authUserVo.getUuid());

	        return authUserVo;

	    } catch (AuthUserAlreadyRegisterException ex) {
	        LOGGER.error(TAG + " - Error:{}", ex.getMessage());
	        throw ex;

	    } catch (Exception ex) {
	        LOGGER.error(TAG + " - Error: ", ex);
	        throw new AuthUserServiceException(ex);
	    }
	}

	
	
	private String generateFullname(String nombre, String firts, String second)
	{
		StringBuilder buffer = new StringBuilder();
		if (nombre!=null && !"".equals(nombre))
			buffer.append(nombre);
		
		if (firts!=null && !"".equals(firts))
			buffer.append(" ").append(firts);
			
		if (second!=null && !"".equals(second))
			buffer.append(" ").append(second);
				
		return buffer.toString();
	}
	
	
	public AuthUserVo create (AuthUserVo authUserVo)
	{
		String TAG ="[authUserService - create]";
		if (authUserVo == null)
			throw new IllegalArgumentException(TAG +" >> 'authUserVo' can not be null");
		
		try{
			AuthUserEntity entity = toAuthUserEntity(authUserVo);
			entity.setFxCreation(Calendar.getInstance());
			em.persist(entity);
			return toAuthUserVo(entity);
			
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
            throw new AuthUserServiceException(ex);
		}
	}
	
	public void delete (Long idauthUser) throws AuthUserNotFoundException
	{
		String TAG ="[authUserService - delete idauthUser:"+idauthUser+"]";
		
		if (idauthUser == null)
			throw new IllegalArgumentException(TAG +" >> 'idauthUser' can not be null");

		try{
			AuthUserEntity entity = em.find(AuthUserEntity.class, idauthUser);
			if(entity == null)
				throw new AuthUserNotFoundException();
			
			em.remove(entity);
			
		}catch (AuthUserNotFoundException ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw ex;
           
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
            throw new AuthUserServiceException(ex);
		}	
	}

	public void deleteByCode (String nif) throws AuthUserNotFoundException
	{
		String TAG ="[authUserService - delete nif:"+nif+"]";
		
		if (nif == null)
			throw new IllegalArgumentException(TAG +" >> 'nif' can not be null");

		try{
				Query query = em.createNamedQuery("AuthUserEntity.deleteByNif");
				query.setParameter("code", nif);
				query.executeUpdate();
				
           
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
            throw new AuthUserServiceException(ex);
		}	
	}

	public AuthUserVo load (Long idAuthUser) throws AuthUserNotFoundException
	{

		String TAG ="[authUserService - load idAuthUser:"+idAuthUser+"]";
		if (idAuthUser == null)
			throw new IllegalArgumentException(TAG +" >> 'idauthUser' can not be null");

		try{

			AuthUserEntity entity = em.find(AuthUserEntity.class, idAuthUser);

			if(entity == null)
				return null;
			else
				return this.toAuthUserVo(entity);

		}catch (javax.persistence.NoResultException ex){
			return null;

		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw new AuthUserServiceException(ex);
		}	
	}

	
	@SuppressWarnings("unchecked")
    public List<AuthUserVo> loadByRole(RoleEnum... role)
    {
        String TAG = "[authUserService - loadByRole]";

    if (role == null || role.length == 0)
        throw new IllegalArgumentException(TAG + " >> 'role' can not be null or empty");

    TAG = "[authUserService - loadByRole role:" + Arrays.toString(role) + "]";

    try {
        Query query = em.createNamedQuery("AuthUserEntity.loadByRole");
        query.setParameter("role", Arrays.asList(role)); 

        List<AuthUserEntity> entityList = (List<AuthUserEntity>) query.getResultList();
        List<AuthUserVo> result = new ArrayList<AuthUserVo>();
        if (entityList != null && !entityList.isEmpty()) {    
            for (AuthUserEntity source : entityList) {
                AuthUserVo target = new AuthUserVo();
                target.setUuid(source.getUuid());
                target.setName(source.getName());
                target.setSurname(source.getSurname());
                target.setFullname(source.getFullname());
                target.setEmail(source.getEmail());
                target.setUsername(source.getUsername());      
                target.setUuidCordinador(source.getUuidCordinador());
                target.setUuidSupervisor(source.getUuidSupervisor());
                target.setRole(source.getRole());

                result.add(target);
            }
        }

        return result;

    } catch (javax.persistence.NoResultException ex) {
        return null;

    } catch (Exception ex) {
        LOGGER.error(TAG + " - Error: ", ex.getMessage());
        throw new AuthUserServiceException(ex);
    }
}
	
	 @SuppressWarnings("unchecked")
	    public List<AuthUserVo> loadMainProviders() {
	        String TAG = "[authUserService - loadMainProviders]";
	        try {
	            Query query = em.createNamedQuery("AuthUserEntity.loadMainProviders");
	            query.setParameter("role", RoleEnum.PROVIDER);
	            List<AuthUserEntity> entityList = (List<AuthUserEntity>) query.getResultList();
	            List<AuthUserVo> result = new ArrayList<AuthUserVo>();
	            if (entityList != null && !entityList.isEmpty()) {
	                for (AuthUserEntity e : entityList) {
	                    result.add(this.toAuthUserVo(e));
	                }
	            }
	            return result;
	        } catch (Exception ex) {
	            LOGGER.error(TAG + " - Error: ", ex.getMessage());
	            throw new AuthUserServiceException(ex);
	        }
	    }
	 
	  @SuppressWarnings("unchecked")
      public List<AuthUserVo> loadProvidersByUsernameLike(String username)
      {
          String TAG = "[authUserService - loadProvidersByUsernameLike]";

          if (username == null)
              throw new IllegalArgumentException(TAG + " >> 'username' can not be null");

          try {
              Query query = em.createNamedQuery("AuthUserEntity.loadProvidersByUsernameLike");
              query.setParameter("username", "%" + username.toUpperCase() + "%");
              query.setParameter("role", RoleEnum.PROVIDER);

              List<AuthUserEntity> entityList = (List<AuthUserEntity>) query.getResultList();
              List<AuthUserVo> result = new ArrayList<AuthUserVo>();
              if (entityList != null && !entityList.isEmpty()) {
                  for (AuthUserEntity source : entityList) {
                      result.add(this.toAuthUserVo(source));
                  }
              }

              return result;

          } catch (Exception ex) {
              LOGGER.error(TAG + " - Error: ", ex.getMessage());
              throw new AuthUserServiceException(ex);
          }
      }
	  
	  @SuppressWarnings("unchecked")
      public List<AuthUserVo> loadSubProvidersByUsernameLike(String username)
      {
          String TAG = "[authUserService - loadSubProvidersByUsernameLike]";

          if (username == null)
              throw new IllegalArgumentException(TAG + " >> 'username' can not be null");

          try {
              Query query = em.createNamedQuery("AuthUserEntity.loadSubProvidersByUsernameLike");
              query.setParameter("username", "%" + username.toUpperCase() + "%");
              query.setParameter("role", RoleEnum.PROVIDER);

              List<AuthUserEntity> entityList = (List<AuthUserEntity>) query.getResultList();
              List<AuthUserVo> result = new ArrayList<AuthUserVo>();
              if (entityList != null && !entityList.isEmpty()) {
                  for (AuthUserEntity source : entityList) {
                      result.add(this.toAuthUserVo(source));
                  }
              }

              return result;

          } catch (Exception ex) {
              LOGGER.error(TAG + " - Error: ", ex.getMessage());
              throw new AuthUserServiceException(ex);
          }
      }
	
	@SuppressWarnings("unchecked")
	public List<AuthUserVo> loadBySupervisorUuid(String uuidSupervisor) {
	    String TAG = "[authUserService - loadBySupervisorUuid]";
	    
	    if (uuidSupervisor == null)
	        throw new IllegalArgumentException(TAG + " >> 'uuidSupervisor' can not be null");
	    
	    TAG = "[authUserService - loadBySupervisorUuid uuidSupervisor:" + uuidSupervisor + "]";
	    
	    try {
	        Query query = em.createNamedQuery("AuthUserEntity.loadBySupervisorUuid");
	        query.setParameter("uuidSupervisor", uuidSupervisor);
	        
	        List<AuthUserEntity> entityList = (List<AuthUserEntity>) query.getResultList();
	        List<AuthUserVo> result = new ArrayList<AuthUserVo>();
	        if (entityList != null && !entityList.isEmpty()) {
	            for (AuthUserEntity source : entityList) {
	                AuthUserVo target = new AuthUserVo();
	                target.setUuid(source.getUuid());
	                target.setName(source.getName());
	                target.setSurname(source.getSurname());
	                target.setFullname(source.getFullname());
	                target.setEmail(source.getEmail());
	                target.setUsername(source.getUsername());
	                result.add(target);
	            }
	        }
	        
	        return result;
	        
	    } catch (javax.persistence.NoResultException ex) {
	        return null;
	        
	    } catch (Exception ex) {
	        LOGGER.error(TAG + " - Error: ", ex.getMessage());
	        throw new AuthUserServiceException(ex);
	    }
	}
	
	@SuppressWarnings("unchecked")
	public List<AuthUserVo> loadByCoordinadorBySupervisorUuid(String uuidSupervisor) {
	    String TAG = "[authUserService - loadBySupervisorUuid]";
	    
	    if (uuidSupervisor == null)
	        throw new IllegalArgumentException(TAG + " >> 'uuidSupervisor' can not be null");
	    
	    TAG = "[authUserService - loadBySupervisorUuid uuidSupervisor:" + uuidSupervisor + "]";
	    
	    try {
	        Query query = em.createNamedQuery("AuthUserEntity.loadByCordinadorbySupervisorUuid");
	        query.setParameter("uuidSupervisor", uuidSupervisor);
	        
	        List<AuthUserEntity> entityList = (List<AuthUserEntity>) query.getResultList();
	        List<AuthUserVo> result = new ArrayList<AuthUserVo>();
	        if (entityList != null && !entityList.isEmpty()) {
	            for (AuthUserEntity source : entityList) {
	                AuthUserVo target = new AuthUserVo();
	                target.setUuid(source.getUuid());
	                target.setName(source.getName());
	                target.setSurname(source.getSurname());
	                target.setFullname(source.getFullname());
	                target.setEmail(source.getEmail());
	                target.setUsername(source.getUsername());
	                result.add(target);
	            }
	        }
	        
	        return result;
	        
	    } catch (javax.persistence.NoResultException ex) {
	        return null;
	        
	    } catch (Exception ex) {
	        LOGGER.error(TAG + " - Error: ", ex.getMessage());
	        throw new AuthUserServiceException(ex);
	    }
	}
	
	@SuppressWarnings("unchecked")
	public List<AuthUserVo> loadByRoleCaptador(RoleEnum role, String uuid) {
	    String TAG = "[authUserService - loadByRoleAndUuid]";
	    
	    if (role == null)
	        throw new IllegalArgumentException(TAG + " >> 'role' can not be null");
	    
	    TAG = "[authUserService - loadByRoleAndUuid role:" + role.toString() + ", uuid:" + uuid + "]";
	    
	    try {
	        Query query;
	        if (role == RoleEnum.SUPERVISOR) {
	            query = em.createNamedQuery("AuthUserEntity.loadByCaptadorUuid");
	            query.setParameter("uuidSupervisor", uuid);
	        } else if (role == RoleEnum.CORDINADOR) {
	            query = em.createNamedQuery("AuthUserEntity.loadByCordinadorUuid");
	            query.setParameter("uuidCordinador", uuid);
	        } else if (role == RoleEnum.ADMINISTRADOR || role == RoleEnum.BACKOFFICE) {
	            query = em.createNamedQuery("AuthUserEntity.loadAllCaptadores");
	        } else {
	            throw new IllegalArgumentException(TAG + " >> Unsupported role: " + role);
	        }
	        
	        List<AuthUserEntity> entityList = (List<AuthUserEntity>) query.getResultList();
	        List<AuthUserVo> result = new ArrayList<>();
	        if (entityList != null && !entityList.isEmpty()) {
	            for (AuthUserEntity source : entityList) {
	                AuthUserVo target = new AuthUserVo();
	                target.setUuid(source.getUuid());
	                target.setName(source.getName());
	                target.setSurname(source.getSurname());
	                target.setFullname(source.getFullname());
	                target.setEmail(source.getEmail());
	                target.setUsername(source.getUsername());
	                result.add(target);
	            }
	        }
	        
	        return result;
	        
	    } catch (javax.persistence.NoResultException ex) {
	        return null;
	        
	    } catch (Exception ex) {
	        LOGGER.error(TAG + " - Error: ", ex.getMessage());
	        throw new AuthUserServiceException(ex);
	    }
	}
	
	@SuppressWarnings("unchecked")
	public List<AuthUserVo> loadByRoleAgente(RoleEnum role, String uuid) {
	    String TAG = "[authUserService - loadByRoleAndUuid]";
	    
	    if (role == null)
	        throw new IllegalArgumentException(TAG + " >> 'role' can not be null");
	    
	    TAG = "[authUserService - loadByRoleAndUuid role:" + role.toString() + ", uuid:" + uuid + "]";
	    
	    try {
	        Query query;
	        if (role == RoleEnum.SUPERVISOR) {
	            query = em.createNamedQuery("AuthUserEntity.loadByAgenteCaptadorUuid");
	            query.setParameter("uuidSupervisor", uuid);
	        } else if (role == RoleEnum.CORDINADOR) {
	            query = em.createNamedQuery("AuthUserEntity.loadByAgenteUuid");
	            query.setParameter("uuidCordinador", uuid);
	        } else if (role == RoleEnum.ADMINISTRADOR || role == RoleEnum.BACKOFFICE) {
	            query = em.createNamedQuery("AuthUserEntity.loadAllAgente");
	        } else {
	            throw new IllegalArgumentException(TAG + " >> Unsupported role: " + role);
	        }
	        
	        List<AuthUserEntity> entityList = (List<AuthUserEntity>) query.getResultList();
	        List<AuthUserVo> result = new ArrayList<>();
	        if (entityList != null && !entityList.isEmpty()) {
	            for (AuthUserEntity source : entityList) {
	                AuthUserVo target = new AuthUserVo();
	                target.setUuid(source.getUuid());
	                target.setName(source.getName());
	                target.setSurname(source.getSurname());
	                target.setFullname(source.getFullname());
	                target.setEmail(source.getEmail());
	                target.setUsername(source.getUsername());
	                result.add(target);
	            }
	        }
	        
	        return result;
	        
	    } catch (javax.persistence.NoResultException ex) {
	        return null;
	        
	    } catch (Exception ex) {
	        LOGGER.error(TAG + " - Error: ", ex.getMessage());
	        throw new AuthUserServiceException(ex);
	    }
	}



	
	@SuppressWarnings("unchecked")
	public AuthUserVo loadByCoordinadorUuid(String uuidCoordinador) {
	    String TAG = "[authUserService - loadByCoordinadorUuid]";
	    
	    if (uuidCoordinador == null)
	        throw new IllegalArgumentException(TAG + " >> 'uuidSupervisor' can not be null");
	    
	    TAG = "[authUserService - loadBySupervisorUuid uuidSupervisor:" + uuidCoordinador + "]";
	    
	    try {
	        Query query = em.createNamedQuery("AuthUserEntity.loadByCoordinadorUuid");
	        query.setParameter("uuidCoordinador", uuidCoordinador);
	        
	        AuthUserEntity source = (AuthUserEntity) query.getSingleResult();
	        List<AuthUserVo> result = new ArrayList<AuthUserVo>();
	        AuthUserVo target =null;
	        if (source != null) {
	            
	                target = new AuthUserVo();
	                target.setUuid(source.getUuid());
	                target.setName(source.getName());
	                target.setSurname(source.getSurname());
	                target.setFullname(source.getFullname());
	                target.setEmail(source.getEmail());
	                target.setUsername(source.getUsername());
	                target.setSupervisorUsername(source.getSupervisorUsername());
	                target.setUuidSupervisor(source.getUuidSupervisor());
	        }
	        
	        return target;
	        
	    } catch (javax.persistence.NoResultException ex) {
	        return null;
	        
	    } catch (Exception ex) {
	        LOGGER.error(TAG + " - Error: ", ex.getMessage());
	        throw new AuthUserServiceException(ex);
	    }
	}

	
	@SuppressWarnings("unchecked")
	public AuthUserVo loadByUsername (String username) 
	{
		String TAG ="[authUserService - loadByUsername username:"+username+"]";
	
		if (username == null)
			throw new IllegalArgumentException(TAG +" >> 'username' can not be null");

		try {
			Query query = em.createNamedQuery("AuthUserEntity.loadByUsername");
			query.setParameter("username", username);
			
			AuthUserEntity entity = (AuthUserEntity)  query.getSingleResult();
			
			return this.toAuthUserVo(entity);
						
			
		}catch (javax.persistence.NoResultException ex){
			return null;
			
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw new AuthUserServiceException(ex);
		}
	}
	

	
	@SuppressWarnings("unchecked")
	public List<AuthUserVo>  loadActive () 
	{
		String TAG ="[authUserService - loadActive]";
	
		try {
			Query query = em.createNamedQuery("AuthUserEntity.loadActive");
			
			List<AuthUserEntity> entityList = (List<AuthUserEntity>)  query.getResultList();
			List<AuthUserVo> result = new ArrayList<AuthUserVo>();
			if (entityList!=null && !entityList.isEmpty())
			{	
				for (AuthUserEntity source: entityList)
				{
					
					AuthUserVo target = new AuthUserVo();

					//target.setId(source.getId());
					target.setUuid(source.getUuid());
					target.setName(source.getName());
					target.setSurname(source.getSurname());
					target.setFullname(source.getFullname());
					target.setUsername(source.getUsername());
					
					target.setEmail(source.getEmail());
					//target.setPassword(source.getPassword());

					target.setIsActive(source.getIsActive());
					target.setRole(source.getRole());
					target.setHowManyMonthsPassExpires(source.getHowManyMonthsPassExpires());

					// DEBUG (Brob)
//					String activos = new String("GTB01,GTB02,GTB03,GTB04,GTC01,GTC02,SCS01,SCS02,SCS03,SCS04");
//					String activos = new String("GTB01,GTB03,GTC01");
//					target.setActives(activos);
//					if (source.getThemes() != null) {
//						target.setThemes(source.getThemes());
//					}else {
//						target.setThemes("");
//					}
					
					result.add(target);
				}
					
			}
						
			return result;
			
		}catch (javax.persistence.NoResultException ex){
			return null;
			
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw new AuthUserServiceException(ex);
		}
	}
	
	@SuppressWarnings("unchecked")
	public AuthUserVo loadByEmail (String email) 
	{
		String TAG ="[authUserService - loadByEmail email:"+email+"]";
	
		if (email == null)
			throw new IllegalArgumentException(TAG +" >> 'email' can not be null");

		try {
			Query query = em.createNamedQuery("AuthUserEntity.loadByEmail");
			query.setParameter("email", email);
			
			AuthUserEntity entity = (AuthUserEntity)  query.getSingleResult();
			
			return this.toAuthUserVo(entity);
						
			
		}catch (javax.persistence.NoResultException ex){
			return null;
			
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw new AuthUserServiceException(ex);
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<AuthUserVo>  loadAll () 
	{
		String TAG ="[authUserService - loadAll]";
	
		try {
			Query query = em.createNamedQuery("AuthUserEntity.loadAll");
			
			List<AuthUserEntity> entityList = (List<AuthUserEntity>)  query.getResultList();
			List<AuthUserVo> result = new ArrayList<AuthUserVo>();
			if (entityList!=null && !entityList.isEmpty())
			{	
				for (AuthUserEntity source: entityList)
				{
					
					AuthUserVo target = new AuthUserVo();

					//target.setId(source.getId());
					target.setUuid(source.getUuid());
					target.setName(source.getName());
					target.setSurname(source.getSurname());
					target.setFullname(source.getFullname());
					target.setUsername(source.getUsername());
					
					target.setEmail(source.getEmail());
					//target.setPassword(source.getPassword());

					target.setIsActive(source.getIsActive());
					target.setRole(source.getRole());
					target.setHowManyMonthsPassExpires(source.getHowManyMonthsPassExpires());

					// DEBUG (Brob)
//					String activos = new String("GTB01,GTB02,GTB03,GTB04,GTC01,GTC02,SCS01,SCS02,SCS03,SCS04");
//					String activos = new String("GTB01,GTB03,GTC01");
//					target.setActives(activos);
//					if (source.getThemes() != null) {
//						target.setThemes(source.getThemes());
//					}else {
//						target.setThemes("");
//					}
					
					result.add(target);
				}
					
			}
						
			return result;
			
		}catch (javax.persistence.NoResultException ex){
			return null;
			
		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw new AuthUserServiceException(ex);
		}
	}
	
	@SuppressWarnings("unchecked")
	public AuthUserVo loadByUuid(String uuid) 
	{
		String TAG ="[authUserService - loadByUuid uuid:"+uuid+"]";

		if (uuid == null)
			throw new IllegalArgumentException(TAG +" >> 'uuid' can not be null");

		try {
			Query query = em.createNamedQuery("AuthUserEntity.loadByUuid");
			query.setParameter("uuid", uuid);

			AuthUserEntity entity = (AuthUserEntity)  query.getSingleResult();
			if (entity == null)
				return null;
			else
				return this.toAuthUserVo(entity);

		} catch (javax.persistence.NoResultException ex) {
			return null;


		}catch (Exception ex){
			LOGGER.error(TAG + " - Error: ",ex);
			throw new AuthUserServiceException(ex);
		}
	}

	
        public void updateUuid (AuthUserVo AuthUserVo, Boolean copyIfNull) throws AuthUserNotFoundException
        {
                String TAG ="[AuthUserService - updateUuid]";
		
		if (AuthUserVo == null)
			throw new IllegalArgumentException(TAG +" >> 'AuthUserVo' can not be null");
		
		if (AuthUserVo.getUuid() == null)
			throw new IllegalArgumentException(TAG +" >> 'uuid' can not be null");
		
		try{
			AuthUserVo.setFullname(this.generateFullname(AuthUserVo.getName(), AuthUserVo.getSurname(),""));
			Query query = em.createNamedQuery("AuthUserEntity.loadByUuid");
			query.setParameter("uuid", AuthUserVo.getUuid());

			AuthUserEntity entity = (AuthUserEntity)  query.getSingleResult();
			
			if(entity == null)
				throw new AuthUserNotFoundException();
			
			if (AuthUserVo.getUuidProviders() != null) {
                List<UserProviderVo> existingRels = userProviderService.loadByUserUuid(AuthUserVo.getUuid());
                Set<String> existing = new HashSet<>();
                Set<String> incoming = new HashSet<>(AuthUserVo.getUuidProviders());
                if (existingRels != null) {
                        for (UserProviderVo rel : existingRels) {
                                existing.add(rel.getUuidProvider());
                                if (!incoming.contains(rel.getUuidProvider())) {
                                        try {
                                                userProviderService.delete(rel.getIdUserProvider());
                                        } catch (Exception e) {
                                                LOGGER.error(TAG + " - Error deleting provider:{}", rel.getUuidProvider());
                                        }
                                }
                        }
                }
                for (String providerUuid : AuthUserVo.getUuidProviders()) {
                        if (!existing.contains(providerUuid)) {
                                try {
                                        AuthUserVo provider = this.loadByUuid(providerUuid);
                                        if (provider != null && RoleEnum.PROVIDER.equals(provider.getRole())) {
                                                UserProviderVo rel = new UserProviderVo();
                                                rel.setUuidUser(AuthUserVo.getUuid());
                                                rel.setUuidProvider(providerUuid);
                                                rel.setUsernameProvider(provider.getUsername());
                                                userProviderService.create(rel);
                                        }
                                } catch (Exception e) {
                                        LOGGER.error(TAG + " - Error assigning provider:{}", providerUuid);
                                }
                        }
                }
        }
			
                        this.toAuthUserEntity(AuthUserVo, entity, copyIfNull);
                        entity.setFxModification(Calendar.getInstance());
                        em.merge(entity);

                        List<Long> parentCompanyIds = resolveParentCompanyIds(AuthUserVo);
                        if (parentCompanyIds != null) {
                                parentCompanyService.replaceUserParentCompanies(entity.getId(), parentCompanyIds);
                        }

                        AuthUserVo.setId(entity.getId());
                        AuthUserVo.setUuid(entity.getUuid());
                        AuthUserVo.setRole(entity.getRole());
                        synchronizeProviderConfig(AuthUserVo);
			
		}catch (AuthUserNotFoundException ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw ex;
           
		}catch (Exception ex){
			LOGGER.error("[AuthUserService - updateAuthUser] - Error: ",ex);
            throw new AuthUserServiceException(ex);
		
		}
        }


        public void update (AuthUserVo AuthUserVo, Boolean copyIfNull) throws AuthUserNotFoundException
        {
		String TAG ="[AuthUserService - update]";
		
		if (AuthUserVo == null)
			throw new IllegalArgumentException(TAG +" >> 'AuthUserVo' can not be null");
		
		if (AuthUserVo.getId() == null)
			throw new IllegalArgumentException(TAG +" >> 'AuthUserVo.getId()' can not be null");

		try{
			
			AuthUserEntity entity = em.find(AuthUserEntity.class, AuthUserVo.getId());
			
			if(entity == null)
				throw new AuthUserNotFoundException();
			
                        this.toAuthUserEntity(AuthUserVo, entity, copyIfNull);
                        entity.setFxModification(Calendar.getInstance());
                        em.merge(entity);

                        List<Long> parentCompanyIds = resolveParentCompanyIds(AuthUserVo);
                        if (parentCompanyIds != null) {
                                parentCompanyService.replaceUserParentCompanies(entity.getId(), parentCompanyIds);
                        }

                        AuthUserVo.setId(entity.getId());
                        AuthUserVo.setUuid(entity.getUuid());
                        AuthUserVo.setRole(entity.getRole());
                        synchronizeProviderConfig(AuthUserVo);

                }catch (AuthUserNotFoundException ex){
			LOGGER.error(TAG + " - Error: ",ex.getMessage());
			throw ex;
           
		}catch (Exception ex){
			LOGGER.error("[AuthUserService - updateAuthUser] - Error: ",ex);
            throw new AuthUserServiceException(ex);
		
        }
        }
        private List<Long> resolveParentCompanyIds(AuthUserVo authUserVo) {
                if (authUserVo == null) {
                        return null;
                }

                List<Long> parentCompanyIds = normalizeParentCompanyIds(authUserVo.getParentCompanyIds());
                boolean hasInput = parentCompanyIds != null && !parentCompanyIds.isEmpty();

                if ((parentCompanyIds == null || parentCompanyIds.isEmpty())) {
                        Long parentCompanyId = normalizeParentCompanyId(authUserVo.getParentCompanyId());
                        if (parentCompanyId != null) {
                                parentCompanyIds = new ArrayList<Long>();
                                parentCompanyIds.add(parentCompanyId);
                                hasInput = true;
                        }
                }

                if ((parentCompanyIds == null || parentCompanyIds.isEmpty()) && authUserVo.getParentCompanies() != null) {
                        parentCompanyIds = authUserVo.getParentCompanies().stream()
                                        .filter(parentCompanyVo -> parentCompanyVo != null)
                                        .map(parentCompanyVo -> normalizeParentCompanyId(parentCompanyVo.getId()))
                                        .filter(parentCompanyId -> parentCompanyId != null)
                                        .distinct()
                                        .collect(Collectors.toList());
                        if (parentCompanyIds != null && !parentCompanyIds.isEmpty()) {
                                hasInput = true;
                        }
                }

                if (!hasInput) {
                        return null;
                }

                if (parentCompanyIds == null) {
                        parentCompanyIds = new ArrayList<Long>();
                }

                List<Long> filteredParentCompanyIds = new ArrayList<Long>();
                for (Long id : parentCompanyIds) {
                        Long normalizedId = normalizeParentCompanyId(id);
                        if (normalizedId != null && !filteredParentCompanyIds.contains(normalizedId)) {
                                filteredParentCompanyIds.add(normalizedId);
                        }
                }

                return filteredParentCompanyIds;
        }

        private List<Long> normalizeParentCompanyIds(List<?> rawParentCompanyIds) {
                if (rawParentCompanyIds == null) {
                        return null;
                }

                List<Long> normalizedParentCompanyIds = new ArrayList<Long>();
                for (Object rawId : rawParentCompanyIds) {
                        Long normalizedId = normalizeParentCompanyId(rawId);
                        if (normalizedId != null && !normalizedParentCompanyIds.contains(normalizedId)) {
                                normalizedParentCompanyIds.add(normalizedId);
                        }
                }

                return normalizedParentCompanyIds;
        }

        private Long normalizeParentCompanyId(Object rawId) {
                if (rawId == null) {
                        return null;
                }

                if (rawId instanceof Number) {
                        return Long.valueOf(((Number) rawId).longValue());
                }

                if (rawId instanceof String) {
                        String value = ((String) rawId).trim();
                        if (value.isEmpty()) {
                                return null;
                        }

                        try {
                                return Long.valueOf(value);
                        } catch (NumberFormatException ex) {
                                LOGGER.warn("[AuthUserService - resolveParentCompanyIds] - Invalid parent company id value: {}", value);
                        }
                }

                return null;
        }
	


        @SuppressWarnings("unchecked")
        public List<AuthUserVo> loadProvidersByUserUuid(String userUuid)
        {
            String TAG = "[authUserService - loadProvidersByUserUuid]";

            if (userUuid == null)
                throw new IllegalArgumentException(TAG + " >> 'userUuid' can not be null");

            try {
                List<UserProviderVo> relations = userProviderService.loadByUserUuid(userUuid);
                List<AuthUserVo> result = new ArrayList<AuthUserVo>();
                if (relations != null && !relations.isEmpty()) {
                    for (UserProviderVo rel : relations) {
                        AuthUserVo provider = this.loadByUuid(rel.getUuidProvider());
                        if (provider != null && RoleEnum.PROVIDER.equals(provider.getRole())) {
                            result.add(provider);
                        }
                    }
                }
                return result;

            } catch (Exception ex) {
                LOGGER.error(TAG + " - Error: ", ex.getMessage());
                throw new AuthUserServiceException(ex);
            }
        }
        
        public AuthUserVo findParentProviderBySubProviderUsername(String username) {
            if (username == null || username.trim().isEmpty()) {
                return null;
            }

            String[] parts = username.toUpperCase().split("_");
            AuthUserVo parent = null;
            if (parts.length > 0) {
                parent = loadMainProviderByUsernameLike(parts[0]);
            }

            if ((parent == null) && parts.length > 1) {
                parent = loadMainProviderByUsernameLike(parts[1]);
            }

            return parent;
        }

        
        @SuppressWarnings("unchecked")
        public AuthUserVo loadMainProviderByUsernameLike(String username) {
            String TAG = "[authUserService - loadMainProviderByUsernameLike]";

            if (username == null || username.trim().isEmpty()) {
                return null;
            }

            try {
                Query query = em.createNamedQuery("AuthUserEntity.loadMainProviderByUsernameLike");
                query.setParameter("username", "%" + username.toUpperCase() + "%");
                query.setParameter("role", RoleEnum.PROVIDER);
                query.setMaxResults(1);

                List<AuthUserEntity> entityList = (List<AuthUserEntity>) query.getResultList();
                if (entityList != null && !entityList.isEmpty()) {
                    return this.toAuthUserVo(entityList.get(0));
                }

                return null;

            } catch (Exception ex) {
                LOGGER.error(TAG + " - Error: ", ex.getMessage());
                throw new AuthUserServiceException(ex);
            }
        }
        
        
public String encrypPassword(String pass) throws Exception
	{
		byte[] salt = PasswordEncryptionService.generateSalt();
		byte[] pepper = PasswordEncryptionService.generateSalt();
		byte[] passEncrypted = PasswordEncryptionService.getEncryptedPassword(pass + Base64.encodeBase64String(pepper), salt);

		return PasswordEncryptionService.joinEncryptedPassword(passEncrypted, pepper, salt);

	}
	
	private void toAuthUserEntity(AuthUserVo source, AuthUserEntity target, Boolean copyIfNull)
	{
		if(copyIfNull || source.getId()!=null)
			target.setId(source.getId());

		if(copyIfNull || source.getUuid()!=null)
			target.setUuid(source.getUuid());
		
		if(copyIfNull || source.getName()!=null)
		target.setName(source.getName());
		
		if(copyIfNull || source.getSurname()!=null)
		target.setSurname(source.getSurname());
		
		if(copyIfNull || source.getFullname()!=null)
		target.setFullname(source.getFullname());
		
		if(copyIfNull || source.getUsername()!=null)
			target.setUsername(source.getUsername());
		
		if(copyIfNull || source.getEmail()!=null)
		target.setEmail(source.getEmail());
		
		
		if(copyIfNull || source.getPassword()!=null)
		target.setPassword(source.getPassword());
		
		if(copyIfNull || source.getIsActive()!=null)
		target.setIsActive(source.getIsActive());
		
		if(copyIfNull || source.getIsMainProvider() != null)
            target.setIsMainProvider(source.getIsMainProvider());
		
		if(copyIfNull || source.getUuidCordinador()!=null)
			target.setUuidCordinador(source.getUuidCordinador());
		
		if(copyIfNull || source.getUuidSupervisor()!=null)
			target.setUuidSupervisor(source.getUuidSupervisor());
		
		
		if(copyIfNull || source.getSupervisorUsername()!=null)
			target.setSupervisorUsername(source.getSupervisorUsername());
		
		if(copyIfNull || source.getCordinadorUsername()!=null)
			target.setCordinadorUsername(source.getCordinadorUsername());
		

		
		
		if(copyIfNull || source.getRole()!=null)
		target.setRole(source.getRole());
		
//		if(copyIfNull || source.getThemes()!=null)
//		target.setThemes(source.getThemes());
		
	

		if(copyIfNull || source.getHasToCheckHistoryPass()!=null)
		target.setHasToCheckHistoryPass(source.getHasToCheckHistoryPass());
		
		if(copyIfNull || source.getIsTemporalPassword()!=null)
		target.setIsTemporalPassword(source.getIsTemporalPassword());
		
		if(copyIfNull || source.getFxTemporal()!=null)
		target.setFxTemporal(source.getFxTemporal());
		
		if(copyIfNull || source.getHowManyMonthsPassExpires()!=null)
		 target.setHowManyMonthsPassExpires(source.getHowManyMonthsPassExpires());
		
		if(copyIfNull || source.getFxExpiration()!=null)
		 target.setFxExpiration(source.getFxExpiration());
		
		
		 
		 

	}

	private AuthUserVo toAuthUserVo(AuthUserEntity source)
	{
		AuthUserVo target = new AuthUserVo();

		target.setId(source.getId());
		target.setUuid(source.getUuid());
		target.setName(source.getName());
		target.setSurname(source.getSurname());
		target.setFullname(source.getFullname());

		target.setUsername(source.getUsername());
		target.setEmail(source.getEmail());
		target.setPassword(source.getPassword());

		target.setIsActive(source.getIsActive());
		target.setIsMainProvider(source.getIsMainProvider());
		target.setRole(source.getRole());

		target.setUuidCordinador(source.getUuidCordinador());
		target.setUuidSupervisor(source.getUuidSupervisor());
		target.setSupervisorUsername(source.getSupervisorUsername());
		target.setCordinadorUsername(source.getCordinadorUsername());

		target.setHasToCheckHistoryPass(source.getHasToCheckHistoryPass());

		target.setIsTemporalPassword(source.getIsTemporalPassword());
		target.setFxTemporal(source.getFxTemporal());

		target.setHowManyMonthsPassExpires(source.getHowManyMonthsPassExpires());
		target.setFxExpiration(source.getFxExpiration());

		   try {
	             List<UserProviderVo> rels = userProviderService.loadByUserUuid(source.getUuid());
	             target.setProviders(rels != null ? rels : new ArrayList<>());
	     } catch (Exception e) {
	             LOGGER.error("[AuthService - toAuthUserVo] - Error loading providers for user:{}", source.getUuid());
	             target.setProviders(new ArrayList<>());
	     }
		   
                List<ParentCompanyVo> parentCompanies = new ArrayList<ParentCompanyVo>();
                if (parentCompanyService != null && source.getId() != null) {
                        List<ParentCompanyVo> userParentCompanies;
                        if (RoleEnum.SUPER_ADMNISTRADOR.equals(source.getRole())) {
                                userParentCompanies = parentCompanyService.findAll();
                        } else {
                                userParentCompanies = parentCompanyService.findByUserId(source.getId());
                        }
                        if (userParentCompanies != null) {
                                parentCompanies.addAll(userParentCompanies);
                        }
                }

		target.setParentCompanies(parentCompanies);
		target.setParentCompanyIds(parentCompanies.stream()
				.filter(parentCompanyVo -> parentCompanyVo != null && parentCompanyVo.getId() != null)
				.map(ParentCompanyVo::getId).collect(Collectors.toList()));

		if (!parentCompanies.isEmpty()) {
			ParentCompanyVo defaultParentCompany = parentCompanies.get(0);
			target.setParentCompanyId(defaultParentCompany.getId());
			target.setParentCompanyName(defaultParentCompany.getName());
		} else {
			target.setParentCompanyId(null);
			target.setParentCompanyName(null);
		}

		loadProviderConfig(target);

		return target;
	}

	private AuthUserEntity toAuthUserEntity(AuthUserVo source)
	{
		AuthUserEntity target = new AuthUserEntity();
		target.setId(source.getId());
		target.setUuid(source.getUuid());
		target.setName(source.getName());
		target.setSurname(source.getSurname());
		target.setFullname(source.getFullname());

		target.setUsername(source.getUsername());
		target.setEmail(source.getEmail());
		target.setPassword(source.getPassword());

		target.setIsActive(source.getIsActive());
		target.setIsMainProvider(source.getIsMainProvider());
		target.setRole(source.getRole());
		target.setUuidCordinador(source.getUuidCordinador());
		target.setUuidSupervisor(source.getUuidSupervisor());
		target.setSupervisorUsername(source.getSupervisorUsername());
		target.setCordinadorUsername(source.getCordinadorUsername());

		target.setHasToCheckHistoryPass(source.getHasToCheckHistoryPass());

		target.setIsTemporalPassword(source.getIsTemporalPassword());
		target.setFxTemporal(source.getFxTemporal());

		target.setHowManyMonthsPassExpires(source.getHowManyMonthsPassExpires());
		target.setFxExpiration(source.getFxExpiration());

		return target;
	}

    private void synchronizeProviderConfig(AuthUserVo authUserVo) {
        if (providerConfigService == null || authUserVo == null) {
            return;
        }

        if (authUserVo.getUuid() == null) {
            return;
        }

        if (!RoleEnum.PROVIDER.equals(authUserVo.getRole())) {
            providerConfigService.deleteByUserUuid(authUserVo.getUuid());
            authUserVo.setProviderConfig(new ArrayList<ProviderConfigVo>());
            return;
        }

        if (authUserVo.getProviderConfig() == null) {
            return;
        }

        List<ProviderConfigVo> savedConfig = providerConfigService.saveOrUpdate(authUserVo.getId(), authUserVo.getUuid(), authUserVo.getProviderConfig());
        authUserVo.setProviderConfig(savedConfig != null ? savedConfig : new ArrayList<ProviderConfigVo>());
    }

    private void loadProviderConfig(AuthUserVo authUserVo) {
        if (providerConfigService == null || authUserVo == null) {
            return;
        }

        if (!RoleEnum.PROVIDER.equals(authUserVo.getRole()) || authUserVo.getUuid() == null) {
            authUserVo.setProviderConfig(new ArrayList<ProviderConfigVo>());
            return;
        }

        List<ProviderConfigVo> configs = providerConfigService.loadByUserUuid(authUserVo.getUuid());
        if (configs == null) {
            configs = new ArrayList<ProviderConfigVo>();
        }
        authUserVo.setProviderConfig(configs);
    }
}
