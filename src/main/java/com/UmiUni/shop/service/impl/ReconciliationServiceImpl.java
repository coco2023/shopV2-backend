package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.entity.PayPalPayment;
import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.repository.PayPalPaymentRepository;
import com.UmiUni.shop.repository.SalesOrderRepository;
import com.UmiUni.shop.service.ReconciliationService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Log4j2
public class ReconciliationServiceImpl implements ReconciliationService {

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private PayPalPaymentRepository payPalPaymentRepository;

    public String reconcilePayment(String salesOrderSn) {
        Optional<SalesOrder> salesOrderOpt = salesOrderRepository.getSalesOrderBySalesOrderSn(salesOrderSn);
        Optional<PayPalPayment> payPalPaymentOpt = payPalPaymentRepository.findBySalesOrderSn(salesOrderSn);

        if( salesOrderOpt.isPresent() && payPalPaymentOpt.isPresent() ) {
            SalesOrder salesOrder = salesOrderOpt.get();
            PayPalPayment payPalPayment = payPalPaymentOpt.get();
            log.info("salesOrder: " + salesOrder.getOrderStatus() + " payPalPayment: " + payPalPayment.getPaymentState());

            if (isReconciliationSuccessful(salesOrder, payPalPayment)) {
                return "Reconciliation successful";
            } else {
                // Handle or log the error
                return "Reconciliation failed";
            }
        } else {
            return "SalesOrder or PayPalPayment not found";
        }

    }

    /**
     * This method checks the four conditions:
     * payPalPayment's paymentState is "COMPLETE".
     * salesOrder's orderStatus is "PROCESSING".
     * The sum of payPalPayment's payPalFee and net equals salesOrder's totalAmount.
     * payPalPayment's updatedAt is before salesOrder's expirationDate.
     */
    private boolean isReconciliationSuccessful(SalesOrder salesOrder, PayPalPayment payPalPayment) {
        boolean isPaymentStateComplete = "complete".equalsIgnoreCase(payPalPayment.getPaymentState());
        boolean isOrderStateProcessing = "PROCESSING".equalsIgnoreCase(salesOrder.getOrderStatus().name());
        Double totalPayPalAmount = payPalPayment.getPayPalFee() + payPalPayment.getNet();
        boolean isAmountMatching = totalPayPalAmount.equals(salesOrder.getTotalAmount().doubleValue());
        boolean isPaymentWithinExpiration = payPalPayment.getUpdatedAt().isBefore(salesOrder.getExpirationDate());
        log.info(isOrderStateProcessing + " " + isOrderStateProcessing + " "  +  isAmountMatching + " " + isPaymentWithinExpiration);

        return isPaymentStateComplete && isOrderStateProcessing && isAmountMatching && isPaymentWithinExpiration;
// 2023-12-17 01:51:52   2023-12-16 19:53:02  2023-12-16 20:12:03
    }


}
