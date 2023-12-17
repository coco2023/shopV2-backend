package com.UmiUni.shop.service;

import java.io.File;
import java.time.LocalDateTime;

public interface ReconciliationService {

    public String reconcilePaymentViaSalesOrderSn(String salesOrderSn);

    String reconcilePastDays(int days);

    String reconcileBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

    File generateMonthlySalesReport(LocalDateTime startDate, LocalDateTime endDate);
}
