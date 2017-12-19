package com.xingen.calendardemo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by ${xingen} on 2017/9/19.
 *blog:http://blog.csdn.net/hexingen
 * 工具类：
 *
 *   1. 公历时间处理成农历时间: 初几，24节气，农历假日
 *   2. 公历假日
 *
 *   资源参考：
 *     http://www.cnblogs.com/zhujiabin/p/5513942.html
 *
 */

public class CalendarUtils {

    /**
     * 用于保存中文的月份
     */
    private final static String CHINESE_NUMBER[] = { "一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "腊" };
    /**
     * 用于保存展示周几使用
     */
    private final static String WEEK_NUMBER[] = { "日", "一", "二", "三", "四", "五", "六" };

    private final static long[] LUNAR_INFO = new long[] { 0x04bd8, 0x04ae0,
            0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0,
            0x055d2, 0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540,
            0x0d6a0, 0x0ada2, 0x095b0, 0x14977, 0x04970, 0x0a4b0, 0x0b4b5,
            0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
            0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3,
            0x092e0, 0x1c8d7, 0x0c950, 0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0,
            0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557, 0x06ca0,
            0x0b550, 0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8,
            0x0e950, 0x06aa0, 0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570,
            0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0, 0x096d0, 0x04dd5,
            0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0,
            0x195a6, 0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50,
            0x06d40, 0x0af46, 0x0ab60, 0x09570, 0x04af5, 0x04970, 0x064b0,
            0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0,
            0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7,
            0x025d0, 0x092d0, 0x0cab5, 0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50,
            0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930, 0x07954,
            0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260,
            0x0ea65, 0x0d530, 0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0,
            0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45, 0x0b5a0, 0x056d0,
            0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20,
            0x0ada0 };

    /**
     * 转换为2012年11月22日格式
     */
    private static SimpleDateFormat chineseDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
    /**
     * 转换为2012-11-22格式
     */
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 计算得到农历的年份
     */
    private int mLuchYear;
    /**
     * 计算得到农历的月份
     */
    private int mLuchMonth;

    /**
     * 计算得到农历的日期
     */
    private int mLuchDay;

    /**
     * 用于标识是事为闰年
     */
    private boolean isLoap;

    /**
     * 用于记录当前处理的时间
     */
    private Calendar mCurrenCalendar;

    /**
     * 传回农历 year年的总天数
     *
     * @param year
     *            将要计算的年份
     * @return 返回传入年份的总天数
     */
    private static int yearDays(int year) {
        int i, sum = 348;
        for (i = 0x8000; i > 0x8; i >>= 1) {
            if ((LUNAR_INFO[year - 1900] & i) != 0)
                sum += 1;
        }
        return (sum + leapDays(year));
    }

    /**
     * 传回农历 year年闰月的天数
     *
     * @param year
     *            将要计算的年份
     * @return 返回 农历 year年闰月的天数
     */
    private static int leapDays(int year) {
        if (leapMonth(year) != 0) {
            if ((LUNAR_INFO[year - 1900] & 0x10000) != 0)
                return 30;
            else
                return 29;
        } else
            return 0;
    }

    /**
     * 传回农历 year年闰哪个月 1-12 , 没闰传回 0
     *
     * @param year
     *            将要计算的年份
     * @return 传回农历 year年闰哪个月 1-12 , 没闰传回 0
     */
    private static int leapMonth(int year) {
        return (int) (LUNAR_INFO[year - 1900] & 0xf);
    }

    /**
     * 传回农历 year年month月的总天数
     *
     * @param year
     *            将要计算的年份
     * @param month
     *            将要计算的月份
     * @return 传回农历 year年month月的总天数
     */
    private static int monthDays(int year, int month) {
        if ((LUNAR_INFO[year - 1900] & (0x10000 >> month)) == 0)
            return 29;
        else
            return 30;
    }

    /**
     * 传回农历 y年的生肖
     *
     * @return 传回农历 y年的生肖
     */
    public String animalsYear() {
        final String[] Animals = new String[] { "鼠", "牛", "虎", "兔", "龙", "蛇",
                "马", "羊", "猴", "鸡", "狗", "猪" };
        return Animals[(mLuchYear - 4) % 12];
    }

    // ====== 传入 月日的offset 传回干支, 0=甲子
    private static String cyclicalm(int num) {
        final String[] Gan = new String[] { "甲", "乙", "丙", "丁", "戊", "己", "庚",
                "辛", "壬", "癸" };
        final String[] Zhi = new String[] { "子", "丑", "寅", "卯", "辰", "巳", "午",
                "未", "申", "酉", "戌", "亥" };

        return (Gan[num % 10] + Zhi[num % 12]);
    }

    // ====== 传入 offset 传回干支, 0=甲子
    public String cyclical() {
        int num = mLuchYear - 1900 + 36;
        return (cyclicalm(num));
    }

    /**
     * 传出y年m月d日对应的农历. yearCyl3:农历年与1864的相差数 ? monCyl4:从1900年1月31日以来,闰月数
     * dayCyl5:与1900年1月31日相差的天数,再加40 ?
     *
     * @param cal
     * @return
     */
    public CalendarUtils(Calendar cal) {
        mCurrenCalendar = cal;
        int leapMonth = 0;
        Date baseDate = null;
        try {
            baseDate = chineseDateFormat.parse("1900年1月31日");
        } catch (ParseException e) {
            e.printStackTrace(); // To change body of catch statement use
            // Options | File Templates.
        }

        // 求出和1900年1月31日相差的天数
        int offset = (int) ((cal.getTime().getTime() - baseDate.getTime()) / 86400000L);
        // 用offset减去每农历年的天数
        // 计算当天是农历第几天
        // i最终结果是农历的年份
        // offset是当年的第几天
        int iYear, daysOfYear = 0;
        for (iYear = 1900; iYear < 2050 && offset > 0; iYear++) {
            daysOfYear = yearDays(iYear);
            offset -= daysOfYear;
        }
        if (offset < 0) {
            offset += daysOfYear;
            iYear--;
        }
        // 农历年份
        mLuchYear = iYear;

        leapMonth = leapMonth(iYear); // 闰哪个月,1-12
        isLoap = false;

        // 用当年的天数offset,逐个减去每月（农历）的天数，求出当天是本月的第几天
        int iMonth, daysOfMonth = 0;
        for (iMonth = 1; iMonth < 13 && offset > 0; iMonth++) {
            // 闰月
            if (leapMonth > 0 && iMonth == (leapMonth + 1) && !isLoap) {
                --iMonth;
                isLoap = true;
                daysOfMonth = leapDays(mLuchYear);
            } else
                daysOfMonth = monthDays(mLuchYear, iMonth);

            offset -= daysOfMonth;
            // 解除闰月
            if (isLoap && iMonth == (leapMonth + 1))
                isLoap = false;
            if (!isLoap) {
            }
        }
        // offset为0时，并且刚才计算的月份是闰月，要校正
        if (offset == 0 && leapMonth > 0 && iMonth == leapMonth + 1) {
            if (isLoap) {
                isLoap = false;
            } else {
                isLoap = true;
                --iMonth;
            }
        }
        // offset小于0时，也要校正
        if (offset < 0) {
            offset += daysOfMonth;
            --iMonth;

        }
        mLuchMonth = iMonth;
        mLuchDay = offset + 1;
    }

    /**
     * 返化成中文格式
     *
     * @param day
     * @return
     */
    public static String getChinaDayString(int day) {
        String chineseTen[] = { "初", "十", "廿", "卅" };
        int n = day % 10 == 0 ? 9 : day % 10 - 1;
        if (day > 30)
            return "";
        if (day == 10)
            return "初十";
        else
            return chineseTen[day / 10] + CHINESE_NUMBER[n];
    }
    /**
     * 主要用于把公历日期处理成24节气
     */
    public static class SolarTermsUtil {

        /**
         * 计算得到公历的年份
         */
        private int gregorianYear;

        /**
         * 计算得到公历的月份
         */
        private int gregorianMonth;

        /**
         * 用于计算得到公历的日期
         */
        private int gregorianDate;

        private int chineseYear;
        private int chineseMonth;
        private int chineseDate;

        // 初始日，公历农历对应日期：
        // 公历 1901 年 1 月 1 日，对应农历 4598 年 11 月 11 日
        private static int baseYear = 1901;
        private static int baseMonth = 1;
        private static int baseDate = 1;
        private static int baseIndex = 0;
        private static int baseChineseYear = 4598 - 1;
        private static int baseChineseMonth = 11;
        private static int baseChineseDate = 11;
        private static char[] daysInGregorianMonth = { 31, 28, 31, 30, 31, 30, 31,
                31, 30, 31, 30, 31 };

        private int sectionalTerm;
        private int principleTerm;

        private static char[][] sectionalTermMap = {
                { 7, 6, 6, 6, 6, 6, 6, 6, 6, 5, 6, 6, 6, 5, 5, 6, 6, 5, 5, 5, 5, 5,
                        5, 5, 5, 4, 5, 5 },
                { 5, 4, 5, 5, 5, 4, 4, 5, 5, 4, 4, 4, 4, 4, 4, 4, 4, 3, 4, 4, 4, 3,
                        3, 4, 4, 3, 3, 3 },
                { 6, 6, 6, 7, 6, 6, 6, 6, 5, 6, 6, 6, 5, 5, 6, 6, 5, 5, 5, 6, 5, 5,
                        5, 5, 4, 5, 5, 5, 5 },
                { 5, 5, 6, 6, 5, 5, 5, 6, 5, 5, 5, 5, 4, 5, 5, 5, 4, 4, 5, 5, 4, 4,
                        4, 5, 4, 4, 4, 4, 5 },
                { 6, 6, 6, 7, 6, 6, 6, 6, 5, 6, 6, 6, 5, 5, 6, 6, 5, 5, 5, 6, 5, 5,
                        5, 5, 4, 5, 5, 5, 5 },
                { 6, 6, 7, 7, 6, 6, 6, 7, 6, 6, 6, 6, 5, 6, 6, 6, 5, 5, 6, 6, 5, 5,
                        5, 6, 5, 5, 5, 5, 4, 5, 5, 5, 5 },
                { 7, 8, 8, 8, 7, 7, 8, 8, 7, 7, 7, 8, 7, 7, 7, 7, 6, 7, 7, 7, 6, 6,
                        7, 7, 6, 6, 6, 7, 7 },
                { 8, 8, 8, 9, 8, 8, 8, 8, 7, 8, 8, 8, 7, 7, 8, 8, 7, 7, 7, 8, 7, 7,
                        7, 7, 6, 7, 7, 7, 6, 6, 7, 7, 7 },
                { 8, 8, 8, 9, 8, 8, 8, 8, 7, 8, 8, 8, 7, 7, 8, 8, 7, 7, 7, 8, 7, 7,
                        7, 7, 6, 7, 7, 7, 7 },
                { 9, 9, 9, 9, 8, 9, 9, 9, 8, 8, 9, 9, 8, 8, 8, 9, 8, 8, 8, 8, 7, 8,
                        8, 8, 7, 7, 8, 8, 8 },
                { 8, 8, 8, 8, 7, 8, 8, 8, 7, 7, 8, 8, 7, 7, 7, 8, 7, 7, 7, 7, 6, 7,
                        7, 7, 6, 6, 7, 7, 7 },
                { 7, 8, 8, 8, 7, 7, 8, 8, 7, 7, 7, 8, 7, 7, 7, 7, 6, 7, 7, 7, 6, 6,
                        7, 7, 6, 6, 6, 7, 7 } };
        private static char[][] sectionalTermYear = {
                { 13, 49, 85, 117, 149, 185, 201, 250, 250 },
                { 13, 45, 81, 117, 149, 185, 201, 250, 250 },
                { 13, 48, 84, 112, 148, 184, 200, 201, 250 },
                { 13, 45, 76, 108, 140, 172, 200, 201, 250 },
                { 13, 44, 72, 104, 132, 168, 200, 201, 250 },
                { 5, 33, 68, 96, 124, 152, 188, 200, 201 },
                { 29, 57, 85, 120, 148, 176, 200, 201, 250 },
                { 13, 48, 76, 104, 132, 168, 196, 200, 201 },
                { 25, 60, 88, 120, 148, 184, 200, 201, 250 },
                { 16, 44, 76, 108, 144, 172, 200, 201, 250 },
                { 28, 60, 92, 124, 160, 192, 200, 201, 250 },
                { 17, 53, 85, 124, 156, 188, 200, 201, 250 } };
        private static char[][] principleTermMap = {
                { 21, 21, 21, 21, 21, 20, 21, 21, 21, 20, 20, 21, 21, 20, 20, 20,
                        20, 20, 20, 20, 20, 19, 20, 20, 20, 19, 19, 20 },
                { 20, 19, 19, 20, 20, 19, 19, 19, 19, 19, 19, 19, 19, 18, 19, 19,
                        19, 18, 18, 19, 19, 18, 18, 18, 18, 18, 18, 18 },
                { 21, 21, 21, 22, 21, 21, 21, 21, 20, 21, 21, 21, 20, 20, 21, 21,
                        20, 20, 20, 21, 20, 20, 20, 20, 19, 20, 20, 20, 20 },
                { 20, 21, 21, 21, 20, 20, 21, 21, 20, 20, 20, 21, 20, 20, 20, 20,
                        19, 20, 20, 20, 19, 19, 20, 20, 19, 19, 19, 20, 20 },
                { 21, 22, 22, 22, 21, 21, 22, 22, 21, 21, 21, 22, 21, 21, 21, 21,
                        20, 21, 21, 21, 20, 20, 21, 21, 20, 20, 20, 21, 21 },
                { 22, 22, 22, 22, 21, 22, 22, 22, 21, 21, 22, 22, 21, 21, 21, 22,
                        21, 21, 21, 21, 20, 21, 21, 21, 20, 20, 21, 21, 21 },
                { 23, 23, 24, 24, 23, 23, 23, 24, 23, 23, 23, 23, 22, 23, 23, 23,
                        22, 22, 23, 23, 22, 22, 22, 23, 22, 22, 22, 22, 23 },
                { 23, 24, 24, 24, 23, 23, 24, 24, 23, 23, 23, 24, 23, 23, 23, 23,
                        22, 23, 23, 23, 22, 22, 23, 23, 22, 22, 22, 23, 23 },
                { 23, 24, 24, 24, 23, 23, 24, 24, 23, 23, 23, 24, 23, 23, 23, 23,
                        22, 23, 23, 23, 22, 22, 23, 23, 22, 22, 22, 23, 23 },
                { 24, 24, 24, 24, 23, 24, 24, 24, 23, 23, 24, 24, 23, 23, 23, 24,
                        23, 23, 23, 23, 22, 23, 23, 23, 22, 22, 23, 23, 23 },
                { 23, 23, 23, 23, 22, 23, 23, 23, 22, 22, 23, 23, 22, 22, 22, 23,
                        22, 22, 22, 22, 21, 22, 22, 22, 21, 21, 22, 22, 22 },
                { 22, 22, 23, 23, 22, 22, 22, 23, 22, 22, 22, 22, 21, 22, 22, 22,
                        21, 21, 22, 22, 21, 21, 21, 22, 21, 21, 21, 21, 22 } };
        private static char[][] principleTermYear = {
                { 13, 45, 81, 113, 149, 185, 201 },
                { 21, 57, 93, 125, 161, 193, 201 },
                { 21, 56, 88, 120, 152, 188, 200, 201 },
                { 21, 49, 81, 116, 144, 176, 200, 201 },
                { 17, 49, 77, 112, 140, 168, 200, 201 },
                { 28, 60, 88, 116, 148, 180, 200, 201 },
                { 25, 53, 84, 112, 144, 172, 200, 201 },
                { 29, 57, 89, 120, 148, 180, 200, 201 },
                { 17, 45, 73, 108, 140, 168, 200, 201 },
                { 28, 60, 92, 124, 160, 192, 200, 201 },
                { 16, 44, 80, 112, 148, 180, 200, 201 },
                { 17, 53, 88, 120, 156, 188, 200, 201 } };

        private static char[] chineseMonths = {
                // 农历月份大小压缩表，两个字节表示一年。两个字节共十六个二进制位数，
                // 前四个位数表示闰月月份，后十二个位数表示十二个农历月份的大小。
                0x00, 0x04, 0xad, 0x08, 0x5a, 0x01, 0xd5, 0x54, 0xb4, 0x09, 0x64,
                0x05, 0x59, 0x45, 0x95, 0x0a, 0xa6, 0x04, 0x55, 0x24, 0xad, 0x08,
                0x5a, 0x62, 0xda, 0x04, 0xb4, 0x05, 0xb4, 0x55, 0x52, 0x0d, 0x94,
                0x0a, 0x4a, 0x2a, 0x56, 0x02, 0x6d, 0x71, 0x6d, 0x01, 0xda, 0x02,
                0xd2, 0x52, 0xa9, 0x05, 0x49, 0x0d, 0x2a, 0x45, 0x2b, 0x09, 0x56,
                0x01, 0xb5, 0x20, 0x6d, 0x01, 0x59, 0x69, 0xd4, 0x0a, 0xa8, 0x05,
                0xa9, 0x56, 0xa5, 0x04, 0x2b, 0x09, 0x9e, 0x38, 0xb6, 0x08, 0xec,
                0x74, 0x6c, 0x05, 0xd4, 0x0a, 0xe4, 0x6a, 0x52, 0x05, 0x95, 0x0a,
                0x5a, 0x42, 0x5b, 0x04, 0xb6, 0x04, 0xb4, 0x22, 0x6a, 0x05, 0x52,
                0x75, 0xc9, 0x0a, 0x52, 0x05, 0x35, 0x55, 0x4d, 0x0a, 0x5a, 0x02,
                0x5d, 0x31, 0xb5, 0x02, 0x6a, 0x8a, 0x68, 0x05, 0xa9, 0x0a, 0x8a,
                0x6a, 0x2a, 0x05, 0x2d, 0x09, 0xaa, 0x48, 0x5a, 0x01, 0xb5, 0x09,
                0xb0, 0x39, 0x64, 0x05, 0x25, 0x75, 0x95, 0x0a, 0x96, 0x04, 0x4d,
                0x54, 0xad, 0x04, 0xda, 0x04, 0xd4, 0x44, 0xb4, 0x05, 0x54, 0x85,
                0x52, 0x0d, 0x92, 0x0a, 0x56, 0x6a, 0x56, 0x02, 0x6d, 0x02, 0x6a,
                0x41, 0xda, 0x02, 0xb2, 0xa1, 0xa9, 0x05, 0x49, 0x0d, 0x0a, 0x6d,
                0x2a, 0x09, 0x56, 0x01, 0xad, 0x50, 0x6d, 0x01, 0xd9, 0x02, 0xd1,
                0x3a, 0xa8, 0x05, 0x29, 0x85, 0xa5, 0x0c, 0x2a, 0x09, 0x96, 0x54,
                0xb6, 0x08, 0x6c, 0x09, 0x64, 0x45, 0xd4, 0x0a, 0xa4, 0x05, 0x51,
                0x25, 0x95, 0x0a, 0x2a, 0x72, 0x5b, 0x04, 0xb6, 0x04, 0xac, 0x52,
                0x6a, 0x05, 0xd2, 0x0a, 0xa2, 0x4a, 0x4a, 0x05, 0x55, 0x94, 0x2d,
                0x0a, 0x5a, 0x02, 0x75, 0x61, 0xb5, 0x02, 0x6a, 0x03, 0x61, 0x45,
                0xa9, 0x0a, 0x4a, 0x05, 0x25, 0x25, 0x2d, 0x09, 0x9a, 0x68, 0xda,
                0x08, 0xb4, 0x09, 0xa8, 0x59, 0x54, 0x03, 0xa5, 0x0a, 0x91, 0x3a,
                0x96, 0x04, 0xad, 0xb0, 0xad, 0x04, 0xda, 0x04, 0xf4, 0x62, 0xb4,
                0x05, 0x54, 0x0b, 0x44, 0x5d, 0x52, 0x0a, 0x95, 0x04, 0x55, 0x22,
                0x6d, 0x02, 0x5a, 0x71, 0xda, 0x02, 0xaa, 0x05, 0xb2, 0x55, 0x49,
                0x0b, 0x4a, 0x0a, 0x2d, 0x39, 0x36, 0x01, 0x6d, 0x80, 0x6d, 0x01,
                0xd9, 0x02, 0xe9, 0x6a, 0xa8, 0x05, 0x29, 0x0b, 0x9a, 0x4c, 0xaa,
                0x08, 0xb6, 0x08, 0xb4, 0x38, 0x6c, 0x09, 0x54, 0x75, 0xd4, 0x0a,
                0xa4, 0x05, 0x45, 0x55, 0x95, 0x0a, 0x9a, 0x04, 0x55, 0x44, 0xb5,
                0x04, 0x6a, 0x82, 0x6a, 0x05, 0xd2, 0x0a, 0x92, 0x6a, 0x4a, 0x05,
                0x55, 0x0a, 0x2a, 0x4a, 0x5a, 0x02, 0xb5, 0x02, 0xb2, 0x31, 0x69,
                0x03, 0x31, 0x73, 0xa9, 0x0a, 0x4a, 0x05, 0x2d, 0x55, 0x2d, 0x09,
                0x5a, 0x01, 0xd5, 0x48, 0xb4, 0x09, 0x68, 0x89, 0x54, 0x0b, 0xa4,
                0x0a, 0xa5, 0x6a, 0x95, 0x04, 0xad, 0x08, 0x6a, 0x44, 0xda, 0x04,
                0x74, 0x05, 0xb0, 0x25, 0x54, 0x03 };

        /**
         * 用于保存24节气
         */
        public static String[] principleTermNames = { "大寒", "雨水", "春分", "谷雨",
                "小满", "夏至", "大暑", "处暑", "秋分", "霜降", "小雪", "冬至" };
        /**
         * 用于保存24节气
         */
       public static String[] sectionalTermNames = { "小寒", "立春", "惊蛰", "清明",
                "立夏", "芒种", "小暑", "立秋", "白露", "寒露", "立冬", "大雪" };

        public SolarTermsUtil(Calendar calendar) {
            gregorianYear = calendar.get(Calendar.YEAR);
            gregorianMonth = calendar.get(Calendar.MONTH) + 1;
            gregorianDate = calendar.get(Calendar.DATE);
            computeChineseFields();
            computeSolarTerms();
        }

        public int computeChineseFields() {
            if (gregorianYear < 1901 || gregorianYear > 2100)
                return 1;
            int startYear = baseYear;
            int startMonth = baseMonth;
            int startDate = baseDate;
            chineseYear = baseChineseYear;
            chineseMonth = baseChineseMonth;
            chineseDate = baseChineseDate;
            // 第二个对应日，用以提高计算效率
            // 公历 2000 年 1 月 1 日，对应农历 4697 年 11 月 25 日
            if (gregorianYear >= 2000) {
                startYear = baseYear + 99;
                startMonth = 1;
                startDate = 1;
                chineseYear = baseChineseYear + 99;
                chineseMonth = 11;
                chineseDate = 25;
            }
            int daysDiff = 0;
            for (int i = startYear; i < gregorianYear; i++) {
                daysDiff += 365;
                if (isGregorianLeapYear(i))
                    daysDiff += 1; // leap year
            }
            for (int i = startMonth; i < gregorianMonth; i++) {
                daysDiff += daysInGregorianMonth(gregorianYear, i);
            }
            daysDiff += gregorianDate - startDate;

            chineseDate += daysDiff;
            int lastDate = daysInChineseMonth(chineseYear, chineseMonth);
            int nextMonth = nextChineseMonth(chineseYear, chineseMonth);
            while (chineseDate > lastDate) {
                if (Math.abs(nextMonth) < Math.abs(chineseMonth))
                    chineseYear++;
                chineseMonth = nextMonth;
                chineseDate -= lastDate;
                lastDate = daysInChineseMonth(chineseYear, chineseMonth);
                nextMonth = nextChineseMonth(chineseYear, chineseMonth);
            }
            return 0;
        }

        public int computeSolarTerms() {
            if (gregorianYear < 1901 || gregorianYear > 2100)
                return 1;
            sectionalTerm = sectionalTerm(gregorianYear, gregorianMonth);
            principleTerm = principleTerm(gregorianYear, gregorianMonth);
            return 0;
        }

        public static int sectionalTerm(int y, int m) {
            if (y < 1901 || y > 2100)
                return 0;
            int index = 0;
            int ry = y - baseYear + 1;
            while (ry >= sectionalTermYear[m - 1][index])
                index++;
            int term = sectionalTermMap[m - 1][4 * index + ry % 4];
            if ((ry == 121) && (m == 4))
                term = 5;
            if ((ry == 132) && (m == 4))
                term = 5;
            if ((ry == 194) && (m == 6))
                term = 6;
            return term;
        }

        public static int principleTerm(int y, int m) {
            if (y < 1901 || y > 2100)
                return 0;
            int index = 0;
            int ry = y - baseYear + 1;
            while (ry >= principleTermYear[m - 1][index])
                index++;
            int term = principleTermMap[m - 1][4 * index + ry % 4];
            if ((ry == 171) && (m == 3))
                term = 21;
            if ((ry == 181) && (m == 5))
                term = 21;
            return term;
        }

        /**
         * 用于判断输入的年份是否为闰年
         *
         * @param year
         *            输入的年份
         * @return true 表示闰年
         */
        public static boolean isGregorianLeapYear(int year) {
            boolean isLeap = false;
            if (year % 4 == 0)
                isLeap = true;
            if (year % 100 == 0)
                isLeap = false;
            if (year % 400 == 0)
                isLeap = true;
            return isLeap;
        }

        public static int daysInGregorianMonth(int y, int m) {
            int d = daysInGregorianMonth[m - 1];
            if (m == 2 && isGregorianLeapYear(y))
                d++; // 公历闰年二月多一天
            return d;
        }

        public static int daysInChineseMonth(int y, int m) {
            // 注意：闰月 m < 0
            int index = y - baseChineseYear + baseIndex;
            int v = 0;
            int l = 0;
            int d = 30;
            if (1 <= m && m <= 8) {
                v = chineseMonths[2 * index];
                l = m - 1;
                if (((v >> l) & 0x01) == 1)
                    d = 29;
            } else if (9 <= m && m <= 12) {
                v = chineseMonths[2 * index + 1];
                l = m - 9;
                if (((v >> l) & 0x01) == 1)
                    d = 29;
            } else {
                v = chineseMonths[2 * index + 1];
                v = (v >> 4) & 0x0F;
                if (v != Math.abs(m)) {
                    d = 0;
                } else {
                    d = 29;
                    for (int i = 0; i < bigLeapMonthYears.length; i++) {
                        if (bigLeapMonthYears[i] == index) {
                            d = 30;
                            break;
                        }
                    }
                }
            }
            return d;
        }

        public static int nextChineseMonth(int y, int m) {
            int n = Math.abs(m) + 1;
            if (m > 0) {
                int index = y - baseChineseYear + baseIndex;
                int v = chineseMonths[2 * index + 1];
                v = (v >> 4) & 0x0F;
                if (v == m)
                    n = -m;
            }
            if (n == 13)
                n = 1;
            return n;
        }

        // 大闰月的闰年年份
        private static int[] bigLeapMonthYears = { 6, 14, 19, 25, 33, 36, 38, 41,
                44, 52, 55, 79, 117, 136, 147, 150, 155, 158, 185, 193 };

        /**
         * 用于获取24节气的值
         *
         * @return 24节气的值
         */
        public String getSolartermsMsg() {
            String str = "";
            String gm = String.valueOf(gregorianMonth);
            if (gm.length() == 1)
                gm = ' ' + gm;
            String cm = String.valueOf(Math.abs(chineseMonth));
            if (cm.length() == 1)
                cm = ' ' + cm;
            String gd = String.valueOf(gregorianDate);
            if (gd.length() == 1)
                gd = ' ' + gd;
            String cd = String.valueOf(chineseDate);
            if (cd.length() == 1)
                cd = ' ' + cd;
            if (gregorianDate == sectionalTerm) {
                str = " " + sectionalTermNames[gregorianMonth - 1];
            } else if (gregorianDate == principleTerm) {
                str = " " + principleTermNames[gregorianMonth - 1];
            }
            return str;
        }
    }

    /**
     * 用于显示农历的初几这种格式
     *
     * @return 农历的日期
     */
    public String toString() {
        String message = "";
        // int n = mLuchDay % 10 == 0 ? 9 : mLuchDay % 10 - 1;
        message = getChinaCalendarMsg(mLuchYear, mLuchMonth, mLuchDay);
        if (StringUtil.isNullOrEmpty(message)) {
            String solarMsg = new SolarTermsUtil(mCurrenCalendar)
                    .getSolartermsMsg();
            // 判断当前日期是否为节气
            if (!StringUtil.isNullOrEmpty(solarMsg)) {
                message = solarMsg;
            } else {
                /**
                 * 判断当前日期是否为公历节日
                 */
                String gremessage = new GregorianUtil(mCurrenCalendar)
                        .getGremessage();
                if (!StringUtil.isNullOrEmpty(gremessage)) {
                    message = gremessage;
                } else if (mLuchDay == 1) {
                    message = CHINESE_NUMBER[mLuchMonth - 1] + "月";
                } else {
                    message = getChinaDayString(mLuchDay);
                }

            }
        }
        return message;
    }

    /**
     * 返回农历的年月日
     *
     * @return 农历的年月日格式
     */
    public String getDay() {
        return (isLoap ? "闰" : "") + CHINESE_NUMBER[mLuchMonth - 1] + "月"
                + getChinaDayString(mLuchDay);
    }

    /**
     * 把calendar转化为当前年月日
     *
     * @param calendar
     *            Calendar
     * @return 返回成转换好的 年月日格式
     */
    public static String getDay(Calendar calendar) {
        return simpleDateFormat.format(calendar.getTime());
    }

    /**
     * 用于比对二个日期的大小
     *
     * @param compareDate
     *            将要比对的时间
     * @param currentDate
     *            当前时间
     * @return true 表示大于当前时间 false 表示小于当前时间
     */
    public static boolean compare(Date compareDate, Date currentDate) {
        return chineseDateFormat.format(compareDate).compareTo(
                chineseDateFormat.format(currentDate)) >= 0;
    }

    /**
     * 获取当前周几
     *
     * @param calendar
     * @return
     */
    public static String getWeek(Calendar calendar) {
        return "周" + WEEK_NUMBER[calendar.get(Calendar.DAY_OF_WEEK) - 1] + "";
    }

    /**
     * 将当前时间转换成要展示的形式
     *
     * @param calendar
     * @return
     */
    public static String getCurrentDay(Calendar calendar) {
        return getDay(calendar) + " 农历" + new CalendarUtils(calendar).getDay()
                + " " + getWeek(calendar);
    }

    /**
     * 用于获取中国的传统节日
     *
     * @param month
     *            农历的月
     * @param day
     *            农历日
     * @return 中国传统节日
     */
    private String getChinaCalendarMsg(int year, int month, int day) {
        String message = "";
        if (((month) == 1) && day == 1) {
            message = "春节";
        } else if (((month) == 1) && day == 15) {
            message = "元宵";
        } else if (((month) == 5) && day == 5) {
            message = "端午";
        } else if ((month == 7) && day == 7) {
            message = "七夕";
        } else if (((month) == 8) && day == 15) {
            message = "中秋";
        } else if ((month == 9) && day == 9) {
            message = "重阳";
        } else if ((month == 12) && day == 8) {
            message = "腊八";
        } else {
            if (month == 12) {
                if ((((monthDays(year, month) == 29) && day == 29))
                        || ((((monthDays(year, month) == 30) && day == 30)))) {
                    message = "除夕";
                }
            }
        }
        return message;
    }




    /**
     * 字符串的处理类
     */
   public static class StringUtil {
        /**
         * 判断是否为null或空值
         *
         * @param str
         *            String
         * @return true or false
         */
        public static boolean isNullOrEmpty(String str) {
            return str == null || str.trim().length() == 0;
        }

        /**
         * 判断str1和str2是否相同
         *
         * @param str1
         *            str1
         * @param str2
         *            str2
         * @return true or false
         */
        public static boolean equals(String str1, String str2) {
            return str1 == str2 || str1 != null && str1.equals(str2);
        }

        /**
         * 判断str1和str2是否相同(不区分大小写)
         *
         * @param str1
         *            str1
         * @param str2
         *            str2
         * @return true or false
         */
        public static boolean equalsIgnoreCase(String str1, String str2) {
            return str1 != null && str1.equalsIgnoreCase(str2);
        }

        /**
         * 判断字符串str1是否包含字符串str2
         *
         * @param str1
         *            源字符串
         * @param str2
         *            指定字符串
         * @return true源字符串包含指定字符串，false源字符串不包含指定字符串
         */
        public static boolean contains(String str1, String str2) {
            return str1 != null && str1.contains(str2);
        }

        /**
         * 判断字符串是否为空，为空则返回一个空值，不为空则返回原字符串
         *
         * @param str
         *            待判断字符串
         * @return 判断后的字符串
         */
        public static String getString(String str) {
            return str == null ? "" : str;
        }
    }

    /**
     * 对公历日期的处理类
     */
    public static  class GregorianUtil {
        private final static String[][] GRE_FESTVIAL = {
                // 一月
                { "元旦", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
                        "", "", "", "", "", "", "", "", "", "", "", "", "", "", "" },
                // 二月
                { "", "", "", "", "", "", "", "", "", "", "", "", "", "情人", "", "",
                        "", "", "", "", "", "", "", "", "", "", "", "", "", "" },
                // 三月
                { "", "", "", "", "", "", "", "妇女", "", "", "", "植树", "", "", "",
                        "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
                        "" },
                // 四月
                { "愚人", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
                        "", "", "", "", "", "", "", "", "", "", "", "", "", "", "" },
                // 五月
                { "劳动", "", "", "青年", "", "", "", "", "", "", "", "", "", "", "",
                        "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
                        "" },
                // 六月
                { "儿童", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
                        "", "", "", "", "", "", "", "", "", "", "", "", "", "", "" },
                // 七月
                { "建党", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
                        "", "", "", "", "", "", "", "", "", "", "", "", "", "", "" },
                // 八月
                { "建军", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
                        "", "", "", "", "", "", "", "", "", "", "", "", "", "", "" },
                // 九月
                { "", "", "", "", "", "", "", "", "", "教师", "", "", "", "", "", "",
                        "", "", "", "", "", "", "", "", "", "", "", "", "", "", "" },
                // 十月
                { "国庆", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
                        "", "", "", "", "", "", "", "", "", "", "", "", "", "", "" },
                // 十一月
                { "", "", "", "", "", "", "", "", "", "", "光棍", "", "", "", "", "",
                        "", "", "", "", "", "", "", "", "", "", "", "", "", "", "" },
                // 十二月
                { "艾滋病", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
                        "", "", "", "", "", "", "", "", "", "圣诞", "", "", "", "",
                        "", "" }, };
        private int mMonth;
        private int mDay;

        public GregorianUtil(Calendar calendar) {
            mMonth = calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DATE);
        }

        public String getGremessage() {
            return GRE_FESTVIAL[mMonth][mDay - 1];
        }
    }


}
