package com.zap.maintenance.config;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.zap.maintenance.service.settings.SettingsService;
import com.zap.maintenance.vo.settings.MongoConfigVo;

import dev.morphia.Datastore;
import dev.morphia.Morphia;



@ApplicationScoped
public class ManagerMongo implements Serializable
{
	private static final long serialVersionUID = -8448538504088495308L;
	private static final Logger LOGGER = LoggerFactory.getLogger(ManagerMongo.class.getName());

//	private static final String IP = "espetech.synology.me";
//	private static final Integer PORT = 27017;
//	private static final String DATA_BASE="zsincro";
	
	private static MongoClient mongoClient;
	private static Datastore datastore;
	
	@Inject
	private SettingsService settingsService;
	
//	@PostConstruct
//	public void init()
//	{
//		try {
//
//			MongoConfigVo config =settingsService.getMongoConfig();
//
//			if (mongoClient==null)
//			{
//				//				mongoClient = new MongoClient(config.getIp(), config.getPort());
//				//				LOGGER.info("[ManagerMongo - init] - MongoClient is created...");
//
//				// Manage the mongo db connection...
//				List<ServerAddress> seeds = new ArrayList<ServerAddress>();
//				seeds.add( new ServerAddress(config.getIp(), config.getPort() ));
//				List<MongoCredential> credentials = new ArrayList<MongoCredential>();
//				credentials.add(
//						MongoCredential.createScramSha1Credential(
//								config.getUser(),
//								config.getMongoDb(),
//								config.getPass().toCharArray()
//								)
//						);
//				mongoClient=  new MongoClient(seeds, credentials);
//			}
//
//			if (datastore==null)
//			{
//				final Morphia morphia = new Morphia();
//				//morphia.mapPackage("com.frinwo.data.be.hotspot.vo");
//
//				datastore = morphia.createDatastore(mongoClient, DATA_BASE);
//				datastore.ensureIndexes();
//			}
//
//
//		} catch (Exception e) {
//			LOGGER.error("[ManagerMongo - init] - MongoClient Exception >>",e);
//		}
//	}
	
	
	@PostConstruct
	public void init() {
	    try {
	        LOGGER.info("[ManagerMongo - init] - Inicializando MongoClient...");
	        MongoConfigVo config = settingsService.getMongoConfig();
	        
	        LOGGER.info("[ManagerMongo - init] - Configuración de MongoDB obtenida.");
	        
	        if (mongoClient == null) {
	            LOGGER.info("[ManagerMongo - init] - Creando MongoClient...");
	            List<ServerAddress> seeds = new ArrayList<>();
	            seeds.add(new ServerAddress(config.getIp(), config.getPort()));

	            MongoCredential credential = MongoCredential.createScramSha1Credential(
	                    config.getUser(),
	                    config.getMongoDb(),
	                    config.getPass().toCharArray()
	            );

	            MongoClientSettings settings = MongoClientSettings.builder()
	                    .applyToClusterSettings(builder -> builder.hosts(seeds))
	                    .credential(credential)
	                    .build();

	            mongoClient = MongoClients.create(settings); // Crear cliente de MongoDB moderno
	            LOGGER.info("[ManagerMongo - init] - MongoClient creado correctamente.");
	        }

	        if (datastore == null) {
	            LOGGER.info("[ManagerMongo - init] - Creando Datastore...");
	            datastore = Morphia.createDatastore(mongoClient, config.getMongoDb());
	            LOGGER.info("[ManagerMongo - init] - Datastore creado correctamente.");
	        }

	    } catch (Exception e) {
	        LOGGER.error("[ManagerMongo - init] - Excepción durante la inicialización de MongoClient:", e);
	    }
	}

	
	@Produces
	public Datastore datastore()
	{

		try {
			if (mongoClient==null || datastore==null)
			{
				init();
			}
		} catch (Exception e) {
			LOGGER.error("[ManagerMongo - create] - MongoClient Exception >>",e);
		}

		return datastore;

	}
	
	
	
	@Produces
	public MongoClient create()
	{
		try {
			if (mongoClient==null)
			{
				init();
			}
		} catch (Exception e) {
			//e.printStackTrace();
			LOGGER.error("[ManagerMongo - create] - MongoClient Exception >>",e);
		}
		return mongoClient;
	}
	
	
	
	
//	
//	public void close(@Disposes final MongoClient mongoClient)
//	{
//	    if (mongoClient!=null)
//	    {
//			mongoClient.close();
//			LOGGER.info("[ManagerMongo - close] - MongoClient is closed...");
//		}
//	    if (datastore!=null)
//	    {
//	    	datastore=null;
//			LOGGER.info("[ManagerMongo - close] - MongoClient is closed...");
//		}
//	}
	
}