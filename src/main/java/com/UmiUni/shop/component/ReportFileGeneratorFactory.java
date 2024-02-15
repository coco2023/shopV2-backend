package com.UmiUni.shop.component;

import com.UmiUni.shop.service.ReportFileGenerator;
import com.UmiUni.shop.service.impl.CsvFileGenerator;
import com.UmiUni.shop.service.impl.JsonFileGenerator;
import org.springframework.stereotype.Component;

@Component
public class ReportFileGeneratorFactory {
    public ReportFileGenerator createFileGenerator(String type) {
        if (type.equalsIgnoreCase("JSON")) {
            return new JsonFileGenerator();
        } else if (type.equalsIgnoreCase("CSV")) {
            return new CsvFileGenerator();
        }
        throw new IllegalArgumentException("Unsupported file type: " + type);
    }
}
