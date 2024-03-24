package com.UmiUni.shop.mq.notification.service;

import com.UmiUni.shop.mq.notification.model.NotificationMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Log4j2
public class SupplierNotificationListener {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    @RabbitListener(queues = "supplier_notification_queue")
    public void receiveNotification(Message message, Channel channel) throws IOException {
        try {

            NotificationMessage notificationMessage = convertMessageToObject(message);

            String supplierId = notificationMessage.getSupplierId();
            String skuCode = notificationMessage.getSkuCode();
            int quantity = notificationMessage.getQuantity();

            // Example processing logic
            System.out.println("RabbitListener: Received inventory reduction notification for supplier " + supplierId + ": SKU " + skuCode + " reduced by " + quantity);

//            // Broadcast the message to WebSocket clients
//            messagingTemplate.convertAndSend("/topic/supplierNotifications", notificationMessage);

            // Manually acknowledge the message
            long deliveryTag = message.getMessageProperties().getDeliveryTag();
            channel.basicAck(deliveryTag, false);  // Acknowledge the message

        } catch (Exception e) {
            log.error("Error processing message", e);
            // Optionally, you can negatively acknowledge the message if an error occurs
            long deliveryTag = message.getMessageProperties().getDeliveryTag();
            channel.basicNack(deliveryTag, false, true);
        }
}

    private NotificationMessage convertMessageToObject(Message message) {
        try {
            // Assuming the message body is a JSON string that can be mapped to InventoryUpdateMessage
            return objectMapper.readValue(message.getBody(), NotificationMessage.class);
        } catch (Exception e) {
            // Handle the exception (log it, throw a custom exception, etc.)
            log.error("Error converting message to InventoryUpdateMessage", e);
            return null; // or throw a custom exception
        }
    }
}
