package com.zap.stripe.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.PriceCollection;
import com.stripe.model.Product;
import com.stripe.model.ProductCollection;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.PriceListParams;
import com.stripe.param.PriceUpdateParams;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.ProductListParams;
import com.stripe.param.ProductUpdateParams;
import com.zap.stripe.exception.ProductStripeException;
import com.zap.stripe.vo.ProductVo;

@Stateless
public class ProductService {

	private static final String STRIPE_KEY_PROD = "STRIPE_SECRET_KEY";
//    private static final String STRIPE_KEY = "STRIPE_SECRET_KEY";

	
	@PostConstruct
	public void initializes()
	{
		Stripe.apiKey = STRIPE_KEY_PROD;
//		Stripe.apiKey = STRIPE_KEY;

	}
	
	
	public ProductVo createProduct(ProductVo productVo) throws ProductStripeException {

		ProductCreateParams params = ProductCreateParams.builder().setName(productVo.getName())
				.setDescription(productVo.getDescription()).build();

		try {
			Product product = Product.create(params);

			PriceCreateParams priceParams = PriceCreateParams.builder().setUnitAmount(productVo.getPrice())
					.setCurrency("eur").setProduct(product.getId()).build();

			Price price = Price.create(priceParams);

			productVo.setId(product.getId());
			productVo.setPrice(price.getUnitAmount());

			return productVo;
		} catch (Exception e) {
			throw new ProductStripeException("Error al crear producto en Stripe", e);
		}
	}

	public ProductVo updateProduct(String productId, ProductVo productVo) throws ProductStripeException {

		try {
			Product product = Product.retrieve(productId);

			ProductUpdateParams productUpdateParams = ProductUpdateParams.builder().setName(productVo.getName())
					.setDescription(productVo.getDescription()).putMetadata("metadata", "new_metadata_value").build();

			Product updatedProduct = product.update(productUpdateParams);

			if (productVo.getPrice() > 0) {
				PriceCreateParams priceCreateParams = PriceCreateParams.builder().setUnitAmount(productVo.getPrice())
						.setCurrency("eur").setProduct(updatedProduct.getId()).build();

				Price newPrice = Price.create(priceCreateParams);
			}

			return toProductVo(updatedProduct);
		} catch (Exception e) {
			throw new ProductStripeException("Error al actualizar el producto", e);
		}
	}

	public ProductVo getProduct(String productId) throws ProductStripeException {

		try {
			Product product = Product.retrieve(productId);

			return toProductVo(product);
		} catch (Exception e) {
			throw new ProductStripeException("Error al obtener el producto de Stripe", e);
		}
	}

	public List<ProductVo> getProducts(Long limit) throws ProductStripeException {

		try {
			ProductListParams params = ProductListParams.builder().setLimit(limit).build();

			ProductCollection products = Product.list(params);

			List<ProductVo> productVos = new ArrayList<>();
			for (Product product : products.getData()) {
				productVos.add(toProductVo(product));
			}

			return productVos;
		} catch (Exception e) {
			throw new ProductStripeException("Error al obtener los productos de Stripe", e);
		}
	}

	public boolean deleteProduct(String productId) throws ProductStripeException {

		try {
			Product product = Product.retrieve(productId);

			PriceCollection prices = Price.list(PriceListParams.builder().setProduct(productId).build());

			for (Price price : prices.getData()) {
				PriceUpdateParams priceUpdateParams = PriceUpdateParams.builder().setActive(false).build();
				price.update(priceUpdateParams);
			}

			product.delete();

			return true;
		} catch (Exception e) {
			throw new ProductStripeException("Error al eliminar el producto de Stripe: " + e.getMessage(), e);
		}
	}

	private ProductVo toProductVo(Product product) {
		ProductVo productVo = new ProductVo();
		productVo.setId(product.getId());
		productVo.setName(product.getName());
		productVo.setDescription(product.getDescription());
		productVo.setCreated(product.getCreated());
		productVo.setActive(product.getActive());
		productVo.setLivemode(product.getLivemode());
		productVo.setMetadata(product.getMetadata());

		try {
			PriceCollection prices = Price.list(PriceListParams.builder().setProduct(product.getId()).build());

			if (!prices.getData().isEmpty()) {
				Price price = prices.getData().get(0);
				productVo.setPrice(price.getUnitAmount());
			} else {
				productVo.setPrice(0);
			}

		} catch (StripeException e) {
			productVo.setPrice(0);
		}

		return productVo;
	}

}
