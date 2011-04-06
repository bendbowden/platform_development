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

package com.android.spare_parts.activity;

import com.android.spare_parts.R;
import com.android.spare_parts.ColorPickerDialog;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.os.Bundle;
import android.util.Log;

public class GalaxySWidget extends PreferenceActivity {
    private static final String TAG = "SpareParts[GalaxySWidget]";

    private static final String WIDGET_DISPLAY = "galaxy_s_widget_display";
    private static final String WIDGET_INDICATOR_COLOR = "galaxy_s_widget_indicator_color";

    private CheckBoxPreference mWidgetDisplayPref;
    private Preference mWidgetIndicatorColorPref;

    private int readWidgetIndicatorColor() {
        try {
            return Settings.System.getInt(getContentResolver(), Settings.System.GALAXY_S_WIDGET_COLOR);
        }
        catch (SettingNotFoundException e) {
            return -16777216;
        }
    }
    ColorPickerDialog.OnColorChangedListener mWidgetIndicatorColorListener =
        new ColorPickerDialog.OnColorChangedListener() {
            public void colorChanged(int color) {
                Settings.System.putInt(getContentResolver(), Settings.System.GALAXY_S_WIDGET_COLOR, color);
            }
        };

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.galaxy_s_widget);

        final PreferenceScreen prefSet = getPreferenceScreen();

        mWidgetDisplayPref = (CheckBoxPreference)prefSet.findPreference(WIDGET_DISPLAY);
        mWidgetDisplayPref.setChecked(Settings.System.getInt(
                getContentResolver(),
                Settings.System.DISPLAY_GALAXY_S_WIDGET, 0) != 0);

        mWidgetIndicatorColorPref = prefSet.findPreference(WIDGET_INDICATOR_COLOR);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if(preference == mWidgetDisplayPref) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.DISPLAY_GALAXY_S_WIDGET,
                    mWidgetDisplayPref.isChecked() ? 1 : 0);
            return true;
        } else if(preference == mWidgetIndicatorColorPref) {
            ColorPickerDialog cp = new ColorPickerDialog(this,
            mWidgetIndicatorColorListener,
            readWidgetIndicatorColor());
            cp.show();
            return true;
        }

        return false;
    }
}
