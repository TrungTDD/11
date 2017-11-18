package com.hackathon.smessage.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.hackathon.smessage.R;
import com.hackathon.smessage.fragments.NestedPreferenceFragment;
import com.hackathon.smessage.fragments.SettingsFragment;


public class SettingsActivity extends AppCompatActivity implements SettingsFragment.Callback {

    private static final String TAG_NESTED = "SUB_FRAGMENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, new SettingsFragment())
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public void onNestedPreferenceSelected(int key) {
        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, NestedPreferenceFragment.newInstance(key), TAG_NESTED)
                .addToBackStack(TAG_NESTED).commit();
    }

}
