package fr.oqom.ouquonmange.utils;

import android.content.Context;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateTimeUtils {

    public static final DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");
    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy/MM/dd");
    public static final  DateTimeZone dateTimeDefault = DateTimeZone.getDefault();

    public static DateTime now() {
        return DateTime.now(dateTimeDefault);
    }

    public static DateTime getDateTime(long millis) {
        return new DateTime(dateTimeDefault).withMillis(millis);
    }
    public static DateTime getDateTimeWithDefaultTZ(DateTime dt,String dateTimeZoneId){

        DateTime dateTime = new DateTime(dt);
        DateTimeZone dtZone = DateTimeZone.forID(dateTimeZoneId);
        DateTime dateTimed = dateTime.withZone(dtZone);

        return dateTimed;
    }

    public static String printTime(DateTime dateTime, Context context) {
        return print(dateTime, timeFormatter, context);
    }

    public static String printDateTime(DateTime dateTime, DateTimeZone dtz) {
        return dateTimeFormatter
                .withZone(dtz)
                .print(dateTime);
    };

    public static String printDateTime(DateTime dateTime) {
        return dateTimeFormatter
                .withZone(DateTimeZone.UTC)
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

    public static String getDefaultDateTimeZoneId(){
        return dateTimeDefault.getID();
    }
    public static String getUTCDateTimeZoneId(){
        return (DateTimeZone.UTC).getID();
    }

    public static DateTimeZone getDateTimeZoneDefault(){
        return dateTimeDefault;
    }

    public static DateTimeZone getDateTimeZoneUTC(){
        return DateTimeZone.UTC;
    }
}
