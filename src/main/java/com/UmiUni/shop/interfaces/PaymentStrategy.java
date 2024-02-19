package com.UmiUni.shop.interfaces;

import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.model.PaymentResponse;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

public interface PaymentStrategy {

    PaymentResponse createPayPal(SalesOrder salesOrder, APIContext apiContext) throws PayPalRESTException;

}
