/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Jordon de Hoog
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ca.hoogit.garagepi.Settings;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;

import ca.hoogit.garagepi.BuildConfig;
import ca.hoogit.garagepi.R;

/**
 * Created by jordon on 12/02/16.
 */
public class SettingsFragment extends PreferenceFragment {

    private IBindPreference mListener;

    public SettingsFragment() {
    }

    public interface IBindPreference {
        void onBind(Preference preference);
    }

    public void setBindListener(IBindPreference listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (mListener != null) {
            mListener.onBind(findPreference(getString(R.string.pref_key_server_address)));
            mListener.onBind(findPreference(getString(R.string.pref_key_server_port)));
            mListener.onBind(findPreference(getString(R.string.pref_key_account_email)));
            mListener.onBind(findPreference(getString(R.string.pref_key_account_password)));
        }

        // Update the current token
        Preference token = findPreference(getString(R.string.pref_key_account_token));
        String updated = token.getSummary() + " " + getString(R.string.never_updated);
        String currentToken = "token: 213j12h3khk312h3kj"; // TODO Replace with user auth logic
        token.setSummary(updated + "\n" + currentToken);

        // Update the current version
        String currentVersion = "Name: " + BuildConfig.VERSION_NAME + "\nHash: " + BuildConfig.GitHash;
        String branch = sharedPref
                .getBoolean(getString(R.string.pref_key_updates_unstable), false)
                ? "develop" : "master";
        Preference version = findPreference(getString(R.string.pref_key_updates_version));
        version.setSummary("Branch: " + branch + "\n" + currentVersion);

        // Update the last checked
        String lastChecked = getString(R.string.never_updated); // TODO Replace with update logic
        Preference check = findPreference(getString(R.string.pref_key_updates_check));
        check.setSummary(check.getSummary() + " " + lastChecked);

        // Handle the authenticate now setting // TODO implement logic
        Preference auth = findPreference(getString(R.string.pref_key_account_authenticate));
        auth.setOnPreferenceClickListener(preference -> {
            Snackbar
                    .make(getView(), "Authenticate test", Snackbar.LENGTH_LONG)
                    .show();
            return true;
        });
    }
}
