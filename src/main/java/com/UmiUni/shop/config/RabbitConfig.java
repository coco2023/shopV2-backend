package com.UmiUni.shop.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@Log4j2
public class RabbitConfig {

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());  // 注册JavaTimeModule
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);  // 禁用日期时间戳格式
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory, final Jackson2JsonMessageConverter converter) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter);
        return rabbitTemplate;
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            builder.modules(new JavaTimeModule());
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        };
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // 注册JavaTimeModule
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 禁用日期时间戳格式
        return objectMapper;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    /**
     * Settings of order Queue
     */
    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable("order_queue")
                .deadLetterExchange("order_dlx_exchange")
                .deadLetterRoutingKey("order_dlx")
                .ttl(180000) // 3 min
                .build();
    }

    @Bean
    public DirectExchange businessExchange() {
        return new DirectExchange("order_queue_exchange");
    }

    @Bean
    public Binding businessBinding(Queue orderQueue, DirectExchange businessExchange) {
        return BindingBuilder.bind(orderQueue).to(businessExchange).with("order_queue_routing");
    }

    // 配置死信交换器: 声明交换机：这可以是任意类型的交换机（direct, topic, fanout, headers) order_dlx_exchange
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("order_dlx_exchange");
    }

    // 配置死信队列: 声明死信队列：这个队列就是用来接收死信的。order_dlx_queue
    @Bean
    public Queue orderDlxQueue() {
        return new Queue("order_dlx_queue", true);
    }

    // 绑定死信队列和死信交换器: 绑定DLX到死信队列：这样DLX上的消息就会路由到这个死信队列
    @Bean
    public Binding dlxBinding(Queue orderDlxQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(orderDlxQueue).to(deadLetterExchange).with("order_dlx");
    }

    /**
     * Settings of Lock Queue
     */
    @Bean
    public Queue inventoryLockQueue() {
        return QueueBuilder.durable("inventory_lock_queue")
                .deadLetterExchange("inventory_lock_dlx_exchange")
                .deadLetterRoutingKey("inventory_lock_dlx_routing")
                .ttl(180000)
                .build();
    }

//    @Bean
//    public DirectExchange inventoryLockExchange() {
//        return new DirectExchange("inventory_lock_queue_exchange");
//    }
    @Bean
    public DirectExchange inventoryLockExchange() {
        return new DirectExchange("inventory_lock_queue_exchange");
    }

    //    @Bean
//    public Binding inventoryLockBinding(Queue inventoryLockQueue, DirectExchange inventoryLockExchange) {
//        return BindingBuilder.bind(inventoryLockQueue).to(inventoryLockExchange).with("inventory_lock_queue_routing");
//    }
//    @Bean
//    public Binding inventoryLockBinding(Queue inventoryLockQueue, DirectExchange inventoryLockExchange) {
//        return BindingBuilder.bind(inventoryLockQueue).to(inventoryLockExchange).with("inventory_lock_queue_routing");
//    }
    @Bean
    public Binding inventoryLockBinding(Queue inventoryLockQueue, DirectExchange inventoryLockExchange) {
        return BindingBuilder.bind(inventoryLockQueue).to(inventoryLockExchange).with("inventory_lock_queue_routing");
    }

//    // Dead Letter Queue (DLQ): inventory_lock_dlq
//    @Bean
//    public Queue inventoryLockDlxQueue() {
//        return new Queue("inventory_lock_dlx_queue", true);
//    }
    // 配置死信队列: 声明死信队列：这个队列就是用来接收死信的。order_dlx_queue
    @Bean
    public Queue inventoryLockDlxQueue() {
        return new Queue("inventory_lock_dlx_queue", true);
    }

//    // Dead Letter Exchange (DLX): inventory_lock_dlx
//    @Bean
//    public DirectExchange inventoryLockDlx() {
//        return new DirectExchange("inventory_lock_dlx_exchange");
//    }
    // 配置死信交换器: 声明交换机：这可以是任意类型的交换机（direct, topic, fanout, headers) order_dlx_exchange
    @Bean
    public DirectExchange inventoryLockDlx() {
        return new DirectExchange("inventory_lock_dlx_exchange");
    }

    // Binding of DLQ and DLX
//    @Bean
//    public Binding inventoryLockDlxBinding(Queue inventoryLockDlxQueue, DirectExchange inventoryLockDlx) {
//        return BindingBuilder.bind(inventoryLockDlxQueue).to(inventoryLockDlx).with("inventory_lock_dlx_routing");
//    }
    // 绑定死信队列和死信交换器: 绑定DLX到死信队列：这样DLX上的消息就会路由到这个死信队列
    @Bean
    public Binding inventoryLockDlxBinding(Queue inventoryLockDlxQueue, DirectExchange inventoryLockDlx) {
        return BindingBuilder.bind(inventoryLockDlxQueue).to(inventoryLockDlx).with("inventory_lock_dlx_routing");
    }

    /**
     * settings of inventory reduction queue
     */
    @Bean
    public Queue inventoryReductionQueue() {
        return new Queue("inventory_reduction_queue", true, false, false, Map.of("x-message-ttl", 180000));
    }

    @Bean
    public DirectExchange inventoryReductionExchange() {
        return new DirectExchange("inventory_reduction_queue_exchange");
    }

    @Bean
    public Binding inventoryReductionBinding(Queue inventoryReductionQueue, DirectExchange inventoryReductionExchange) {
        return BindingBuilder.bind(inventoryReductionQueue).to(inventoryReductionExchange).with("inventory_reduction_queue_routing");
    }

}
