package com.hackathon.smessage.utils;


import android.app.Activity;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.renderscript.RenderScript;
import android.util.Log;
import com.hackathon.smessage.BuildConfig;

import com.hackathon.smessage.models.Message;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by hungt_000 on 11/18/2017.
 */

public class Utils {

    private static boolean m_isShowLog = BuildConfig.DEBUG;
    private static String m_LOG = "SMS_SECURITY";

    public static void LOG(String log){
        if(m_isShowLog)
            Log.d(m_LOG, log);
    }

    /**
     * Compare time of message: top --> down
     * @param o1
     * @param o2
     * @return
     */
    public static int compareTime(Message o1, Message o2) {
        Message current = (Message)o1;
        Message another = (Message)o2;

        SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.DATE_AND_TIME_FORMAT);
        Date myDate = null, anotherDate = null;
        try {
            myDate = sdf.parse(current.getDate());
            anotherDate = sdf.parse(another.getDate());
        } catch (ParseException e) {
            Utils.LOG(e.toString());
        }

        if(myDate.compareTo(anotherDate)>0){
            return 1;
        }else if(myDate.compareTo(anotherDate)<0){
            return -1;
        }
        return 0;
    }

    /**
     * Check body of message: ASCII or US2
     * @param message
     * @return
     */
    public static boolean isAsciiMessage(String message){

        for(int i = 0; i < message.length(); i++){
            char ch = message.charAt(i);
            if(ch > 127){
                return false;
            }
        }
        return true;
    }

    public static void setVibrate(Activity activity, int milisecond){
        Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(milisecond);
    }

    @SuppressWarnings("deprecation")
    public static void setVibrate(Context context, int milisecond){
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(milisecond);
    }

    public static void playRingtone(Context context){
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(context, notification);
        r.play();
    }


}
