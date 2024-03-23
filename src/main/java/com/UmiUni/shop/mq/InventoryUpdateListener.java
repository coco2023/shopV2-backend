package com.UmiUni.shop.mq;

import com.UmiUni.shop.model.InventoryUpdateMessage;
import com.UmiUni.shop.mq.notification.service.SupplierNotificationSender;
import com.UmiUni.shop.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class InventoryUpdateListener {

    @Autowired
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SupplierNotificationSender supplierNotificationSender;

    @RabbitListener(queues = "#{@inventoryLockQueue}") // the name of the inventoryLockQueue @bean in the RabbitConfig
    public void handleInventoryLock(Message message, Channel channel) throws Exception {
        InventoryUpdateMessage inventoryUpdateMessage = convertMessageToObject(message);
        try {
            productService.lockInventory(inventoryUpdateMessage.getSkuCode(), inventoryUpdateMessage.getQuantity());
            log.info("handleInventoryLock message: " + inventoryUpdateMessage);

            // Schedule the unlock task to run after 5 minutes
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.schedule(() -> {
                try {
                    productService.unlockInventory(inventoryUpdateMessage.getSkuCode(), inventoryUpdateMessage.getQuantity());
                    log.info("Inventory unlocked for: " + inventoryUpdateMessage.getSkuCode());
                } catch (Exception e) {
                    log.error("Error unlocking inventory for: " + inventoryUpdateMessage.getSkuCode(), e);
                }
            }, 5, TimeUnit.MILLISECONDS);  // TimeUnit.MILLISECONDS

            // Manually acknowledge the message
            long deliveryTag = message.getMessageProperties().getDeliveryTag();
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Error processing message", e);
            // Optionally, you can negatively acknowledge the message if an error occurs
            long deliveryTag = message.getMessageProperties().getDeliveryTag();
            channel.basicNack(deliveryTag, false, true);
        }
    }

    @RabbitListener(queues = "#{@inventoryReductionQueue}") // the name of the inventoryReductionQueue @bean in the RabbitConfig
    public void handleInventoryReduction(Message message, Channel channel) throws IOException {
        InventoryUpdateMessage inventoryUpdateMessage = convertMessageToObject(message);

        try {
            productService.reduceProductInventory(inventoryUpdateMessage.getSkuCode(), inventoryUpdateMessage.getQuantity());
            log.info("Inventory reduced: " + inventoryUpdateMessage);

            // Notify the supplier about the inventory reduction
            Long supplierId = productService.getSupplierIdBySkuCode(inventoryUpdateMessage.getSkuCode());
            log.info("supplierId: {}, inventoryUpdateMessage: {}", supplierId, inventoryUpdateMessage);
            supplierNotificationSender.notifySupplier(supplierId.toString(), inventoryUpdateMessage.getSkuCode(), inventoryUpdateMessage.getQuantity());

            long deliveryTag = message.getMessageProperties().getDeliveryTag();
            channel.basicAck(deliveryTag, false);  // Acknowledge the message
        } catch (Exception e) {
            log.error("Error processing inventory reduction", e);
            long deliveryTag = message.getMessageProperties().getDeliveryTag();
            channel.basicNack(deliveryTag, false, true);  // Negative acknowledgment in case of error
        }
    }

    private InventoryUpdateMessage convertMessageToObject(Message message) {
        try {
            // Assuming the message body is a JSON string that can be mapped to InventoryUpdateMessage
            return objectMapper.readValue(message.getBody(), InventoryUpdateMessage.class);
        } catch (Exception e) {
            // Handle the exception (log it, throw a custom exception, etc.)
            log.error("Error converting message to InventoryUpdateMessage", e);
            return null; // or throw a custom exception
        }
    }

}

///**
// * test using PlayLoad
// * @param inventoryUpdateMessage
// * @param channel
// * @param tag
// * @throws IOException
// */
//    @RabbitListener(queues = "#{@inventoryLockQueue}")
//    public void handleInventoryLock(@Payload InventoryUpdateMessage inventoryUpdateMessage,
//                                    Channel channel,
//                                    @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
//        try {
//            productService.lockInventory(inventoryUpdateMessage.getSkuCode(), inventoryUpdateMessage.getQuantity());
//            log.info("handleInventoryLock message: " + inventoryUpdateMessage);
//            // Manually acknowledge the message
//            channel.basicAck(tag, false);
//        } catch (Exception e) {
//            log.error("Error processing message", e);
//            // Optionally, you can negatively acknowledge the message if an error occurs
//            channel.basicNack(tag, false, true);
//        }
//    }
