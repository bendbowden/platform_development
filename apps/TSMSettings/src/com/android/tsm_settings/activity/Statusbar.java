/* //device/apps/Settings/src/com/android/settings/Keyguard.java
**
** Copyright 2006, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
** http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/

package com.android.tsm_settings.activity;

import com.android.tsm_settings.R;

import com.android.tsm_settings.ColorPickerDialog;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.os.Bundle;
import android.util.Log;

public class Statusbar extends PreferenceActivity {
    private static final String TAG = "TSMSettings[Statusbar]";

    private static final String DISPLAY_CLOCK_PREF = "display_clock";
    private static final String CLOCK_COLOR_PREF = "clock_color";
    private static final String BATTERY_PERCENTAGE_PREF = "battery_percentage";
    private static final String BATTERY_COLOR_PREF = "battery_color";

    private CheckBoxPreference mDisplayClockPref;
    private Preference mClockColorPref;
    private CheckBoxPreference mBatteryPercentagePref;
    private Preference mBatteryColorPref;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.statusbar);

        PreferenceScreen prefSet = getPreferenceScreen();

        mDisplayClockPref = (CheckBoxPreference) prefSet.findPreference(DISPLAY_CLOCK_PREF);
        mDisplayClockPref.setChecked(Settings.System.getInt(
                getContentResolver(),
                Settings.System.DISPLAY_CLOCK, 1) != 0);

        mClockColorPref = prefSet.findPreference(CLOCK_COLOR_PREF);

        mBatteryPercentagePref = (CheckBoxPreference) prefSet.findPreference(BATTERY_PERCENTAGE_PREF);
        mBatteryPercentagePref.setChecked(Settings.System.getInt(
                getContentResolver(),
                Settings.System.BATTERY_PERCENTAGE, 1) != 0);

        mBatteryColorPref = prefSet.findPreference(BATTERY_COLOR_PREF);

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
    boolean value;
	if (preference == mClockColorPref) {
            ColorPickerDialog cp = new ColorPickerDialog(this,
            mClockFontColorListener,
            readClockFontColor());
            cp.show();
        
        } else if (preference == mBatteryColorPref) {
            ColorPickerDialog cp = new ColorPickerDialog(this,
            mBatteryFontColorListener,
            readBatteryFontColor());
            cp.show();
        } else if (preference == mDisplayClockPref) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.DISPLAY_CLOCK,
                    mDisplayClockPref.isChecked() ? 1 : 0);

        } else if (preference == mBatteryPercentagePref) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.BATTERY_PERCENTAGE,
                    mBatteryPercentagePref.isChecked() ? 1 : 0);
            return true;
        }
        return false;
    }

    private int readClockFontColor() {
        try {
            return Settings.System.getInt(getContentResolver(), Settings.System.CLOCK_COLOR);
        }
        catch (SettingNotFoundException e) {
            return -16777216;
        }
    }
    ColorPickerDialog.OnColorChangedListener mClockFontColorListener =
        new ColorPickerDialog.OnColorChangedListener() {
            public void colorChanged(int color) {
                Settings.System.putInt(getContentResolver(), Settings.System.CLOCK_COLOR, color);
            }
        };
    private int readBatteryFontColor() {
        try {
            return Settings.System.getInt(getContentResolver(), Settings.System.BATTERY_COLOR);
        }
        catch (SettingNotFoundException e) {
            return -1;
        }
    }
    ColorPickerDialog.OnColorChangedListener mBatteryFontColorListener =
        new ColorPickerDialog.OnColorChangedListener() {
            public void colorChanged(int color) {
                Settings.System.putInt(getContentResolver(), Settings.System.BATTERY_COLOR, color);
            }
        };
}
