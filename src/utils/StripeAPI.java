package utils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.Token;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author khiar
 */
public class StripeAPI {
    private static final String SECRET_KEY = "sk_test_51Mh9npLUnQyq4G5F4WtnOzRSeSS5nbg5u33GcXHl3GxNuVMgmqzsamn3HQEAZFx0rVCIBRX4uicA6ppTHNVK7syY005hl9FGSX";

    static {
        Stripe.apiKey = SECRET_KEY;
    }
    
public static String createCustomerWithCard(String name,String email,String CardNumber, int expMonth, int expYear, String cvc) throws StripeException {
    
    // Create card object
    Map<String, Object> cardParams = new HashMap<>();
    cardParams.put("number", CardNumber);
    cardParams.put("exp_month", expMonth);
    cardParams.put("exp_year", expYear);
    cardParams.put("cvc", cvc);

    Map<String, Object> tokenParams = new HashMap<>();
    tokenParams.put("card", cardParams);

    Token token = Token.create(tokenParams);

    Map<String, Object> customerParams = new HashMap<>();
    customerParams.put("email", email);
    customerParams.put("name", name);
    customerParams.put("source", token.getId());

    Customer customer = Customer.create(customerParams);
    return customer.getId();
}
 
     public static void createCharge(String customerId, int amount, String currency, String email) throws StripeException {
    Map<String, Object> chargeParams = new HashMap<>();
    chargeParams.put("amount", amount);
    chargeParams.put("currency", currency);
    chargeParams.put("customer", customerId);
    chargeParams.put("receipt_email", email);

    Charge.create(chargeParams);
}
}
