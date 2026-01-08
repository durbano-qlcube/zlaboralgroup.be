package com.zap.sales.vo.particular;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class VentaPartSearchResponseExtVo implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long PDTE_DOC=0l;
	private Long PDTE_PAGO=0l;
	private Long PDTE_FIRMA=0l;
	private Long PDTE_INICIO_CURSO=0l;
	private Long EJECUCION_CURSO=0l;
	private Long CURSO_FINALIZADO=0l;
	private Long CANCELADO=0l;
	private Long COBRADO=0l;
	private Long TOTAL_RECORDS;
	private Long TOTAL_PAGES; 
	private Double TOTAL_CHARGED =0.0; 
	private Double TOTAL_PENDING_CHARGED =0.0;
	    
	List<VentaPartSearchResponseVo> data; 

}
