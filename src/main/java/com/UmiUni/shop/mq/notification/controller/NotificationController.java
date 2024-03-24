package com.UmiUni.shop.mq.notification.controller;

import com.UmiUni.shop.mq.notification.model.NotificationMessage;
import com.UmiUni.shop.mq.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/{supplierId}/pending-messages")
    public ResponseEntity<List<NotificationMessage>> getPendingMessages(@PathVariable String supplierId) {
        List<NotificationMessage> messages = notificationService.fetchMessagesForSupplier(supplierId);
        return ResponseEntity.ok(messages);
    }

}
