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

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;

import com.afollestad.materialdialogs.MaterialDialog;

import ca.hoogit.garagepi.Auth.AuthManager;
import ca.hoogit.garagepi.Auth.User;
import ca.hoogit.garagepi.Auth.UserManager;
import ca.hoogit.garagepi.BuildConfig;
import ca.hoogit.garagepi.R;

/**
 * Created by jordon on 12/02/16.
 * Handle the updating of the settings views
 */
public class SettingsFragment extends PreferenceFragment {

    private IBindPreference mListener;

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

        // Bind the views to the onchange event in SettingsActivity
        if (mListener != null) {
            mListener.onBind(findPreference(getString(R.string.pref_key_server_address)));
            mListener.onBind(findPreference(getString(R.string.pref_key_account_email)));
            mListener.onBind(findPreference(getString(R.string.pref_key_account_password)));
        }

        updateViews();

        Preference auth = findPreference(getString(R.string.pref_key_account_authenticate));
        AuthManager manager = new AuthManager(getActivity());
        auth.setOnPreferenceClickListener(preference -> {
            MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                    .title(R.string.dialog_authenticate_title)
                    .content(R.string.dialog_wait)
                    .progress(true, 0).build();

            dialog.show();
            manager.login((wasSuccess, responseMessage) -> {
                if (wasSuccess) {
                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    mainHandler.post(this::updateViews);
                }
                dialog.dismiss();
                Snackbar.make(getView(), responseMessage, Snackbar.LENGTH_SHORT).show();
            });
            return true;
        });
    }

    private void updateViews() {
        User user = UserManager.getInstance().user();

        // Update the current token
        Preference tokenPref = findPreference(getString(R.string.pref_key_account_token));
        String tokenSummary = getString(R.string.pref_summary_account_token);
        String currentToken = user.getPrettyToken();
        String updated = tokenSummary + " " + user.getPrettyLastUpdated();
        tokenPref.setSummary(currentToken + "\n" + updated);

        // Update the current version
        Preference versionPref = findPreference(getString(R.string.pref_key_updates_version));
        String currentVersion = "Name: " + BuildConfig.VERSION_NAME;
        String hash = "Hash: " + BuildConfig.GitHash;
        String branch = "Branch: master"; // TODO replace with update logic
        versionPref.setSummary(TextUtils.join("\n", new String[]{currentVersion, hash, branch}));

        // Update the last checked
        Preference checkNowPref = findPreference(getString(R.string.pref_key_updates_check));
        String checkNowSummary = getString(R.string.pref_summary_updates_check);
        String lastChecked = "Never"; // TODO implement;
        checkNowPref.setSummary(checkNowSummary + " " + lastChecked);
    }
}
