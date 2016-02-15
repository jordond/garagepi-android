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
import android.util.Log;

import ca.hoogit.garagepi.R;
import ca.hoogit.garagepi.Utils.Consts;
import ca.hoogit.garagepi.Utils.SharedPrefs;

/**
 * Created by jordon on 15/02/16.
 * Manager for handling the user object
 */
public class UserManager {

    private static final String TAG = UserManager.class.getSimpleName();
    private static UserManager sInstance;

    public static UserManager getInstance() {
        return sInstance;
    }

    private Context mContext;
    private User mUser;

    private UserManager(Context context) {
        this.mContext = context;
    }

    public static void init(Context context) {
        sInstance = new UserManager(context);
    }

    public User get() {
        SharedPrefs prefs = SharedPrefs.getInstance();
        User user = new User();
        Log.d(TAG, "get: Retrieving user from shared preferences");
        user.setEmail(prefs.getString(mContext.getString(R.string.pref_key_account_email), ""));
        user.setPassword(prefs.getString(mContext.getString(R.string.pref_key_account_password), ""));
        user.setToken(prefs.getString(mContext.getString(R.string.pref_key_account_token), "None"));
        user.setLastUpdated(prefs.getLong(Consts.SharedPrefs.KEY_USER_LAST_UPDATED, 0));
        mUser = user;
        return user;
    }

    public void save() {
        SharedPrefs prefs = SharedPrefs.getInstance();
        prefs.putString(mContext.getString(R.string.pref_key_account_email), mUser.getEmail());
        prefs.putString(mContext.getString(R.string.pref_key_account_password), mUser.getPassword());
        prefs.putString(mContext.getString(R.string.pref_key_account_token), mUser.getToken());
        prefs.putLong(Consts.SharedPrefs.KEY_USER_LAST_UPDATED, mUser.getLastUpdated());
        Log.d(TAG, "save: User has been saved to shared preferences");
    }
}
