package com.UmiUni.shop.mq.notification;

import com.UmiUni.shop.mq.notification.model.NotificationMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class SupplierNotificationSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final String notificationExchange = "supplier_notification_exchange";
    private final String notificationRoutingKey = "supplier.notification";

    public void notifySupplier(String supplierId, String skuCode, int quantity) {

        NotificationMessage notificationMessage = new NotificationMessage(supplierId, skuCode, quantity);
//        String message = buildNotificationMessage(supplierId, skuCode, quantity);
        rabbitTemplate.convertAndSend(notificationExchange, notificationRoutingKey, notificationMessage);
    }

    private String buildNotificationMessage(String supplierId, String skuCode, int quantity) {
        // Construct the notification message
        return String.format("{\"supplierId\":\"%s\", \"message\":\"Inventory for SKU %s has been reduced by %d units.\"}",
                supplierId, skuCode, quantity);
    }
}
