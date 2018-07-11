package your.project.demo.common;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 提供各种关于日期、时间的常用方法。
 * <p/>
 * Created by HuQingmiao on 2016-6-20.
 */
public class DateTimeUtil {

    private static Logger log = LoggerFactory.getLogger(DateTimeUtil.class);

    /**
     * 优雅完整的日期时间格式
     */
    public static final String PRETTY_DT_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * 紧凑完整的日期时间格式
     */
    public static final String COMPACT_DT_FORMAT = "yyyyMMddHHmmssSSS";


    /**
     * 对给定的日期时间字符串按照指定的格式进行解析。
     * <p/>
     * Parses text from the given string to produce a java.util.Date.
     *
     * @param datetimeStr the data/time string which format is given by the second
     *                    parameter.
     * @param format      the format of data/time string, such as: "yyyy-MM-dd
     *                    HH:mm:ss.SSS"
     */
    public static Date parse(String datetimeStr, String format) {
        try {
            SimpleDateFormat df = new SimpleDateFormat(format);
            Date d = df.parse(datetimeStr);
            return d;

        } catch (ParseException e) {
            log.error("", e);
            return null;
        }
    }

    /**
     * 把日期时间格式化为字符串。
     * <p/>
     * Formats a Date to produce a date/time string, which format
     * like:'yyyy-MM-dd HH:mm:ss.SSS'
     *
     * @param date   java.util.Date object, that need to be formatted.
     * @param format the format of the data/time string, such as: "yyyy-MM-dd
     *               HH:mm:ss.SSS"
     */
    public static String format(Date date, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(date);
    }

    /**
     * 取得相对于指定时间的间隔天数的时间。
     * 若参数n为负数，则返回当前时间之前第n天的时间；
     * 若参数n为正数，则返回当前时间之后的第n天的时间；
     * 若参数n等于0， 则返回当前时间。
     *
     * @param n    相对当前时间的间隔天数
     * @param zero 是否将小时、分、秒、毫秒归零
     */
    public static Date getTimeByRalativeDay(Date raletiveDate, int n, boolean zero) {
        if (n == 0 && !zero) {
            return raletiveDate;
        }

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(raletiveDate);

        int day = calendar.get(Calendar.DAY_OF_MONTH) + n;
        calendar.set(Calendar.DAY_OF_MONTH, day);

        if (zero) {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }

        return calendar.getTime();
    }


    /**
     * 取得相对于指定时间的间隔小时数的时间。
     * 若参数n为负数，则返回当前时间之前第n个小时的时间；
     * 若参数n为负数，则返回当前时间之后第n个小时的时间；
     * 若参数n等于0， 则返回当前时间。
     *
     * @param n    相对当前时间的间隔小时数
     * @param zero 是否将分、秒、毫秒归零
     */
    public static Date getTimeByRalativeHor(Date raletiveDate, int n, boolean zero) {
        if (n == 0 && !zero) {
            return raletiveDate;
        }

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(raletiveDate);

        int hour = calendar.get(Calendar.HOUR_OF_DAY) + n;
        calendar.set(Calendar.HOUR_OF_DAY, hour);

        if (zero) {
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }

        return calendar.getTime();
    }


    /**
     * 取得相对于指定时间的间隔分钟数的时间。
     * 若参数n为负数，则返回当前时间之前第n个分钟数的时间；
     * 若参数n为正数，则返回当前时间之后的第n个分数的时间；
     * 若参数n等于0， 则返回当前时间。
     *
     * @param n    相对当前时间的间隔分钟数
     * @param zero 是否将秒、毫秒归零
     */
    public static Date getTimeByRalativeMin(Date raletiveDate, int n, boolean zero) {
        if (n == 0 && !zero) {
            return raletiveDate;
        }

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(raletiveDate);

        int min = calendar.get(Calendar.MINUTE) + n;
        calendar.set(Calendar.MINUTE, min);

        if (zero) {
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }

        return calendar.getTime();
    }


    /**
     * 将java.util.date类型的日期转为java.sql.date, 以适应实体类的日期类型
     *
     * @param date java.util.Date
     * @return java.sql.Date
     */
    public static java.sql.Date toSqlDate(Date date) {
        return new java.sql.Date(date.getTime());
    }

    /**
     * 将java.util.date类型的时间转为java.sql.Time, 以适应实体类的时间类型
     *
     * @param date java.util.date
     * @return java.sql.date
     */
    public static java.sql.Time toSqlTime(Date date) {
        return new java.sql.Time(date.getTime());
    }

    /**
     * 将java.util.date类型的日期时间转为java.sql.Timestamp, 以适应实体类的日期时间类型
     *
     * @param date java.util.date
     * @return java.sql.date
     */
    public static java.sql.Timestamp toSqlTimestamp(Date date) {
        return new java.sql.Timestamp(date.getTime());
    }

    /**
     * 取java.sql.Timestamp类型的当前时间
     *
     * @return java.sql.Timestamp类型的当前时间
     */
    public static java.sql.Timestamp currentTime() {
        return new java.sql.Timestamp(new Date().getTime());
    }


    public static void main(String[] args) {
        System.out.println(DateTimeUtil.format(getTimeByRalativeDay(new Date(), 0, true),
                DateTimeUtil.PRETTY_DT_FORMAT));

        System.out.println(DateTimeUtil.format(getTimeByRalativeDay(new Date(), -1, false),
                DateTimeUtil.PRETTY_DT_FORMAT));

        System.out.println(DateTimeUtil.format(getTimeByRalativeDay(new Date(), -1, true),
                DateTimeUtil.PRETTY_DT_FORMAT));

        System.out.println(DateTimeUtil.format(getTimeByRalativeHor(new Date(), -1, true),
                DateTimeUtil.PRETTY_DT_FORMAT));

        System.out.println(DateTimeUtil.format(getTimeByRalativeMin(new Date(), -120, true),
                DateTimeUtil.PRETTY_DT_FORMAT));

    }

}
