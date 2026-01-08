package com.zap.scheduling.job;

import java.math.BigDecimal;
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
public class EmpresaCreditoUpdateJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZapCubeToGestDirectVenta.class);

    @Inject
    EmpresaService empresaService;

//    @Schedule(hour = "*", minute = "*/1", second = "0", persistent = false)
    @Schedule(dayOfMonth = "1", month = "1", hour = "0", minute = "0", second = "0", persistent = false)
    public void doExecute() {
        long t = System.currentTimeMillis();
        String TAG = "[ZapCubeToGestDirectVenta - " + t + " doExecute]";

        try {
            LOGGER.info(TAG + " Iniciando actualizacion de empresas con estado NUEVA.");

            actualizarCreditosDeEmpresas();

        } catch (Exception e) {
            LOGGER.error(TAG + " - Error al ejecutar el job: ", e);
        }
    }
    
    
    public void actualizarCreditosDeEmpresas() {
        String TAG = "[EmpresaCreditoUpdateJob - actualizarCreditosDeEmpresas]";

        try {
            List<EmpresaVo> empresas = empresaService.loadByEstado("NUEVA");

            LOGGER.info(TAG + " Numero de empresas con estado NUEVA: " + empresas.size());

            for (EmpresaVo empresaVo : empresas) {
            	empresaVo.setCreditosGastados(new BigDecimal(0));

            	 if (empresaVo.getCreditosDisponibles() == null || empresaVo.getBonificacion() == null 
                         || empresaVo.getCreditosDisponibles().compareTo(empresaVo.getBonificacion()) != 0) {
                     empresaVo.setCreditosDisponibles(empresaVo.getBonificacion());
                 }
            	 empresaVo.setEstado("CARTERA");
                empresaService.saveOrUpdate(empresaVo);
                LOGGER.info(TAG + " Empresa con ID " + empresaVo.getIdEmpresa() + " actualizada con CREDITOS_GASTADOS=0 y CREDITOS_DISPONIBLES igual a BONIFICACION");
            }

        } catch (Exception e) {
            LOGGER.error(TAG + " - Error al actualizar las empresas: ", e);
            e.printStackTrace();
        }
    }

    
}
