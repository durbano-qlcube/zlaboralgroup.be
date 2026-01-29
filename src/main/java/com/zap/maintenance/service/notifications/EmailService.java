package com.zap.maintenance.service.notifications;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.zap.maintenance.exception.notifications.EmailServiceException;
import com.zap.maintenance.service.settings.SettingsService;
import com.zap.maintenance.vo.notifications.EmailVo;
import com.zap.maintenance.vo.settings.EmailSettingsVo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class EmailService  implements Serializable {

	private static final long serialVersionUID = -2615227480156796697L;
	private static final Logger log = LoggerFactory.getLogger(EmailService.class);
	


	// ------ EJB Injection Definitions --------
	@Inject
	private SettingsService settingsService;

//	@Inject
//	private TripleDesService tripleDesService;
	
	
	private EmailSettingsVo EMAIL_SETTINGS_VO;

	
	
	@PostConstruct
	public void initializes()throws EmailServiceException
	{
		try{
			if(settingsService == null)
				settingsService = new SettingsService();
			
			EMAIL_SETTINGS_VO = settingsService.getEmailSettings();
		}catch (Exception ex) {
			log.error("[EmailService - initializes] - Error: ",ex);
			 throw new EmailServiceException (ex);
		}
	}
	
	
	
	//@Asynchronous
	public void sendEmailPlainTexMultipart(EmailVo emailVo, String filepath, String fileName)
	{
		//log.debug("[EmailService - sendEmailPlainTexMultipart] - Init");
		//long T1=System.currentTimeMillis();
		
		try{
			if(EMAIL_SETTINGS_VO==null)
				this.initializes();


			String user =EMAIL_SETTINGS_VO.getUser();
			String pass =EMAIL_SETTINGS_VO.getPass(); 
			
			log.debug("[EmailService - sendEmailPlainTex] - input emailVo:"+emailVo.toString());
			//Propiedades de conexion
			Properties props = new Properties();
			props.setProperty("mail.smtp.host", EMAIL_SETTINGS_VO.getHost());
			props.setProperty("mail.smtp.starttls.enable", EMAIL_SETTINGS_VO.getStarttls());
			props.setProperty("mail.smtp.port", EMAIL_SETTINGS_VO.getPort());
			props.setProperty("mail.smtp.user", user);
			props.setProperty("mail.smtp.auth", EMAIL_SETTINGS_VO.getAuthIsNeeds()); 

			//Iniciando Session
			Authenticator auth = new SMTPAuthenticator(user,pass);
			Session session = Session.getInstance(props, auth);
			session.setDebug(EMAIL_SETTINGS_VO.getDebug());
			
			//Creando Mensaje
			MimeMessage message = new MimeMessage(session);

			message.setFrom(new InternetAddress(user,"Secretaria"));
			
			if (emailVo.getTo()!=null && !"".equals(emailVo.getTo().trim()))
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailVo.getTo()));
			
			if (emailVo.getToList()!=null && !emailVo.getToList().isEmpty())
			{
				for (String emailTo : emailVo.getToList())
				{
					message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailTo));
				}
			}

			if (emailVo.getCc()!=null && !"".equals(emailVo.getCc().trim()))
				message.addRecipient(Message.RecipientType.CC, new InternetAddress(emailVo.getCc()));

			if (emailVo.getCco()!=null && !"".equals(emailVo.getCco().trim()))
				message.addRecipient(Message.RecipientType.BCC, new InternetAddress(emailVo.getCco()));

			//log.debug("[EmailService - sendEmailPlainTexMultipart]  - configure attachment email.");
			message.setSubject(emailVo.getSubject());
			message.setSentDate(new Date());
			//message.setText(emailVo.getBody(), "UTF-8","html");
			//message.setContent(emailVo.getBody(), "text/html");
			//message.setContent(stream, "multipart/form-data");//MediaType.MULTIPART_FORM_DATA
			
			//BodyPart messageBodyPart = new MimeBodyPart();
			//messageBodyPart.setText(emailVo.getBody());
			
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent( emailVo.getBody(), "text/html; charset=utf-8" );
	         
	         
	         
			Multipart multipart = new MimeMultipart();
			BodyPart attachment  = new MimeBodyPart();
			DataSource source = new FileDataSource(filepath);
		    attachment.setDataHandler(new DataHandler(source));
		    attachment.setFileName(fileName);
			multipart.addBodyPart(attachment );
			multipart.addBodyPart(messageBodyPart);
			
			message.setContent(multipart);
			
			//Enviando Mensage
			Transport t = session.getTransport(EMAIL_SETTINGS_VO.getTransport());
			t.connect();
			t.sendMessage(message,message.getAllRecipients());	
			t.close();
			
			//log.debug("[EmailService - sendEmailPlainTexMultipart]  - send email.");
