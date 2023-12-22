package com.UmiUni.shop.service;

import com.UmiUni.shop.model.DailyReport;
import com.UmiUni.shop.model.PaypalTransactionRecord;
import com.UmiUni.shop.model.ReconcileOrderAndPayment;
import com.UmiUni.shop.model.ReconcileResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ReconciliationService {

    public ReconcileOrderAndPayment reconcilePaymentViaSalesOrderSn(String salesOrderSn);

    List<ReconcileOrderAndPayment> reconcilePastDays(int days);

    List<ReconcileOrderAndPayment> reconcileBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

    Map<LocalDate, DailyReport> generateMonthlySalesReport(LocalDateTime startDate, LocalDateTime endDate, String type);

    List<ReconcileResult> readTransactions(MultipartFile file);
}
