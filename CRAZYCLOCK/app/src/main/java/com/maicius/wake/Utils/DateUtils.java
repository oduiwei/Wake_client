package com.maicius.wake.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Eric Barton on 2018/3/11.
 */

public class DateUtils {
    private static int weeks = 0;// 用来全局控制 上一周，本周，下一周的周数变化
    private static int MaxDate; // 一月最大天数
    private static int MaxYear; // 一年最大天数

    private static int getMondayPlus() {
        Calendar cd = Calendar.getInstance();

        int dayOfWeek = cd.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == 0) {
            // 今天是星期天
            return -6;
        } else {
            return 2 - dayOfWeek;
        }
    }

    private static int getYearPlus() {
        Calendar cd = Calendar.getInstance();
        int yearOfNumber = cd.get(Calendar.DAY_OF_YEAR);// 获得当天是一年中的第几天
        cd.set(Calendar.DAY_OF_YEAR, 1);// 把日期设为当年第一天
        cd.roll(Calendar.DAY_OF_YEAR, -1);// 把日期回滚一天。
        int MaxYear = cd.get(Calendar.DAY_OF_YEAR);
        if (yearOfNumber == 1) {
            return -MaxYear;
        } else {
            return 1 - yearOfNumber;
        }
    }

    /**
     * 获取今天日期
     * @return
     */
    public static String getTodayDate() {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");// 可以方便地修改日期格式
        String date = dateFormat.format(now);
        return date;
    }

    /**
     * 获取本周一的日期
     * @return
     */
    public static  String getMondayOfWeek() {
        weeks = 0;
        int mondayPlus = getMondayPlus();
        //System.out.println(mondayPlus);
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus);
        Date monday = currentDate.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(monday);
        return date;
        //以下代码会变成中文日期
//        DateFormat df = DateFormat.getDateInstance();
//        String preMonday = df.format(monday);
//        return preMonday;
    }

    /**
     * 获取本月第一天日期
     * @return
     */
    public static String getFirstDayOfMonth() {
        String str = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Calendar lastDate = Calendar.getInstance();
        lastDate.set(Calendar.DATE, 1);// 设为当前月的1号
        str = sdf.format(lastDate.getTime());
        return str;
    }

    /**
     * 获取今年第一天日期
     * @return
     */
    public static String getFirstDayOfYear() {
        int yearPlus = getYearPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, yearPlus);
        Date yearDay = currentDate.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(yearDay);
        return date;
//        DateFormat df = DateFormat.getDateInstance();
//        String preYearDay = df.format(yearDay);
//        return preYearDay;
    }
}
