package com.hackathon.smessage.receivers;

import android.app.ActionBar;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;

import com.hackathon.smessage.activities.ConversationActivity;
import com.hackathon.smessage.activities.ReplyMessageActivity;
import com.hackathon.smessage.configs.AppConfigs;
import com.hackathon.smessage.configs.Defines;
import com.hackathon.smessage.controllers.BlockedOperation;
import com.hackathon.smessage.controllers.MessageOpearation;
import com.hackathon.smessage.models.Message;
import com.hackathon.smessage.sqlitehelper.SqliteHelper;
import com.hackathon.smessage.utils.PhoneNumberUtils;
import com.hackathon.smessage.utils.Security;
import com.hackathon.smessage.utils.TimeUtils;
import com.hackathon.smessage.utils.Utils;

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
            if(!BlockedOperation.getInstance().isBlockedMessage(list.get(0), true)
                    && !BlockedOperation.getInstance().isBlockedMessage(list.get(0), false)) {
                for (Message message : list) {
                    message.setIsSecurity(Security.getInstance().isSecurity(message.getBody()));
                    MessageOpearation.getInstance().add(message);
                    message.decrypt(); //decrypt to show
                }

                currentTime = Calendar.getInstance().getTimeInMillis();
                AppConfigs.getInstance().setLastTimeReceivedSms(currentTime);

                boolean isSecurity  = list.get(0).isSecurity();
                if (AppConfigs.getInstance().isAppRunning() || AppConfigs.getInstance().isPopupShowing()) {
                    sendBroadcastToApp(context, list.get(0));
                } else {
                        if(AppConfigs.getInstance().isReplyPopup(isSecurity))
                            showReply(context, list.get(0));

                }



                if(AppConfigs.getInstance().isNotification(isSecurity)){

                    if(AppConfigs.getInstance().isRing(isSecurity)){
                        //ring
                        if(!AppConfigs.getInstance().isMuteContact(list.get(0).getPhone())){
                            Utils.playRingtone(context);
                        }
                    }else{
                        if(!AppConfigs.getInstance().isMuteContact(list.get(0).getPhone())){
                            Utils.playRingtone(context);
                        }
                    }

                    if(AppConfigs.getInstance().isVibrate(isSecurity)){
                        Utils.setVibrate(context, 500);
                    }

                    if(AppConfigs.getInstance().isStatusBar(isSecurity)){
                        //show Notifu
                        if(!AppConfigs.getInstance().isEnablePassword(isSecurity)){
                            boolean showUnread = false;
                            //list.get(0).setUnreadNumber(MessageOpearation.getInstance().getUnreadNumber(list.get(0)));
                            if (AppConfigs.getInstance().isUnreadMessage(isSecurity)){
                                showUnread = true;
                            }

                            showNotification(context,list.get(0),showUnread);
                        }
                    }


                }

            }
            else{
                Utils.LOG("AAAAAAAAAAAAAAAAa");
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


        private void showNotification(Context context,Message message,boolean showUnRead){


            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(android.R.drawable.stat_notify_chat)
                            .setContentTitle(message.getPhone()+((showUnRead)?"("+MessageOpearation.getInstance().getUnreadNumber(message)
                                    +" tin nhắn)":""))
                            .setAutoCancel(true)
                            .setContentText(message.getBody())
                            .setPriority(android.support.v7.app.NotificationCompat.PRIORITY_MAX)
                            .setVibrate(new long[]{0});


// Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(context, ConversationActivity.class);
            resultIntent.putExtra(Defines.PASS_MESSAGE_FROM_INBOX_TO_CONVERSATION, message);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
// Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(ConversationActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );


            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
            mNotificationManager.notify(1, mBuilder.build());
        }
    }

