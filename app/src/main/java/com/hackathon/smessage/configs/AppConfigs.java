package com.hackathon.smessage.configs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.hackathon.smessage.R;

/**
 * Created by tai.nguyenduc on 11/18/2017.
 */

public class AppConfigs {
    private static AppConfigs sInstance = null;
    private Context mContext;

    private SharedPreferences mSharedPreferences;

    private boolean mIsAppRunning;
    private boolean mIsPopupShowing;
    private boolean mIsSecurity;

    private AppConfigs(){
        mIsAppRunning = false;
        mIsPopupShowing = false;
        mIsSecurity = false;
    }

    public static AppConfigs getInstance(){
        if(sInstance == null){
            sInstance = new AppConfigs();
        }
        return sInstance;
    }

    public boolean isInitialized(){
        return (mSharedPreferences != null);
    }

    public void init(Context context){
        mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setIsAppRunning(boolean isRunning){
        mIsAppRunning = isRunning;
    }

    public boolean isAppRunning(){
        return mIsAppRunning;
    }

    public void setIsPopupShowing(boolean showing){
        mIsPopupShowing = showing;
    }

    public boolean isPopupShowing(){
        return mIsPopupShowing;
    }


    public void setIsSecurity(boolean isSecurity){
        mIsSecurity = isSecurity;
    }

    public boolean isSecurity(){
        return mIsSecurity;
    }

    public void setLastTimeReceivedSms(long time){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putLong(mContext.getString(R.string.key_last_time_received_sms), time);
        editor.commit();
    }

    public long getLastTimeReceivedSms(){
        return mSharedPreferences.getLong(mContext.getString(R.string.key_last_time_received_sms), 0);
    }

    public boolean isEnablePassword(boolean isSecurity){
        if(isSecurity){
            return mSharedPreferences.getBoolean(mContext.getString(R.string.key_privacy_inbox_security_enable_password), false);
        }
        return mSharedPreferences.getBoolean(mContext.getString(R.string.key_privacy_inbox_common_enable_password), false);
    }

    public void setIsEnablePassword(boolean isSecurity, boolean value){
        String key = mContext.getString(R.string.key_privacy_inbox_common_enable_password);
        if(isSecurity){
            key = mContext.getString(R.string.key_privacy_inbox_security_enable_password);
        }
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public void setPassword(boolean isSecurity, String password){
        String key = mContext.getString(R.string.key_password_inbox_common);
        if(isSecurity){
            key = mContext.getString(R.string.key_password_inbox_security);
        }
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, password);
        editor.commit();
    }

    public String getPassword(boolean isSecurity){
        if(isSecurity){
            return mSharedPreferences.getString(mContext.getString(R.string.key_password_inbox_security), "");
        }
        return mSharedPreferences.getString(mContext.getString(R.string.key_password_inbox_common), "");
    }

    public void setRing(boolean isSecurity, boolean isRing){
        String key = mContext.getString(R.string.key_privacy_inbox_common_ring);
        if(isSecurity){
            key = mContext.getString(R.string.key_privacy_inbox_security_ring);
        }
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(key, isRing);
        editor.commit();
    }

    public boolean isRing(boolean isSecurity){
        if(isSecurity){
            return mSharedPreferences.getBoolean(mContext.getString(R.string.key_privacy_inbox_security_ring), false);
        }
        return mSharedPreferences.getBoolean(mContext.getString(R.string.key_privacy_inbox_common_ring), false);
    }

    public void setVibrate(boolean isSecurity, boolean isVibrate){
        String key = mContext.getString(R.string.key_privacy_inbox_common_vibrate);
        if(isSecurity){
            key = mContext.getString(R.string.key_privacy_inbox_security_vibrate);
        }
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(key, isVibrate);
        editor.commit();
    }

    public boolean isVibrate(boolean isSecurity){
        if(isSecurity){
            return mSharedPreferences.getBoolean(mContext.getString(R.string.key_privacy_inbox_security_vibrate), false);
        }
        return mSharedPreferences.getBoolean(mContext.getString(R.string.key_privacy_inbox_common_vibrate), false);
    }

    public void setStatusBar(boolean isSecurity, boolean isShowStatus){
        String key = mContext.getString(R.string.key_privacy_inbox_common_status_bar);
        if(isSecurity){
            key = mContext.getString(R.string.key_privacy_inbox_security_status_bar);
        }
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(key, isShowStatus);
        editor.commit();
    }

    public boolean isStatusBar(boolean isSecurity){
        if(isSecurity){
            return mSharedPreferences.getBoolean(mContext.getString(R.string.key_privacy_inbox_security_status_bar), false);
        }
        return mSharedPreferences.getBoolean(mContext.getString(R.string.key_privacy_inbox_common_status_bar), false);
    }

    public void setMuteContact(String phone, boolean isMute){
        String key = phone;
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(phone,isMute);
        editor.commit();
    }

    public boolean isMuteContact(String phone){
        String key = phone;
        return mSharedPreferences.getBoolean(phone,false);
    }

    public boolean isNotification(boolean isSecurity){
        if(isSecurity){
            return mSharedPreferences.getBoolean(mContext.getString(R.string.key_privacy_inbox_security_notification), false);
        }
        return mSharedPreferences.getBoolean(mContext.getString(R.string.key_privacy_inbox_common_notification), false);
    }

    public boolean isReplyPopup(boolean isSecurity){
        if(isSecurity){
            return mSharedPreferences.getBoolean(mContext.getString(R.string.key_privacy_inbox_security_reply_popup), false);
        }
        return mSharedPreferences.getBoolean(mContext.getString(R.string.key_privacy_inbox_common_reply_popup), false);
    }

}