//			Transport.send(message);
			



		}catch (Exception ex) {
			log.error("[EmailService - sendEmailPlainTex] - Error: ",ex);
			throw new EmailServiceException(ex);
		
		//}finally{
		//	log.debug("[EmailService - sendEmailPlainTex] - Finish Timing:"+(System.currentTimeMillis()-T1));
		}
	}
	
	
	
	
	
	
	
	
	
	
	//@Asynchronous
	public Integer sendEmail (List<EmailVo> emailVoList)
	{
		int emailSent=0;
		
		try{
			if(EMAIL_SETTINGS_VO==null)
				this.initializes();

			
			String user =EMAIL_SETTINGS_VO.getUser();
			String pass =EMAIL_SETTINGS_VO.getPass(); 

			//Propiedades de conexion
			Properties props = new Properties();
			props.setProperty("mail.smtp.host", EMAIL_SETTINGS_VO.getHost());
			props.setProperty("mail.smtp.starttls.enable", EMAIL_SETTINGS_VO.getStarttls());
			props.setProperty("mail.smtp.port", EMAIL_SETTINGS_VO.getPort());
			props.setProperty("mail.smtp.user", user);
			props.setProperty("mail.smtp.auth", EMAIL_SETTINGS_VO.getAuthIsNeeds()); 

			//Iniciando Session
			Authenticator auth = new SMTPAuthenticator(user,pass);
			Session session = Session.getInstance(props, auth);
			session.setDebug(EMAIL_SETTINGS_VO.getDebug());
			
			//Enviando Mensage
			Transport t = session.getTransport(EMAIL_SETTINGS_VO.getTransport());
			t.connect();
			
			
			for(EmailVo emailVo: emailVoList)
			{
				if (emailVo.getTo()!=null && !"".equals(emailVo.getTo().trim()))
				{
					//Creando Mensaje
					MimeMessage message = new MimeMessage(session);
					message.setFrom(new InternetAddress(user));
					
					if (emailVo.getTo()!=null && !"".equals(emailVo.getTo().trim()))
						message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailVo.getTo()));

					if (emailVo.getToList()!=null && !emailVo.getToList().isEmpty())
					{
						for (String emailTo : emailVo.getToList())
						{
							message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailTo));
						}
					}
					


					if (emailVo.getCc()!=null && !"".equals(emailVo.getCc().trim()))
						message.addRecipient(Message.RecipientType.CC, new InternetAddress(emailVo.getCc()));

					if (emailVo.getCco()!=null && !"".equals(emailVo.getCco().trim()))
						message.addRecipient(Message.RecipientType.BCC, new InternetAddress(emailVo.getCco()));

					message.setSubject(emailVo.getSubject());
					message.setSentDate(new Date());
					log.debug("[EmailService - sendEmailAws] iteration:{} sent email to:{} ",emailSent, emailVo.getTo());
					//message.setContent(emailVo.getBody(), "text/html");

					final MimeBodyPart htmlPart = new MimeBodyPart();
			        htmlPart.setContent(emailVo.getBodyHtml(), "text/html; charset=utf-8");
					
			        final MimeBodyPart textPart = new MimeBodyPart();
					textPart.setContent( emailVo.getBodyTxt(), "text/plain; charset=utf-8" );
			         
					Multipart multipart = new MimeMultipart("alternative");
					
					multipart.addBodyPart(textPart);
					multipart.addBodyPart(htmlPart);
					
					message.setContent(multipart);
					
					try{
						t.sendMessage(message,message.getAllRecipients());
						emailSent++;
					}catch (Exception ex) {
						log.debug("[EmailService - sendEmail] ERROR iteration:{} sent email to:{} ",emailSent, emailVo.getTo());
					}
					
				}
			}
			
			t.close();
			
		}catch (Exception ex) {
			log.error("[EmailService - sendEmailAws] - Error: ",ex);
			//new EmailServiceException(ex);
		
		//}finally{
		//	log.info("[EmailService - sendEmailAws] - Finish Timing:"+(System.currentTimeMillis()-T1));
		}
		
		return emailSent;
	}
	
	
