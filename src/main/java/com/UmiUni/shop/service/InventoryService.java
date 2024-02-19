package com.UmiUni.shop.service;

import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.entity.SalesOrderDetail;
import com.UmiUni.shop.model.InventoryUpdateMessage;
import com.UmiUni.shop.mq.RabbitMQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Autowired
    private SalesOrderDetailService salesOrderDetailService;

    public void lockInventoryForOrder(SalesOrder salesOrder) {
        List<SalesOrderDetail> salesOrderDetails = salesOrderDetailService.getSalesOrderDetailsBySalesOrderSn(salesOrder.getSalesOrderSn());

        for (SalesOrderDetail detail : salesOrderDetails) {
            String skuCode = detail.getSkuCode();
            int quantity = detail.getQuantity();
            InventoryUpdateMessage message = new InventoryUpdateMessage(skuCode, quantity);
            rabbitMQSender.sendInventoryLock(message);
        }
    }

}
