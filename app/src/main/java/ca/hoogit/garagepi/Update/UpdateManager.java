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

package ca.hoogit.garagepi.Update;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import ca.hoogit.garagepi.R;
import ca.hoogit.garagepi.Utils.Helpers;

/**
 * Created by jordon on 22/02/16.
 * Handle all update related tasks
 * TODO Store list of hashes that have already been downloaded, to avoid re-downloading the same version
 */
public class UpdateManager implements IUpdateEvent {

    private static final String TAG = UpdateManager.class.getSimpleName();

    private Context mContext;
    private MaterialDialog mDialog;
    private UpdateReceiver mReceiver;
    private View mView;

    /**
     * Create the Manager object and set the Broadcast receiver's listener to this object
     * @param context Calling activity
     */
    public UpdateManager(Context context) {
        this.mContext = context;
        mReceiver = new UpdateReceiver(context, this);
    }

    /**
     * Create the manager and set the broadcast receivers listener to this object
     * @param context Calling activity
     * @param snackbarView View to attach SnackBar notifications to
     */
    public UpdateManager(Context context, View snackbarView) {
        this.mContext = context;
        this.mView = snackbarView;
        mReceiver = new UpdateReceiver(context, this);
    }

    /**
     * Register the UpdateReceiver with the local broadcast instance
     */
    public void register() {
        this.mReceiver.register();
    }

    /**
     * Unregister the UpdateReceiver with the local broadcast instance
     */
    public void stop() {
        this.mReceiver.unRegister();
    }

    /**
     * Check for an update only if a check hasn't happened within debounce limit
     */
    public void check() {
        check(false);
    }

    /**
     * Check for an update regardless of when the last check took place
     */
    public void forceCheck() {
        check(true);
    }

    /**
     * Call the update service and check for a new update
     * @param force Ignore the last checked limit
     */
    public void check(boolean force) {
        if (Version.shouldCheckForUpdate() || force) {
            this.mDialog = Helpers.buildProgressDialog(mContext);
            mDialog.show();
            UpdateService.startUpdateCheck(mContext);
        } else {
            Log.d(TAG, "check: Not checking for update");
        }
    }

    /**
     * Enable SnackBar notifications
     * @param view View to attach the SnackBar too
     */
    public void enableNotifications(View view) {
        this.mView = view;
    }

    /**
     * IUpdateEvents
     */

    @Override
    public void onUpdateResponse(boolean hasUpdate) {
        this.mDialog.dismiss();
        if (hasUpdate) {
            this.mDialog = buildUpdateAvailableDialog(mContext);
            this.mDialog.show();
        }
    }

    @Override
    public void onDownloadStarted() {
        this.mDialog = Helpers.buildProgressDialog(mContext);
        this.mDialog.show();
    }

    @Override
    public void onDownloadFinished(boolean wasSuccess, String message) {
        mDialog.dismiss();
    }

    @Override
    public void onError(String message) {
        mDialog.dismiss();
        if (mView != null) {
            Snackbar.make(mView, message, Snackbar.LENGTH_LONG).show();
        } else {
            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Build a dialog informing the user that an update is available
     * @param context Calling activity
     * @return Built MaterialDialog
     */
    public static MaterialDialog buildUpdateAvailableDialog(Context context) {
        return new MaterialDialog.Builder(context)
                .title(R.string.update_available_title)
                .content(R.string.update_available_content)
                .positiveText(R.string.update_available_positive)
                .neutralText(R.string.cancel)
                .onPositive((dialog, which) -> UpdateService.startDownload(context))
                .build();
    }
}
