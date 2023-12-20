package com.UmiUni.shop.mq;

import com.UmiUni.shop.model.InventoryUpdateMessage;
import com.UmiUni.shop.service.ProductService;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class InventoryUpdateListener {

    @Autowired
    private ProductService productService;

//    @RabbitListener(queues = "${inventory.queue.name}")
//    public void receive(InventoryUpdateMessage message) {
//        // Handle the message, e.g., log it or trigger further processing
//        log.info("receive message: " + message);
//    }

    @RabbitListener(queuesToDeclare = @Queue(name = "inventory_lock_queue"))
    public void handleInventoryLock(InventoryUpdateMessage message) {
        productService.lockInventory(message.getSkuCode(), message.getQuantity());
        log.info("handleInventoryLock message: " + message);
    }

    @RabbitListener(queuesToDeclare = @Queue(name = "inventory_reduction_queue"))
    public void handleInventoryReduction(InventoryUpdateMessage message) {
        productService.reduceProductInventory(message.getSkuCode(), message.getQuantity());
        log.info("handleInventoryReduction message: " + message);
    }

}
