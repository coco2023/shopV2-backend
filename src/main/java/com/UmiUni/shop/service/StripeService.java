package com.UmiUni.shop.service;

import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.model.PaymentResponse;

public interface StripeService {
    public PaymentResponse createCharge(SalesOrder salesOrder, String token);

}
