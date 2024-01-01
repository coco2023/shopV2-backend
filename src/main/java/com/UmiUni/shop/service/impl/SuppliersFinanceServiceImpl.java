package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.constant.OrderStatus;
import com.UmiUni.shop.constant.ReportType;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.ToDoubleFunction;
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

    /**
     * this method aims to generate daily, monthly, yearly Financial Report for supplier
     * @param valueList
     * @param supplierId
     * @param startDate
     * @param reportDateString
     * @param reportType
     * @param getTaxFunction
     * @param getServiceFeeFunction
     * @param getNetAmountFunction
     * @return
     * @param <T>
     */
    private <T> FinancialReport commonGenerateReport(
            List<T> valueList,
            Long supplierId,
            LocalDateTime startDate,
            String reportDateString,
            ReportType reportType,
            ToDoubleFunction<T> getTaxFunction,
            ToDoubleFunction<T> getServiceFeeFunction,
            ToDoubleFunction<T> getNetAmountFunction
            ) {

        // get the prev closing balance info
        SupplierFinance prevSupplierFinance = supplierFinanceRepository.findBySupplierIdAndReportDate(supplierId, String.valueOf(LocalDate.from(startDate.minusDays(1))))
                .orElseGet(() -> {
                    SupplierFinance newSupplierFinance = new SupplierFinance();
                    newSupplierFinance.setReportDate(String.valueOf(LocalDate.from(startDate)));
                    newSupplierFinance.setReportType(ReportType.DAILY);
                    return newSupplierFinance;
                });
        Double prevClosingBalance = prevSupplierFinance.getClosingBalance();

        // init supplierFinance and FinancialReport
        Double addToClosingBalance = Double.valueOf(0);
        int paymentsNumReceived = 0;
        Double totalAmountReceived = Double.valueOf(0);
        Double totalNetAmount = Double.valueOf(0);
        Double totalTax = Double.valueOf(0);
        Double totalServiceFee = Double.valueOf(0);

        for (T item : valueList) {
            paymentsNumReceived++;
            totalTax += getTaxFunction.applyAsDouble(item);
            totalServiceFee += getServiceFeeFunction.applyAsDouble(item);
            totalNetAmount += getNetAmountFunction.applyAsDouble(item);
            totalAmountReceived += getNetAmountFunction.applyAsDouble(item);
            addToClosingBalance += getNetAmountFunction.applyAsDouble(item);
        }
        Double otherFees = Double.valueOf(0);

        // set as totalAmountReceived
        Double accountsReceivable = totalAmountReceived;
        Double actualReceipts = totalAmountReceived;
        Double outstandingAccounts = Double.valueOf(0);

        Double openingBalance = prevClosingBalance;
        Double closingBalance = prevClosingBalance + addToClosingBalance;

        // init SupplierFinance report
        SupplierFinance supplierFinance = SupplierFinance.builder()
                .reportDate(reportDateString)
                .reportType(reportType)
                .supplierId(supplierId)

                .paymentsNumReceived(paymentsNumReceived)
                .totalAmountReceived(totalAmountReceived)
                .totalTax(totalTax)
                .totalServiceFee(totalServiceFee)
                .otherFees(otherFees)
                .totalNetAmount(totalNetAmount)

                .accountsReceivable(accountsReceivable)
                .actualReceipts(actualReceipts)
                .outstandingAccounts(outstandingAccounts)

                .openingBalance(openingBalance)
                .closingBalance(closingBalance)

                .build();
        supplierFinanceRepository.save(supplierFinance);

        FinancialReport financialReport = new FinancialReport();
        copyProperties(supplierFinance, financialReport);

        return financialReport;
    }

    @Override
    public List<SupplierFinance> getMonthlySalesReport(Long supplierId, String startDate, String endDate, ReportType reportType) {
        return supplierFinanceRepository.findAllBySupplierIdAndReportTypeAndReportDateBetween(
                supplierId, reportType, startDate, endDate);
    }

    @Override
    public List<SupplierFinance> getYearlySalesReport(Long supplierId, String start, String end, ReportType reportType) {
        return supplierFinanceRepository.findAllBySupplierIdAndReportTypeAndReportDateBetween(
                supplierId, reportType, start, end);
    }


    /**
     * this method only responsible for generating daily report for supplier
     * @param supplierId
     * @param startDate
     * @param endDate
     */
    @Override
    public FinancialReport generateDailyFinancialReport(Long supplierId, LocalDateTime startDate, LocalDateTime endDate, ReportType reportType) {

        // convert reportDate
        String reportDateString = String.valueOf(LocalDate.from(startDate));

        // find if the current month's report exit in db
        SupplierFinance supplierFinance = supplierFinanceRepository.findBySupplierIdAndReportDateAndReportType(supplierId, reportDateString, reportType)
                .orElse(null);
        if (supplierFinance != null) {
            FinancialReport financialReport = new FinancialReport();
            copyProperties(supplierFinance, financialReport);
            return financialReport;
        }

        // get the salesOrderList between these days
        List<SalesOrder> salesOrderList = salesOrderRepository.findBySupplierIdAndOrderDateBetweenAndOrderStatus(supplierId, startDate, endDate, OrderStatus.PROCESSING);
        List<String> salesOrderSnList = salesOrderList.stream().map(SalesOrder::getSalesOrderSn).collect(Collectors.toList());
        List<PayPalPayment> payPalPaymentList = salesOrderSnList.stream()
                .map(salesOrderSn -> payPalPaymentRepository.findBySalesOrderSn(salesOrderSn))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        return commonGenerateReport(
                payPalPaymentList,
                supplierId,
                startDate,
                reportDateString,
                ReportType.DAILY,
                PayPalPayment::getTax,
                PayPalPayment::getPayPalFee,
                PayPalPayment::getNet
        );

//        // get the prev closing balance info
//        SupplierFinance prevSupplierFinance = supplierFinanceRepository.findByReportDate(String.valueOf(LocalDate.from(startDate.minusDays(1))))
//                .orElseGet(() -> {
//                    SupplierFinance newSupplierFinance = new SupplierFinance();
//                    newSupplierFinance.setReportDate(String.valueOf(LocalDate.from(startDate)));
//                    newSupplierFinance.setReportType(ReportType.DAILY);
//                    return newSupplierFinance;
//                });
//        Double prevClosingBalance = prevSupplierFinance.getClosingBalance();
//
//        // init supplierFinance and FinancialReport
//        Double addToClosingBalance = Double.valueOf(0);
//        int paymentsNumReceived = 0;
//        Double totalAmountReceived = Double.valueOf(0);
//        Double totalNetAmount = Double.valueOf(0);
//        Double totalTax = Double.valueOf(0);
//        Double totalServiceFee = Double.valueOf(0);
//
//        for (PayPalPayment payment : payPalPaymentList) {
//            paymentsNumReceived += 1;
//            totalTax += payment.getTax();
//            totalServiceFee += payment.getPayPalFee();
//            totalNetAmount += payment.getNet();
//            totalAmountReceived += payment.getNet();
//            addToClosingBalance += payment.getNet();
//        }
//        Double otherFees = Double.valueOf(0);
//
//        // set as totalAmountReceived
//        Double accountsReceivable = totalAmountReceived;
//        Double actualReceipts = totalAmountReceived;
//        Double outstandingAccounts = Double.valueOf(0);
//
//        Double openingBalance = prevClosingBalance;
//        Double closingBalance = prevClosingBalance + addToClosingBalance;
//
//        // init SupplierFinance report
//        SupplierFinance supplierFinance = SupplierFinance.builder()
//                .reportDate(reportDateString)
//                .reportType(reportType)
//                .supplierId(supplierId)
//
//                .paymentsNumReceived(paymentsNumReceived)
//                .totalAmountReceived(totalAmountReceived)
//                .totalTax(totalTax)
//                .totalServiceFee(totalServiceFee)
//                .otherFees(otherFees)
//                .totalNetAmount(totalNetAmount)
//
//                .accountsReceivable(accountsReceivable)
//                .actualReceipts(actualReceipts)
//                .outstandingAccounts(outstandingAccounts)
//
//                .openingBalance(openingBalance)
//                .closingBalance(closingBalance)
//
//                .build();
//        supplierFinanceRepository.save(supplierFinance);
//
//        FinancialReport financialReport = new FinancialReport();
//        copyProperties(supplierFinance, financialReport);
//
//        return financialReport;
    }

    @Override
    public FinancialReport generateMonthlyFinancialReport(Long supplierId, LocalDateTime startDate, LocalDateTime endDate, ReportType reportType) {

        // convert reportDate
        String reportDateString = startDate.getYear() + "-" + startDate.getMonth().getValue();
        log.info("reportDateString: " + reportDateString);

        // find if the current month's report exit in db
        SupplierFinance supplierFinance = supplierFinanceRepository.findBySupplierIdAndReportDateAndReportType(supplierId, reportDateString, reportType)
                .orElse(null);
        if (supplierFinance != null) {
            FinancialReport financialReport = new FinancialReport();
            copyProperties(supplierFinance, financialReport);
            return financialReport;
        }

        // get monthly supplierFinance in db between start - end
        String start = String.valueOf(startDate);
        String end = String.valueOf(endDate);
        List<SupplierFinance> supplierFinanceList = supplierFinanceRepository.findBySupplierIdAndReportDateBetween(supplierId, start, end);

        return commonGenerateReport(
                supplierFinanceList,
                supplierId,
                startDate,
                reportDateString,
                ReportType.MONTHLY,
                SupplierFinance::getTotalTax,
                SupplierFinance::getTotalServiceFee,
                SupplierFinance::getTotalNetAmount
        );

//        // get the prev closing balance info
//        SupplierFinance prevSupplierFinance = supplierFinanceRepository.findByReportDate(String.valueOf(LocalDate.from(startDate.minusDays(1))))
//                .orElseGet(() -> {
//                    SupplierFinance newSupplierFinance = new SupplierFinance();
//                    newSupplierFinance.setReportDate(String.valueOf(LocalDate.from(startDate)));
//                    newSupplierFinance.setReportType(ReportType.DAILY);
//                    return newSupplierFinance;
//                });
//        Double prevClosingBalance = prevSupplierFinance.getClosingBalance();
//
//        // init supplierFinance and FinancialReport
//        Double addToClosingBalance = Double.valueOf(0);
//        int paymentsNumReceived = 0;
//        Double totalAmountReceived = Double.valueOf(0);
//        Double totalNetAmount = Double.valueOf(0);
//        Double totalTax = Double.valueOf(0);
//        Double totalServiceFee = Double.valueOf(0);
//
//        for (SupplierFinance supplierFinance : supplierFinanceList) {
//            paymentsNumReceived += 1;
//            totalTax += supplierFinance.getTotalTax();
//            totalServiceFee += supplierFinance.getTotalServiceFee();
//            totalNetAmount += supplierFinance.getTotalNetAmount();
//            totalAmountReceived += supplierFinance.getTotalNetAmount();
//            addToClosingBalance += supplierFinance.getTotalNetAmount();
//        }
//        Double otherFees = Double.valueOf(0);
//
//        // set as totalAmountReceived
//        Double accountsReceivable = totalAmountReceived;
//        Double actualReceipts = totalAmountReceived;
//        Double outstandingAccounts = Double.valueOf(0);
//
//        Double openingBalance = prevClosingBalance;
//        Double closingBalance = prevClosingBalance + addToClosingBalance;
//
//        // init SupplierFinance report
//        SupplierFinance supplierFinance = SupplierFinance.builder()
//                .reportDate(reportDateString)
//                .reportType(reportType)
//                .supplierId(supplierId)
//
//                .paymentsNumReceived(paymentsNumReceived)
//                .totalAmountReceived(totalAmountReceived)
//                .totalTax(totalTax)
//                .totalServiceFee(totalServiceFee)
//                .otherFees(otherFees)
//                .totalNetAmount(totalNetAmount)
//
//                .accountsReceivable(accountsReceivable)
//                .actualReceipts(actualReceipts)
//                .outstandingAccounts(outstandingAccounts)
//
//                .openingBalance(openingBalance)
//                .closingBalance(closingBalance)
//
//                .build();
//        supplierFinanceRepository.save(supplierFinance);
//
//        FinancialReport financialReport = new FinancialReport();
//        copyProperties(supplierFinance, financialReport);
//
//        return financialReport;
    }

    @Override
    public FinancialReport generateYearlyFinancialReport(Long supplierId, LocalDateTime startDate, LocalDateTime endDate, ReportType reportType) {

        // convert reportDate
        String reportDateString = String.valueOf(startDate.getYear());

        // find if the current month's report exit in db
        SupplierFinance supplierFinance = supplierFinanceRepository.findBySupplierIdAndReportDateAndReportType(supplierId, reportDateString, reportType)
                .orElse(null);
        if (supplierFinance != null) {
            FinancialReport financialReport = new FinancialReport();
            copyProperties(supplierFinance, financialReport);
            return financialReport;
        }

        // get monthly supplierFinance in db between start - end
        String start = String.valueOf(startDate);
        String end = String.valueOf(endDate);
        List<SupplierFinance> supplierFinanceList = supplierFinanceRepository.findBySupplierIdAndReportDateBetween(supplierId, start, end);

        return commonGenerateReport(
                supplierFinanceList,
                supplierId,
                startDate,
                reportDateString,
                ReportType.YEARLY,
                SupplierFinance::getTotalTax,
                SupplierFinance::getTotalServiceFee,
                SupplierFinance::getTotalNetAmount
        );
//        // get the prev closing balance info
//        SupplierFinance prevSupplierFinance = supplierFinanceRepository.findByReportDate(String.valueOf(LocalDate.from(startDate.minusDays(1))))
//                .orElseGet(() -> {
//                    SupplierFinance newSupplierFinance = new SupplierFinance();
//                    newSupplierFinance.setReportDate(String.valueOf(LocalDate.from(startDate)));
//                    newSupplierFinance.setReportType(ReportType.DAILY);
//                    return newSupplierFinance;
//                });
//        Double prevClosingBalance = prevSupplierFinance.getClosingBalance();
//
//        // init values
//        Double addToClosingBalance = Double.valueOf(0);
//        int paymentsNumReceived = 0;
//        Double totalAmountReceived = Double.valueOf(0);
//        Double totalNetAmount = Double.valueOf(0);
//        Double totalTax = Double.valueOf(0);
//        Double totalServiceFee = Double.valueOf(0);
//
//        for (SupplierFinance supplierFinance : supplierFinanceList) {
//            paymentsNumReceived += 1;
//            totalTax += supplierFinance.getTotalTax();
//            totalServiceFee += supplierFinance.getTotalServiceFee();
//            totalNetAmount += supplierFinance.getTotalNetAmount();
//            totalAmountReceived += supplierFinance.getTotalNetAmount();
//            addToClosingBalance += supplierFinance.getTotalNetAmount();
//        }
//        Double otherFees = Double.valueOf(0);
//
//        // set as totalAmountReceived
//        Double accountsReceivable = totalAmountReceived;
//        Double actualReceipts = totalAmountReceived;
//        Double outstandingAccounts = Double.valueOf(0);
//
//        Double openingBalance = prevClosingBalance;
//        Double closingBalance = prevClosingBalance + addToClosingBalance;
//
//        // init SupplierFinance report
//        SupplierFinance supplierFinance = SupplierFinance.builder()
//                .reportDate(reportDateString)
//                .reportType(reportType)
//                .supplierId(supplierId)
//
//                .paymentsNumReceived(paymentsNumReceived)
//                .totalAmountReceived(totalAmountReceived)
//                .totalTax(totalTax)
//                .totalServiceFee(totalServiceFee)
//                .otherFees(otherFees)
//                .totalNetAmount(totalNetAmount)
//
//                .accountsReceivable(accountsReceivable)
//                .actualReceipts(actualReceipts)
//                .outstandingAccounts(outstandingAccounts)
//
//                .openingBalance(openingBalance)
//                .closingBalance(closingBalance)
//
//                .build();
//        supplierFinanceRepository.save(supplierFinance);
//
//        FinancialReport financialReport = new FinancialReport();
//        copyProperties(supplierFinance, financialReport);
//
//        return financialReport;
    }

