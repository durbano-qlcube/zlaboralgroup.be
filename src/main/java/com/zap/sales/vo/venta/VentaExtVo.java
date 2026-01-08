package com.zap.sales.vo.venta;

import java.io.Serializable;
import java.util.List;

import com.zap.sales.vo.alumno.AlumnoVo;
import com.zap.sales.vo.doc.DocVo;
import com.zap.sales.vo.empresa.EmpresaVo;
import com.zap.sales.vo.formacion.FormacionVo;

import lombok.Data;

@Data
public class VentaExtVo implements Serializable {

    private static final long serialVersionUID = 1L;

 
	private VentaVo venta;
    private EmpresaVo empresa;
    private FormacionVo formacion;
    private List<AlumnoVo> alumnos;
    private List<AlumnoVo> trabajadores;
    private VentaSearchResponseExtVo ventaSearchResponseExtVo;
    private List<DocVo> documentos;

    public VentaExtVo() {
        this.venta = new VentaVo();
    }
    
}
