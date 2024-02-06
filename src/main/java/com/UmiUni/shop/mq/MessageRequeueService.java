package com.UmiUni.shop.mq;

import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class MessageRequeueService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 重新入队消息的方法。
     *
     * @param exchangeName 交换机名称。
     * @param routingKey   路由键。
     * @param message      需要重新入队的消息。
     * @param retryHeader  重试次数的头部名称。
     * @param maxRetries   最大重试次数。
     */
    public void requeueMessage(String exchangeName, String routingKey, Message message, String retryHeader, int maxRetries) {
        Integer retriesHeader = (Integer) message.getMessageProperties()
                                            .getHeaders().getOrDefault(retryHeader, 0);

        if (retriesHeader < maxRetries) {
            // 增加重试次数
            message.getMessageProperties().setHeader(retryHeader, retriesHeader + 1);

            // 重新发布消息到原始交换机和路由键
            rabbitTemplate.send(exchangeName, routingKey, message);
        } else {
            // 超过重试次数，可以选择记录日志、发送警告或将消息转移到另一个“错误队列”
            log.error("Retries limit exceeded for message with routing key {}. Sending to error queue or handling accordingly.", routingKey);
            // 此处添加发送到错误队列的逻辑
        }
    }
}
