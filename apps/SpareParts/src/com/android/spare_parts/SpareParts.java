/* //device/apps/Settings/src/com/android/settings/Keyguard.java
**
** Copyright 2006, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License"); 
** you may not use this file except in compliance with the License. 
** You may obtain a copy of the License at 
**
**     http://www.apache.org/licenses/LICENSE-2.0 
**
** Unless required by applicable law or agreed to in writing, software 
** distributed under the License is distributed on an "AS IS" BASIS, 
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
** See the License for the specific language governing permissions and 
** limitations under the License.
*/

package com.android.spare_parts;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.IWindowManager;

import java.util.List;

public class SpareParts extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "SpareParts";

    private static final String BATTERY_HISTORY_PREF = "battery_history_settings";
    private static final String BATTERY_INFORMATION_PREF = "battery_information_settings";
    private static final String USAGE_STATISTICS_PREF = "usage_statistics_settings";
    
    //extra
    private static final String MENU_UNLOCK_SCREEN_PREF = "menu_unlock_screen";
    private static final String LAUNCHER_ORIENTATION_PREF = "launcher_orientation";
    private static final String USE_ROTARY_LOCKSCREEN_PREF = "use_rotary_lockscreen";
    private static final String DISPLAY_CLOCK_PREF = "display_clock";
    private static final String CLOCK_COLOR_PREF = "clock_color";
    private static final String BATTERY_PERCENTAGE_PREF = "battery_percentage";
    private static final String BATTERY_COLOR_PREF = "battery_color";
    //end extra

    private static final String WINDOW_ANIMATIONS_PREF = "window_animations";
    private static final String TRANSITION_ANIMATIONS_PREF = "transition_animations";
    private static final String FANCY_IME_ANIMATIONS_PREF = "fancy_ime_animations";
    private static final String KEY_COMPATIBILITY_MODE = "compatibility_mode";

    //extra
    private CheckBoxPreference mMenuUnlockScreenPref;
    private CheckBoxPreference mLauncherOrientationPref;
    private CheckBoxPreference mUseRotaryLockscreenPref;
    private CheckBoxPreference mDisplayClockPref;
    private Preference mClockColorPref;
    private CheckBoxPreference mBatteryPercentagePref;
    private Preference mBatteryColorPref;
    //end extra

    private ListPreference mWindowAnimationsPref;
    private ListPreference mTransitionAnimationsPref;
    private CheckBoxPreference mFancyImeAnimationsPref;
    private CheckBoxPreference mCompatibilityMode;

    private IWindowManager mWindowManager;

    public static boolean updatePreferenceToSpecificActivityOrRemove(Context context,
            PreferenceGroup parentPreferenceGroup, String preferenceKey, int flags) {
        
        Preference preference = parentPreferenceGroup.findPreference(preferenceKey);
        if (preference == null) {
            return false;
        }
        
        Intent intent = preference.getIntent();
        if (intent != null) {
            // Find the activity that is in the system image
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
            int listSize = list.size();
            for (int i = 0; i < listSize; i++) {
                ResolveInfo resolveInfo = list.get(i);
                if ((resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)
                        != 0) {
                    
                    // Replace the intent with this specific activity
                    preference.setIntent(new Intent().setClassName(
                            resolveInfo.activityInfo.packageName,
                            resolveInfo.activityInfo.name));
                    
                    return true;
                }
            }
        }

        // Did not find a matching activity, so remove the preference
        parentPreferenceGroup.removePreference(preference);
        
        return true;
    }
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.spare_parts);

        PreferenceScreen prefSet = getPreferenceScreen();
        
        //extra
        mMenuUnlockScreenPref = (CheckBoxPreference) prefSet.findPreference(MENU_UNLOCK_SCREEN_PREF);
        mLauncherOrientationPref = (CheckBoxPreference) prefSet.findPreference(LAUNCHER_ORIENTATION_PREF);
        mUseRotaryLockscreenPref = (CheckBoxPreference) prefSet.findPreference(USE_ROTARY_LOCKSCREEN_PREF);
        mDisplayClockPref = (CheckBoxPreference) prefSet.findPreference(DISPLAY_CLOCK_PREF);
        mClockColorPref = prefSet.findPreference(CLOCK_COLOR_PREF);
        mBatteryPercentagePref = (CheckBoxPreference) prefSet.findPreference(BATTERY_PERCENTAGE_PREF);
        mBatteryColorPref = prefSet.findPreference(BATTERY_COLOR_PREF);
        //end extra

        mWindowAnimationsPref = (ListPreference) prefSet.findPreference(WINDOW_ANIMATIONS_PREF);
        mWindowAnimationsPref.setOnPreferenceChangeListener(this);
        mTransitionAnimationsPref = (ListPreference) prefSet.findPreference(TRANSITION_ANIMATIONS_PREF);
        mTransitionAnimationsPref.setOnPreferenceChangeListener(this);
        mFancyImeAnimationsPref = (CheckBoxPreference) prefSet.findPreference(FANCY_IME_ANIMATIONS_PREF);
        mCompatibilityMode = (CheckBoxPreference) findPreference(KEY_COMPATIBILITY_MODE);
        mCompatibilityMode.setPersistent(false);
        mCompatibilityMode.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.COMPATIBILITY_MODE, 1) != 0);

        mWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        
        final PreferenceGroup parentPreference = getPreferenceScreen();
        updatePreferenceToSpecificActivityOrRemove(this, parentPreference,
                BATTERY_HISTORY_PREF, 0);
        updatePreferenceToSpecificActivityOrRemove(this, parentPreference,
                BATTERY_INFORMATION_PREF, 0);
        updatePreferenceToSpecificActivityOrRemove(this, parentPreference,
                USAGE_STATISTICS_PREF, 0);
        
        parentPreference.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    private void updateToggles() {
        //extra
        mMenuUnlockScreenPref.setChecked(Settings.System.getInt(
                getContentResolver(), 
                Settings.System.MENU_UNLOCK_SCREEN, 0) != 0);
        mLauncherOrientationPref.setChecked(Settings.System.getInt(
                getContentResolver(), 
                Settings.System.LAUNCHER_ORIENTATION, 0) != 0);
        mUseRotaryLockscreenPref.setChecked(Settings.System.getInt(
                getContentResolver(),
                Settings.System.USE_ROTARY_LOCKSCREEN, 0) != 0);
        mDisplayClockPref.setChecked(Settings.System.getInt(
                getContentResolver(), 
                Settings.System.DISPLAY_CLOCK, 0) != 0);
        mBatteryPercentagePref.setChecked(Settings.System.getInt(
                getContentResolver(), 
                Settings.System.BATTERY_PERCENTAGE, 0) != 0);
        //end extra
        mFancyImeAnimationsPref.setChecked(Settings.System.getInt(
                getContentResolver(), 
                Settings.System.FANCY_IME_ANIMATIONS, 0) != 0);
    }
    
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mWindowAnimationsPref) {
            writeAnimationPreference(0, objValue);
        } else if (preference == mTransitionAnimationsPref) {
            writeAnimationPreference(1, objValue);
        }
        // always let the preference setting proceed.
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mCompatibilityMode) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.COMPATIBILITY_MODE,
                    mCompatibilityMode.isChecked() ? 1 : 0);
            return true;
        }
        //extra
        else if (preference == mClockColorPref) {
            ColorPickerDialog cp = new ColorPickerDialog(this,
            mClockFontColorListener,
            readClockFontColor());
            cp.show();            
        }
        else if (preference == mBatteryColorPref) {
            ColorPickerDialog cp = new ColorPickerDialog(this,
            mBatteryFontColorListener,
            readBatteryFontColor());
            cp.show();            
        }
        //end extra
        return false;
    }

    public void writeAnimationPreference(int which, Object objValue) {
        try {
            float val = Float.parseFloat(objValue.toString());
            mWindowManager.setAnimationScale(which, val);
        } catch (NumberFormatException e) {
        } catch (RemoteException e) {
        }
    }

    int floatToIndex(float val, int resid) {
        String[] indices = getResources().getStringArray(resid);
        float lastVal = Float.parseFloat(indices[0]);
        for (int i=1; i<indices.length; i++) {
            float thisVal = Float.parseFloat(indices[i]);
            if (val < (lastVal + (thisVal-lastVal)*.5f)) {
                return i-1;
            }
            lastVal = thisVal;
        }
        return indices.length-1;
    }
    
    public void readAnimationPreference(int which, ListPreference pref) {
        try {
            float scale = mWindowManager.getAnimationScale(which);
            pref.setValueIndex(floatToIndex(scale,
                    R.array.entryvalues_animations));
        } catch (RemoteException e) {
        }
    }

    //extra
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
    //end extra

    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        //extra
        if (MENU_UNLOCK_SCREEN_PREF.equals(key)) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.MENU_UNLOCK_SCREEN,
                    mMenuUnlockScreenPref.isChecked() ? 1 : 0);
        } else if (LAUNCHER_ORIENTATION_PREF.equals(key)) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LAUNCHER_ORIENTATION,
                    mLauncherOrientationPref.isChecked() ? 1 : 0);
        } else if (USE_ROTARY_LOCKSCREEN_PREF.equals(key)) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.USE_ROTARY_LOCKSCREEN,
                    mUseRotaryLockscreenPref.isChecked() ? 1 : 0);
        } else if (DISPLAY_CLOCK_PREF.equals(key)) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.DISPLAY_CLOCK,
                    mDisplayClockPref.isChecked() ? 1 : 0);
        } else if (BATTERY_PERCENTAGE_PREF.equals(key)) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.BATTERY_PERCENTAGE,
                    mBatteryPercentagePref.isChecked() ? 1 : 0);
        //end extra
        } else if (FANCY_IME_ANIMATIONS_PREF.equals(key)) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.FANCY_IME_ANIMATIONS,
                    mFancyImeAnimationsPref.isChecked() ? 1 : 0);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        readAnimationPreference(0, mWindowAnimationsPref);
        readAnimationPreference(1, mTransitionAnimationsPref);
        updateToggles();
    }
}
