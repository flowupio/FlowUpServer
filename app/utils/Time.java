package utils;

import org.joda.time.*;

public class Time {

    public DateTime now() {
        DateTimeZone timeZone = getTimeZone();
        return new DateTime(timeZone);
    }

    public DateTime getYesterdayMidnightDate() {
        DateTimeZone timeZone = getTimeZone();
        DateTime tomorrow = new DateTime(timeZone).plusDays(-1);
        return tomorrow.withTimeAtStartOfDay();
    }

    public DateTime getTodayMidnightDate() {
        DateTimeZone timeZone = getTimeZone();
        return new DateTime(timeZone).withTimeAtStartOfDay();
    }

    public DateTime getTomorrowMidhtDate() {
        DateTimeZone timeZone = getTimeZone();
        DateTime tomorrow = new DateTime(timeZone).plusDays(1);
        return tomorrow.withTimeAtStartOfDay();
    }

    private DateTimeZone getTimeZone() {
        return DateTimeZone.forID("Europe/Berlin");
    }


}
