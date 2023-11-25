package com.UmiUni.shop.service;

import com.UmiUni.shop.entity.Invoice;

import java.util.List;

public interface InvoiceService {
    Invoice createInvoice(Invoice invoice);
    Invoice getInvoice(Long id);
    List<Invoice> getAllInvoices();
    Invoice updateInvoice(Long id, Invoice invoiceDetails);
    void deleteInvoice(Long id);
}
