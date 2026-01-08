package com.zap.stripe.service;

import com.google.api.client.util.Strings;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.PriceCollection;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.PriceListParams;
import com.stripe.param.PriceUpdateParams;
import com.zap.stripe.vo.PriceVo;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;


@Stateless
public class PriceService {

    private static final String STRIPE_KEY_PROD = "STRIPE_SECRET_KEY";
//    private static final String STRIPE_KEY = "STRIPE_SECRET_KEY";

	@PostConstruct
	public void initializes()
	{
		Stripe.apiKey = STRIPE_KEY_PROD;
//		Stripe.apiKey = STRIPE_KEY;

	}

    public PriceVo createPrice(String productId, long unitAmount, String currency, String interval) throws StripeException
    {

    	if (Strings.isNullOrEmpty(productId))
            throw new IllegalArgumentException("productId no puede estar vacío");
        
    	if (unitAmount <= 0)
            throw new IllegalArgumentException("unitAmount debe ser mayor que 0");
        
    	if (Strings.isNullOrEmpty(currency))
            throw new IllegalArgumentException("currency no puede estar vacío");
        
    	if (Strings.isNullOrEmpty(interval))
            throw new IllegalArgumentException("Pinterval no puede estar vacío");
        
    	
    	
        PriceCreateParams params = PriceCreateParams.builder()
                .setCurrency(currency)
                .setUnitAmount(unitAmount)
//                .setRecurring(PriceCreateParams.Recurring.builder()
//                        .setInterval(PriceCreateParams.Recurring.Interval.valueOf(interval.toUpperCase()))
//                        .build())
                .setProductData(PriceCreateParams.ProductData.builder().setName(productId).build())
                .build();

        return ToPriceVo(Price.create(params));
    }

    public PriceVo updatePrice(String priceId, String orderId) throws StripeException {
        Price price = Price.retrieve(priceId);

        PriceUpdateParams params = PriceUpdateParams.builder()
                .putMetadata("order_id", orderId)
                .build();

        return ToPriceVo(price.update(params));
    }

    public PriceVo getPriceById(String priceId) throws StripeException {
        return ToPriceVo(Price.retrieve(priceId));
    }

    public List<PriceVo> listPrices(long limit) throws StripeException {

        PriceListParams params = PriceListParams.builder()
                .setLimit(limit)
                .build();

        PriceCollection priceCollection = Price.list(params);
        List<PriceVo> priceVos = new ArrayList<>();
        
        for (Price price : priceCollection.getData()) {
            priceVos.add(ToPriceVo(price));
        }

        return priceVos;
    }
    
    private PriceVo ToPriceVo(Price price) {
        PriceVo priceVo = new PriceVo();
        priceVo.setId(price.getId());
        priceVo.setProductId(price.getProduct());
        priceVo.setUnitAmount(price.getUnitAmount());
        priceVo.setCurrency(price.getCurrency());
        priceVo.setActive(price.getActive());
        priceVo.setInterval(price.getRecurring() != null ? price.getRecurring().getInterval().toString() : "");
        priceVo.setMetadata(price.getMetadata() != null ? price.getMetadata().toString() : "");
        return priceVo;
    }
}
