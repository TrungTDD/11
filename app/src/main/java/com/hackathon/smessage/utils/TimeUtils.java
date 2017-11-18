package com.hackathon.smessage.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
/**
 * Created by KA on 11/18/2017.
 */

public class TimeUtils {

    public static final String DATE_AND_TIME_FORMAT = "HH:mm:ss, dd/MM/yyyy";
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String TIME_FORMAT = "HH:mm:ss";

    private static TimeUtils sInstance = null;

    private TimeUtils(){

    }

    public static TimeUtils getInstance(){
        if(sInstance == null){
            sInstance = new TimeUtils();
        }
        return sInstance;
    }

    public String getTimeSystem(){
        String time = new SimpleDateFormat(DATE_AND_TIME_FORMAT).format(Calendar.getInstance().getTime());
        return time;
    }

    public boolean isToday(String date){
        String today = new SimpleDateFormat(DATE_AND_TIME_FORMAT).format(Calendar.getInstance().getTime());

        String splitDate = date.substring(TIME_FORMAT.length(), DATE_AND_TIME_FORMAT.length());
        String splitToday = today.substring(TIME_FORMAT.length(),DATE_AND_TIME_FORMAT.length());
        return splitDate.equals(splitToday);
    }

    public String getTimeFormat(String format, String date){
        String output = date;
        if(format.equals(TIME_FORMAT)){
            output = date.substring(0, format.length());
        }
        else if(format.equals(DATE_FORMAT)){
            output = date.substring(TIME_FORMAT.length() + 1, date.length());
        }
        return output;
    }
}
