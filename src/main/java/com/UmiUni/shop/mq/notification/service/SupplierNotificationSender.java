package com.UmiUni.shop.mq.notification.service;

import com.UmiUni.shop.entity.Supplier;
import com.UmiUni.shop.mq.notification.model.NotificationMessage;
import com.UmiUni.shop.service.SupplierService;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class SupplierNotificationSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // notification of inventory reduction queue
    @Value("${rabbitmq.queues.supplier_notification_for_inventory_reduction_process.name}")
    private String notificationQueue;

    @Value("${rabbitmq.queues.supplier_notification_for_inventory_reduction_process.exchange.name}")
    private String notificationExchange;

    @Value("${rabbitmq.queues.supplier_notification_for_inventory_reduction_process.routing-key}")
    private String notificationRoutingKey;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private SupplierService supplierService;

    public void notifySupplier(String supplierId, String skuCode, int quantity) {

        NotificationMessage notificationMessage = new NotificationMessage(supplierId, skuCode, quantity);

        // Send the message to the specific supplier's WebSocket session via supplierName
        Supplier supplier = supplierService.getSupplier(Long.valueOf(supplierId));
        String supplerName = supplier.getName();
        log.info(supplier + "supplerName: {}, supplierId: {}", supplerName, supplierId);

        // TODO: check if other supplier also can receive the message
        messagingTemplate.convertAndSendToUser(
                supplerName, // supplierName,
                "/queue/inventoryReduction",  // The client-side should be subscribed to this destination
                notificationMessage
        );
        log.info("success notify the supplier: {}", supplierId);
    }

    public void notifySupplierWithHistoryMessage(String supplierId, String skuCode, int quantity) {

        NotificationMessage notificationMessage = new NotificationMessage(supplierId, skuCode, quantity);

        // Publish to RabbitMQ for persistence
        publishToRabbitMQDynamic(notificationMessage, supplierId);  // publishToRabbitMQ

        // Attempt to send real-time message via WebSocket
        String supplierName = supplierService.getSupplier(Long.valueOf(supplierId)).getName();
        notifySupplierViaWebSocket(supplierName, notificationMessage);
    }

    public void publishToRabbitMQDynamic(NotificationMessage notificationMessage, String supplierId) {
        String queueName = notificationQueue + supplierId;
        String routingKeyForSupplier = notificationRoutingKey + supplierId;

        // Ensure the queue exists before sending the message
        Queue queue = new Queue(queueName, true); // 'true' for a durable queue
        rabbitAdmin.declareQueue(queue); // rabbitAdmin is an instance of RabbitAdmin

        // Ensure the exchange is declared (this could be a no-op if the exchange already exists)
        DirectExchange exchange = new DirectExchange(notificationExchange);
        rabbitAdmin.declareExchange(exchange);

        // Declare the binding between the queue and the exchange
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKeyForSupplier);
        rabbitAdmin.declareBinding(binding);

        // Now, send the message
        rabbitTemplate.convertAndSend(notificationExchange, routingKeyForSupplier, notificationMessage);
        log.info("Published message to RabbitMQ for supplier: {}", supplierId);
    }

    public void notifySupplierViaWebSocket(String supplierName, NotificationMessage notificationMessage) {
        messagingTemplate.convertAndSendToUser(
                supplierName,
                "/queue/inventoryReduction",
                notificationMessage
        );
        log.info("Sent real-time notification to supplier: {}", supplierName);
    }

}
