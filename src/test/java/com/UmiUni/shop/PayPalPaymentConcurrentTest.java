//package com.UmiUni.shop;
//
//import com.UmiUni.shop.constant.OrderStatus;
//import com.UmiUni.shop.constant.PaymentStatus;
//import com.UmiUni.shop.dto.SalesOrderDTO;
//import com.UmiUni.shop.model.PaymentResponse;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//
//@SpringBootTest
//public class PayPalPaymentConcurrentTest {
//
//    @Test
//    public void testPayPalPaymentProcess() throws InterruptedException {
//
//        int numberOfThreads = 10; // 10 payment threads
//        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
//
//        for (int i = 0; i < numberOfThreads; i++) {
//            int finalI = i;
//            executor.submit(() -> {
//                try {
//                    simulatePaymentProcess(finalI);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });
//        }
//
//        executor.shutdown();
//        executor.awaitTermination(1, TimeUnit.HOURS); // waiting all the tasks complete
//
//    }
//
//    private void simulatePaymentProcess(int paymentIndex) throws Exception {
//        // step 1: create payment order
//        SalesOrderDTO salesOrder = creatSalesOrder(paymentIndex);
//        PaymentResponse createPaymentResponse = createPayment(salesOrder);
//
//        // step 2: Polling Payment status
//        PaymentResponse statusResponse = null;
//        while (statusResponse == null || PaymentStatus.CREATED.name().equals(statusResponse.getStatus())) {
//            Thread.sleep(5000); // 每5秒轮询一次
//            statusResponse = checkPaymentStatus(salesOrder.getSalesOrderSn());
//        }
//
//        // step 3: complete payment
//        if (PaymentStatus.CREATED.name().equals(statusResponse.getStatus())) {
//            completePayment(statusResponse.getTransactionId(), "PayerID", salesOrder.getSupplierId().toString());
//        }
//    }
//
//    private SalesOrderDTO creatSalesOrder(int paymentIndex) {
//        SalesOrderDTO salesOrder = new SalesOrderDTO();
//        salesOrder.setSalesOrderSn("ORDER" + paymentIndex);
//        salesOrder.setTotalAmount(new BigDecimal("2.00"));
//        salesOrder.setOrderStatus(OrderStatus.PROCESSING);
//        salesOrder.setOrderDate(LocalDateTime.now());
//        return salesOrder;
//    }
//
//    private PaymentResponse createPayment(SalesOrderDTO salesOrder) {
//        PaymentResponse response = new PaymentResponse();
//        response.setStatus("CREATED");
//        response.setTransactionId("TRANS" + salesOrder.getSalesOrderSn());
//        response.setApprovalUrl("https://fakepaypal.com/approve?transactionId=" + response.getTransactionId());
//        return response;
//    }
//
//    private PaymentResponse checkPaymentStatus(String salesOrderSn) {
//        PaymentResponse response = new PaymentResponse();
//        // 模拟状态变化以模拟支付完成
//        response.setStatus(PaymentStatus.CREATED.name());
//        return response;
//    }
//
//    private void completePayment(String paymentId, String payerID, String toString) {
//        System.out.println("Payment with ID " + paymentId + " has been completed successfully.");
//    }
//
////    public static void main(String[] args) throws InterruptedException {
////        new PayPalPaymentConcurrentTest().testPayPalPaymentProcess();
////    }
//}
