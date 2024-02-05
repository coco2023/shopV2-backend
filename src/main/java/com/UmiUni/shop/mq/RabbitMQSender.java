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
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class RabbitMQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;
//    private final RabbitTemplate rabbitTemplate;

//    @Autowired
//    public RabbitMQSender(RabbitTemplate rabbitTemplate, Jackson2JsonMessageConverter messageConverter) {
//        this.rabbitTemplate = rabbitTemplate;
//        this.rabbitTemplate.setMessageConverter(messageConverter);
//    }

    @Value("${inventory.queue.name}")
    private String inventoryQueue;

    @Value("${inventory.lock.queue.name}")
    private String inventoryLockQueue;

    @Value("${inventory.reduce.queue.name}")
    private String inventoryReductionQueue;

    @Value("${rabbitmq.queues.order_process.name}")
    private String orderQueue;

    @Value("${rabbitmq.queues.order_process.exchange.name}")
    private String orderExchangeName;

    @Value("${rabbitmq.queues.order_process.routing-key}")
    private String orderRoutingKey;


    @Autowired
    private PaymentErrorHandlingService paymentErrorHandlingService;

    public void sendInventoryUpdate(InventoryUpdateMessage message) {
        rabbitTemplate.convertAndSend(inventoryQueue, message);
    }

    public void sendInventoryLock(InventoryUpdateMessage message) {
        try {
            rabbitTemplate.convertAndSend(inventoryLockQueue, message);
            log.info("sendInventoryLock! " + message);
//            throw new AmqpException("error test for AmqpException!");
        } catch (AmqpException e) {
            paymentErrorHandlingService.handleAmqpException(e, message);
        }
    }

    public void sendInventoryReduction(InventoryUpdateMessage message) {
        try {
            rabbitTemplate.convertAndSend(inventoryReductionQueue, message);
            log.info("sendInventoryReduction! " + message);
        } catch (AmqpException e) {
            paymentErrorHandlingService.handleAmqpException(e, message);
        }
    }

    public void sendOrder(SalesOrderDTO salesOrder) {
        try {
            log.info("sent order to queue!! {}", salesOrder );

//            rabbitTemplate.convertAndSend(orderQueue, salesOrder);
            rabbitTemplate.convertAndSend(orderExchangeName, orderRoutingKey, salesOrder);
            log.info("Order sent to queue: {}", salesOrder);
        } catch (AmqpException e) {
            e.printStackTrace();
            log.error("Failed to send order to queue: {}", e.getMessage());
            // 可能需要错误处理逻辑
        }
    }

}
