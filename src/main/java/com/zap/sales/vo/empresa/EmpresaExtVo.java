package com.zap.sales.vo.empresa;

import java.io.Serializable;
import java.util.List;

import com.zap.sales.vo.formacion.FormacionVo;
import com.zap.sales.vo.venta.VentaVo;

import lombok.Data;

@Data
public class EmpresaExtVo implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private EmpresaVo empresaVo; 
  
    private List<FormacionVo> formaciones;
     
    private List<VentaVo> ventas;
  

}
