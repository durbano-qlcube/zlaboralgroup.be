package com.zap.stripe.entity;

import javax.persistence.*;

import com.zap.acquisition.entity.AcquisitionEntity;
import com.zap.acquisition.vo.StatusAcquisitionEnum;
import com.zap.stripe.vo.StatusStripeEnum;

import lombok.Data;

import java.util.Calendar;

@Entity
@Table(name = "STRIPE_EVENT")

@Data
public class StripeEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "timestamp", nullable = false)
    private Calendar timestamp;

    @Column(name = "data", columnDefinition = "TEXT")
    private String data;

//    @Column(name = "status", nullable = false)
//    private String status;
    
    @javax.persistence.Column(name = "status", insertable = true, updatable = true, length = 25, columnDefinition = "VARCHAR(25)")
    @javax.persistence.Enumerated(javax.persistence.EnumType.STRING)
    private StatusStripeEnum status;

    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;
}
