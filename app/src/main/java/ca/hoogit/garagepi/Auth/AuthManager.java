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

package ca.hoogit.garagepi.Auth;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import ca.hoogit.garagepi.R;
import ca.hoogit.garagepi.Utils.Helpers;
import ca.hoogit.garagepi.Utils.IBaseReceiver;

/**
 * Created by jordon on 22/02/16.
 * Handle all authentication tasks
 */
public class AuthManager implements IAuthEvent {

    private static final String TAG = AuthManager.class.getSimpleName();

    private Context mContext;
    private MaterialDialog mDialog;
    private View mView;

    private AuthReceiver mReceiver;
    private IAuthEvent mAuthEventListener;

    public AuthManager(Context context) {
        this.mContext = context;
        this.mDialog = new MaterialDialog.Builder(context).build();
        this.mReceiver = new AuthReceiver(context, this);
    }

    public AuthManager(Context context, IAuthEvent listener) {
        this.mContext = context;
        this.mAuthEventListener = listener;
        this.mDialog = new MaterialDialog.Builder(context).build();
        this.mReceiver = new AuthReceiver(context, this);
    }

    public void enableNotifications(View view) {
        this.mView = view;
    }

    public void onAuthEvent(IAuthEvent listener) {
        this.mAuthEventListener = listener;
    }

    public void onMessage(IBaseReceiver listener) {
        this.mReceiver.setOnMessage(listener);
    }

    public void register() {
        this.mReceiver.register();
    }

    public void stop() {
        this.mReceiver.unRegister();
    }

    public void authenticate() {
        authenticate(false, false);
    }

    public void forceAuthWithDialog() {
        authenticate(true, true);
    }

    public void authenticate(boolean showDialog, boolean force) {
        mDialog.dismiss();
        if (showDialog) {
            mDialog = Helpers.buildProgressDialog(mContext);
            mDialog.show();
        }
        if (force || UserManager.shouldAuthenticate()) {
            AuthService.startLogin(mContext);
        }
    }

    public void logout() {
        mDialog.dismiss();
        mDialog = buildLogoutDialog(mContext);
        mDialog.show();
    }

    @Override
    public void onLogin(boolean wasSuccess, String message) {
        mDialog.dismiss();
        showNotification(message);
        if (mAuthEventListener != null) {
            mAuthEventListener.onLogin(wasSuccess, message);
        }
    }

    @Override
    public void onLogout(boolean wasSuccess, String message) {
        mDialog.dismiss();
        showNotification(message);
        if (mAuthEventListener != null) {
            mAuthEventListener.onLogout(wasSuccess, message);
        }
    }

    @Override
    public void onError(String message) {
        mDialog.dismiss();
        if (!showNotification(message)) {
            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
        }
        if (mAuthEventListener != null) {
            mAuthEventListener.onError(message);
        }
    }

    private boolean showNotification(String message) {
        if (mView != null) {
            Snackbar.make(mView, message, Snackbar.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

    public static MaterialDialog buildLogoutDialog(Context context) {
        return new MaterialDialog.Builder(context)
                .title(R.string.dialog_sure)
                .content(R.string.dialog_logout_content)
                .positiveText(R.string.dialog_okay)
                .negativeText(R.string.dialog_cancel)
                .onPositive(((dialog, which) -> AuthService.startLogout(context)))
                .build();
    }
}
