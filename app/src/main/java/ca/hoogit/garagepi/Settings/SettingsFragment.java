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
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.design.widget.Snackbar;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;

import ca.hoogit.garagepi.Auth.AuthReceiver;
import ca.hoogit.garagepi.Auth.AuthService;
import ca.hoogit.garagepi.Auth.User;
import ca.hoogit.garagepi.Auth.UserManager;
import ca.hoogit.garagepi.R;
import ca.hoogit.garagepi.Update.IUpdateEvent;
import ca.hoogit.garagepi.Update.UpdateReceiver;
import ca.hoogit.garagepi.Update.UpdateService;
import ca.hoogit.garagepi.Update.Version;
import ca.hoogit.garagepi.Utils.Consts;
import ca.hoogit.garagepi.Utils.Helpers;
import ca.hoogit.garagepi.Utils.IBaseReceiver;

/**
 * Created by jordon on 12/02/16.
 * Handle the updating of the settings views
 */
public class SettingsFragment extends PreferenceFragment implements IBaseReceiver {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    private IBindPreference mListener;
    private MaterialDialog mDialog;

    public interface IBindPreference {
        void onBind(Preference preference);
    }

    public void setBindListener(IBindPreference listener) {
        mListener = listener;
    }

    private AuthReceiver mAuthReceiver;
    private UpdateReceiver mUpdateReceiver;

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
            mDialog = Helpers.buildProgressDialog(getActivity());
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

        Preference checkNowPref = findPreference(getString(R.string.pref_key_updates_check));
        checkNowPref.setOnPreferenceClickListener(preference -> {
            mDialog = Helpers.buildProgressDialog(getActivity());
            mDialog.show();
            UpdateService.startUpdateCheck(getActivity());
            return true;
        });

        mAuthReceiver = new AuthReceiver(getActivity());
        mAuthReceiver.setOnMessage(this);

        mUpdateReceiver = new UpdateReceiver(getActivity());
        mUpdateReceiver.setOnMessage(this);
        mUpdateReceiver.setListener(new IUpdateEvent() {
            @Override
            public void onUpdateResponse(boolean hasUpdate) {

            }

            @Override
            public void onDownloadStarted() {
                mDialog = Helpers.buildProgressDialog(getActivity());
                mDialog.show();
            }

            @Override
            public void onDownloadFinished(boolean wasSuccess, String message) {
                mDialog.dismiss();
            }
        });
    }

    @Override
    public void onMessage(String action, boolean status, String message) {
        if (action.equals(Consts.ACTION_UPDATE_CHECK)) {
            if (status) {
                Helpers.buildUpdateAvailableDialog(getActivity()).show();
            }
        } else {
            Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
        }
        updateViews();
        mDialog.dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAuthReceiver.register();
    }

    @Override
    public void onPause() {
        mAuthReceiver.unRegister();
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
        String lastChecked = Version.getPrettyLastChecked();
        checkNowPref.setSummary(checkNowSummary + " " + lastChecked);
    }
}
