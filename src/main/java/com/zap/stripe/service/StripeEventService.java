package com.zap.stripe.service;

import com.zap.stripe.entity.StripeEventEntity;
import com.zap.stripe.vo.StripeEventVo;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import java.util.logging.Logger;



@Stateless
public class StripeEventService {

    private static final Logger LOGGER = Logger.getLogger(StripeEventService.class.getName());

    @PersistenceContext(unitName = "zapLaboralgrouPool")
    private EntityManager em;

    @Transactional
    public StripeEventVo create(StripeEventVo stripeEventVo) {
        String TAG = "[StripeEventService - create]";
        if (stripeEventVo == null) {
            throw new IllegalArgumentException(TAG + " >> 'stripeEventVo' cannot be null");
        }

        try {
            StripeEventEntity eventEntity = toStripeEventEntity(stripeEventVo);


            em.persist(eventEntity);
            em.flush(); 

            LOGGER.info(TAG + " Evento Stripe guardado exitosamente: " + eventEntity.getId());

            return toStripeEventVo(eventEntity);
        } catch (Exception ex) {
            LOGGER.severe(TAG + " - Error: " + ex.getMessage());
            throw new RuntimeException("Failed to save Stripe event", ex);
        }
    }

    
    private StripeEventEntity toStripeEventEntity(StripeEventVo vo) {
        StripeEventEntity entity = new StripeEventEntity();
        entity.setEventType(vo.getEventType());
        entity.setData(vo.getData());
        entity.setTimestamp(vo.getTimestamp());
        entity.setEventId(vo.getEventId());
        entity.setOrderId(vo.getOrderId());
        return entity;
    }

    private StripeEventVo toStripeEventVo(StripeEventEntity entity) {
        StripeEventVo vo = new StripeEventVo();
        vo.setEventType(entity.getEventType());
        vo.setData(entity.getData());
        vo.setTimestamp(entity.getTimestamp());
        vo.setEventId(entity.getEventId());
        vo.setOrderId(entity.getOrderId());
        return vo;
    }

}
