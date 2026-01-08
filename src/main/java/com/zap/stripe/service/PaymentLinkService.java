package com.zap.stripe.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;

import com.google.api.client.util.Strings;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentLink;
import com.stripe.model.PaymentLinkCollection;
import com.stripe.param.PaymentLinkCreateParams;
import com.stripe.param.PaymentLinkListParams;
import com.stripe.param.PaymentLinkUpdateParams;
import com.zap.stripe.vo.PaymentLinkVo;

@Stateless
public class PaymentLinkService {
	

    private static final String STRIPE_KEY_PROD = "STRIPE_SECRET_KEY";
//	 private static final String STRIPE_KEY = "STRIPE_SECRET_KEY";

 
    
	@PostConstruct
	public void initializes()
	{
		Stripe.apiKey = STRIPE_KEY_PROD;
//		Stripe.apiKey = STRIPE_KEY;

	}
    
    
    
    public PaymentLinkVo createPaymentLink(String priceId, long quantity, String orderId) throws StripeException
    {
    	if (Strings.isNullOrEmpty(priceId))
            throw new IllegalArgumentException("Price ID no puede estar vacío");
        
    	if (quantity <= 0)
            throw new IllegalArgumentException("Quantity debe ser mayor que 0");
        
        if (Strings.isNullOrEmpty(orderId))
            throw new IllegalArgumentException("Order ID no puede estar vacío");
        
        
        PaymentLinkCreateParams.Builder paramsBuilder = PaymentLinkCreateParams.builder()
                .addLineItem(
                        PaymentLinkCreateParams.LineItem.builder()
                                .setPrice(priceId)
                                .setQuantity(quantity)
                                .build()
                );

        paramsBuilder.putMetadata("order_id", orderId);  

        PaymentLink paymentLink = PaymentLink.create(paramsBuilder.build());

        return toPaymentLinkVo(paymentLink);  
    }

    
    public PaymentLinkVo updatePaymentLink(String paymentLinkId, String orderId) throws StripeException
    {

    	if (Strings.isNullOrEmpty(paymentLinkId))
    		throw new IllegalArgumentException("paymentLinkId no puede estar vacío");

    	if (Strings.isNullOrEmpty(orderId))
    		throw new IllegalArgumentException("Order ID no puede estar vacío");


    	PaymentLink paymentLink = PaymentLink.retrieve(paymentLinkId);

    	PaymentLinkUpdateParams params = PaymentLinkUpdateParams.builder()
    			.putMetadata("order_id", orderId) 
    			.build();

    	PaymentLink updatedPaymentLink = paymentLink.update(params);

    	return toPaymentLinkVo(updatedPaymentLink);
    }
    
    public List<PaymentLinkVo> listPaymentLinks(long limit) throws StripeException {

        PaymentLinkListParams params = PaymentLinkListParams.builder()
                .setLimit(limit)
                .build();

        PaymentLinkCollection paymentLinks = PaymentLink.list(params);
        
        List<PaymentLinkVo> paymentLinkVos = new ArrayList<>();
        
        for (PaymentLink paymentLink : paymentLinks.getData()) {
            paymentLinkVos.add(toPaymentLinkVo(paymentLink));  
        }

        return paymentLinkVos;
    }

    private PaymentLinkVo toPaymentLinkVo(PaymentLink paymentLink) {
        PaymentLinkVo vo = new PaymentLinkVo();
        vo.setId(paymentLink.getId());
        vo.setUrl(paymentLink.getUrl());
        vo.setActive(paymentLink.getActive());
        return vo;
    }
 
}
