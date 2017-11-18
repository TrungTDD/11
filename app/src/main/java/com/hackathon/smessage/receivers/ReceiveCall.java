package com.hackathon.smessage.receivers;

import com.android.internal.telephony.ITelephony;
import com.hackathon.smessage.controllers.BlockedOperation;
import com.hackathon.smessage.utils.PhoneNumberUtils;
import com.hackathon.smessage.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;

import java.lang.reflect.Method;

/**
 * Created by tai.nguyenduc on 11/18/2017.
 */
public class ReceiveCall extends BroadcastReceiver {
    private ITelephony telephonyService;

    @Override
    public void onReceive(Context context, Intent intent) {
        Utils.LOG("Incoming phone : progressReceive");
        Bundle bundle = intent.getExtras();
        if(bundle != null){
            try{
                String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                    String inComingPhone = PhoneNumberUtils.format(intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER));
                    Utils.LOG("Incoming phone : " + inComingPhone);
                    Utils.LOG("Incoming phone is blocked: " + BlockedOperation.getInstance().isBlockedCall(inComingPhone));
                    if(BlockedOperation.getInstance().isBlockedCall(inComingPhone)){
                        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                        Class c = Class.forName(telephony.getClass().getName());
                        Method method = c.getDeclaredMethod("getITelephony");
                        method.setAccessible(true);
                        telephonyService = (ITelephony) method.invoke(telephony);
                        telephonyService.endCall();
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
