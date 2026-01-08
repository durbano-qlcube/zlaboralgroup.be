package com.zap.lleida.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.zap.lleida.vo.SmsRequestVo;
import com.zap.lleida.vo.SmsResponseVo;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

public class SmsService {

    private static final String USERNAME = "zapikey";
    private final static String URL = "https://api.clickandsign.eu/sms/v2/";
    private static final Logger LOGGER = LoggerFactory.getLogger(SmsService.class.getName());

    public SmsResponseVo sendSms(SmsRequestVo smsRequest) {
        long t = System.currentTimeMillis();
        String tag = "[SmsService " + t + "]";
        SmsResponseVo response = new SmsResponseVo();

        if (smsRequest.getSms().getDst().getNum() == null || smsRequest.getSms().getDst().getNum().length == 0)
            throw new IllegalArgumentException(tag + " - 'recipient' can not be null or empty");

        if (smsRequest.getSms().getTxt() == null || smsRequest.getSms().getTxt().isEmpty())
            throw new IllegalArgumentException(tag + " - 'message' can not be null or empty");

        StringBuffer jsonBuffer = new StringBuffer();
        jsonBuffer.append("{");
        jsonBuffer.append("\"sms\": {");
        jsonBuffer.append("\"user\":\"").append(USERNAME).append("\",");
        jsonBuffer.append("\"dst\": {");
        jsonBuffer.append("\"num\":[").append("\"").append(smsRequest.getSms().getDst().getNum()[0]).append("\"");
        jsonBuffer.append("]},");
        jsonBuffer.append("\"txt\":\"").append(smsRequest.getSms().getTxt()).append("\"");
        jsonBuffer.append("}");
        jsonBuffer.append("}");

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jsonBuffer.toString());

        Request request = new Request.Builder()
                .url(URL)
                .addHeader("Authorization", "x-api-key jUa8Y6WdKbLptDJ98f7kRaF0ikemky3f")
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        try (Response httpResponse = client.newCall(request).execute()) {
            Gson gson = new Gson();
            String responseBody = httpResponse.body().string();
            int httpCode = httpResponse.code();

            if (httpCode == HttpURLConnection.HTTP_OK || httpCode == HttpURLConnection.HTTP_CREATED) {
                if (responseBody.trim().startsWith("<")) {
                    // Es XML → parse manual
                    Document doc = DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder()
                            .parse(new ByteArrayInputStream(responseBody.getBytes("UTF-8")));

                    String status = doc.getElementsByTagName("status").item(0).getTextContent();
                    String code = doc.getElementsByTagName("code").item(0).getTextContent();

                    response.setStatus(status);
                    response.setCode(code);
                    response.setDescription("Respuesta XML procesada correctamente");

                    System.out.println("Respuesta XML: status = " + status + ", code = " + code);
                } else {
                    // JSON válido
                    SmsResponseVo parsed = gson.fromJson(responseBody, SmsResponseVo.class);
                    response.setStatus(parsed.getStatus());
                    response.setDescription(parsed.getDescription());
                    response.setMessageId(parsed.getMessageId());
                    response.setCode(parsed.getCode());

                    System.out.println("Respuesta JSON: " + responseBody);
                }

            } else {
                LOGGER.error("{} - response {} ", tag, responseBody);
                throw new Exception("Unexpected response: " + responseBody);
            }

        } catch (Exception e) {
            LOGGER.error(tag + " >> Exception while sending SMS: ", e);
            response.setStatus("ERROR");
            response.setDescription(e.getMessage());
        }

        return response;
    }
}
