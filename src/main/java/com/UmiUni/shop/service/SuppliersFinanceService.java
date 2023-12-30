package com.UmiUni.shop.service;

import com.UmiUni.shop.model.FinancialReport;

import java.time.LocalDateTime;

public interface SuppliersFinanceService {
    FinancialReport generateDailyFinancialReport(Long supplierId, LocalDateTime startDateTime, LocalDateTime endDateTime);
}
