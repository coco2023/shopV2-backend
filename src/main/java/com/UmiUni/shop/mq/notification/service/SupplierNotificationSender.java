package com.UmiUni.shop.mq.notification.service;

import com.UmiUni.shop.mq.notification.model.NotificationMessage;
import com.UmiUni.shop.service.SupplierService;
import lombok.extern.log4j.Log4j2;
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

    // inventory reduction queue part
    @Value("${rabbitmq.queues.supplier_notification_for_inventory_reduction_process.exchange.name}")
    private String notificationExchange;

    @Value("${rabbitmq.queues.supplier_notification_for_inventory_reduction_process.routing-key}")
    private String notificationRoutingKey;

    @Autowired
    private SupplierService supplierService;

    public void notifySupplier(String supplierId, String skuCode, int quantity) {

        NotificationMessage notificationMessage = new NotificationMessage(supplierId, skuCode, quantity);
//        rabbitTemplate.convertAndSend(notificationExchange, notificationRoutingKey, notificationMessage);
        // Send the message to the specific supplier's WebSocket session via supplierName
        String supplerName = supplierService.getSupplier(Long.valueOf(supplierId)).getName();

        // TODO: check if other supplier also can receive the message
        messagingTemplate.convertAndSendToUser(
                supplerName, // supplierName,
                "/queue/inventoryReduction",  // The client-side should be subscribed to this destination
                notificationMessage
        );
        log.info("success notify the supplier: {}", supplierId);
    }
}
