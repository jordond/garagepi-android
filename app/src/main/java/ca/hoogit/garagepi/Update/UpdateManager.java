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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import ca.hoogit.garagepi.R;
import ca.hoogit.garagepi.Utils.Consts;
import ca.hoogit.garagepi.Utils.Helpers;

/**
 * Created by jordon on 22/02/16.
 * Handle all update related tasks
 */
public class UpdateManager implements IUpdateEvent {

    private static final String TAG = UpdateManager.class.getSimpleName();

    private Context mContext;
    private MaterialDialog mDialog;
    private UpdateReceiver mReceiver;
    private View mView;

    private ScheduledExecutorService mScheduler;
    private ScheduledFuture<?> mFuture;

    /**
     * Create the Manager object and set the Broadcast receiver's listener to this object
     *
     * @param context Calling activity
     */
    public UpdateManager(Context context) {
        this.mContext = context;
        mReceiver = new UpdateReceiver(context, this);
        mScheduler = Executors.newScheduledThreadPool(1);
    }

    /**
     * Create the manager and set the broadcast receivers listener to this object
     *
     * @param context      Calling activity
     * @param snackbarView View to attach SnackBar notifications to
     */
    public UpdateManager(Context context, View snackbarView) {
        this.mContext = context;
        this.mView = snackbarView;
        mReceiver = new UpdateReceiver(context, this);
        mScheduler = Executors.newScheduledThreadPool(1);
    }

    /**
     * Register the UpdateReceiver with the local broadcast instance
     */
    public void start() {
        this.mReceiver.register();
        this.mFuture = this.mScheduler.scheduleAtFixedRate((Runnable) this::checkWithoutDialog,
                Consts.MINIMUM_UPDATE_DEBOUNCE_MILLIS,
                Consts.MINIMUM_UPDATE_DEBOUNCE_MILLIS,
                TimeUnit.MILLISECONDS);
    }

    /**
     * Unregister the UpdateReceiver with the local broadcast instance
     */
    public void stop() {
        this.mReceiver.unRegister();
        if (this.mFuture != null) {
            this.mFuture.cancel(true);
        }
    }

    /**
     * Check for an update only if a check hasn't happened within debounce limit
     */
    public void check() {
        check(false, true);
    }

    /**
     * Check for an update without showing a progress dialog
     */
    public void checkWithoutDialog() {
        check(false, false);
    }

    /**
     * Check for an update regardless of when the last check took place
     */
    public void forceCheck() {
        check(true, true);
    }

    /**
     * Force check for update without displaying a progress dialog
     */
    public void forceCheckWithoutDialog() {
        check(true, false);
    }

    /**
     * Call the update service and check for a new update
     *
     * @param force      Ignore the last checked limit
     * @param showDialog Display the progress dialog
     */
    public void check(boolean force, boolean showDialog) {
        if (force || Version.shouldCheckForUpdate()) {
            this.mDialog = Helpers.buildProgressDialog(mContext);
            if (showDialog) {
                mDialog.show();
            }
            UpdateService.startUpdateCheck(mContext);
        } else {
            Log.d(TAG, "check: Not checking for update");
        }
    }

    /**
     * Enable SnackBar notifications
     *
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
     *
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
