package com.zap.scheduling.job;

import java.util.Arrays;
import java.util.List;

import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zap.gestdirect.service.GestDirectService;
import com.zap.gestdirect.vo.GestdirectVentaVo;
import com.zap.sales.service.AlumnoService;
import com.zap.sales.service.EmpresaService;
import com.zap.sales.service.FormacionService;
import com.zap.sales.service.VentasService;
import com.zap.sales.vo.alumno.AlumnoVo;
import com.zap.sales.vo.empresa.EmpresaVo;
import com.zap.sales.vo.formacion.FormacionVo;
import com.zap.sales.vo.venta.StatusVentaEnum;
import com.zap.sales.vo.venta.VentaExtVo;
import com.zap.sales.vo.venta.VentaVo;

@Stateless
public class ZapCubeToGestDirectVenta {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZapCubeToGestDirectVenta.class);
	


	@Inject
	VentasService ventasService;

    @Inject
    GestDirectService gestDirectService;
    
    @Inject
    FormacionService formacionService;
    
    @Inject
    EmpresaService empresaService;
    
    @Inject
    AlumnoService alumnoService;
	
//	@Schedule(hour = "*", minute = "*/2", second = "0", persistent = false)
	public void doExecute()
	{

		long t= System.currentTimeMillis();
		String TAG = "[ZapCubeToGestDirectVenta - "+t+" doExecute]";

		try {

			List<VentaVo> ventas = ventasService.loadGestdirectByStatus(Arrays.asList(StatusVentaEnum.PDTE_DOC,
					StatusVentaEnum.PDTE_INICIO_CURSO, StatusVentaEnum.EJECUCION_CURSO,StatusVentaEnum.CURSO_FINALIZADO,
					StatusVentaEnum.NOTIFICADO_FUNDAE,StatusVentaEnum.CANCELADO));

			LOGGER.info(TAG+ "Número de ventas encontradas: " + ventas.size());

			for (VentaVo ventaVo : ventas)
			{
				FormacionVo formacionVo = formacionService.loadByIdVenta(ventaVo.getIdVenta());
				EmpresaVo empresaVo = empresaService.load(ventaVo.getIdEmpresa());
				List<AlumnoVo> alumnosVo = alumnoService.loadByIdFormacion(formacionVo.getIdFormacion());

				VentaExtVo ventaExtVo = new VentaExtVo();
				ventaExtVo.setVenta(ventaVo);
				ventaExtVo.setFormacion(formacionVo);
				ventaExtVo.setEmpresa(empresaVo);
				ventaExtVo.setAlumnos(alumnosVo);
				LOGGER.info(TAG + "Send Venta to GestDirect venta IdVenta: " + ventaVo.getIdVenta());
                
				GestdirectVentaVo response = gestDirectService.saveOrUpdateVenta(ventaExtVo);
				
				if (response != null) {
					 LOGGER.info(TAG + "Processed venta IdVentaGestdirect: " + response.getIdVenta());
		                
		                ventaVo.setIdVentaGestdirect(response.getIdVenta());
		                ventasService.update(ventaVo, false);
		                LOGGER.info(TAG + "Update Venta IdVenta: " + ventaVo.getIdVenta());
				}
               
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
