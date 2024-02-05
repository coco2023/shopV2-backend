package com.UmiUni.shop.mq;

import com.UmiUni.shop.dto.SalesOrderDTO;
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

    @RabbitListener(queues = "#{@dlxQueue}")
    public void onExpiredOrderReceived(Message message, Channel channel) throws IOException {
        try {
            SalesOrderDTO salesOrder = convertMessageToSalesOrderDTO(message);
            log.info("Received expired order in DLX: {}", salesOrder.getSalesOrderSn());

            // 处理过期订单逻辑，例如更新订单状态为取消
            // ...

            // 手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("Failed to process expired order: {}", e.getMessage());

            // 对于处理失败的消息，您可以选择拒绝并重新入队，或者直接丢弃
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
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

//    @RabbitListener(queues = "#{@dlxQueue}")
//    public void onExpiredOrderReceived(SalesOrderDTO salesOrder) {
//        log.info("Received expired order in DLX: {}", salesOrder.getSalesOrderSn());
//        // Change order status to CANCEL here
//        // Update the order status in your database or service layer
//    }

}
