package com.UmiUni.shop.mq;

import com.UmiUni.shop.constant.ErrorCategory;
import com.UmiUni.shop.constant.OrderStatus;
import com.UmiUni.shop.dto.SalesOrderDTO;
import com.UmiUni.shop.exception.PaymentExpiredException;
import com.UmiUni.shop.repository.SalesOrderRepository;
import com.UmiUni.shop.service.PaymentErrorHandlingService;
import com.UmiUni.shop.service.SalesOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.springframework.amqp.core.Message;
import com.rabbitmq.client.Channel;

import java.io.IOException;

@Component
@Log4j2
public class DlxOrderListener {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SalesOrderService salesOrderService;

    @Autowired
    private PaymentErrorHandlingService paymentErrorHandlingService;

    @RabbitListener(queues = "#{@orderDlxQueue}")
    public void onExpiredOrderReceived(Message message, Channel channel) throws IOException {
        SalesOrderDTO salesOrder = convertMessageToSalesOrderDTO(message);
        try {
            log.info("Received expired order in DLX: {}", salesOrder.getSalesOrderSn());

            // 处理过期订单逻辑，例如更新订单状态为取消
            salesOrder.setOrderStatus(OrderStatus.EXPIRED);
            salesOrderService.updateOrderStatusBySalesOrderSn(salesOrder.getSalesOrderSn(), OrderStatus.EXPIRED);
            // 手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            throw new PaymentExpiredException("SalesOrder has been expired!", ErrorCategory.ORDER_EXPIRED);
        } catch (PaymentExpiredException e) {
            log.error("Failed to process expired order: {}", e.getMessage());
            paymentErrorHandlingService.handlePaymentExpiredError(e, null, String.valueOf(salesOrder.getSalesOrderId()));
            // 对于处理失败的消息，您可以选择拒绝并重新入队，或者直接丢弃
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }

    @RabbitListener(queues = "#{@inventoryLockDlxQueue}")
    public void handleInventoryLockDlxMessage(Message message, Channel channel) throws Exception {
        // Process the DLQ message
        log.info("Received message in inventoryLockDlxQueue: " + new String(message.getBody()));
        // Acknowledge the message if necessary
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    private SalesOrderDTO convertMessageToSalesOrderDTO(Message message) {
        try {
            // 使用已配置的 ObjectMapper 实例进行反序列化
            return objectMapper.readValue(message.getBody(), SalesOrderDTO.class);
        } catch (IOException e) {
            log.error("Error converting message to SalesOrderDTO", e);
            throw new RuntimeException("Error converting message to SalesOrderDTO", e);
        }
    }

}
