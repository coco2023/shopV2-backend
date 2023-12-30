package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.constant.OrderStatus;
import com.UmiUni.shop.entity.PayPalPayment;
import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.entity.SupplierFinance;
import com.UmiUni.shop.model.FinancialReport;
import com.UmiUni.shop.repository.PayPalPaymentRepository;
import com.UmiUni.shop.repository.SalesOrderRepository;
import com.UmiUni.shop.repository.SupplierFinanceRepository;
import com.UmiUni.shop.service.SuppliersFinanceService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.beans.BeanUtils.copyProperties;

@Service
@Log4j2
public class SuppliersFinanceServiceImpl implements SuppliersFinanceService {

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private PayPalPaymentRepository payPalPaymentRepository;

    @Autowired
    private SupplierFinanceRepository supplierFinanceRepository;

    @Override
    public FinancialReport generateDailyFinancialReport(Long supplierId, LocalDateTime startDate, LocalDateTime endDate) {
//        log.info("start date {} ; end date {}", startDate, endDate);
        // get all the salesOrder between these days
        List<SalesOrder> salesOrderList = salesOrderRepository.findBySupplierIdAndOrderDateBetweenAndOrderStatus(supplierId, startDate, endDate, OrderStatus.PROCESSING);
//        log.info("salesOrderList: " + salesOrderList);


        // find the finance report the last day before today
        SupplierFinance supplierFinance = supplierFinanceRepository.findByReportDate(LocalDate.from(startDate.minusDays(1)));
        if (supplierFinance == null) {
            FinancialReport financialReport = new FinancialReport();
            supplierFinance = SupplierFinance.builder()
                    .reportDate(LocalDate.from(startDate.minusDays(1)))
                    .supplierId(supplierId)
                    .openingBalance(Double.valueOf(0))
                    .closingBalance(Double.valueOf(0))
                    .build();
            supplierFinanceRepository.save(supplierFinance);
            copyProperties(supplierFinance,financialReport);
//            return financialReport;
        }
        // generate today's report
        FinancialReport financialReport = generateFinancialReport(salesOrderList, supplierId, supplierFinance.getClosingBalance(), LocalDate.from(startDate));

        // find if today's report exit in db  //only save the daily report
        SupplierFinance supplierFinanceThisDay = supplierFinanceRepository.findByReportDate(LocalDate.from(startDate));
        // save this day's finance report if it is not exit.
        if (supplierFinanceThisDay == null) {
            saveThisDaySupplierFinance(financialReport);
        }

        return financialReport;
    }

    private void saveThisDaySupplierFinance(FinancialReport financialReport) {
        SupplierFinance supplierFinance = SupplierFinance.builder()
                .reportDate(financialReport.getReportDate())
                .supplierId(financialReport.getSupplierId())
                .paymentsNumReceived(financialReport.getPaymentsNumReceived())
                .totalAmountReceived(financialReport.getTotalAmountReceived())
                .totalTax(financialReport.getTotalTax())
                .totalServiceFee(financialReport.getTotalServiceFee())
                .otherFees(financialReport.getOtherFees())
                .totalNetAmount(financialReport.getTotalNetAmount())
                .accountsReceivable(financialReport.getAccountsReceivable())
                .actualReceipts(financialReport.getActualReceipts())
                .outstandingAccounts(financialReport.getOutstandingAccounts())
                .openingBalance(financialReport.getOpeningBalance())
                .closingBalance(financialReport.getClosingBalance())
                .build();

        supplierFinanceRepository.save(supplierFinance);
    }

    private FinancialReport generateFinancialReport(List<SalesOrder> salesOrderList, Long supplierId, Double closingBalance, LocalDate reportDate) {

        // init Finance report
        FinancialReport financialReport = new FinancialReport();
        financialReport.setReportDate(reportDate);
        financialReport.setSupplierId(supplierId);

        // get supplier balance
        List<String> salesOrderSnList = salesOrderList.stream().map(SalesOrder::getSalesOrderSn).collect(Collectors.toList());

        Double addToClosingBalance = Double.valueOf(0);

        // get paypal-payment record
        for (String salesOrderSn : salesOrderSnList) {
            PayPalPayment payment = payPalPaymentRepository.findBySalesOrderSn(salesOrderSn).orElse(null);
//                    .orElseThrow(() -> new RuntimeException("no Payment exit for salesOrderSn: " + salesOrderSn));

            if (payment == null) {
                continue;
            }

            financialReport.setPaymentsNumReceived(financialReport.getPaymentsNumReceived() + 1);
            financialReport.setTotalTax(financialReport.getTotalTax() + payment.getTax());
            financialReport.setTotalServiceFee(financialReport.getTotalServiceFee() + payment.getPayPalFee());
            financialReport.setOtherFees(financialReport.getOtherFees());
            financialReport.setTotalNetAmount(financialReport.getTotalNetAmount() + payment.getNet());
            financialReport.setTotalAmountReceived(financialReport.getTotalAmountReceived() + payment.getNet());

            // set as totalAmountReceived
            financialReport.setAccountsReceivable(financialReport.getTotalAmountReceived());
            financialReport.setActualReceipts(financialReport.getTotalAmountReceived());
            financialReport.setOutstandingAccounts(Double.valueOf(0));

            // how to get it?
            addToClosingBalance += payment.getNet();
        }

        // open close
        financialReport.setOpeningBalance(closingBalance);
        financialReport.setClosingBalance(financialReport.getOpeningBalance() + addToClosingBalance);

        return financialReport;
    }
}
