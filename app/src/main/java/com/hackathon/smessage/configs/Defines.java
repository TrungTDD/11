package com.hackathon.smessage.configs;

import com.hackathon.smessage.R;

import static com.hackathon.smessage.BuildConfig.APPLICATION_ID;

/**
 * Created by tai.nguyenduc on 11/18/2017.
 */

public interface Defines {

    public static final int MENU_INBOX_COMMON = 0;
    public static final int MENU_INBOX_SECURITY = 1;

    public static final String PASS_MESSAGE_FROM_INBOX_TO_CONVERSATION = "pass_data_from_inbox_to_conversation";
    public static final String PASS_MESSAGE_FROM_CONVERSATION_TO_SEND = "pass_data_from_conversation_to_send";
    public static final String PASS_MESSAGE_FROM_RECEIVER = "pass_data_from_broadcase_receiver";
    public static final String ACTION_RECEIVE_SMS = "com.smessage.RECEIVE_SMS";
    public static final String ACTION_SEND_SMS = "com.smessage.SEND_SMS";

    public static final long DURATION_RECEIVED_SMS = 200;

    public static final int REQUEST_PICK_CONTACT = 201;

    public static final String PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=" + APPLICATION_ID;

    public static final int []AVARTA_COLOR = {
            R.drawable.avatar_color_1,
            R.drawable.avatar_color_2,
            R.drawable.avatar_color_3,
            R.drawable.avatar_color_4,
            R.drawable.avatar_color_5,
            R.drawable.avatar_color_6,
            R.drawable.avatar_color_7,
            R.drawable.avatar_color_8,
            R.drawable.avatar_color_9,
            R.drawable.avatar_color_10,
            R.drawable.avatar_color_11,
            R.drawable.avatar_color_12
    };
}