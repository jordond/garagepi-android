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

package ca.hoogit.garagepi.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.IntDef;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatDelegate;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.MalformedURLException;
import java.net.URL;

import ca.hoogit.garagepi.R;
import ca.hoogit.garagepi.Update.UpdateService;

/**
 * Created by jordon on 15/02/16.
 * Some helper functions
 */
public class Helpers {

    private static final String TAG = Helpers.class.getSimpleName();

    /**
     * Set the NightDay system mode.
     * Moved to helper as linter was throwing error by giving it an int
     *
     * @param mode Theme mode
     */
    public static void setDefaultNightMode(int mode) {
        String modeString = "";
        switch (mode) {
            case AppCompatDelegate.MODE_NIGHT_AUTO:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
                modeString = "Auto";
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                modeString = "Disabled";
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                modeString = "Enabled";
                break;
        }
        Log.i(TAG, "setDefaultNightMode: Night mode is being set to " + modeString);
    }

    /**
     * Create a relative from now time string
     *
     * @param timestamp Time since epoch
     * @return Formatted string ie. "2 Minutes from now"
     */
    public static String epochToFromNow(long timestamp) {
        return DateUtils
                .getRelativeTimeSpanString(
                        timestamp,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS)
                .toString();
    }

    /**
     * Capitalize  the first letter in string
     *
     * @param toCapitalize String to be capitalized
     * @return Capitalized string
     */
    public static String capitalize(String toCapitalize) {
        if (toCapitalize.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder(toCapitalize.toLowerCase());
        builder.setCharAt(0, Character.toUpperCase(builder.charAt(0)));
        return builder.toString();
    }

    /**
     * Check if internet access is available.
     *
     * @param context Application context
     * @return State of internet connection
     * @see <a href="http://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android">From StackOverflow</a>
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Parse the url path, and rebuild it with supplied paths
     *
     * @param oldUrl Original url
     * @param paths  Paths to add to the url
     * @return String Built url
     * @throws MalformedURLException
     */
    public static String urlBuilder(String oldUrl, String... paths) throws MalformedURLException {
        URL url = new URL(oldUrl);
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(url.getProtocol())
                .authority(url.getAuthority())
                .path(url.getPath());
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build().toString();
    }

    /**
     * Build an api route, based on the user entered server address.
     *
     * @param paths Paths to add to the server address
     * @return String Built url
     * @throws MalformedURLException
     */
    public static String getApiRoute(String... paths) throws MalformedURLException {
        return urlBuilder(SharedPrefs.getInstance().getAddress(), paths);
    }

    /**
     * Helper method to broadcast the outcome of the service
     *
     * @param action     Calling action
     * @param wasSuccess Whether or not action was successful
     * @param message    Outcome message
     */
    public static void broadcast(Context context, String filter, String action, boolean wasSuccess, String message) {
        Intent broadcast = new Intent(filter);
        broadcast.putExtra(Consts.KEY_BROADCAST_ACTION, action);
        broadcast.putExtra(Consts.KEY_BROADCAST_SUCCESS, wasSuccess);
        broadcast.putExtra(Consts.KEY_BROADCAST_MESSAGE, message);
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
        Log.d(TAG, "broadcast: Message: " + wasSuccess + " - " + message);
    }

    /**
     * Create a progress dialog
     *
     * @param context Reference to calling activity
     * @return MaterialDialog Built progress dialog
     */
    public static MaterialDialog buildProgressDialog(Context context) {
        return new MaterialDialog.Builder(context)
                .title(R.string.dialog_wait)
                .content(R.string.magic_is_happening)
                .cancelable(false)
                .progress(true, 0).build();
    }

    /**
     * Create an update available dialog
     *
     * @param context Application context
     * @return MaterialDialog Built update dialog
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
