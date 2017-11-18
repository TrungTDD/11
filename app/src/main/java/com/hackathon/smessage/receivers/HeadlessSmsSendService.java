package com.hackathon.smessage.receivers;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by tai.nguyenduc on 11/18/2017.
 */

public class HeadlessSmsSendService extends IntentService {

    public HeadlessSmsSendService() {
        super(HeadlessSmsSendService.class.getName());

    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

}
