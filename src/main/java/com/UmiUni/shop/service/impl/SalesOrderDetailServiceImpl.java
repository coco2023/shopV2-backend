package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.entity.SalesOrderDetail;
import com.UmiUni.shop.repository.SalesOrderDetailRepository;
import com.UmiUni.shop.service.SalesOrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SalesOrderDetailServiceImpl implements SalesOrderDetailService {

    @Autowired
    private SalesOrderDetailRepository salesOrderDetailRepository;

    @Override
    public SalesOrderDetail createSalesOrderDetail(SalesOrderDetail salesOrderDetail) {
        return salesOrderDetailRepository.save(salesOrderDetail);
    }

    @Override
    public SalesOrderDetail getSalesOrderDetail(Long id) {
        return salesOrderDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SalesOrderDetail not found with id: " + id));
    }

    @Override
    public List<SalesOrderDetail> getAllSalesOrderDetails() {
        return salesOrderDetailRepository.findAll();
    }

    @Override
    public SalesOrderDetail updateSalesOrderDetail(Long id, SalesOrderDetail salesOrderDetailDetails) {
        SalesOrderDetail salesOrderDetail = getSalesOrderDetail(id);
        salesOrderDetail.setSalesOrderSn(salesOrderDetailDetails.getSalesOrderSn());
        salesOrderDetail.setSkuCode(salesOrderDetailDetails.getSkuCode());
        salesOrderDetail.setQuantity(salesOrderDetailDetails.getQuantity());
        salesOrderDetail.setUnitPrice(salesOrderDetailDetails.getUnitPrice());
        salesOrderDetail.setLineTotal(salesOrderDetailDetails.getLineTotal());
        return salesOrderDetailRepository.save(salesOrderDetail);
    }

    @Override
    public void deleteSalesOrderDetail(Long id) {
        salesOrderDetailRepository.deleteById(id);
    }
}
