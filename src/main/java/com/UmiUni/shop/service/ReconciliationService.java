package com.UmiUni.shop.service;

import com.UmiUni.shop.model.PaypalTransactionRecord;
import com.UmiUni.shop.model.ReconcileResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

public interface ReconciliationService {

    public String reconcilePaymentViaSalesOrderSn(String salesOrderSn);

    String reconcilePastDays(int days);

    String reconcileBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

    File generateMonthlySalesReport(LocalDateTime startDate, LocalDateTime endDate, String type);

    List<ReconcileResult> readTransactions(MultipartFile file);
}
