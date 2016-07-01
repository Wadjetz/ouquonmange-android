package fr.oqom.ouquonmange.utils;

import android.content.Context;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

public class TimeUtils {

    public static final DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");
    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy/MM/dd");

    public static DateTime now() {
        return DateTime.now(DateTimeZone.UTC);
    }

    public static DateTime getDateTime(long millis) {
        return new DateTime(DateTimeZone.UTC).withMillis(millis);
    }

    public static String printTime(DateTime dateTime, Context context) {
        return print(dateTime, timeFormatter, context);
    }

    public static String printDateTime(DateTime dateTime) {
        return dateTimeFormatter
                .withZoneUTC()
                .print(dateTime);
    };

    public static String printDateTime(DateTime dateTime, Context context) {
        return print(dateTime, dateTimeFormatter, context);
    };

    private static String print(DateTime dateTime, DateTimeFormatter dateTimeFormatter, Context context) {
        return dateTimeFormatter
                .withLocale(context.getResources().getConfiguration().locale)
                .print(dateTime);
    }

    public static String printDate(DateTime dateTime, Context context) {
        return print(dateTime, dateFormatter, context);
    }
}
