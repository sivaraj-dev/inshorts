package in.sdev.android.inshorts.utilities;

import android.content.Context;
import android.text.format.Time;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import in.sdev.android.inshorts.R;

/*
* Class used for showing the date in format
* */
public class DateUtils {
    private static final long DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1);
    private static final String DATE_FORMAT = "yyyyMMdd";
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private static String LOG_TAG = DateUtils.class.getSimpleName();

    public static long getNormalizedUtcDateForToday() {
        long utcNowMillis = System.currentTimeMillis();
        TimeZone currentTimeZone = TimeZone.getDefault();
        long gmtOffsetMillis = currentTimeZone.getOffset(utcNowMillis);
        long timeSinceEpochLocalTimeMillis = utcNowMillis + gmtOffsetMillis;
        long daysSinceEpochLocal = TimeUnit.MILLISECONDS.toDays(timeSinceEpochLocalTimeMillis);
        long normalizedUtcMidnightMillis = TimeUnit.DAYS.toMillis(daysSinceEpochLocal);
        return normalizedUtcMidnightMillis;
    }

    private static long getLocalMidnightFromNormalizedUtcDate(long normalizedUtcDate) {
        TimeZone timeZone = TimeZone.getDefault();
        long gmtOffset = timeZone.getOffset(normalizedUtcDate);
        long localMidnightMillis = normalizedUtcDate - gmtOffset;
        return localMidnightMillis;
    }

    private static String getReadableDateString(Context context, long timeInMillis) {
        int flags = android.text.format.DateUtils.FORMAT_SHOW_DATE
                | android.text.format.DateUtils.FORMAT_NO_YEAR
                | android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY;
        return android.text.format.DateUtils.formatDateTime(context, timeInMillis, flags);
    }

    private static String getDayName(Context context, long dateInMillis) {
        long daysFromEpochToProvidedDate = elapsedDaysSinceEpoch(dateInMillis);
        long daysFromEpochToToday = elapsedDaysSinceEpoch(System.currentTimeMillis());
        int daysAfterToday = (int) (daysFromEpochToProvidedDate - daysFromEpochToToday);
        switch (daysAfterToday) {
            case 0:
                return context.getString(R.string.today);
            case 1:
                return context.getString(R.string.tomorrow);
            default:
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
                return dayFormat.format(dateInMillis);
        }
    }

    private static long elapsedDaysSinceEpoch(long utcDate) {
        return TimeUnit.MILLISECONDS.toDays(utcDate);
    }

    public static String getTimeAgo(Context context, long time) {
        if (time < 1000000000000L) {
            time *= 1000;
        }
        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return "Just now";
        }
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "Just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "1 min";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " mins";
        } else if (diff < 90 * MINUTE_MILLIS || diff / HOUR_MILLIS == 1) {
            return "1 hr";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hrs";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "Yesterday";
        } else {
            return getFormattedMonthDay(context, time);
        }
    }

    public static String getFormattedMonthDay(Context context, long dateInMillis) {
        Time time = new Time();
        time.setToNow();
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(DateUtils.DATE_FORMAT);
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMM dd, yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mma");
        String monthDayString = monthDayFormat.format(dateInMillis);
        String timeString = (timeFormat.format(dateInMillis)).replace("AM", "am").replace("PM", "pm");
        return monthDayString + " at " + timeString;
    }
}
