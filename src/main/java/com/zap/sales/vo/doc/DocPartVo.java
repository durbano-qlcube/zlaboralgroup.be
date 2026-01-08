package com.zap.sales.vo.doc;

import java.io.Serializable;
import java.util.Calendar;

import lombok.Data;

@Data
public class DocPartVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idDoc;
    private String docType;
    private String path;
    private String  filename;
    private String filenameOriginal;
    private String filepath;
    private String url;
    private Calendar fechaCreacion;
    private Calendar fechaModificacion;
    private String user;
    private Integer idVenta;

}
