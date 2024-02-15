package com.UmiUni.shop.service;

import com.UmiUni.shop.model.DailyReport;

import java.util.Map;
import java.time.LocalDate;
import java.io.IOException;
import java.io.OutputStream;

public interface ReportFileGenerator {
    void generateFile(Map<LocalDate, DailyReport> reportMap, OutputStream outputStream) throws IOException;
}
