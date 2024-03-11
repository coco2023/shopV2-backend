package com.UmiUni.shop.mq.notification.controller;

import com.UmiUni.shop.mq.notification.SupplierNotificationSender;
import com.UmiUni.shop.mq.notification.model.NotificationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {

    @Autowired
    private SupplierNotificationSender notificationService;

    @PostMapping("/notify")
    public ResponseEntity<String> notifySupplier(@RequestBody NotificationMessage notificationMessage) {
        notificationService.notifySupplier(
                notificationMessage.getSupplierId(),
                notificationMessage.getSkuCode(),
                notificationMessage.getQuantity()
        );
        return ResponseEntity.ok("Notification sent");
    }
}
