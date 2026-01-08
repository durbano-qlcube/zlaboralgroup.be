package com.zap.sales;

import java.io.IOException;
import java.util.Map;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfFormField;
import com.itextpdf.text.pdf.PdfReader;

public class ReadAcroFields {
    public static void main(String[] args) {
        String pdfPath = "/opt/share/zap/encomienda.pdf";

        try {
        
             PdfReader reader = new PdfReader(pdfPath);
             AcroFields fields = reader.getAcroFields();

             Map<String, AcroFields.Item> formFields = fields.getFields();
             for (Map.Entry<String, AcroFields.Item> entry : formFields.entrySet())
             {
                 String fieldName = entry.getKey();
                 String fieldValue = fields.getField(fieldName);
                 System.out.println(fieldName + ": " + fieldValue);
             }
             
             reader.close();
               
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
