package com.UmiUni.shop.mq;

import com.UmiUni.shop.dto.SalesOrderDTO;
import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.service.PayPalService;
import com.UmiUni.shop.service.SalesOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.amqp.core.Message;

import java.io.IOException;

@Component
@Log4j2
public class OrderMessageListener {

    @Autowired
    private PayPalService payPalService; // 假设这是一个服务类，用于处理订单逻辑

    @Autowired
    private SalesOrderService salesOrderService;

    @Autowired
    private ObjectMapper objectMapper;

    //    @RabbitListener(queuesToDeclare = @Queue("${rabbitmq.queues.order_process.name}"))
    @RabbitListener(queues = "#{@orderQueue}")
    public void onOrderReceived(Message message, Channel channel) throws IOException {
        try {
            SalesOrderDTO salesOrder = convertMessageToSalesOrderDTO(message);

            log.info("Asynchronously processing order for salesOrderSn: {}", salesOrder.getSalesOrderSn());
            log.info("sales order: {}", salesOrderService.getSalesOrderBySalesOrderSn(salesOrder.getSalesOrderSn()));

            // 在这里进行订单处理，例如验证订单、检查库存、保存订单到数据库等
            payPalService.createPayment(salesOrder);

            // 手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 可以在这里实现错误处理逻辑，如重试或将失败的订单信息发送到另一个队列进行进一步处理
            log.error("Failed to process order asynchronously: {}", e.getMessage());

            // 对于处理失败的消息，您可以选择拒绝并重新入队，或者直接丢弃
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }

    private SalesOrderDTO convertMessageToSalesOrderDTO(Message message) {
        try {
            // 假设消息体是 JSON 格式，并且能够被直接映射到 SalesOrderDTO 类
            // 使用已配置的 ObjectMapper 实例进行反序列化
            return objectMapper.readValue(message.getBody(), SalesOrderDTO.class);
        } catch (IOException e) {
            log.error("Error converting message to SalesOrderDTO", e);
            throw new RuntimeException("Error converting message to SalesOrderDTO", e);
        }
    }

//    @RabbitListener(queues = "#{@orderQueue}")
//    public void onOrderReceived(SalesOrderDTO salesOrder) {
//        log.info("Asynchronously processing order for salesOrderSn: {}", salesOrder.getSalesOrderSn());
//        log.info("sales order: {}", salesOrderService.getSalesOrderBySalesOrderSn(salesOrder.getSalesOrderSn()));
//
//        try {
//            // 在这里进行订单处理，例如验证订单、检查库存、保存订单到数据库等
//            payPalService.createPayment(salesOrder);
//        } catch (Exception e) {
//            log.error("Failed to process order asynchronously: {}", e.getMessage());
//            // 可以在这里实现错误处理逻辑，如重试或将失败的订单信息发送到另一个队列进行进一步处理
//        }
//    }

}
