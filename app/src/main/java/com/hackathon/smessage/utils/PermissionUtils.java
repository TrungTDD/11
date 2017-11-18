package com.hackathon.smessage.utils;

/**
 * Created by tam on 18-11-2017.
 */
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.hackathon.smessage.R;

public class PermissionUtils {
    private static final int PERMISSIONS_REQUEST = 1000;

    //index in array
    private static final int PERMISSIONS_SEND_SMS = 0;
    private static final int PERMISSIONS_RECEIVE_SMS = 1;
    private static final int PERMISSIONS_READ_CONTACTS = 2;
    private static final int PERMISSIONS_WRITE_CONTACTS = 3;
    private static final int PERMISSIONS_CALL_PHONE = 4;
    private static final int PERMISSIONS_READ_PHONE_STATE = 5;

    //Permission
    private static final String mPermisions[] = {
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE
    };

    public static boolean onRequestPermissionsResult(Activity activity, int requestCode) {
        switch (requestCode){
            case PERMISSIONS_REQUEST:
                if(isAllPermissionAlready(activity)) {
                    return true;
                }
                else if(shouldShowRequestPermissionsRationale(activity)){
                    showExplain(activity);
                }
                else{
                    showRequestGoSettingOrExit(activity);
                }
                break;
        }
        return false;
    }

    public static void requestPermissions(Activity activity){
        ActivityCompat.requestPermissions(activity, mPermisions, PERMISSIONS_REQUEST);
    }

    public static boolean isAllPermissionAlready(Activity activity){
        for(int i = 0; i < mPermisions.length; i++){
            if (ActivityCompat.checkSelfPermission(activity, mPermisions[i]) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    public static void requireRegisterDefaultApp(Activity activity){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (!Telephony.Sms.getDefaultSmsPackage(activity).equals(activity.getPackageName())) {
                Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, activity.getPackageName());
                activity.startActivity(intent);
            }
        }
    }

    private static boolean shouldShowRequestPermissionsRationale(Activity activity){
        for(int i = 0; i < mPermisions.length; i++){
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, mPermisions[i])){
                return true;
            }
        }
        return false;
    }

    private static void showExplain(final  Activity activity){
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setTitle(R.string.permission);
        dialog.setMessage(R.string.explain_info);

        dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestPermissions(activity);
            }
        });
        dialog.create().show();
    }

    private static void showRequestGoSettingOrExit(final  Activity activity){
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setTitle(R.string.permission);
        dialog.setMessage(R.string.confirm_go_setting_exit);

        dialog.setPositiveButton(R.string.setting, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.finish();
                gotoSetting(activity);
            }
        });
        dialog.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.finish();
            }
        });

        dialog.create().show();
    }

    private static void gotoSetting(Activity activity){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }
}
