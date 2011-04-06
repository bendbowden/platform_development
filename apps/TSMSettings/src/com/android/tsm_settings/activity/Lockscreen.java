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

public class Lockscreen extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "TSMParts[Lockscreen]";
    
    private static final String LOCKSCREEN_STYLE_PREF = "pref_lockscreen_style";

    private static final String LOCKSCREEN_CUSTOM_APP_TOGGLE = "pref_lockscreen_custom_app_toggle";

    private static final String LOCKSCREEN_CUSTOM_APP_ACTIVITY = "pref_lockscreen_custom_app_activity";

    private static final String LOCKSCREEN_ROTARY_UNLOCK_DOWN_TOGGLE = "pref_lockscreen_rotary_unlock_down_toggle";

    private static final String LOCKSCREEN_ROTARY_HIDE_ARROWS_TOGGLE = "pref_lockscreen_rotary_hide_arrows_toggle";

    private static final String MENU_UNLOCK_SCREEN_PREF = "menu_unlock_screen";

    private ListPreference mLockScreenStylePref;
    private CheckBoxPreference mCustomAppTogglePref;
    private CheckBoxPreference mRotaryUnlockDownToggle;
    private CheckBoxPreference mRotaryHideArrowsToggle;
    private CheckBoxPreference mMenuUnlockScreenPref;
    private Preference mCustomAppActivityPref;


    private IWindowManager mWindowManager;

    private int mKeyNumber = 1;

    private static final int REQUEST_PICK_SHORTCUT = 1;

    private static final int REQUEST_PICK_APPLICATION = 2;

    private static final int REQUEST_CREATE_SHORTCUT = 3;
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.lockscreen);

        PreferenceScreen prefSet = getPreferenceScreen();

        mLockScreenStylePref = (ListPreference) prefSet.findPreference(LOCKSCREEN_STYLE_PREF);
        int lockScreenStyle = Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_STYLE_PREF, 3);
        mLockScreenStylePref.setValue(String.valueOf(lockScreenStyle));
        mLockScreenStylePref.setOnPreferenceChangeListener(this);

        mRotaryUnlockDownToggle = (CheckBoxPreference) prefSet
                .findPreference(LOCKSCREEN_ROTARY_UNLOCK_DOWN_TOGGLE);
        mRotaryUnlockDownToggle.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_ROTARY_UNLOCK_DOWN, 0) == 1);

        mRotaryHideArrowsToggle = (CheckBoxPreference) prefSet
                .findPreference(LOCKSCREEN_ROTARY_HIDE_ARROWS_TOGGLE);
        mRotaryHideArrowsToggle.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_ROTARY_HIDE_ARROWS, 0) == 1);

        mCustomAppTogglePref = (CheckBoxPreference) prefSet
                .findPreference(LOCKSCREEN_CUSTOM_APP_TOGGLE);
        mCustomAppTogglePref.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_CUSTOM_APP_TOGGLE, 0) == 1);

       updateStylePrefs(lockScreenStyle);

        mCustomAppActivityPref = (Preference) prefSet
                .findPreference(LOCKSCREEN_CUSTOM_APP_ACTIVITY);

        mMenuUnlockScreenPref = (CheckBoxPreference) prefSet.findPreference(MENU_UNLOCK_SCREEN_PREF);

    }
    
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLockScreenStylePref) {
            int lockScreenStyle = Integer.valueOf((String) newValue);
            Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_STYLE_PREF,
                    lockScreenStyle);
            updateStylePrefs(lockScreenStyle);
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
boolean value;
        if (preference == mCustomAppTogglePref) {
            value = mCustomAppTogglePref.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_APP_TOGGLE, value ? 1 : 0);
            int lockscreenStyle = Settings.System.getInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_STYLE_PREF, 3);
            updateStylePrefs(lockscreenStyle);
            return true;
        } else if (preference == mRotaryUnlockDownToggle) {
            value = mRotaryUnlockDownToggle.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_ROTARY_UNLOCK_DOWN, value ? 1 : 0);
            return true;
        } else if (preference == mRotaryHideArrowsToggle) {
            value = mRotaryHideArrowsToggle.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_ROTARY_HIDE_ARROWS, value ? 1 : 0);
            return true;
        }
        else if (preference == mCustomAppActivityPref) {
            pickShortcut(4);
	}
        return false;
    }
    //custom app picker
    private void pickShortcut(int keyNumber) {
        mKeyNumber = keyNumber;
        Bundle bundle = new Bundle();
        ArrayList<String> shortcutNames = new ArrayList<String>();
        shortcutNames.add(getString(R.string.group_applications));
        bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);
        ArrayList<ShortcutIconResource> shortcutIcons = new ArrayList<ShortcutIconResource>();
        shortcutIcons.add(ShortcutIconResource
                .fromContext(this, R.drawable.ic_launcher_application));
        bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcons);
        Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
        pickIntent.putExtra(Intent.EXTRA_INTENT, new Intent(Intent.ACTION_CREATE_SHORTCUT));
        pickIntent.putExtra(Intent.EXTRA_TITLE, getText(R.string.select_custom_app_title));
        pickIntent.putExtras(bundle);
        startActivityForResult(pickIntent, REQUEST_PICK_SHORTCUT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICK_APPLICATION:
                    completeSetCustomApp(data);
                    break;
                case REQUEST_CREATE_SHORTCUT:
                    completeSetCustomShortcut(data);
                    break;
                case REQUEST_PICK_SHORTCUT:
                    processShortcut(data, REQUEST_PICK_APPLICATION, REQUEST_CREATE_SHORTCUT);
                    break;
            }
        }
    }

    void processShortcut(Intent intent, int requestCodeApplication, int requestCodeShortcut) {
        // Handle case where user selected "Applications"
        String applicationName = getResources().getString(R.string.group_applications);
        String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        if (applicationName != null && applicationName.equals(shortcutName)) {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
            pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
            startActivityForResult(pickIntent, requestCodeApplication);
        } else {
            startActivityForResult(intent, requestCodeShortcut);
        }
    }

    void completeSetCustomShortcut(Intent data) {
        Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        int keyNumber = mKeyNumber;
        if (keyNumber == 4) {
            if (Settings.System.putString(getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITY, intent.toUri(0))) {
                mCustomAppActivityPref.setSummary(intent.toUri(0));
            }
        }
    }

    void completeSetCustomApp(Intent data) {
        int keyNumber = mKeyNumber;
        if (keyNumber == 4) {
            if (Settings.System.putString(getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITY, data.toUri(0))) {
                mCustomAppActivityPref.setSummary(data.toUri(0));
            }
        }
    }
    //end custom app picker
    
    @Override
    public void onResume() {
        super.onResume();
        mCustomAppActivityPref.setSummary(Settings.System.getString(getContentResolver(),
                Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITY));
    }

    private void updateStylePrefs(int lockScreenStyle) {

        if (lockScreenStyle == 1 || lockScreenStyle == 4) {
            mRotaryHideArrowsToggle.setChecked(false);
            mRotaryHideArrowsToggle.setEnabled(false);
            mRotaryUnlockDownToggle.setChecked(false);
            mRotaryUnlockDownToggle.setEnabled(false);

        } else if (lockScreenStyle == 2 || lockScreenStyle == 3) {
            mRotaryHideArrowsToggle.setEnabled(true);
            if (mCustomAppTogglePref.isChecked() == true) {
                mRotaryUnlockDownToggle.setEnabled(true);
            } else {
                mRotaryUnlockDownToggle.setChecked(false);
                mRotaryUnlockDownToggle.setEnabled(false);
            }
        }

        if (lockScreenStyle == 4) {
            mCustomAppTogglePref.setChecked(false);
            mCustomAppTogglePref.setEnabled(false);
        } else {
            mCustomAppTogglePref.setEnabled(true);
        }
        boolean value = mRotaryUnlockDownToggle.isChecked();
        Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_ROTARY_UNLOCK_DOWN,
                value ? 1 : 0);
        value = mRotaryHideArrowsToggle.isChecked();
        Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_ROTARY_HIDE_ARROWS,
                value ? 1 : 0);
        value = mCustomAppTogglePref.isChecked();
        Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_CUSTOM_APP_TOGGLE,
                value ? 1 : 0);
    }
}
