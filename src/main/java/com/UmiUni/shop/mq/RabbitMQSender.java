package com.UmiUni.shop.mq;

import com.UmiUni.shop.dto.SalesOrderDTO;
import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.exception.AmqpException;
import com.UmiUni.shop.model.InventoryUpdateMessage;
import com.UmiUni.shop.service.PaymentErrorHandlingService;
import com.UmiUni.shop.service.SalesOrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class RabbitMQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.queues.order_process.name}")
    private String orderQueue;

    @Value("${rabbitmq.queues.order_process.exchange.name}")
    private String orderExchangeName;

    @Value("${rabbitmq.queues.order_process.routing-key}")
    private String orderRoutingKey;

//     @Autowired
//    private SimpMessagingTemplate template;

    // inventory lock queue part
    @Value("${rabbitmq.queues.inventory_lock_process.exchange.name}")
    private String inventoryLockExchange;

    @Value("${rabbitmq.queues.inventory_lock_process.routing-key}")
    private String inventoryLockRoutingKey;

    // inventory reduction queue part
    @Value("${rabbitmq.queues.inventory_reduction_process.exchange.name}")
    private String inventoryReductionExchangeName;

    @Value("${rabbitmq.queues.inventory_reduction_process.routing-key}")
    private String inventoryReductionRoutingKey;

    @Autowired
    private PaymentErrorHandlingService paymentErrorHandlingService;

    public void sendInventoryLock(InventoryUpdateMessage message) {
        try {
            rabbitTemplate.convertAndSend(inventoryLockExchange, inventoryLockRoutingKey, message);
            log.info("sendInventoryLock! " + message);
//            throw new AmqpException("error test for AmqpException!");
        } catch (AmqpException e) {
            paymentErrorHandlingService.handleAmqpException(e, message);
        }
    }

    public void sendInventoryReduction(InventoryUpdateMessage message) {
        try {
            rabbitTemplate.convertAndSend(inventoryReductionExchangeName, inventoryReductionRoutingKey, message);
            log.info("sendInventoryReduction! " + message);

            // Send message to topic
//            template.convertAndSend("/topic/inventoryReduction", message);
        } catch (AmqpException e) {
            paymentErrorHandlingService.handleAmqpException(e, message);
        }
    }

    public void sendOrder(SalesOrderDTO salesOrder) {
        try {
            log.info("sent order to queue! {}");

//            rabbitTemplate.convertAndSend(orderQueue, salesOrder);
            rabbitTemplate.convertAndSend(orderExchangeName, orderRoutingKey, salesOrder);
            log.info("Order sent to queue！ {}");
        } catch (AmqpException e) {
            e.printStackTrace();
            log.error("Failed to send order to queue: {}", e.getMessage());
            // 可能需要错误处理逻辑
        }
    }

}
