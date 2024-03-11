package com.UmiUni.shop.mq.notification.controller;

import com.UmiUni.shop.mq.notification.model.NotificationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class NotificationWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = "supplier_notification_queue")
    public void receiveNotification(NotificationMessage notificationMessage) {
        forwardNotificationToSupplier(notificationMessage);
    }

    public void forwardNotificationToSupplier(NotificationMessage notificationMessage) {
        messagingTemplate.convertAndSendToUser(
                notificationMessage.getSupplierId(),
                "/queue/notifications",
                notificationMessage
        );
    }

}
