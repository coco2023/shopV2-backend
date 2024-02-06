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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Log4j2
public class RabbitConfig {

    @Value("${rabbitmq.queues.order_process.name}")
    private String orderQueueName;

    @Value("${rabbitmq.queues.order_process.ttl}")
    private int orderQueueTTL;

    @Value("${rabbitmq.dlx.exchange.name}")
    private String dlxExchangeName;

    @Value("${rabbitmq.dlx.queue.name}")
    private String dlxQueueName;

    @Value("${rabbitmq.dlx.routing-key}")
    private String dlxRoutingKey;

//    @Bean
//    public Queue orderQueue() {
//        return new Queue(orderQueueName, true); // true 表示队列是持久的
//    }

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

    // 配置订单队列，设置消息TTL和DLX: 声明队列：在声明队列时，需要指定一些参数，将队列与DLX绑定
//    @Bean
//    public Queue orderQueue() {
//        Map<String, Object> args = new HashMap<>();
//        log.info("Attempting to declare order_queue with x-message-ttl: {}", orderQueueTTL);
//        args.put("x-message-ttl", 0);  // "60000" Long.valueOf(60000)
//        args.put("x-dead-letter-exchange", dlxExchangeName); //"dlx_exchange"
//        args.put("x-dead-letter-routing-key", dlxRoutingKey); //"dlx_order"
//        return new Queue(orderQueueName, true, false, false, args);
//    }
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

    // 配置死信交换器: 声明交换机：这可以是任意类型的交换机（direct, topic, fanout, headers)
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("order_dlx_exchange");
    }

    // 配置死信队列: 声明死信队列：这个队列就是用来接收死信的。
    @Bean
    public Queue dlxQueue() {
        return new Queue("order_dlx_queue", true);
    }

    // 绑定死信队列和死信交换器: 绑定DLX到死信队列：这样DLX上的消息就会路由到这个死信队列
    @Bean
    public Binding dlxBinding(Queue dlxQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(dlxQueue).to(deadLetterExchange).with("order_dlx");
    }

}
