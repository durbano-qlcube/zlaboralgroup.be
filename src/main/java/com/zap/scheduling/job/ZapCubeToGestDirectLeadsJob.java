package com.zap.scheduling.job;

import java.util.Arrays;
import java.util.List;

import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zap.acquisition.service.AcquisitionService;
import com.zap.acquisition.vo.AcquisitionVo;
import com.zap.acquisition.vo.StatusAcquisitionEnum;
import com.zap.gestdirect.service.GestDirectService;
import com.zap.gestdirect.vo.ResponseGestdirectVo;
import com.zap.sales.service.EmpresaService;

@Stateless
public class ZapCubeToGestDirectLeadsJob {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZapCubeToGestDirectLeadsJob.class);
	

    
    @Inject
    AcquisitionService acquisitionService;
    
    @Inject
    EmpresaService empresaService;
     
    
    @Inject
    GestDirectService gestDirectService;
    
    
//     @Schedule(hour = "*", minute = "*/2", second = "0", persistent = false)
    public void doExecute()
    {
    	long t= System.currentTimeMillis();
		String TAG = "[ZapCubeToGestDirectLeadsJob - "+t+" doExecute]";
		
        try {
            List<StatusAcquisitionEnum> estados = Arrays.asList(StatusAcquisitionEnum.CODIFICADO, StatusAcquisitionEnum.ABIERTO);

  
            List<AcquisitionVo> entitiesInTramite = acquisitionService.loadGestdirectByStatus(estados);
            LOGGER.info(TAG+ "loaded {} Acquisition with origin GestDirect... ",entitiesInTramite.size());
            
            for (AcquisitionVo src : entitiesInTramite)
            {
               // LOGGER.info(TAG + "Procesando entidad con ORIGIN_ID_ADQUISITION: " + src.getOriginIdAdquision());
                ResponseGestdirectVo response = gestDirectService.updateAdquisition(src);
                LOGGER.info(TAG + "Processed entidad con ID_ADQUISITION: {}", src.getIdAcquisition());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    


}
