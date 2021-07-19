package cn.bp.scada.common.utils.data;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间工具类
 *
 * @author ruoyi
 */
@Component
public class DateUtils extends org.apache.commons.lang3.time.DateUtils {
    public static String YYYY = "yyyy";

    public static String YYYY_MM = "yyyy-MM";

    public static String YYYY_MM_DD = "yyyy-MM-dd";

    public static String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    public static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    private static String[] parsePatterns = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

    /**
     * 获取当前Date型日期
     *
     * @return Date() 当前日期
     */
    public static Date getNowDate() {
        return new Date();
    }

    /**
     * 获取当前日期, 默认格式为yyyy-MM-dd
     *
     * @return String
     */
    public static String getDate() {
        return dateTimeNow(YYYY_MM_DD);
    }

    public static final String getTime() {
        return dateTimeNow(YYYY_MM_DD_HH_MM_SS);
    }

    public static final String dateTimeNow() {
        return dateTimeNow(YYYYMMDDHHMMSS);
    }

    public static final String dateTimeNow(final String format) {
        return parseDateToStr(format, new Date());
    }

    public static final String dateTime(final Date date) {
        return parseDateToStr(YYYY_MM_DD, date);
    }

    public static final String parseDateToStr(final String format, final Date date) {
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * String类型日期转为date类型
     *
     * @param format yyyy-mm...
     * @param ts     时间：2020-。。
     * @return
     */
    public static final Date dateTime(final String format, final String ts) {
        try {
            return new SimpleDateFormat(format).parse(ts);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 日期路径 即年/月/日 如2018/08/08
     */
    public static final String datePath() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyy/MM/dd");
    }

    /**
     * 日期路径 即年/月/日 如20180808
     */
    public static final String dateTime() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyyMMdd");
    }

    /**
     * 日期型字符串转化为日期 格式
     */
    public static Date parseDate(Object str) {
        if (str == null) {
            return null;
        }
        try {
            return parseDate(str.toString(), parsePatterns);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 获取服务器启动时间
     */
    public static Date getServerStartDate() {
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        return new Date(time);
    }

    /**
     * 计算两个时间差
     */
    public static String getDatePoor(Date endDate, Date nowDate) {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return day + "天" + hour + "小时" + min + "分钟";
    }

    /**
     * 计算两个时间差,单独返回分钟
     */
    public static Long getDatePoorMin(Date endDate, Date nowDate) {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return min;
    }

    /**
     * 计算两个时间差,返回分钟数
     */
    public static long getDatePoorMins(Date endDate, Date nowDate) {
        long min = 0;
        if (endDate == null || nowDate == null) {
            min = 0;
        } else {

            try {
                long nd = 1000 * 24 * 60 * 60;
                long nh = 1000 * 60 * 60;
                long nm = 1000 * 60;
                // long ns = 1000;
                // 获得两个时间的毫秒时间差异
                long diff = endDate.getTime() - nowDate.getTime();
                // 计算差多少天
                long day = diff / nd;
                // 计算差多少小时
                long hour = diff % nd / nh;
                // 计算差多少分钟
                min = Math.round(diff / nm);
                // 计算差多少秒//输出结果
                // long sec = diff % nd % nh % nm / ns;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return min;
    }

    /**
     * 返回HHMMSS字符串
     */
    public static String HMSString(Date Date) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        String time = df.format(Date);
        return time;
    }

    /**
     * 实际时间 Date 输入，进行HHMMSS Date格式化，然后输出分钟的long格式
     */
    public static long HMSDTD(Date Date1) {
        long min5 = 0;
        try {
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
            String time = df.format(Date1);
            Date date = df.parse(time);

            long nh = 1000 * 60 * 60;
            long nm = 1000 * 60;
            // long ns = 1000;
            // 获得毫秒时间
            long diff = date.getTime();
            // 计算差多少分钟
            min5 = diff / nm + 480;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return min5;
    }

    /**
     * 数据库String 输入，进行HHMMSS Date格式化，然后输出分钟的long格式
     */
    public static long HMSSTD(String string) {
        long min3 = 0;
        if (string == null) {
            min3 = 0;
        } else {
            try {
                SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
                Date time9 = df.parse(string);
                long nh = 1000 * 60 * 60;
                long nm = 1000 * 60;
                // long ns = 1000;
                // 获得毫秒时间
                long sfidsi = time9.getTime() + (long) 8 * nh + 30 * nm;
                long diff = sfidsi - (long) 8 * nh - 30 * nm;
                if (diff < 0) {
                    diff = diff + 24 * 1000 * 60 * 60;
                }
                // 计算差多少分钟
                min3 = diff / nm + 480;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return min3;
    }

    public static long HMSSTD(String string, String string2) {
        long result = 0;
        if (string == null || string2 == null) {
            result = 0;
        } else {
            try {
                SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
                Date time9 = df.parse(string);
                Date time10 = df.parse(string2);
                long nh = 1000 * 60 * 60;
                long nm = 1000 * 60;
                // long ns = 1000;
                // 获得毫秒时间
                long diff = time9.getTime() - 30 * nm;
                if (diff < 0) {
                    diff = diff + 24 * 1000 * 60 * 60;
                }
                long diff1 = time10.getTime() - 30 * nm;
                if (diff1 < 0) {
                    diff1 = diff1 + 24 * 1000 * 60 * 60;
                }
                // 计算差多少分钟
                long min3 = diff / nm ;
                long min4 = diff1 / nm ;
                result = min3 - min4;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 规范时间格式-08：30：00时间,输出long min
     */
    public static long dateFormat(Date Date2) {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;

        long diff = Date2.getTime() - 8 * nh - 30 * nm;//格式化每天时间
//        Date date3=new Date(diff);
        long min1 = diff % nd % nh / nm;
        return min1;
    }

    public static Date dateFormat1(Date Date2, long hour, long min) {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;

        long diff = Date2.getTime() + hour * nh + min * nm;//格式化每天时间
        Date date3 = new Date(diff);
        return date3;
    }

    /**
     * 两个时间相差距离多少天多少小时多少分多少秒
     *
     * @param str1 时间参数 1 格式：1990/01/01 12:00:00
     * @param str2 时间参数 2 格式：2009/01/01 12:00:00
     * @return long[] 返回值为：{天, 时, 分, 秒}
     */
    public static long[] getDistanceTimes(String starttime, String endtime) {
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date one;
        Date two;
        long day = 0;
        long hour = 0;
        long min = 0;
        long sec = 0;
        try {
            one = df.parse(starttime);
            two = df.parse(endtime);
            long time1 = one.getTime();
            long time2 = two.getTime();
            long diff;
            if (time1 < time2) {
                diff = time2 - time1;
            } else {
                diff = time1 - time2;
            }
            day = diff / (24 * 60 * 60 * 1000);
            hour = (diff / (60 * 60 * 1000) - day * 24);
            min = ((diff / (60 * 1000)));
            sec = (diff / 1000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long[] times = {day, hour, min, sec};
        return times;
    }
}
