package com.zap.gestdirect.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class GestdirectVentaExtVo implements Serializable {

    private static final long serialVersionUID = 1L;

 
	private GestdirectVentaVo venta;
    private GestdirectEmpresaVo empresa;
    private GestdirectFormacionVo formacion;
    private List<GestdirectAlumnoVo> alumnos;
    
}
