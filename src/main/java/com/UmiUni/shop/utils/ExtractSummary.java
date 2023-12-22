package com.UmiUni.shop.utils;

public class ExtractSummary {

    public static String extractSummaryToGetString(String stackTrace) {
        int maxLines = 10; // Number of lines to include in the summary
        String[] lines = stackTrace.split("\n");
        StringBuilder summary = new StringBuilder();

        for (int i = 0; i < Math.min(lines.length, maxLines); i++) {
            summary.append(lines[i]).append("\n");
        }
        return summary.toString();
    }

}
