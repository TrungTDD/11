package com.hackathon.smessage.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hackathon.smessage.R;
import com.hackathon.smessage.configs.AppConfigs;

/**
 * Created by tai.nguyenduc on 11/18/2017.
 */

public class NestedPreferenceFragment extends PreferenceFragment{

    public static final int NESTED_SCREEN_INBOX_COMMON_PRIVACY = 1;
    public static final int NESTED_SCREEN_INBOX_SECURITY_PRIVACY = 2;

    private static final String TAG_KEY = "NESTED_KEY";

    private boolean mIsSettingInboxSecurity;
    private SwitchPreference mSpEnablePassword;
    private Preference mSpChangePassword;

    public static NestedPreferenceFragment newInstance(int key) {
        NestedPreferenceFragment fragment = new NestedPreferenceFragment();
        Bundle args = new Bundle();
        args.putInt(TAG_KEY, key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadPreferences();
        getWidgets();
        setWidgets();
        addWidgetsListener();
    }

    private void loadPreferences(){
        int key = getArguments().getInt(TAG_KEY);
        switch (key) {
            case NESTED_SCREEN_INBOX_COMMON_PRIVACY:
                mIsSettingInboxSecurity = false;
                addPreferencesFromResource(R.xml.privacy_inbox_common);
                break;

            case NESTED_SCREEN_INBOX_SECURITY_PRIVACY:
                mIsSettingInboxSecurity = true;
                addPreferencesFromResource(R.xml.privacy_inbox_security);
                break;
            default:
                break;
        }
    }

    private void getWidgets(){
        if(mIsSettingInboxSecurity) {
            mSpEnablePassword = (SwitchPreference) findPreference(getString(R.string.key_privacy_inbox_security_enable_password));
            mSpChangePassword = (Preference) findPreference(getString(R.string.key_privacy_inbox_security_change_password));
        }
        else{
            mSpEnablePassword = (SwitchPreference) findPreference(getString(R.string.key_privacy_inbox_common_enable_password));
            mSpChangePassword = (Preference) findPreference(getString(R.string.key_privacy_inbox_common_change_password));
        }
    }

    private void setWidgets(){

    }

    private void addWidgetsListener(){
        mSpEnablePassword.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                if(AppConfigs.getInstance().isEnablePassword(mIsSettingInboxSecurity)){
                    showDisablePassword();
                }
                else{
                    showRegisterPassword();
                }
                return false;
            }
        });
        mSpChangePassword.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showChangePassword();
                return true;
            }
        });
    }

    private void showRegisterPassword(){
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View layout = inflater.inflate(R.layout.dialog_register_password, null);

        final EditText etPassword = (EditText)layout.findViewById(R.id.etPassword);
        final EditText etConfirmPassword = (EditText)layout.findViewById(R.id.etConfirmPassword);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.register_password)
                .setIcon(R.drawable.ic_dialog_locked)
                .setView(layout)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button btnOK = dialog.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE);
                btnOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String pass = etPassword.getText().toString();
                        String confirmPass = etConfirmPassword.getText().toString();
                        if(pass.trim().equals("")){
                            Toast.makeText(getActivity(), getString(R.string.register_password_empty), Toast.LENGTH_SHORT).show();
                        }
                        else {
                            if(pass.equals(confirmPass)) {
                                AppConfigs.getInstance().setPassword(mIsSettingInboxSecurity, pass);
                                mSpEnablePassword.setChecked(true);
                                AppConfigs.getInstance().setIsEnablePassword(mIsSettingInboxSecurity, true);
                                Toast.makeText(getActivity(), getString(R.string.successful), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                            else{
                                Toast.makeText(getActivity(), getString(R.string.register_password_wrong_confirm), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });
        dialog.show();
    }

    private void showDisablePassword(){
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View layout = inflater.inflate(R.layout.dialog_enter_password, null);

        final EditText etPassword = (EditText)layout.findViewById(R.id.etPassword);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.password)
                .setIcon(R.drawable.ic_dialog_locked)
                .setView(layout)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button btnOK = dialog.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE);
                btnOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String currentPass = AppConfigs.getInstance().getPassword(mIsSettingInboxSecurity);
                        String pass = etPassword.getText().toString();
                        if(currentPass.equals(pass)) {
                            mSpEnablePassword.setChecked(false);
                            AppConfigs.getInstance().setIsEnablePassword(mIsSettingInboxSecurity, false);
                            dialog.dismiss();
                        }
                        else{
                            Toast.makeText(getActivity(), getString(R.string.wrong_password), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        dialog.show();
    }

    private void showChangePassword(){
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View layout = inflater.inflate(R.layout.dialog_change_password, null);

        final EditText etOldPassword = (EditText)layout.findViewById(R.id.etOldPassword);
        final EditText etPassword = (EditText)layout.findViewById(R.id.etPassword);
        final EditText etConfirmPassword = (EditText)layout.findViewById(R.id.etConfirmPassword);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.change_password)
                .setIcon(R.drawable.ic_dialog_locked)
                .setView(layout)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button btnOK = dialog.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE);
                btnOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String currentPass = AppConfigs.getInstance().getPassword(mIsSettingInboxSecurity);
                        String oldPass = etOldPassword.getText().toString();
                        String pass = etPassword.getText().toString();
                        String confirmPass = etConfirmPassword.getText().toString();
                        if(currentPass.equals(oldPass)) {
                            if (pass.trim().equals("")) {
                                Toast.makeText(getActivity(), getString(R.string.register_password_empty), Toast.LENGTH_SHORT).show();
                            } else {
                                if (pass.equals(confirmPass)) {
                                    AppConfigs.getInstance().setPassword(mIsSettingInboxSecurity, pass);
                                    mSpEnablePassword.setChecked(true);
                                    AppConfigs.getInstance().setIsEnablePassword(mIsSettingInboxSecurity, true);
                                    Toast.makeText(getActivity(), getString(R.string.successful), Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(getActivity(), getString(R.string.register_password_wrong_confirm), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        else{
                            Toast.makeText(getActivity(), getString(R.string.wrong_password), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        dialog.show();
    }
}