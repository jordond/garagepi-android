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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;

import ca.hoogit.garagepi.Auth.AuthReceiver;
import ca.hoogit.garagepi.Auth.AuthService;
import ca.hoogit.garagepi.Auth.IAuthEvent;
import ca.hoogit.garagepi.Auth.User;
import ca.hoogit.garagepi.Auth.UserManager;
import ca.hoogit.garagepi.R;
import ca.hoogit.garagepi.Update.Version;
import ca.hoogit.garagepi.Utils.Consts;

/**
 * Created by jordon on 12/02/16.
 * Handle the updating of the settings views
 */
public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    private IBindPreference mListener;
    private MaterialDialog mDialog;

    public interface IBindPreference {
        void onBind(Preference preference);
    }

    public void setBindListener(IBindPreference listener) {
        mListener = listener;
    }

    private AuthReceiver mReceiver;

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

        // Handle the clicking of authenticate now field.
        Preference authPref = findPreference(getString(R.string.pref_key_account_authenticate));
        authPref.setOnPreferenceClickListener(preference -> {
            mDialog = new MaterialDialog.Builder(getActivity())
                    .title(R.string.dialog_authenticate_title)
                    .content(R.string.dialog_wait)
                    .progress(true, 0).build();

            mDialog.show();
            AuthService.startLogin(getActivity());
            return true;
        });

        // Handle the clicking of the logout field
        Preference logoutPref = findPreference(getString(R.string.pref_key_account_logout));
        logoutPref.setOnPreferenceClickListener(preference -> {
            mDialog = new MaterialDialog.Builder(getActivity())
                    .title(R.string.dialog_sure)
                    .content(R.string.dialog_logout_content)
                    .positiveText(R.string.dialog_okay)
                    .negativeText(R.string.dialog_cancel)
                    .onPositive(((dialog, which) -> AuthService.startLogout(getActivity())))
                    .build();
            mDialog.show();
            return true;
        });

        mReceiver = new AuthReceiver(getActivity(), new IAuthEvent() {
            @Override
            public void onEvent(String action, boolean wasSuccess, String message) {
                if (wasSuccess) {
                    updateViews();
                }
                mDialog.dismiss();
                Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onLogin(boolean wasSuccess, String message) {
            }

            @Override
            public void onLogout(boolean wasSuccess, String message) {
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mReceiver.register();
    }

    @Override
    public void onPause() {
        mReceiver.unRegister();
        super.onPause();
    }

    private void updateViews() {
        User user = UserManager.getInstance().user();

        Preference emailPref = findPreference(getString(R.string.pref_key_account_email));
        emailPref.setSummary(user.getEmail());

        // Mask the password
        Preference passwordPref = findPreference(getString(R.string.pref_key_account_password));
        EditText edit = ((EditTextPreference) passwordPref).getEditText();
        String masked = edit.getTransformationMethod().getTransformation(user.getPassword(), edit).toString();
        passwordPref.setSummary(masked);

        // Update the current token
        Preference tokenPref = findPreference(getString(R.string.pref_key_account_token));
        String tokenSummary = getString(R.string.pref_summary_account_token);
        String currentToken = user.getPrettyToken();
        String updated = tokenSummary + " " + user.getPrettyLastUpdated();
        tokenPref.setSummary(currentToken + "\n" + updated);

        // Update the current version
        Preference versionPref = findPreference(getString(R.string.pref_key_updates_version));
        versionPref.setSummary(Version.output());

        // Update the last checked
        Preference checkNowPref = findPreference(getString(R.string.pref_key_updates_check));
        String checkNowSummary = getString(R.string.pref_summary_updates_check);
        String lastChecked = "Never"; // TODO implement;
        checkNowPref.setSummary(checkNowSummary + " " + lastChecked);
    }
}