//	@Asynchronous
	public void sendEmailHtml (EmailVo emailVo)
	{
		log.info("[EmailService - sendEmailPlainTex] - Init");
		long T1=System.currentTimeMillis();
		
		try{
			if(EMAIL_SETTINGS_VO==null)
				this.initializes();

		
			String user =EMAIL_SETTINGS_VO.getUser();
			String pass =EMAIL_SETTINGS_VO.getPass(); 
			
			log.info("[EmailService - sendEmailPlainTex] - user:"+user + " pass:" + pass);
			log.info("[EmailService - sendEmailPlainTex] - input emailVo:"+emailVo.toString());
			log.info("[EmailService - sendEmailPlainTex] - input To :"+emailVo.getTo());
			if (emailVo.getToList()!=null && !emailVo.getToList().isEmpty())
			{
				for (String email : emailVo.getToList())
				log.info("[EmailService - sendEmailPlainTex] - input ToList email:"+email);
			}
			
			
			//Propiedades de conexion
			Properties props = new Properties();
			props.setProperty("mail.smtp.host", EMAIL_SETTINGS_VO.getHost());
			props.setProperty("mail.smtp.starttls.enable", EMAIL_SETTINGS_VO.getStarttls());
			props.setProperty("mail.smtp.port", EMAIL_SETTINGS_VO.getPort());
			props.setProperty("mail.smtp.user", user);
			props.setProperty("mail.smtp.auth", EMAIL_SETTINGS_VO.getAuthIsNeeds()); 
			props.setProperty("mail.smtp.ssl.enable", "true"); 

			
			//Iniciando Session
			Authenticator auth = new SMTPAuthenticator(user, pass);
			Session session = Session.getInstance(props, auth);
			session.setDebug(EMAIL_SETTINGS_VO.getDebug());
			
			//Creando Mensaje
			MimeMessage message = new MimeMessage(session);

			message.setFrom(new InternetAddress(user));
//			message.setFrom(new InternetAddress(emailVo.getFrom(),emailVo.getFromName()));
//			message.setFrom(new InternetAddress(EMAIL_SETTINGS_VO.getFrom(),EMAIL_SETTINGS_VO.getFromName()));
			
			if (emailVo.getTo()!=null && !"".equals(emailVo.getTo().trim()))
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailVo.getTo()));
			
			if (emailVo.getToList()!=null && !emailVo.getToList().isEmpty())
			{
				for (String emailTo : emailVo.getToList())
				{
					message.addRecipient(Message.RecipientType.BCC, new InternetAddress(emailTo));
				}
			}

			if (emailVo.getCc()!=null && !"".equals(emailVo.getCc().trim()))
				message.addRecipient(Message.RecipientType.CC, new InternetAddress(emailVo.getCc()));

			if (emailVo.getCco()!=null && !"".equals(emailVo.getCco().trim()))
				message.addRecipient(Message.RecipientType.BCC, new InternetAddress(emailVo.getCco()));

			message.setSubject(emailVo.getSubject(), "UTF-8");
			message.setSentDate(new Date());
			//message.setText(emailVo.getBody(), "UTF-8","html");
			//message.setContent(emailVo.getBody(), "text/plain; charset=UTF-8");
			message.setContent(emailVo.getBody(), "text/html; charset=UTF-8");
			
			//Enviando Mensage
			Transport t = session.getTransport(EMAIL_SETTINGS_VO.getTransport());
			t.connect();
			t.sendMessage(message,message.getAllRecipients());
			t.close();

		}catch (Exception ex) {
			log.error("[EmailService - sendEmailPlainTex] - Error: ",ex);
			throw new EmailServiceException(ex);
		
		}finally{
			log.info("[EmailService - sendEmailPlainTex] - Finish Timing:"+(System.currentTimeMillis()-T1));
		}
	}
	
	@Asynchronous
	public void sendEmail (EmailVo emailVo)
	{
		log.info("[EmailService - sendEmailPlainTex] - Init");
		long T1=System.currentTimeMillis();
		
		try{
			if(EMAIL_SETTINGS_VO==null)
				this.initializes();

	
			String user =EMAIL_SETTINGS_VO.getUser();
			String pass =EMAIL_SETTINGS_VO.getPass(); 
			
			log.info("[EmailService - sendEmailPlainTex] - user:"+user + " pass:" + pass);
			log.info("[EmailService - sendEmailPlainTex] - input emailVo:"+emailVo.toString());
			log.info("[EmailService - sendEmailPlainTex] - input To :"+emailVo.getTo());
			if (emailVo.getToList()!=null && !emailVo.getToList().isEmpty())
			{
				for (String email : emailVo.getToList())
				log.info("[EmailService - sendEmailPlainTex] - input ToList email:"+email);
			}
			
			
			//Propiedades de conexion
			Properties props = new Properties();
			props.setProperty("mail.smtp.host", EMAIL_SETTINGS_VO.getHost());
			props.setProperty("mail.smtp.starttls.enable", EMAIL_SETTINGS_VO.getStarttls());
			props.setProperty("mail.smtp.port", EMAIL_SETTINGS_VO.getPort());
			props.setProperty("mail.smtp.user", user);
			props.setProperty("mail.smtp.auth", EMAIL_SETTINGS_VO.getAuthIsNeeds()); 
			
			
			//Iniciando Session
			Authenticator auth = new SMTPAuthenticator(user, pass);
			Session session = Session.getInstance(props, auth);
			session.setDebug(EMAIL_SETTINGS_VO.getDebug());
			
			//Creando Mensaje
			MimeMessage message = new MimeMessage(session);

			message.setFrom(new InternetAddress(user));
//			message.setFrom(new InternetAddress(emailVo.getFrom(),emailVo.getFromName()));
//			message.setFrom(new InternetAddress(EMAIL_SETTINGS_VO.getFrom(),EMAIL_SETTINGS_VO.getFromName()));
			
			if (emailVo.getTo()!=null && !"".equals(emailVo.getTo().trim()))
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailVo.getTo()));
			
			if (emailVo.getToList()!=null && !emailVo.getToList().isEmpty())
			{
				for (String emailTo : emailVo.getToList())
				{
					message.addRecipient(Message.RecipientType.BCC, new InternetAddress(emailTo));
				}
			}

			if (emailVo.getCc()!=null && !"".equals(emailVo.getCc().trim()))
				message.addRecipient(Message.RecipientType.CC, new InternetAddress(emailVo.getCc()));

			if (emailVo.getCco()!=null && !"".equals(emailVo.getCco().trim()))
				message.addRecipient(Message.RecipientType.BCC, new InternetAddress(emailVo.getCco()));

			message.setSubject(emailVo.getSubject(), "UTF-8");
			message.setSentDate(new Date());
			//message.setText(emailVo.getBody(), "UTF-8","html");
			message.setContent(emailVo.getBody(), "text/plain; charset=UTF-8");
			//message.setContent(emailVo.getBody(), "text/html; charset=UTF-8");
			
			//Enviando Mensage
			Transport t = session.getTransport(EMAIL_SETTINGS_VO.getTransport());
			t.connect();
			t.sendMessage(message,message.getAllRecipients());
			t.close();

		}catch (Exception ex) {
			log.error("[EmailService - sendEmailPlainTex] - Error: ",ex);
			throw new EmailServiceException(ex);
		
		}finally{
			log.info("[EmailService - sendEmailPlainTex] - Finish Timing:"+(System.currentTimeMillis()-T1));
		}
	}
	
	
	
	@Asynchronous
	public void sendEmail (EmailVo emailVo, Long idCentro, Boolean isSentSecretaria)
	{
		log.info("[EmailService - sendEmailPlainTex idCentro:"+idCentro+"] - Init");
		long T1=System.currentTimeMillis();
		
		try{
			if(EMAIL_SETTINGS_VO==null)
				this.initializes();


			String user =EMAIL_SETTINGS_VO.getUser();
			String pass =EMAIL_SETTINGS_VO.getPass(); 
			
			log.info("[EmailService - sendEmailPlainTex idCentro:"+idCentro+"] - user:"+user + " pass:" + pass);
			log.info("[EmailService - sendEmailPlainTex idCentro:"+idCentro+"] - input emailVo:"+emailVo.toString());
			log.info("[EmailService - sendEmailPlainTex idCentro:"+idCentro+"] - input To :"+emailVo.getTo());
			if (emailVo.getToList()!=null && !emailVo.getToList().isEmpty())
			{
				for (String email : emailVo.getToList())
				log.info("[EmailService - sendEmailPlainTex idCentro:"+idCentro+"] - input ToList email:"+email);
			}
			
			
			//Propiedades de conexion
			Properties props = new Properties();
			props.setProperty("mail.smtp.host", EMAIL_SETTINGS_VO.getHost());
			props.setProperty("mail.smtp.starttls.enable", EMAIL_SETTINGS_VO.getStarttls());
			props.setProperty("mail.smtp.port", EMAIL_SETTINGS_VO.getPort());
			props.setProperty("mail.smtp.user", user);
			props.setProperty("mail.smtp.auth", EMAIL_SETTINGS_VO.getAuthIsNeeds()); 
			
			
			//Iniciando Session
			Authenticator auth = new SMTPAuthenticator(user, pass);
			Session session = Session.getInstance(props, auth);
			session.setDebug(EMAIL_SETTINGS_VO.getDebug());
			
			//Creando Mensaje
			MimeMessage message = new MimeMessage(session);

			message.setFrom(new InternetAddress(user));
//			message.setFrom(new InternetAddress(emailVo.getFrom(),emailVo.getFromName()));
//			message.setFrom(new InternetAddress(EMAIL_SETTINGS_VO.getFrom(),EMAIL_SETTINGS_VO.getFromName()));
			
			if (emailVo.getTo()!=null && !"".equals(emailVo.getTo().trim()))
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailVo.getTo()));
			
			if (emailVo.getToList()!=null && !emailVo.getToList().isEmpty())
			{
				for (String emailTo : emailVo.getToList())
				{
					message.addRecipient(Message.RecipientType.BCC, new InternetAddress(emailTo));
				}
			}

			if (emailVo.getCc()!=null && !"".equals(emailVo.getCc().trim()))
				message.addRecipient(Message.RecipientType.CC, new InternetAddress(emailVo.getCc()));

			if (emailVo.getCco()!=null && !"".equals(emailVo.getCco().trim()))
				message.addRecipient(Message.RecipientType.BCC, new InternetAddress(emailVo.getCco()));

			if(isSentSecretaria)
				message.addRecipient(Message.RecipientType.BCC, new InternetAddress(user));
			
			message.setSubject(emailVo.getSubject(), "UTF-8");
			message.setSentDate(new Date());
			//message.setText(emailVo.getBody(), "UTF-8","html");
			message.setContent(emailVo.getBody(), "text/plain; charset=UTF-8");
			//message.setContent(emailVo.getBody(), "text/html; charset=UTF-8");
			
			//Enviando Mensage
			Transport t = session.getTransport(EMAIL_SETTINGS_VO.getTransport());
			t.connect();
			t.sendMessage(message,message.getAllRecipients());
			t.close();

		}catch (Exception ex) {
			log.error("[EmailService - sendEmailPlainTex idCentro:"+idCentro+"] - Error: ",ex);
			throw new EmailServiceException(ex);
		
		}finally{
			log.info("[EmailService - sendEmailPlainTex idCentro:"+idCentro+"] - Finish Timing:"+(System.currentTimeMillis()-T1));
		}
	}
	
	public static String checkCharacters (String str)
	{
		
		str=str.replaceAll("á", "&aacute;");
		str=str.replaceAll("é", "&eacute;");
		str=str.replaceAll("í", "&iacute;");
		str=str.replaceAll("ó", "&oacute;");
		str=str.replaceAll("ú", "&uacute;");
		
		str=str.replaceAll("Á", "&Aacute;");
		str=str.replaceAll("É", "&Eacute;");
		str=str.replaceAll("Í", "&Iacute;");
		str=str.replaceAll("Ó", "&Oacute;");
		str=str.replaceAll("Ú", "&Uacute;");
		
		
//		str=str.replaceAll("¿", "&iquest;");
//		str=str.replaceAll("\\?", "&#63;");
		str=str.replaceAll("Ñ", "&Ntilde;");
		str=str.replaceAll("ñ", "&ntilde;");
		str=str.replaceAll("¡", "&iexcl;");
		str=str.replaceAll("!", "&#33;");
		str=str.replaceAll("ª", "&ordf;");
		str=str.replaceAll("º", "&ordm;");
		
		
		return str;
	}
	
	
	private class SMTPAuthenticator extends javax.mail.Authenticator
	{
        private String user;
        private String pass;
		
        public SMTPAuthenticator (String user, String pass)
        {
        	this.user= user;
        	this.pass= pass;
        }
		
		public javax.mail.PasswordAuthentication getPasswordAuthentication()
        {

           return new javax.mail.PasswordAuthentication(user, pass);
        }
    }
	

	
	
}
