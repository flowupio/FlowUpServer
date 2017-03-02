package utils;

import org.joda.time.*;

public class Time {

    public DateTime now() {
        DateTimeZone timeZone = getTimeZone();
        return new DateTime(timeZone);
    }

    public DateTime getYesterdayMidnightDate() {
        DateTimeZone timeZone = getTimeZone();
        DateTime tomorrow = new DateTime(timeZone).minusDays(1);
        return tomorrow.withTimeAtStartOfDay();
    }

    public DateTime daysAgo(int daysAgo) {
        DateTimeZone timeZone = getTimeZone();
        DateTime pastDay = new DateTime(timeZone).minusDays(daysAgo);
        return pastDay;
    }

    public DateTime getTodayMidnightDate() {
        DateTimeZone timeZone = getTimeZone();
        return new DateTime(timeZone).withTimeAtStartOfDay();
    }

    public DateTime getTomorrowMidnightDate() {
        DateTimeZone timeZone = getTimeZone();
        DateTime tomorrow = new DateTime(timeZone).plusDays(1);
        return tomorrow.withTimeAtStartOfDay();
    }

    public DateTime getMonthAgoMidnightDate() {
        DateTimeZone timeZone = getTimeZone();
        DateTime tomorrow = new DateTime(timeZone).minusMonths(1);
        return tomorrow.withTimeAtStartOfDay();
    }

    public int getTodayNumericDay() {
        return now().dayOfMonth().get();
    }

    private DateTimeZone getTimeZone() {
        return DateTimeZone.forID("Europe/Berlin");
    }

}
