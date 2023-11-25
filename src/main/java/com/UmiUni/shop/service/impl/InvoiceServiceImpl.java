package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.entity.Invoice;
import com.UmiUni.shop.repository.InvoiceRepository;
import com.UmiUni.shop.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Override
    public Invoice createInvoice(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }

    @Override
    public Invoice getInvoice(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + id));
    }

    @Override
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    @Override
    public Invoice updateInvoice(Long id, Invoice invoiceDetails) {
        Invoice invoice = getInvoice(id);
        invoice.setSalesOrderId(invoiceDetails.getSalesOrderId());
        invoice.setIssueDate(invoiceDetails.getIssueDate());
        invoice.setDueDate(invoiceDetails.getDueDate());
        invoice.setItemsTotal(invoiceDetails.getItemsTotal());
        invoice.setItemsDiscount(invoiceDetails.getItemsDiscount());
        invoice.setSubTotal(invoiceDetails.getSubTotal());
        invoice.setTaxAmount(invoiceDetails.getTaxAmount());
        invoice.setShippingAmount(invoiceDetails.getShippingAmount());
        invoice.setTotalAmount(invoiceDetails.getTotalAmount());
        invoice.setPaidStatus(invoiceDetails.getPaidStatus());
        // other updates as needed
        return invoiceRepository.save(invoice);
    }

    @Override
    public void deleteInvoice(Long id) {
        invoiceRepository.deleteById(id);
    }
}
