package com.ag.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public static Date parseDate(String dateString) {
        try {
            return formatter.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse '" + dateString + "'", e);
        }

    }

    public static String formatDate(Date date) {
        return formatter.format(date);
    }

}
