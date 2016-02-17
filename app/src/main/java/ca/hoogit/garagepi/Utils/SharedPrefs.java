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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import ca.hoogit.garagepi.R;

/**
 * Created by jordon on 12/02/16.
 * Shared Preferences manager
 */
public class SharedPrefs {

    private static SharedPrefs mInstance;

    private Context mContext;

    private SharedPreferences sharedPreferences;

    private SharedPrefs(Context context) {
        mContext = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void create(Context context) {
        mInstance = new SharedPrefs(context);
    }

    public static SharedPrefs getInstance() {
        return mInstance;
    }

    public void setFirstRun(boolean firstRun) {
        sharedPreferences.edit().putBoolean(Consts.SharedPrefs.KEY_FIRST_RUN, firstRun).apply();
    }

    public boolean isFirstRun() {
        return sharedPreferences.getBoolean(Consts.SharedPrefs.KEY_FIRST_RUN, true);
    }

    public String getAddress() {
        String addressKey = mContext.getString(R.string.pref_key_server_address);
        return sharedPreferences.getString(addressKey, mContext.getString(R.string.server_address));
    }

    public String getToken() {
        return sharedPreferences.getString(mContext.getString(R.string.pref_key_account_token), "");
    }

    public String getBranch() {
        boolean isDev = sharedPreferences.getBoolean(mContext.getString(R.string.pref_key_updates_unstable), false);
        return isDev ? "develop" : "master";
    }

    /**
     * Shared Preferences accessors
     */

    public SharedPreferences get() {
        return sharedPreferences;
    }

    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public void putString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    public long getLong(String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }

    public void putLong(String key, long value) {
        sharedPreferences.edit().putLong(key, value).apply();
    }

}
