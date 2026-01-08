package com.zap.sales.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.zap.maintenance.service.crypto.TripleDesService;
import com.zap.maintenance.service.settings.SettingsService;
import com.zap.sales.vo.doc.DocPartVo;
import com.zap.sales.vo.doc.DocVo;


@WebServlet("/api/v1.0/DownloadFileServlet")
public class DownloadFileServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadFileServlet.class);
    private static String URL ="";


    @Inject
    SettingsService settingsService;

    @Inject
    TripleDesService tripleDesService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
    @PostConstruct
	public void initializes()
	{

    	URL = settingsService.loadUrl();
	}


    
    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        String TAG = "[DownloadFileServlet " + startTime + "] ";
        
        String token = request.getParameter("t");
        String filePath = request.getParameter("FILEPATH");
        String fileName = request.getParameter("FILENAME");

        try {
            if (token != null && !token.isEmpty()) {
                LOGGER.debug(TAG + "- DesEncrypting");
                String tokenDesEncrypted = tripleDesService.decoding3Des(token);
                LOGGER.debug(TAG + " - tokenDesEncrypted:" + tokenDesEncrypted);

                String[] tokenParts = tokenDesEncrypted.split(settingsService.getTokenizer());
                if (tokenParts.length != 4) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid token format");
                    return;
                }

                filePath = tokenParts[0];
                fileName = tokenParts[1];
            }

            if (filePath != null && fileName != null) {
                File file = new File(filePath);
                if (!file.exists()) {
                    LOGGER.error(TAG + "- File not found: " + filePath);
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
                    return;
                }

                if (token != null && !token.isEmpty()) {
                 
                    response.setContentType("application/pdf");
                    response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

                    try (InputStream fileInputStream = new FileInputStream(file);
                         OutputStream responseOutputStream = response.getOutputStream()) {
                        IOUtils.copy(fileInputStream, responseOutputStream);
                    } catch (IOException e) {
                        LOGGER.error(TAG + " - Error while reading file: " + e.getMessage());
                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while reading file");
                    }
                } else {
                  
                    DocVo fileVoZip = new DocVo();
                    fileVoZip.setFilepath(filePath);
                    fileVoZip.setFilename(fileName);
                    fileVoZip.setPath(filePath);
                    LOGGER.info(TAG + " - Generating Download URL for " + fileVoZip.getFilename() + " ...");

                    String url = generateDownloadUrl(fileVoZip);
                    Map<String, String> result = new HashMap<>();
                    result.put("url", url);
                    LOGGER.info(TAG + " - Download URL: " + url);

                 
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(result);

                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(jsonResponse);
                }
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing filePath or fileName parameters");
            }
        } catch (Exception e) {
            LOGGER.error(TAG + " - Error processing request: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request");
        } finally {
            LOGGER.info(TAG + " - Request processed in " + (System.currentTimeMillis() - startTime) + " ms");
        }
    }

    public String generateDownloadUrl(DocVo fileVo) throws Exception {
        String TAG = "[DownloadFileServlet - generateDownloadUrl]";
        LOGGER.info(TAG + " - input " + fileVo.toString());

        String TOKENIZER = settingsService.getTokenizer();
        StringBuilder token = new StringBuilder();
        token.append(fileVo.getFilepath()).append(TOKENIZER);
        token.append(fileVo.getFilename()).append(TOKENIZER);
        token.append(fileVo.getPath()).append(TOKENIZER);
        token.append(System.currentTimeMillis());

        LOGGER.info(TAG + " - Build Token: " + token.toString());

        LOGGER.info(TAG + " - Encrypting Token....");
        String encToken = tripleDesService.encoding3Des(token.toString());

        String encTokenStr = URLEncoder.encode(encToken, "UTF-8");
        LOGGER.info(TAG + " - Encoding to URL: " + encTokenStr);

        return URL+"/api/v1.0/DownloadFileServlet?t=" + encTokenStr;
//        return "http://localhost:1024/zapcube.be/api/v1.0/DownloadFileServlet?t=" + encTokenStr;
    }
    
    public String generateDownloadUrlPart (DocPartVo fileVo) throws Exception {
        String TAG = "[DownloadFileServlet - generateDownloadUrl]";
        LOGGER.info(TAG + " - input " + fileVo.toString());

        String TOKENIZER = settingsService.getTokenizer();
        StringBuilder token = new StringBuilder();
        token.append(fileVo.getFilepath()).append(TOKENIZER);
        token.append(fileVo.getFilename()).append(TOKENIZER);
        token.append(fileVo.getPath()).append(TOKENIZER);
        token.append(System.currentTimeMillis());

        LOGGER.info(TAG + " - Build Token: " + token.toString());

        LOGGER.info(TAG + " - Encrypting Token....");
        String encToken = tripleDesService.encoding3Des(token.toString());

        String encTokenStr = URLEncoder.encode(encToken, "UTF-8");
        LOGGER.info(TAG + " - Encoding to URL: " + encTokenStr);

        return URL+"api/v1.0/DownloadFileServlet?t=" + encTokenStr;
//        return "http://localhost:1024/zapcube.be/api/v1.0/DownloadFileServlet?t=" + encTokenStr;
    }
}