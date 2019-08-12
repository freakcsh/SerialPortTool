package com.freak.serialporttool.utils;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Freak
 * @date 2019/8/12.
 */
public class TimeUtil {

    @SuppressLint("SimpleDateFormat")
    public static final SimpleDateFormat DEFAULT_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public static String currentTime() {
        Date date = new Date();
        return DEFAULT_FORMAT.format(date);
    }
}
