package org.example.springboot.util;

import java.time.LocalDate;

public class DateUtils {
    /**
     * 计算上个月的第一天�?
     * @return 上个月第一天的LocalDate对象�?
     */
    public static LocalDate getLastMonthFirstDay() {
        LocalDate today = LocalDate.now(); // 获取当前日期
        int currentMonth = today.getMonthValue(); // 获取当前月份
        int currentYear = today.getYear(); // 获取当前年份

        // 如果当前月份�?月，则上个月是去年的12�?
        if (currentMonth == 1) {
            return LocalDate.of(currentYear - 1, 12, 1);
        } else {
            // 否则，上个月的第一天就是当前年加上上个月的月份�?，日期为1
            return LocalDate.of(currentYear, currentMonth - 1, 1);
        }
    }
}
