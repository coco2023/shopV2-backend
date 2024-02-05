//package com.UmiUni.shop.mq;
//
//import com.UmiUni.shop.dto.SalesOrderDTO;
//import com.UmiUni.shop.service.SalesOrderService;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//@Service
//@Log4j2
//public class OrderExpirationListener {
//
//    @Autowired
//    private SalesOrderService orderService;
//
//    @RabbitListener(queues = "${rabbitmq.dlx.queue.name}")
//    private void onOrderExpired(SalesOrderDTO salesOrder) {
//        log.info("Received expired order from DLX queue: " + salesOrder.getSalesOrderSn());
//        try {
//            // Check if the order is still in a state that can be cancelled
//            if (orderService.canCancelOrder(salesOrder.getSalesOrderSn())) {
//                orderService.cancelOrder(salesOrder.getSalesOrderSn());
//                log.info("Order cancelled successfully: " + salesOrder.getSalesOrderSn());
//            } else {
//                log.info("Order cannot be cancelled or is already processed: " + salesOrder.getSalesOrderSn());
//            }
//        } catch (Exception e) {
//            log.error("Error handling expired order: " + e.getMessage());
//        }
//    }
//
//}
