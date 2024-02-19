package com.UmiUni.shop.mq;

import com.UmiUni.shop.constant.PaymentStatus;
import com.UmiUni.shop.dto.SalesOrderDTO;
import com.UmiUni.shop.entity.PayPalPayment;
import com.UmiUni.shop.interfaces.PayPalPaymentService;
import com.UmiUni.shop.model.PaymentResponse;
import com.UmiUni.shop.repository.PayPalPaymentRepository;
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
    private PayPalService payPalService;

    @Autowired
    private PayPalPaymentService payPalPaymentService;

    @Autowired
    private PayPalPaymentRepository payPalPaymentRepository;

    @Autowired
    private SalesOrderService salesOrderService;

    @Autowired
    @Qualifier("asyncExecutor") // 使用 @Qualifier 指定注入的 Executor Bean
    private Executor asyncExecutor;

    private static final long POLLING_INTERVAL_MS = 6000; // 轮询间隔，例如6秒
    private static final long PAYMENT_TIMEOUT_MS = 30000; // 支付超时时间，例如12s后

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MessageRequeueService messageRequeueService;

    //    @RabbitListener(queuesToDeclare = @Queue("${rabbitmq.queues.order_process.name}"))
    @RabbitListener(queues = "#{@orderQueue}")
    public void onOrderReceived(Message message, Channel channel) throws IOException {
        try {
            SalesOrderDTO salesOrder = convertMessageToSalesOrderDTO(message);
            log.info("开始创建订单: {}", salesOrder.getSalesOrderSn());

//            // 这里使用同步处理，但会产生死信队列无法接收过期消息的情况；故使用异步处理
//            // 在这里进行订单处理，例如验证订单、检查库存、保存订单到数据库等
//            payPalService.createPayment(salesOrder);

//            // 异步执行创建支付，并在支付创建成功后启动轮询支付状态. 使用CompletableFuture异步执行同步的支付处理方法
            CompletableFuture<PaymentResponse> paymentFuture = CompletableFuture.supplyAsync(() -> {
                log.info("使用CompletableFuture异步执行同步的支付");
                PaymentResponse createPaymentResponse = payPalService.createPayment(salesOrder);
//                PaymentResponse createPaymentResponse = payPalPaymentService.createPayment(salesOrder);
                log.info("异步PaymentResponse: {}", createPaymentResponse);
                String tansId = createPaymentResponse.getTransactionId();
                return createPaymentResponse;
            }, asyncExecutor); // 使用Spring管理的异步执行器来执行异步任务

            paymentFuture.thenAccept(paymentResponse -> {
                log.info("异步处理订单, Payment creation response received: {}", paymentResponse);

                // 如果检测到特定的错误条件，比如支付超时
                boolean paymentTimedOut = checkPaymentTimeout(salesOrder.getOrderDate());
                if (paymentTimedOut) {
                    // 显式拒绝消息，并且不重新入队
                    try {
                        log.info("Payment timeout!");
                        channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return; // 结束方法执行
                }

                // deal with the payment response: if payment has been created successfully, continue; else try to re-enter the queue
                // get the payment entity by transId
                if (paymentResponse.getStatus().equals(PaymentStatus.CREATED.name())) {

//                handlePaymentResponse(paymentResponse, salesOrder, channel, message);
                    // 创建支付成功，启动轮询检查支付状态
                    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
                    AtomicBoolean paymentCompleted = new AtomicBoolean(false);

                    log.info("后端轮询检查状态");
                    ScheduledFuture<?> pollingTask = scheduler.scheduleAtFixedRate(() -> {
                        PaymentResponse completeResponse = payPalService.checkCompletePaymentStatus(salesOrder.getSalesOrderSn());
                        // if the PaymentStatus is "SUCCESS", send the complete ack
                        log.info("后端轮询检查状态-支付是否结束 {}, completeResponse: {}, pollingTask: ", completeResponse.getStatus(), completeResponse, paymentCompleted);
                        if (completeResponse.getStatus() == PaymentStatus.SUCCESS.name()) {  // 轮询会失败
                            paymentCompleted.set(true);
                            scheduler.shutdown();
                            safelyAcknowledge(channel, message);
                        }
                    }, 0, POLLING_INTERVAL_MS, TimeUnit.MILLISECONDS);
//
                    // 设置超时以停止轮询
                    scheduler.schedule(() -> {
                        if (!paymentCompleted.get()) {
                            pollingTask.cancel(true);
                            scheduler.shutdown();
                            safelyAcknowledge(channel, message);
                        }
                    }, PAYMENT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                } else {
                    // 创建支付失败，处理失败逻辑
                    log.error("Failed to create payment for salesOrderSn: {}", salesOrder.getSalesOrderSn());
                    safelyAcknowledge(channel, message);
                }
            }).exceptionally(ex -> {
                // 支付处理失败，拒绝消息并可选择是否重新入队
                safeNack(channel, message);
                return null;
            });

        } catch (Exception e) {
            // 如果在启动异步支付处理之前发生异常，拒绝消息并可选择是否重新入队
            // 可以在这里实现错误处理逻辑，如重试或将失败的订单信息发送到另一个队列进行进一步处理
            log.error("Failed to process order asynchronously: {}", e.getMessage());
            safeNack(channel, message);
        }
    }

    private void handlePaymentResponse(PaymentResponse paymentResponse, SalesOrderDTO salesOrder, Channel channel, Message message) {
        if (paymentResponse.getStatus().equals(PaymentStatus.CREATED.name())) {
            log.info("Payment created successfully, starting to poll for payment status for salesOrderSn: {}", salesOrder.getSalesOrderSn());
            startPollingPaymentStatus(paymentResponse, salesOrder, channel, message);
        } else {
            log.error("Payment creation failed for salesOrderSn: {}", salesOrder.getSalesOrderSn());
            safelyAcknowledge(channel, message);
        }
    }

    private void startPollingPaymentStatus(PaymentResponse paymentResponse, SalesOrderDTO salesOrder, Channel channel, Message message) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        AtomicBoolean paymentCompleted = new AtomicBoolean(false);

        ScheduledFuture<?> pollingTask = scheduler.scheduleAtFixedRate(() -> {
            log.info("Polling for payment status for salesOrderSn: {}", salesOrder.getSalesOrderSn());
//            PaymentResponse completeResponse = payPalService.checkCompletePaymentStatus(salesOrder.getSalesOrderSn());
            PayPalPayment payPalPayment = payPalService.checkCompleteStatusByTransactionId(paymentResponse.getTransactionId());

            if (payPalPayment.getStatus().equals(PaymentStatus.SUCCESS.name())) {
                paymentCompleted.set(true);
                scheduler.shutdown();
                safelyAcknowledge(channel, message);
            }
        }, 0, POLLING_INTERVAL_MS, TimeUnit.MILLISECONDS);

        scheduler.schedule(() -> {
            if (!paymentCompleted.get()) {
                pollingTask.cancel(true);
                scheduler.shutdown();
                log.error("Payment polling timeout for salesOrderSn: {}", salesOrder.getSalesOrderSn());
                safelyAcknowledge(channel, message);
            }
        }, PAYMENT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    private void safelyAcknowledge(Channel channel, Message message) {
        try {
            log.info("send the success ACK!");
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            log.error("Failed to acknowledge message for salesOrderSn: {}", convertMessageToSalesOrderDTO(message).getSalesOrderSn(), e);
        }
    }

    private void safeNack(Channel channel, Message message) {
        try {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        } catch (IOException ex) {
            log.error("Failed to send negative acknowledgment for message: {}", ex.getMessage());
            // Consider additional error handling here, such as alerting or logging to an external system
        }
    }

    public boolean checkPaymentTimeout(LocalDateTime orderDate) {
        // 定义订单有效期（例如，30分钟）
        final long PAYMENT_TIMEOUT_MINUTES = 30;

        // 计算从订单创建到现在的时间差
        long minutesSinceOrderCreated = ChronoUnit.MINUTES.between(orderDate, LocalDateTime.now());

        log.info("计算从订单创建到现在的时间差: {}, 时间差: {}", orderDate, minutesSinceOrderCreated);

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
