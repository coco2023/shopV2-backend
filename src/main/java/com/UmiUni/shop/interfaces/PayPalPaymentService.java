package com.UmiUni.shop.interfaces;

import com.UmiUni.shop.constant.OrderStatus;
import com.UmiUni.shop.dto.SalesOrderDTO;
import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.entity.SalesOrderDetail;
import com.UmiUni.shop.exception.PaymentProcessingException;
import com.UmiUni.shop.interfaces.impl.PayPalPaymentStrategy;
import com.UmiUni.shop.model.InventoryUpdateMessage;
import com.UmiUni.shop.model.PaymentResponse;
import com.UmiUni.shop.mq.RabbitMQSender;
import com.UmiUni.shop.repository.SalesOrderDetailRepository;
import com.UmiUni.shop.repository.SalesOrderRepository;
import com.UmiUni.shop.service.InventoryService;
import com.UmiUni.shop.service.SalesOrderDetailService;
import com.UmiUni.shop.service.SalesOrderService;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Log4j2
public class PayPalPaymentService {

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private SalesOrderService salesOrderService;

    @Autowired
    private APIContextFactory apiContextFactory;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private SalesOrderDetailRepository salesOrderDetailRepository;

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public PaymentResponse createPayment(SalesOrderDTO salesOrderRequest) {

        log.info("start create payment: {}", salesOrderRequest.getSalesOrderSn());
        SalesOrder salesOrder = getSalesOrder(salesOrderRequest.getSalesOrderSn());
        validateOrder(salesOrder); // Validate order before processing
        log.info("Payment Created Test!");

        APIContext apiContext = apiContextFactory.createApiContextForSupplier(salesOrder.getSupplierId());

        // Strategy can be chosen based on order or system config
        PaymentStrategy paymentStrategy = getPaymentStrategy(salesOrder);
        log.info("Payment strategy: {}", paymentStrategy);

        PaymentResponse paymentResponse;
        try {
            paymentResponse = paymentStrategy.createPayPal(salesOrder, apiContext);
            log.info("Payment Created Test!");
            postPaymentProcessing(salesOrder);
            log.info("Inventory update!");
            return paymentResponse;
        } catch (PaymentProcessingException e) {
            log.error("Payment processing failed", e);
            return handleError(e, salesOrder); // Centralized error handling
        } catch (PayPalRESTException e) {
            return handleError(e, salesOrder); // Centralized error handling
        }
    }

    private SalesOrder getSalesOrder(String salesOrderSn) {
        return salesOrderRepository.getSalesOrderBySalesOrderSn(salesOrderSn)
                .orElseThrow();
    }

    private PaymentStrategy getPaymentStrategy(SalesOrder salesOrder) {
        // This can be enhanced to select different strategies based on the sales order details
        return new PayPalPaymentStrategy(); // Default to PayPal for illustration
    }

    private void validateOrder(SalesOrder salesOrder) {
        // Implement order validation logic
        // Throw exceptions or return errors if validation fails
    }

    private void postPaymentProcessing(SalesOrder salesOrder) {
        // Update order status and lock inventory
        updateOrderStatus(salesOrder);
        lockProductInventory(salesOrder);
    }

    private void lockProductInventory(SalesOrder salesOrder) {
        List<SalesOrderDetail> salesOrderDetails = salesOrderDetailRepository.findSalesOrderDetailsBySalesOrderSn(salesOrder.getSalesOrderSn());
        for (SalesOrderDetail detail : salesOrderDetails) {
            String skuCode = detail.getSkuCode();
            int quantity = detail.getQuantity();
            InventoryUpdateMessage message = new InventoryUpdateMessage(skuCode, quantity);
            rabbitMQSender.sendInventoryLock(message); // Assuming rabbitMQSender is available in your service
            log.info("Inventory lock message sent for SKU: " + skuCode + ", Quantity: " + quantity);
        }
    }

    // system requires immediate consistency (the order status needs to be updated right away before proceeding), using RabbitMQ might introduce eventual consistency, which might not be desirable
    private void updateOrderStatus(SalesOrder salesOrder) {
        salesOrder.setOrderStatus(OrderStatus.CREATED);
        salesOrder.setLastUpdated(LocalDateTime.now());
        salesOrderService.updateSalesOrder(salesOrder.getSalesOrderId(), salesOrder);
        log.info("Order status updated to PENDING for SalesOrderSn: " + salesOrder.getSalesOrderSn());
    }

    private PaymentResponse handleError(Exception e, SalesOrder salesOrder) {
        // Implement error handling logic
        // Log error, update order status if necessary, and prepare error response
        log.error("Error processing payment for order " + salesOrder.getSalesOrderSn(), e);
        salesOrder.setOrderStatus(OrderStatus.FAILED);
        updateOrderStatus(salesOrder); // Update order status to indicate failure

        return new PaymentResponse("FAILED", null, "Payment processing failed due to an error.");
    }

}
