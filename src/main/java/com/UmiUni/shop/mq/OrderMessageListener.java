package com.UmiUni.shop.mq;

import com.UmiUni.shop.constant.PaymentStatus;
import com.UmiUni.shop.dto.SalesOrderDTO;
import com.UmiUni.shop.model.PaymentResponse;
import com.UmiUni.shop.service.PayPalService;
import com.UmiUni.shop.service.SalesOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.amqp.core.Message;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Log4j2
public class OrderMessageListener {

    @Autowired
    private PayPalService payPalService; // 假设这是一个服务类，用于处理订单逻辑

    @Autowired
    private SalesOrderService salesOrderService;

    @Autowired
    @Qualifier("asyncExecutor") // 使用 @Qualifier 指定注入的 Executor Bean
    private Executor asyncExecutor;

    private static final long POLLING_INTERVAL_MS = 5000; // 轮询间隔，例如5秒
    private static final long PAYMENT_TIMEOUT_MS = 600000; // 支付超时时间，例如10分钟后

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MessageRequeueService messageRequeueService;

    //    @RabbitListener(queuesToDeclare = @Queue("${rabbitmq.queues.order_process.name}"))
    @RabbitListener(queues = "#{@orderQueue}")
    public void onOrderReceived(Message message, Channel channel) throws IOException {
        try {
            SalesOrderDTO salesOrder = convertMessageToSalesOrderDTO(message);
            log.info("Asynchronously processing order for salesOrderSn: {}", salesOrder.getSalesOrderSn());

//            // 这里使用同步处理，但会产生死信队列无法接收过期消息的情况；故使用异步处理
//            // 在这里进行订单处理，例如验证订单、检查库存、保存订单到数据库等
//            payPalService.createPayment(salesOrder);

            // 异步执行创建支付，并在支付创建成功后启动轮询支付状态
            // 使用CompletableFuture异步执行同步的支付处理方法
            CompletableFuture<PaymentResponse> paymentFuture = CompletableFuture.supplyAsync(() -> {
                return payPalService.createPayment(salesOrder);
            }, asyncExecutor); // 使用Spring管理的异步执行器来执行异步任务

            paymentFuture.thenAccept(paymentResponse -> {

                // 如果检测到特定的错误条件，比如支付超时
                boolean paymentTimedOut = checkPaymentTimeout(salesOrder.getOrderDate());
                if (paymentTimedOut) {
                    // 显式拒绝消息，并且不重新入队
                    try {
                        channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return; // 结束方法执行
                }

                // deal with the payment response: if payment has been created successfully, continue; else try to re-enter the queue
                if (paymentResponse.getStatus() == PaymentStatus.CREATED.name()) {
                    // 创建支付成功，启动轮询检查支付状态
                    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
                    AtomicBoolean paymentCompleted = new AtomicBoolean(false);

                    ScheduledFuture<?> pollingTask = scheduler.scheduleAtFixedRate(() -> {
                        PaymentResponse completeResponse = payPalService.checkCompletePaymentStatus(salesOrder.getSalesOrderSn());
                        // if the PaymentStatus is "SUCCESS", send the complete ack
                        if (completeResponse.getStatus() == PaymentStatus.SUCCESS.name()) {
                            paymentCompleted.set(true);
                            scheduler.shutdown();
                            try {
                                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                                log.info("Payment completed successfully for salesOrderSn: {}", salesOrder.getSalesOrderSn());
                            } catch (IOException e) {
                                log.error("Failed to acknowledge message: {}", e.getMessage());
                            }
                        }
                    }, 0, POLLING_INTERVAL_MS, TimeUnit.MICROSECONDS);

                    // 设置超时以停止轮询
                    scheduler.schedule(() -> {
                        if (!paymentCompleted.get()) {
                            pollingTask.cancel(true);
                            scheduler.shutdown();
                            try {
                                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
                                log.error("Payment timeout for salesOrderSn: {}", salesOrder.getSalesOrderSn());
                            } catch (IOException e) {
                                log.error("Failed to nack message: {}", e.getMessage());
                            }
                        }
                    }, PAYMENT_TIMEOUT_MS, TimeUnit.MICROSECONDS);
                } else {
                    // 创建支付失败，处理失败逻辑
                    log.error("Failed to create payment for salesOrderSn: {}", salesOrder.getSalesOrderSn());
                    // 重新入队
                    messageRequeueService.requeueMessage(
                            message.getMessageProperties().getReceivedExchange(),
                            message.getMessageProperties().getReceivedRoutingKey(),
                            message,
                            "x-retries",
                            3 // 假设最大重试次数为3
                    );
                    // 确认原消息，避免消息被RabbitMQ再次投递
                    try {
                        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                    } catch (IOException ioex) {
                        log.error("Failed to ack message: {}", ioex.getMessage());
                    }
                }
            }).exceptionally(ex -> {
                // 支付处理失败，拒绝消息并可选择是否重新入队
                // 重新入队
                messageRequeueService.requeueMessage(
                        message.getMessageProperties().getReceivedExchange(),
                        message.getMessageProperties().getReceivedRoutingKey(),
                        message,
                        "x-retries",
                        3 // 假设最大重试次数为3
                );
                // 确认原消息，避免消息被RabbitMQ再次投递
                try {
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                } catch (IOException ioex) {
                    log.error("Failed to ack message: {}", ioex.getMessage());
                }
                return null;
            });

        } catch (Exception e) {
            // 如果在启动异步支付处理之前发生异常，拒绝消息并可选择是否重新入队
            // 可以在这里实现错误处理逻辑，如重试或将失败的订单信息发送到另一个队列进行进一步处理
            log.error("Failed to process order asynchronously: {}", e.getMessage());

            // 重新入队
            messageRequeueService.requeueMessage(
                    message.getMessageProperties().getReceivedExchange(),
                    message.getMessageProperties().getReceivedRoutingKey(),
                    message,
                    "x-retries",
                    3 // 假设最大重试次数为3
            );
            // 确认原消息，避免消息被RabbitMQ再次投递
            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException ex) {
                log.error("Failed to ack message: {}", ex.getMessage());
            }
        }
    }

