package com.zap.scheduling.job;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zap.sales.service.VentasService;
import com.zap.sales.vo.empresa.EmpresaVo;
import com.zap.sales.vo.venta.VentaExtVo;
import com.zap.sales.vo.venta.VentaVo;
import com.zap.scheduling.vo.QlCubeOrgVentaResult;
import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;

@Singleton
public class OrgVentaToMongoJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrgVentaToMongoJob.class);

    @Inject
    private Datastore datastore; 
    
    @Inject
    private VentasService ventasService; 


    
//    @Schedule(hour = "*", minute = "0", second = "0", persistent = false)
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public void process() {
        try {
            LOGGER.info("Job iniciado: OrgVentaToMongoJob");

            List<VentaExtVo> resultados = ventasService.loadAllExt();             
            LOGGER.info("Se encontraron {} ventas en la tabla ORG_VENTA", resultados.size());

            for (VentaExtVo ventaExtVo : resultados) {
                QlCubeOrgVentaResult result = mapVentaExtVoToQlCubeOrgVentaResult(ventaExtVo);
                
                if (isVentaExistente(result.getIdVenta())) {
                    LOGGER.info("La venta con idVenta {} ya existe en MongoDB, se omitirá.", result.getIdVenta());
                } else {
                    datastore.save(result);
                    LOGGER.info("Venta con idVenta {} guardada en MongoDB.", result.getIdVenta());
                }
            }


            LOGGER.info("Job completado exitosamente.");
        } catch (Exception e) {
            LOGGER.error("Error durante la ejecucion de OrgVentaToMongoJob: {}", e.getMessage(), e);
        }
    }

    private boolean isVentaExistente(Integer idVenta) {
        return datastore.find(QlCubeOrgVentaResult.class)
                .filter(Filters.eq("idVenta", idVenta)) 
                .count() > 0;
    }

    private QlCubeOrgVentaResult mapVentaExtVoToQlCubeOrgVentaResult(VentaExtVo ventaExtVo) {
        VentaVo ventaVo = ventaExtVo.getVenta();
        EmpresaVo empresaVo = ventaExtVo.getEmpresa();

        QlCubeOrgVentaResult result = new QlCubeOrgVentaResult();

        result.setIdVenta(ventaVo.getIdVenta());
        result.setUuIdVenta(ventaVo.getUuId());
        result.setFxCreation(ventaVo.getFxCreation() != null ? ventaVo.getFxCreation().getTime() : null);
        result.setFxModification(ventaVo.getFxModification() != null ? ventaVo.getFxModification().getTime() : null);
        result.setFxVenta(ventaVo.getFxVenta() != null ? ventaVo.getFxVenta().getTime() : null);
        result.setPrice(ventaVo.getPrice());
        result.setPriceDeductedExpenses(ventaVo.getPriceDeductedExpenses());
        result.setPriceWithIva(ventaVo.getPriceWithIva());
        result.setIva(ventaVo.getIva());
        result.setCommission(ventaVo.getCommission());
        result.setCharged(ventaVo.getCharged());
        result.setPdteFirma(ventaVo.getPdteFirma());
        result.setPdteCobroInicioCurso(ventaVo.getPdteCobroInicioCurso());
        result.setPdteCobroFinCurso(ventaVo.getPdteCobroFinCurso());
        result.setObservaciones(ventaVo.getObservaciones());
        result.setStripePaymentLink(ventaVo.getStripePaymentLink());
        result.setStripePaymentId(ventaVo.getStripePaymentId());
        result.setStripePaymentStatus(ventaVo.getStripePaymentStatus());
        result.setStripeCustomerId(ventaVo.getStripeCustomerId());
        result.setStripeProductId(ventaVo.getStripeProductId());
        result.setStripePrecioId(ventaVo.getStripePrecioId());
        result.setVentaSegment(ventaVo.getVentaSegment());
        result.setStripeUuidOrderId(ventaVo.getStripeUuidOrderId());
        result.setPercentageToPay(ventaVo.getPercentageToPay());

        if (empresaVo != null) {
            result.setIdEmpresa(empresaVo.getIdEmpresa());
            result.setRazonSocial(empresaVo.getRazonSocial());
            result.setCif(empresaVo.getCif());
            result.setPhoneVenta(empresaVo.getTelefonoContacto());

        }

        return result;
    }

    
    

}
