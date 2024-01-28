package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.entity.PayPalPayment;
import com.UmiUni.shop.entity.Payment;
import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.repository.PayPalPaymentRepository;
import com.UmiUni.shop.repository.PaymentRepository;
import com.UmiUni.shop.repository.SalesOrderRepository;
import com.UmiUni.shop.service.SupplierPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupplierPaymentServiceImpl implements SupplierPaymentService {

    @Autowired
    private PayPalPaymentRepository payPalPaymentRepository;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Override
    public PayPalPayment getPayPalPayment(String supplierId, Long id) {
        return payPalPaymentRepository.findBySupplierIdAndId(supplierId, id);
    }

    @Override
    public List<PayPalPayment> getAllPayPalPayment(String supplierId) {
        return payPalPaymentRepository.findAllBySupplierId(supplierId);
    }

    @Override
    public List<PayPalPayment> getPayPalPaymentBySupplierIdAndSalesOrderSn(String supplierId, String salesOrderSn) {

        return payPalPaymentRepository.findBySupplierIdAndSalesOrderSn(supplierId, salesOrderSn);
    }

    @Override
    public List<PayPalPayment> getPayPalPaymentsBySalesOrderSn(String supplierId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "salesOrderId");
        // find the salesOrderSn list by supplierId
        List<SalesOrder> salesOrders = salesOrderRepository.findAllBySupplierId(Long.valueOf(supplierId), sort);
        List<String> salesOrderSnList = salesOrders.stream().map(SalesOrder::getSalesOrderSn).collect(Collectors.toList());
        // get the transactionId
        List<PayPalPayment> payPalPaymentList = new ArrayList<>();

        // get the paypal payments
        for (String orderSn : salesOrderSnList) {
            PayPalPayment payment = payPalPaymentRepository.findBySalesOrderSn(orderSn)
                    .orElseThrow(() -> new RuntimeException("No payment exit!"));
            payment.setSupplierId(supplierId);
            payPalPaymentRepository.save(payment);
            payPalPaymentList.add(payment);
        }

        return payPalPaymentList;
    }


}
