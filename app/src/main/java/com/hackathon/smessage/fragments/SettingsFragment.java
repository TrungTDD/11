package com.hackathon.smessage.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.hackathon.smessage.R;

/**
 * Created by tai.nguyenduc on 11/18/2017.
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener{

    private Callback mCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = activity = getActivity();

        if (activity instanceof Callback) {
            mCallback = (Callback) activity;
        } else {
            throw new IllegalStateException("Owner must implement URLCallback interface");
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings_preferences);

        // add listeners for non-default actions
        Preference preference = findPreference(getString(R.string.key_common_privacy));
        preference.setOnPreferenceClickListener(this);

        preference = findPreference(getString(R.string.key_security_privacy));
        preference.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(getString(R.string.key_common_privacy))) {
            getActivity().setTitle(R.string.common_privacy);
            mCallback.onNestedPreferenceSelected(NestedPreferenceFragment.NESTED_SCREEN_INBOX_COMMON_PRIVACY);
        }

        if (preference.getKey().equals(getString(R.string.key_security_privacy))) {
            getActivity().setTitle(R.string.security_privacy);
            mCallback.onNestedPreferenceSelected(NestedPreferenceFragment.NESTED_SCREEN_INBOX_SECURITY_PRIVACY);
        }
        return false;
    }

    public interface Callback {
        void onNestedPreferenceSelected(int key);
    }

}
