package com.UmiUni.shop.mq;

import com.UmiUni.shop.exception.AmqpException;
import com.UmiUni.shop.model.InventoryUpdateMessage;
import com.UmiUni.shop.service.PaymentErrorHandlingService;
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

}
