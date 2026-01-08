package com.zap.stripe.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.CustomerCollection;
import com.stripe.model.CustomerSearchResult;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerListParams;
import com.stripe.param.CustomerSearchParams;
import com.stripe.param.CustomerUpdateParams;
import com.zap.stripe.vo.CustomerVo;
import com.zap.stripe.vo.InvoiceSettingsVo;
import com.zap.stripe.exception.CustomStripeException;

@Stateless
public class CustomerService {

    private static final String STRIPE_KEY_PROD = "STRIPE_SECRET_KEY"; 
//	 private static final String STRIPE_KEY = "STRIPE_SECRET_KEY";
    
	@PostConstruct
	public void initializes()
	{
		Stripe.apiKey = STRIPE_KEY_PROD;
//		Stripe.apiKey = STRIPE_KEY;
	}
	
	
    public CustomerVo getCustomerById(String customerId) throws CustomStripeException {

        try {
            Customer customer = Customer.retrieve(customerId);

            return ToCustomerVo(customer);
        } catch (StripeException e) {
            throw new CustomStripeException("Error al recuperar el cliente desde Stripe", e);
        }
    }

    public CustomerVo createCustomer(CustomerVo customerVo) throws CustomStripeException {
    	
    	
        CustomerCreateParams params = CustomerCreateParams.builder()
                .setName(customerVo.getName())
                .setEmail(customerVo.getEmail())
                .setDescription(customerVo.getDescription())
                .build();

        try {
            Customer customer = Customer.create(params);

            return ToCustomerVo(customer);
        } catch (StripeException e) {
            throw new CustomStripeException("Error al crear cliente en Stripe", e);
        }
    }
    
    
    public CustomerVo updateCustomer(String customerId, CustomerVo customerVo) throws StripeException {

        Customer customer = Customer.retrieve(customerId);

        CustomerUpdateParams params = CustomerUpdateParams.builder()
                .setEmail(customerVo.getEmail()) 
                .setName(customerVo.getName())
                .build();

        customer = customer.update(params);
        return ToCustomerVo(customer);
    }
    
    
    public List<CustomerVo> listCustomers(int limit) throws StripeException {

        CustomerListParams params = CustomerListParams.builder()
                .setLimit((long) limit)
                .build();

        CustomerCollection customers = Customer.list(params);

        List<CustomerVo> customerVoList = new ArrayList<>();
        for (Customer customer : customers.getData()) {
        	customerVoList.add(ToCustomerVo(customer));
        }

        return customerVoList;
    }
    
    public boolean deleteCustomer(String customerId) throws CustomStripeException {

        try {
            Customer customer = Customer.retrieve(customerId);
            customer.delete();
            return true; 
        } catch (StripeException e) {
            throw new CustomStripeException("Error al eliminar el cliente desde Stripe: " + e.getMessage(), e);
        }
    }

    
    public List<CustomerVo> searchCustomers(String query) throws CustomStripeException {

        try {
            CustomerSearchParams params = CustomerSearchParams.builder()
                    .setQuery(query) 
                    .build();

            CustomerSearchResult result = Customer.search(params);

            List<CustomerVo> customers = new ArrayList<>();
            for (Customer customer : result.getData()) {
            	customers.add(ToCustomerVo(customer));
            }

            return customers;
        } catch (StripeException e) {
            throw new CustomStripeException("Error al realizar la busqueda de clientes en Stripe", e);
        }
    }
    
    
    private CustomerVo ToCustomerVo(Customer customer) {
        CustomerVo customerVo = new CustomerVo();
        customerVo.setId(customer.getId());
        customerVo.setObject(customer.getObject());
        customerVo.setEmail(customer.getEmail());
        customerVo.setName(customer.getName());
        customerVo.setDescription(customer.getDescription());
        customerVo.setCreated(customer.getCreated());
        customerVo.setDelinquent(customer.getDelinquent());
        customerVo.setInvoicePrefix(customer.getInvoicePrefix());
        customerVo.setLivemode(customer.getLivemode());
        customerVo.setTaxExempt(customer.getTaxExempt());

        InvoiceSettingsVo invoiceSettings = new InvoiceSettingsVo();
        invoiceSettings.setFooter(customer.getInvoiceSettings().getFooter());
        customerVo.setInvoiceSettings(invoiceSettings);

        return customerVo;
    }

}