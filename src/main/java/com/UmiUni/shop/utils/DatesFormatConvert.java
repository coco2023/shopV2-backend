package com.UmiUni.shop.utils;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Log4j2
@Service
public class DatesFormatConvert {

    public List<LocalDateTime> convertFinancialDateFormat(String start, String rangeType) {
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            startDateTime = LocalDate.parse(start, formatter).atStartOfDay();

            if (rangeType.equals("daily")) {
                endDateTime = startDateTime.plusDays(1);
            } else if (rangeType.equals("monthly")) {
                startDateTime = startDateTime.with(TemporalAdjusters.firstDayOfMonth());
                endDateTime = startDateTime.with(TemporalAdjusters.firstDayOfNextMonth());
            } else if (rangeType.equals("yearly")) {
                startDateTime = startDateTime.with(TemporalAdjusters.firstDayOfYear());
                endDateTime = startDateTime.with(TemporalAdjusters.firstDayOfNextYear());
            }
//            else {
//                endDateTime = LocalDate.parse(end, DateTimeFormatter.ofPattern("yyyy/MM/dd")).atTime(23, 59, 59);
//            }
        } catch (DateTimeParseException e) {
            log.error("Invalid date format: " + e.getMessage());
        }
        return Arrays.asList(startDateTime, endDateTime);
    }

    public ArrayList<LocalDateTime> convertStartAndEndDateFormat(String start, String end) {

        ArrayList<LocalDateTime> dates = new ArrayList<>();
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            LocalDate startDateTime = LocalDate.parse(start, formatter);
            LocalDate endDateTime = LocalDate.parse(end, formatter);

            LocalDateTime startDate = startDateTime.atStartOfDay();
            LocalDateTime endDate = endDateTime.atTime(23, 59, 59);
            dates.add(startDate);
            dates.add(endDate);
        }  catch (DateTimeParseException e) {
            log.error("Invalid date format: " + e.getMessage());
        }
        return dates;
    }

}
