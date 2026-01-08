package com.zap.sales.vo.particular;

import java.io.Serializable;
import java.util.List;

import com.zap.sales.vo.doc.DocPartVo;

import lombok.Data;

@Data
public class VentaPartExtVo implements Serializable {

    private static final long serialVersionUID = 1L;

 
	private VentaPartVo venta;
    private PersonaVo persona;
    
    private VentaPartSearchResponseExtVo ventaPartSearchResponseExtVo;
    private List<DocPartVo> documentos;

    public VentaPartExtVo() {
        this.venta  = new VentaPartVo();
    }
    
}
