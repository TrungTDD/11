package com.hackathon.smessage.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.hackathon.smessage.configs.AppConfigs;
import com.hackathon.smessage.configs.Defines;
import com.hackathon.smessage.controllers.ContactOpearation;
import com.hackathon.smessage.sqlitehelper.SqliteHelper;


public class DefaultActivity extends AppCompatActivity {

    protected Activity mActivity;

    //broadcast: update inbox
    protected BroadcastReceiver mBroadcastReceivedSMS;
    protected IntentFilter mIntentFilterSMS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIntentFilterSMS = new IntentFilter(Defines.ACTION_RECEIVE_SMS);
        if(!ContactOpearation.isInitialized()){
            ContactOpearation.init(this);
        }
        if(!SqliteHelper.isInitialized()){
            SqliteHelper.init(this);
        }
        if(!AppConfigs.getInstance().isInitialized()){
            AppConfigs.getInstance().init(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mBroadcastReceivedSMS != null){
            registerReceiver(mBroadcastReceivedSMS, mIntentFilterSMS);
        }
        AppConfigs.getInstance().setIsAppRunning(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mBroadcastReceivedSMS != null){
            unregisterReceiver(mBroadcastReceivedSMS);
        }
        AppConfigs.getInstance().setIsAppRunning(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
