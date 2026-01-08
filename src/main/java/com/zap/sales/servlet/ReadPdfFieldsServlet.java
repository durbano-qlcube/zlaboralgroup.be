package com.zap.sales.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/api/v1.0/readPdfFields")
@MultipartConfig
public class ReadPdfFieldsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ReadPdfFieldsServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Part filePart = request.getPart("file");

        if (filePart == null || filePart.getSize() == 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "File is required");
            return;
        }

        try (InputStream fileInputStream = filePart.getInputStream()) {
            PdfReader reader = new PdfReader(fileInputStream);
            AcroFields fields = reader.getAcroFields();

            Map<String, AcroFields.Item> formFields = fields.getFields();
            StringBuilder result = new StringBuilder("List of fields in the PDF:\n");

            for (Map.Entry<String, AcroFields.Item> entry : formFields.entrySet()) {
                String fieldName = entry.getKey();
                String fieldValue = fields.getField(fieldName);
                result.append(fieldName).append(": ").append(fieldValue).append("\n");
            }

            reader.close();

            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(result.toString());
        } catch (Exception e) {
            LOGGER.error("Error processing PDF", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing PDF");
        }
    }
}
