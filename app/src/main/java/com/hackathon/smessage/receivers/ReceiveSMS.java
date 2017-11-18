package com.hackathon.smessage.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.hackathon.smessage.activities.ReplyMessageActivity;
import com.hackathon.smessage.configs.AppConfigs;
import com.hackathon.smessage.configs.Defines;
import com.hackathon.smessage.controllers.MessageOpearation;
import com.hackathon.smessage.models.Message;
import com.hackathon.smessage.sqlitehelper.SqliteHelper;
import com.hackathon.smessage.utils.PhoneNumberUtils;
import com.hackathon.smessage.utils.Security;
import com.hackathon.smessage.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by tai.nguyenduc on 11/18/2017.
 */

public class ReceiveSMS extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(!SqliteHelper.isInitialized()){
            SqliteHelper.init(context);
        }

        if(!AppConfigs.getInstance().isInitialized()){
            AppConfigs.getInstance().init(context);
        }

        //skip double sms (bug global)
        ArrayList<Message> list = getMessages(intent);
        long lastTimeReceived = AppConfigs.getInstance().getLastTimeReceivedSms();
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if(list.size() > 0 && (currentTime - lastTimeReceived >= Defines.DURATION_RECEIVED_SMS)) {

            for (Message message : list) {
                message.setIsSecurity(Security.getInstance().isSecurity(message.getBody()));
                MessageOpearation.getInstance().add(message);
                message.decrypt(); //decrypt to show
            }

            currentTime = Calendar.getInstance().getTimeInMillis();
            AppConfigs.getInstance().setLastTimeReceivedSms(currentTime);
            if (AppConfigs.getInstance().isAppRunning() || AppConfigs.getInstance().isPopupShowing()) {
                sendBroadcastToApp(context, list.get(0));
            } else {
                showReply(context, list.get(0));
            }
        }
        //ignore sms to old inbox
        abortBroadcast();

    }

    private ArrayList<Message> getMessages(Intent intent){
        ArrayList<Message> list = new ArrayList<>();
        Bundle bundle = intent.getExtras();
        if(bundle != null){
            Object[] smsExtra = (Object[]) bundle.get("pdus");
            for(int i = 0; i < smsExtra.length; i++) {
                SmsMessage smsMessage;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    String format = bundle.getString("format");
                    smsMessage = SmsMessage.createFromPdu((byte[]) smsExtra[i], format);
                } else {
                    smsMessage = SmsMessage.createFromPdu((byte[]) smsExtra[i]);
                }

                String body = smsMessage.getMessageBody();
                String phone = PhoneNumberUtils.format(smsMessage.getOriginatingAddress());
                String date = TimeUtils.getInstance().getTimeSystem();
                boolean isSecurity = false;
                Message sms = new Message(phone, body, date, false, true, Message.SEND_TO_STATUS_SENT, isSecurity);
                list.add(sms);
            }
        }
        return list;
    }

    private void showReply(Context context, Message message){
        Intent intent = new Intent(context, ReplyMessageActivity.class);
        intent.putExtra(Defines.PASS_MESSAGE_FROM_RECEIVER, message);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void sendBroadcastToApp(Context context, Message message){
        Intent intent = new Intent();
        intent.putExtra(Defines.PASS_MESSAGE_FROM_RECEIVER, message);
        intent.setAction(Defines.ACTION_RECEIVE_SMS);
        context.sendBroadcast(intent);
    }

}
