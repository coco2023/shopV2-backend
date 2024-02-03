package com.UmiUni.shop.mq;

import com.UmiUni.shop.dto.SalesOrderDTO;
import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.service.PayPalService;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class OrderMessageListener {

    @Autowired
    private PayPalService payPalService; // 假设这是一个服务类，用于处理订单逻辑

    @RabbitListener(queuesToDeclare = @Queue("${order.queue.name}"))
    public void onOrderReceived(SalesOrderDTO salesOrder) {
        log.info("Asynchronously processing order for salesOrderSn: {}", salesOrder.getSalesOrderSn());

        try {
            // 在这里进行订单处理，例如验证订单、检查库存、保存订单到数据库等
            payPalService.createPayment(salesOrder);
        } catch (Exception e) {
            log.error("Failed to process order asynchronously: {}", e.getMessage());
            // 可以在这里实现错误处理逻辑，如重试或将失败的订单信息发送到另一个队列进行进一步处理
        }
    }
}
