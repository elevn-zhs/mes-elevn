package com.elevn.mes.common.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateTools {

    /**
     * 得到当前日期字符串
     * @param patten
     * @return
     */
    public static String getDateStr(String patten){
        if (patten == null || patten.equals("")){
            patten="yyy-MM-dd HH:mm:ss";
        }
        return  LocalDate.now().format(DateTimeFormatter.ofPattern(patten));
    }

}
