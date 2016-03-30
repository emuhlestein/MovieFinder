package com.intelliviz.moviefinder.ui;

import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import com.intelliviz.moviefinder.R;

/**
 * Settings activity. Manage user setttings.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener{

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_sort_by_key)));
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref_general);
    }

    private void bindPreferenceSummaryToValue(Preference preference) {

        preference.setOnPreferenceChangeListener(this);

        onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                .getString(preference.getKey(), ""));

    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String value = newValue.toString();
        String key = preference.getKey();
        Preference pref = findPreference(key);

        String v = "";
        if (preference instanceof ListPreference) {
            ListPreference listPref = (ListPreference) preference;
            int index = listPref.findIndexOfValue(newValue.toString());
            CharSequence[] entries = listPref.getEntries();
            preference.setSummary(entries[index]);
            //v = s.toString();
        }

        //String val = getResources().getString((String)newValue);

        //preference.get
        //preference.setSummary((String) newValue);
        return true;
    }
}
