package com.zap.sales.vo.venta;

import java.io.Serializable;
import java.util.List;

import com.zap.sales.vo.alumno.AlumnoVo;
import com.zap.sales.vo.doc.DocVo;
import com.zap.sales.vo.empresa.EmpresaVo;
import com.zap.sales.vo.formacion.FormacionVo;

import lombok.Data;

@Data
public class VentaParticularesExtVo implements Serializable {

    private static final long serialVersionUID = 1L;

 
	private VentaVo venta;
    private EmpresaVo empresa;

    public VentaParticularesExtVo() {
        this.venta = new VentaVo();
    }
    
}
