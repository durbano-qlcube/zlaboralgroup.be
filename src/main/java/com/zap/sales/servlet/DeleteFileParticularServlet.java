package com.zap.sales.servlet;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zap.sales.service.DocPartService;
import com.zap.sales.service.DocService;
import com.zap.sales.vo.doc.DocPartVo;
import com.zap.sales.vo.doc.DocVo;

@WebServlet("/api/v1.0/DeleteDocumentPartServlet")
public class DeleteFileParticularServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteFileParticularServlet.class);

    @Inject
    private DocPartService docPartService;

    public DeleteFileParticularServlet() {
        super();
    }

 
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idDocStr = request.getParameter("idDoc");
        if (idDocStr == null || idDocStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "El parámetro idDoc falta");
            return;
        }

        DocPartVo docPartVo = null;
        boolean fileDeleted = false;

        try {
            Integer idDoc = Integer.parseInt(idDocStr);

            
            docPartVo = docPartService.load(idDoc);
            if (docPartVo == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Documento no encontrado con id: " + idDoc);
                return;
            }

          
            String filePath = docPartVo.getFilepath();
            if (filePath != null && !filePath.isEmpty()) {
                File file = new File(filePath);
                if (file.exists()) {
                    
                    fileDeleted = file.delete();
                    if (!fileDeleted) {
                        LOGGER.warn("No se pudo eliminar el archivo: {}", filePath);
                    } else {
                        LOGGER.debug("Archivo eliminado exitosamente: {}", filePath);
                    }
                } else {
                    LOGGER.warn("El archivo no existe: {}", filePath);
                }
            }

          
            if (fileDeleted) {
            	docPartService.delete(docPartVo.getIdDoc());
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"status\":\"success\"}");
            } else {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"status\":\"partial success\", \"message\":\"El archivo no fue eliminado\"}");
            }

        } catch (NumberFormatException e) {
            LOGGER.error("Formato de idDoc inválido", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Formato de idDoc inválido");
        } catch (Exception e) {
            LOGGER.error("Error al eliminar el documento", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
//    private void deleteFileAttachmentFromHD(String filePath) {
//        try {
//            File file = new File(filePath);
//            if (file.exists()) {
//                boolean deleted = file.delete();
//                if (deleted) {
//                    LOGGER.debug("File successfully deleted: {}", filePath);
//                } else {
//                    LOGGER.warn("Failed to delete file: {}", filePath);
//                }
//            } else {
//                LOGGER.warn("File does not exist: {}", filePath);
//            }
//        } catch (Exception ex) {
//            LOGGER.error("Error deleting file", ex);
//        }
//    }
}
