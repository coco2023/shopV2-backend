package com.UmiUni.shop.service;

public interface ReconciliationService {

    public String reconcilePaymentViaSalesOrderSn(String salesOrderSn);

    String reconcilePastDays(int days);
}
