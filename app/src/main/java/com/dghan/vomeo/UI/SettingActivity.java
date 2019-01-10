package com.dghan.vomeo.UI;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.dghan.vomeo.R;

import java.util.Calendar;
import java.util.Date;

public class SettingActivity extends PreferenceActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Load setting
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainSettingFragment()).commit();

    }

    public static class MainSettingFragment extends PreferenceFragment{

        @Override
        public void onCreate(@Nullable final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);

            bindSummaryValue(findPreference("key_full_name"));
            bindSummaryValue(findPreference("key_email"));
            bindSummaryValue(findPreference("key_set_time"));
            bindSummaryValue(findPreference("key_theme"));
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        StringBuilder info = new StringBuilder();

        //Receiving info
        info.append("\nName: "+ sharedPreferences.getString("key_full_name",""));
        info.append("\nEmail: " + sharedPreferences.getString("key_email", ""));
        info.append("\nEnable Alarm: " + sharedPreferences.getBoolean("enable_alarm", false));
        info.append("\nTime: " + sharedPreferences.getString("key_set_time", "-1") + ":00");
        info.append("\nTheme: " + sharedPreferences.getString("key_theme", "-1"));

        Log.d("checkInfo",info.toString());

        if (sharedPreferences.getBoolean("enable_alarm", false) == true) {
            setTimer();
        }

    }

    private void setTimer() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        Date date = new Date();
        Calendar cal_alarm = Calendar.getInstance();
        Calendar cal_now = Calendar.getInstance();

        cal_now.setTime(date);
        cal_alarm.setTime(date);

        String toHour = sharedPreferences.getString("key_set_time", "-1");
        int hour = Integer.parseInt(toHour);
        cal_alarm.set(Calendar.HOUR_OF_DAY, hour);
        cal_alarm.set(Calendar.MINUTE,0);
        cal_alarm.set(Calendar.SECOND,0);

        if(cal_alarm.before(cal_now)){
            cal_alarm.add(Calendar.DATE,1);
        }

        Intent i = new Intent(SettingActivity.this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(SettingActivity.this, 24444,i,0);
        alarmManager.set(AlarmManager.RTC_WAKEUP,cal_alarm.getTimeInMillis(),pendingIntent);
    }

    private static void bindSummaryValue(Preference preference){
        preference.setOnPreferenceChangeListener(listener);
        listener.onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    private static Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();
            if(preference instanceof ListPreference){
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                //Set the summary to reflect new value
                preference.setSummary(index > 0
                        ? listPreference.getEntries()[index]
                        : null);
            }else if (preference instanceof EditTextPreference) {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };


}
