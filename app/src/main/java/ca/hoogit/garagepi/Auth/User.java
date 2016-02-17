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
import android.text.format.DateUtils;
import android.util.Log;

import java.io.Serializable;

import ca.hoogit.garagepi.R;
import ca.hoogit.garagepi.Utils.Consts;
import ca.hoogit.garagepi.Utils.SharedPrefs;

/**
 * Created by jordon on 15/02/16.
 * Handle the saving and retrieving of the user to the database (sharedprefs)
 */
public class User implements Serializable {

    private static final String TAG = User.class.getSimpleName();

    private String email;
    private String password;
    private String token;
    private long lastUpdated;

    public User() {
        this.email = "";
        this.password = "";
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public void save() {
        UserManager.getInstance().save(this);
    }

    public boolean canAuthenticate() {
        return !this.email.isEmpty() && !this.password.isEmpty();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = "None".equals(token) ? "" : token;
        this.lastUpdated = System.currentTimeMillis();
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getPrettyToken() {
        if (this.token.isEmpty() || "None".equals(this.token)) {
            return "None";
        } else {
            String token = this.token;
            if (token.length() > Consts.TOKEN_DISPLAY_LENGTH) {
                token = token.substring(
                        token.length() - Consts.TOKEN_DISPLAY_LENGTH, token.length())
                        + "...";
            }
            return "Bearer " + token;
        }
    }

    public String getPrettyLastUpdated() {
        if (this.lastUpdated == 0) {
            return "Never";
        } else {
            return DateUtils
                    .getRelativeTimeSpanString(
                            this.lastUpdated,
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS)
                    .toString();
        }
    }
}
