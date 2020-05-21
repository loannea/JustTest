package com.loannea.signature.constant;

import java.time.format.DateTimeFormatter;
/**
 * {@code DateCovertConstant} 日期转换常量
 * @author zjw
 */
public class DateCovertConstant {

    public static DateTimeFormatter full = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static DateTimeFormatter simple = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static DateTimeFormatter middle = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    public static DateTimeFormatter simpleByChinese = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    public static DateTimeFormatter easy = DateTimeFormatter.ofPattern("yyyyMMdd");
}
