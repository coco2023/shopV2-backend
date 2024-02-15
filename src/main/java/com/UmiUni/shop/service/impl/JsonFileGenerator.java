package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.model.DailyReport;
import com.UmiUni.shop.service.ReportFileGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.Map;

public class JsonFileGenerator implements ReportFileGenerator {
    @Override
    public void generateFile(Map<LocalDate, DailyReport> reportMap, OutputStream outputStream) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(outputStream, reportMap);
    }
}
