package com.zap.sales.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zap.maintenance.service.crypto.TripleDesService;
import com.zap.maintenance.service.settings.SettingsService;
import com.zap.sales.service.DocPartService;
import com.zap.sales.service.DocService;
import com.zap.sales.service.EmpresaService;
import com.zap.sales.service.VentasParticularService;
import com.zap.sales.service.VentasService;
import com.zap.sales.vo.doc.DocPartVo;
import com.zap.sales.vo.doc.DocVo;
import com.zap.sales.vo.empresa.EmpresaVo;
import com.zap.sales.vo.particular.VentaPartVo;
import com.zap.sales.vo.venta.VentaVo;
import com.zap.security.service.authenticate.AuthService;
import com.zap.security.service.jwt.JwtService;
import com.zap.security.vo.authenticate.AuthUserVo;


@WebServlet("/api/v1.0/UploadFileParticularServlet")
public class UploadFileParticularServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
       
	
	/** The Constant log. */
	private static final Logger LOGGER = LoggerFactory.getLogger(UploadFileParticularServlet.class.getName());
	private static  String PATH = "";
//	private static final String PATH = "C:/Users/Emanuel/wildfly-21.0.2.Final/standalone/deployments/";

	private static final String DOCTYPE = "doctype";
	private static final String UUID_VENTA = "uuidVenta";
	private static final String FILE_NAME = "fileName";
	
	
	@Inject
	TripleDesService tripleDesService;

	@Inject
	SettingsService settingsService;
	
	@Inject
	DocPartService docPartService;
	
	
	@Inject
	JwtService jwtService;
	
	@Inject
	AuthService authService;
	
	@Inject
	private VentasParticularService ventasParticularService; 
	
    @Inject
    private DownloadFileServlet downloadFileServlet;
    

    public UploadFileParticularServlet() {
        super();
    }


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doService(request, response);
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doService(request, response);
	}
	
	  @PostConstruct
		public void initializes()
		{

		  PATH = settingsService.loadUrlSaveDoc();
//		  PATH = "C:/Users/Emanuel/wildfly-21.0.2.Final/standalone/deployments/";
		}

	
	protected void doService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	    String TAG = "[UploadFileServlet - doService]";
	    LOGGER.info(TAG + "[ - init");
	    long currentSystemTime = System.currentTimeMillis();

	    HttpServletRequest httpRequest = (HttpServletRequest) request;

	    String jwt = httpRequest.getHeader("Authorization");
	    jwt = jwt != null ? jwt.substring(7) : ""; 
	    LOGGER.info(TAG + "[ - jwt:" + jwt);

	    InputStream file = null;
	    String doctype = null;
	    String filename = null;
	    String uuidVenta = null;
	    String filenameOriginal = null;

	    try {

	        AuthUserVo authUser = getSecurityIdUser(jwt);
	        TAG = "[UploadFileServlet - doService >> Username:" + authUser.getUsername() + "]";

	        if (!ServletFileUpload.isMultipartContent(request)) {
	            throw new ServletException("Content type is not multipart/form-data");
	        }

	        List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
	        LOGGER.debug("[UploadFileServlet - doService] - Load multipart file items size:{}...", items.size());

	        for (FileItem item : items) {
	            if (!item.isFormField()) { 
	                String fieldName = item.getFieldName();
	                file = item.getInputStream();
	                filename = System.currentTimeMillis() + ".pdf"; 
	                filenameOriginal = item.getName();
	                LOGGER.debug("[UploadFileServlet - doService] - Found file from frontend:{} with original filename:{}", fieldName, filenameOriginal);
	            } else {
	                String fieldName = item.getFieldName();
	                if (fieldName.equalsIgnoreCase(DOCTYPE)) {
	                	
	                    String docType = item.getString();
//	                    doctype = DocTypeEnum.valueOf(docType);
	                    doctype = docType;
	                    LOGGER.info("[UploadFileServlet - doService] - doctype:{}", doctype);
	                } else if (fieldName.equalsIgnoreCase(UUID_VENTA)) {
	                    uuidVenta = item.getString();
	                    LOGGER.info("[UploadFileServlet - doService] - uuidVenta:{}", uuidVenta);
	                }
	            }
	        }

	        if (filename == null) throw new IllegalArgumentException("filename cannot be null");
	        if (file == null) throw new IllegalArgumentException("file cannot be null");
	        
	        if (doctype == null) throw new IllegalArgumentException("doctype cannot be null");
	        
	        
	        	if (uuidVenta == null) throw new IllegalArgumentException("uuidVenta cannot be null");
			
	        
	        

	        String fullpath = PATH + filename;
	        LOGGER.debug("[UploadFileServlet - doService] - Save file:{} in location:{}", filename, fullpath);
	        saveFile(file, fullpath);

	        DocPartVo docPartVo = new DocPartVo();
	        docPartVo.setDocType(doctype);
	        docPartVo.setPath(fullpath);
	        docPartVo.setFilename(String.valueOf(System.currentTimeMillis())); 
	        docPartVo.setFilenameOriginal(filenameOriginal);
	        docPartVo.setFilepath(fullpath);
	        
//	        docPartVo.setUrl("/api/v1.0/DownloadFileServlet?file=" + filename);
	        String downloadUrl = downloadFileServlet.generateDownloadUrlPart(docPartVo);
	        docPartVo.setUrl(downloadUrl);

	        
	        docPartVo.setFechaCreacion(Calendar.getInstance());
	        docPartVo.setFechaModificacion(Calendar.getInstance());
	        docPartVo.setUser(authUser.getUsername());
	        
	        VentaPartVo ventaPartVo = null;
	        if (uuidVenta != null) {
	        	ventaPartVo = ventasParticularService.loadByuuid(uuidVenta);
			}
	        
	    
	        
	        if (ventaPartVo == null 	) {
	            throw new IllegalArgumentException("VentaEntity not found for UUID: " + uuidVenta);
	        }
	        if (ventaPartVo != null) {
	        	docPartVo.setIdVenta(ventaPartVo.getIdVenta());
			}
	       

	        docPartService.create(docPartVo);

	        response.setContentType("application/json");
	        response.setCharacterEncoding("UTF-8");
	        response.getWriter().write("{\"status\":\"success\"}");

	    } catch (IllegalArgumentException ex) {
	        LOGGER.error("[UploadFileServlet - doService] - IllegalArgumentException: ", ex);
	        response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
	    } catch (FileUploadException ex) {
	        LOGGER.error("[UploadFileServlet - doService] - FileUploadException: ", ex);
	        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error uploading file");
	    } catch (ServletException ex) {
	        LOGGER.error("[UploadFileServlet - doService] - ServletException: ", ex);
	        response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
	    } catch (IOException ex) {
	        LOGGER.error("[UploadFileServlet - doService] - IOException: ", ex);
	        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "I/O error occurred");
	    } catch (Exception ex) {
	        LOGGER.error("[UploadFileServlet - doService] - Exception: ", ex);
	        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred");
	    } finally {
	        LOGGER.debug("[UploadFileServlet - doService] - Finish Timing:{}", (System.currentTimeMillis() - currentSystemTime));
	    }
	}

	
	
	private AuthUserVo getSecurityIdUser(String token) throws Exception
	{

		if(token==null  || "".equalsIgnoreCase(token))
			throw new IllegalArgumentException(" - 'JWT' is null or empty");
	
		String guid = jwtService.validateToken(token);
		
		if (guid == null)
			throw new IllegalArgumentException(" - 'JWT' Session not found.");

		AuthUserVo user = authService.loadByUuid(guid);

		return user;
	}


	
	
	
	private void saveFile(InputStream uploadedInputStream, String serverLocation) throws IOException {
		
		OutputStream outpuStream=null;
		try {
			 outpuStream = new FileOutputStream(new File(serverLocation));
			 int read = 0;
			 byte[] bytes = new byte[1024];
			 outpuStream = new FileOutputStream(new File(serverLocation));
			 while ((read = uploadedInputStream.read(bytes)) != -1) {
				 outpuStream.write(bytes, 0, read);
			 }
			 outpuStream.flush();
			 outpuStream.close();

		} catch( IOException ex){
			throw ex;
		} finally{
			if(outpuStream!=null){
				outpuStream.close();
			}
		}
	}

}
