package com.UmiUni.shop.model;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class PaypalTransactionRecord {

//    @CsvBindByPosition(position = 0)
//    private String date;
//
//    @CsvBindByPosition(position = 1)
//    private String time;
//
//    @CsvBindByPosition(position = 2)
//    private String timeZone;
//
//    @CsvBindByPosition(position = 5)
//    private String status;
//
//    @CsvBindByPosition(position = 6)
//    private String currency;

    @CsvBindByPosition(position = 8)
    private String fees;

    @CsvBindByPosition(position = 9) // Assuming 'Net Amount' is at position 11
    private String net;

    @CsvBindByPosition(position = 12) // Assuming 'Net Amount' is at position 11
    private String transactionId;

    @CsvBindByPosition(position = 26)
    private String salesOrderSn;    // Custom Number

//    @CsvBindByPosition(position = 1)
//    private String paymentsReceived;
//
//    @CsvBindByPosition(position = 2)
//    private String amountReceived;


}
