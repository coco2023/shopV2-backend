package com.UmiUni.shop.mq;

import com.UmiUni.shop.model.InventoryUpdateMessage;
import com.UmiUni.shop.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Log4j2
public class InventoryUpdateListener {

    @Autowired
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = "#{@inventoryLockQueue}") // the name of the inventoryLockQueue @bean in the RabbitConfig
    public void handleInventoryLock(Message message, Channel channel) throws Exception {
        InventoryUpdateMessage inventoryUpdateMessage = convertMessageToObject(message);
        try {
            productService.lockInventory(inventoryUpdateMessage.getSkuCode(), inventoryUpdateMessage.getQuantity());
            log.info("handleInventoryLock message: " + inventoryUpdateMessage);
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

//    @RabbitListener(queuesToDeclare = @Queue(name = "inventory_lock_queue"))
//    public void handleInventoryLock(InventoryUpdateMessage message) {
//        productService.lockInventory(message.getSkuCode(), message.getQuantity());
//        log.info("handleInventoryLock message: " + message);
//    }

