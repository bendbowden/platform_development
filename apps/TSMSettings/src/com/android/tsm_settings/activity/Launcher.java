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

import android.content.Intent.ShortcutIconResource;
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
import java.util.ArrayList;
import com.android.tsm_settings.R;

public class Launcher extends PreferenceActivity 
        implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "TSMSettings[Launcher]";

    private static final String LAUNCHER_ORIENTATION_PREF = "launcher_orientation";

    private static final String WINDOW_ANIMATIONS_PREF = "window_animations";
    private static final String TRANSITION_ANIMATIONS_PREF = "transition_animations";
    private static final String FANCY_IME_ANIMATIONS_PREF = "fancy_ime_animations";

    private CheckBoxPreference mLauncherOrientationPref;

    private ListPreference mWindowAnimationsPref;
    private ListPreference mTransitionAnimationsPref;
    private CheckBoxPreference mFancyImeAnimationsPref;
    private CheckBoxPreference mCompatibilityMode;

    private IWindowManager mWindowManager;
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.launcher);

        PreferenceScreen prefSet = getPreferenceScreen();

        mLauncherOrientationPref = (CheckBoxPreference) prefSet.findPreference(LAUNCHER_ORIENTATION_PREF);
        mLauncherOrientationPref.setChecked(Settings.System.getInt(
                getContentResolver(),
                Settings.System.LAUNCHER_ORIENTATION, 1) != 0);

        mWindowAnimationsPref = (ListPreference) prefSet.findPreference(WINDOW_ANIMATIONS_PREF);
        mWindowAnimationsPref.setOnPreferenceChangeListener(this);
        mTransitionAnimationsPref = (ListPreference) prefSet.findPreference(TRANSITION_ANIMATIONS_PREF);
        mTransitionAnimationsPref.setOnPreferenceChangeListener(this);

        mFancyImeAnimationsPref = (CheckBoxPreference) prefSet.findPreference(FANCY_IME_ANIMATIONS_PREF);
        mFancyImeAnimationsPref.setChecked(Settings.System.getInt(
                getContentResolver(),
                Settings.System.FANCY_IME_ANIMATIONS, 1) != 0);

        mWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));

    }
    
    public boolean onPreferenceChange(Preference preference, Object newValue) {
	if (preference == mWindowAnimationsPref) {
            writeAnimationPreference(0, newValue);
        } else if (preference == mTransitionAnimationsPref) {
            writeAnimationPreference(1, newValue);
        }
        return true;
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

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
    boolean value;
        if (preference == mLauncherOrientationPref) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LAUNCHER_ORIENTATION,
                    mLauncherOrientationPref.isChecked() ? 1 : 0);
        } else if (preference == mFancyImeAnimationsPref) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.FANCY_IME_ANIMATIONS,
                    mFancyImeAnimationsPref.isChecked() ? 1 : 0);
            return true;
        }
        return false;
    }
}