//    private FinancialReport generateFinancialReport(List<SalesOrder> salesOrderList, Long supplierId, Double prevClosingBalance, LocalDate reportDate, ReportType reportType) {
//
//        // get supplier balance
//        List<String> salesOrderSnList = salesOrderList.stream().map(SalesOrder::getSalesOrderSn).collect(Collectors.toList());
//
//        String reportDateString = String.valueOf(reportDate);
//        Double addToClosingBalance = Double.valueOf(0);
//        int paymentsNumReceived = 0;
//        Double totalAmountReceived = Double.valueOf(0);
//        Double totalNetAmount = Double.valueOf(0);
//        Double totalTax = Double.valueOf(0);
//        Double totalServiceFee = Double.valueOf(0);
//
//        // get paypal-payment record
//        for (String salesOrderSn : salesOrderSnList) {
//            PayPalPayment payment = payPalPaymentRepository.findBySalesOrderSn(salesOrderSn).orElse(null);
//            if (payment == null) {
//                continue;
//            }
//            paymentsNumReceived += 1;
//            totalTax += payment.getTax();
//            totalServiceFee += payment.getPayPalFee();
//            totalNetAmount += payment.getNet();
//            totalAmountReceived += payment.getNet();
//            addToClosingBalance += payment.getNet();
//        }
//        Double otherFees = Double.valueOf(0);
//
//        // set as totalAmountReceived
//        Double accountsReceivable = totalAmountReceived;
//        Double actualReceipts = totalAmountReceived;
//        Double outstandingAccounts = Double.valueOf(0);
//
//        Double openingBalance = prevClosingBalance;
//        Double closingBalance = prevClosingBalance + addToClosingBalance;
//
//        // init SupplierFinance report
//        SupplierFinance supplierFinance = SupplierFinance.builder()
//                .reportDate(reportDateString)
//                .reportType(ReportType.DAILY)
//                .supplierId(supplierId)
//
//                .paymentsNumReceived(paymentsNumReceived)
//                .totalAmountReceived(totalAmountReceived)
//                .totalTax(totalTax)
//                .totalServiceFee(totalServiceFee)
//                .otherFees(otherFees)
//                .totalNetAmount(totalNetAmount)
//
//                .accountsReceivable(accountsReceivable)
//                .actualReceipts(actualReceipts)
//                .outstandingAccounts(outstandingAccounts)
//
//                .openingBalance(openingBalance)
//                .closingBalance(closingBalance)
//
//                .build();
//        supplierFinanceRepository.save(supplierFinance);
//
//        FinancialReport financialReport = new FinancialReport();
//        copyProperties(supplierFinance, financialReport);
//
//        return financialReport;
//    }

    @Override
    public FinancialReport autoGenerateDailyFinancialReport(Long supplierId, LocalDateTime startDate, LocalDateTime endDate) {
//        log.info("start date {} ; end date {}", startDate, endDate);
        // get all the salesOrder between these days
        List<SalesOrder> salesOrderList = salesOrderRepository.findBySupplierIdAndOrderDateBetweenAndOrderStatus(supplierId, startDate, endDate, OrderStatus.PROCESSING);
//        log.info("salesOrderList: " + salesOrderList);

        // find the finance report the day before today
        SupplierFinance supplierFinance = supplierFinanceRepository.findBySupplierIdAndReportDate(supplierId, String.valueOf(LocalDate.from(startDate.minusDays(1))))
                .orElse(null);
        if (supplierFinance == null) {
            FinancialReport financialReport = new FinancialReport();
            supplierFinance = SupplierFinance.builder()
                    .reportDate(String.valueOf(LocalDate.from(startDate.minusDays(1))))
                    .reportType(ReportType.DAILY)
                    .supplierId(supplierId)
                    .openingBalance(Double.valueOf(0))
                    .closingBalance(Double.valueOf(0))
                    .build();
            supplierFinanceRepository.save(supplierFinance);
            copyProperties(supplierFinance,financialReport);
//            return financialReport;
        }

        // generate today's report
        FinancialReport financialReport = autoGenerateFinancialReport(salesOrderList, supplierId, supplierFinance.getClosingBalance(), LocalDate.from(startDate));

        // find if today's report exit in db  //only save the daily report
        SupplierFinance supplierFinanceThisDay = supplierFinanceRepository.findBySupplierIdAndReportDate(supplierId, String.valueOf(LocalDate.from(startDate))).orElse(null);
        // save this day's finance report if it is not exit.
        if (supplierFinanceThisDay == null) {
            saveThisDaySupplierFinance(financialReport);
        }
        return financialReport;
    }

    private void saveThisDaySupplierFinance(FinancialReport financialReport) {
        SupplierFinance supplierFinance = SupplierFinance.builder()
                .reportDate(financialReport.getReportDate())
                .reportType(ReportType.DAILY)
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

    private FinancialReport autoGenerateFinancialReport(List<SalesOrder> salesOrderList, Long supplierId, Double closingBalance, LocalDate reportDate) {

        // init Finance report
        FinancialReport financialReport = new FinancialReport();
        financialReport.setReportDate(String.valueOf(reportDate));
        financialReport.setSupplierId(supplierId);
        financialReport.setReportType(ReportType.DAILY);

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
