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

    /**
     * Create the Manager object and set the Broadcast receiver's listener to this object
     * @param context Calling activity
     */
    public AuthManager(Context context) {
        this.mContext = context;
        this.mDialog = new MaterialDialog.Builder(context).build();
        this.mReceiver = new AuthReceiver(context, this);
    }

    /**
     * In addition to this class listening for IAuthEvents, allow an additional listener to be
     * attached.
     * @param context Calling activity
     * @param listener Additional IAuthEvent listener
     */
    public AuthManager(Context context, IAuthEvent listener) {
        this.mContext = context;
        this.mAuthEventListener = listener;
        this.mDialog = new MaterialDialog.Builder(context).build();
        this.mReceiver = new AuthReceiver(context, this);
    }

    /**
     * Show snackbar notifications upon errors, or IAuthEvents
     * @param view View to attach snackbar too
     */
    public void enableNotifications(View view) {
        this.mView = view;
    }

    /**
     * Accessor method to add an additional IAuthEvent listener
     * @param listener Additional IAuthEvent listener
     */
    public void onAuthEvent(IAuthEvent listener) {
        this.mAuthEventListener = listener;
    }

    /**
     * Set a listener for when the receiver sends any message
     * @param listener BaseReceiver listener
     */
    public void onMessage(IBaseReceiver listener) {
        this.mReceiver.setOnMessage(listener);
    }

    /**
     * Register the AuthReceiver with the local broadcast instance
     */
    public void register() {
        this.mReceiver.register();
    }

    /**
     * Unregister the AuthReceiver with the local broadcast instance
     */
    public void stop() {
        this.mReceiver.unRegister();
    }

    /**
     * Silently authenticate the User with the server, and only if an authentication is needed
     * i.e. No progress dialogs, and if it's been too soon since the last auth
     * @return False if no authentication is needed
     */
    public boolean authenticate() {
        return authenticate(false, false);
    }

    /**
     * Force an authentication regardless of when the last auth was, as well as showing a progress
     * dialog.
     * @return False if no authentication is needed
     */
    public boolean forceAuthWithDialog() {
        return authenticate(true, true);
    }

    /**
     * Call the authentication service to attempt to authenticate the user with the server
     * @param showDialog Whether or not to show a progress dialog
     * @param force Ignore the debounce check for authenticating
     * @return False if no authentication is needed
     */
    public boolean authenticate(boolean showDialog, boolean force) {
        mDialog.dismiss();
        if (force || UserManager.shouldAuthenticate()) {
            if (showDialog) {
                mDialog = Helpers.buildProgressDialog(mContext);
                mDialog.show();
            }
            AuthService.startLogin(mContext);
            return true;
        }
        return false;
    }

    /**
     * Call the logout method of the authentication service
     */
    public void logout() {
        mDialog.dismiss();
        mDialog = buildLogoutDialog(mContext);
        mDialog.show();
    }

    /**
     * IAuthEvents
     */

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

    /**
     * Helper method to determine whether or not to display a Snackbar notification
     * @param message Desired output message
     * @return Whether or not a notification was displayed
     */
    private boolean showNotification(String message) {
        if (mView != null) {
            Snackbar.make(mView, message, Snackbar.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

    /**
     * Build a dialog informing the user of the desire to logout
     * @param context Calling activity
     * @return Built MaterialDialog
     */
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
