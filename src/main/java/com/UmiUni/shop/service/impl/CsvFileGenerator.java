package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.model.DailyReport;
import com.UmiUni.shop.service.ReportFileGenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class CsvFileGenerator implements ReportFileGenerator {
    @Override
    public void generateFile(Map<LocalDate, DailyReport> reportMap, OutputStream outputStream) throws IOException {
        try (PrintWriter pw = new PrintWriter(outputStream)) {
            pw.println("Date,Payments Received,Amount Received,Fees,Net Amount");
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            reportMap.forEach((date, report) -> {
                String line = String.join(",",
                        dateFormatter.format(date),
                        String.valueOf(report.getPaymentsReceived()),
                        report.getTotalAmountReceived().toString(),
                        report.getFees().toString(),
                        report.getNetAmount().toString()
                );
                pw.println(line);
            });
        }
    }
}