    public boolean checkPaymentTimeout(LocalDateTime orderDate) {
        // 定义订单有效期（例如，30分钟）
        final long PAYMENT_TIMEOUT_MINUTES = 30;

        // 计算从订单创建到现在的时间差
        long minutesSinceOrderCreated = ChronoUnit.MINUTES.between(orderDate, LocalDateTime.now());

        // 如果时间差超过了定义的超时时间，则认为支付超时
        return minutesSinceOrderCreated > PAYMENT_TIMEOUT_MINUTES;
    }

    private SalesOrderDTO convertMessageToSalesOrderDTO(Message message) {
        try {
            // 假设消息体是 JSON 格式，并且能够被直接映射到 SalesOrderDTO 类
            // 使用已配置的 ObjectMapper 实例进行反序列化
            return objectMapper.readValue(message.getBody(), SalesOrderDTO.class);
        } catch (IOException e) {
            log.error("Error converting message to SalesOrderDTO", e);
            throw new RuntimeException("Error converting message to SalesOrderDTO", e);
        }
    }

//    @RabbitListener(queues = "#{@orderQueue}")
//    public void onOrderReceived(SalesOrderDTO salesOrder) {
//        log.info("Asynchronously processing order for salesOrderSn: {}", salesOrder.getSalesOrderSn());
//        log.info("sales order: {}", salesOrderService.getSalesOrderBySalesOrderSn(salesOrder.getSalesOrderSn()));
//
//        try {
//            // 在这里进行订单处理，例如验证订单、检查库存、保存订单到数据库等
//            payPalService.createPayment(salesOrder);
//        } catch (Exception e) {
//            log.error("Failed to process order asynchronously: {}", e.getMessage());
//            // 可以在这里实现错误处理逻辑，如重试或将失败的订单信息发送到另一个队列进行进一步处理
//        }
//    }

}
