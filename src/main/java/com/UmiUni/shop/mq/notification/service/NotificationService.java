package com.UmiUni.shop.mq.notification.service;

import com.UmiUni.shop.model.InventoryUpdateMessage;
import com.UmiUni.shop.mq.notification.model.NotificationMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used for fetching pending messages from RabbitMQ.
 * It retrieves all messages from a supplier's queue, ensuring no message is lost, even if the supplier was offline.
 */
@Service
public class NotificationService {

    @Value("${rabbitmq.queues.supplier_notification_for_inventory_reduction_process.name}")
    private String notificationQueue;

    @Autowired
    private ObjectMapper objectMapper; // Jackson's ObjectMapper for JSON processing

    private final RabbitTemplate rabbitTemplate;

    // Constructor injection of RabbitTemplate
    @Autowired
    public NotificationService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public List<NotificationMessage> fetchMessagesForSupplier(String supplierId) {
        String queueName = notificationQueue + supplierId; // Ensure this matches the queue naming convention
        List<NotificationMessage> messages = new ArrayList<>();

        while (true) {
            Message message = rabbitTemplate.receive(queueName);
            if (message == null) break;

            NotificationMessage notificationMessage = convertMessage(message);
            messages.add(notificationMessage);
        }

        return messages;
    }

    // Method to convert RabbitMQ Message to NotificationMessage object
    private NotificationMessage convertMessage(Message message) {
        try {
            // Assuming the message body is a JSON string
            return objectMapper.readValue(message.getBody(), NotificationMessage.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert message to NotificationMessage", e);
        }
    }
}
