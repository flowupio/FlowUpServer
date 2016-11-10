package utils;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;

public class Time {

    public Date getYesterdayMidnightDate() {
        Calendar calendar = getTodayCalendar();
        calendar.add(Calendar.DATE, -1);
        return calendar.getTime();
    }

    public Date getTodayMidnightDate() {
        Calendar calendar = getTodayCalendar();
        return calendar.getTime();
    }

    public Date getTomorrowMidhtDate() {
        Calendar calendar = getTodayCalendar();
        calendar.add(Calendar.DATE, 1);
        return calendar.getTime();
    }

    @NotNull
    private Calendar getTodayCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), 0, 0, 0);
        return calendar;
    }

}
