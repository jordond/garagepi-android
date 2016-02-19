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

/**
 * Created by jordon on 12/02/16.
 * Some global constants
 */
public class Consts {
    public class SharedPrefs {
        public static final String KEY_FIRST_RUN = "first_run";
        public static final String KEY_USER_LAST_UPDATED = "user_last_updated";
        public static final String KEY_UPDATE_LAST_CHECK = "last_update_check";
    }
    public static final String ACTION_AUTH_TOKEN_VALIDATE = "valid";
    public static final String ACTION_AUTH_TOKEN_REFRESH = "refresh";
    public static final String ACTION_AUTH_LOGOUT = "logout";
    public static final String ACTION_AUTH_LOGIN = "local";
    public static final String ACTION_UPDATE_CHECK = "update_check";
    public static final String ACTION_UPDATE_DOWNLOAD = "update_download";
    public static final String ERROR = "error";
    public static final String INTENT_MESSAGE_AUTH = "auth_message";
    public static final String INTENT_MESSAGE_UPDATE = "update_message";
    public static final String KEY_BROADCAST_ACTION = "action";
    public static final String KEY_BROADCAST_SUCCESS = "success";
    public static final String KEY_BROADCAST_MESSAGE = "message";
    public static final String FIELD_LOGIN = "email";
    public static final String FIELD_PASSWORD = "password";

    public static final long HOUR_IN_MILLIS = 3600000;
    public static final long MINIMUM_AUTH_DEBOUNCE_MILLIS = HOUR_IN_MILLIS;
    public static final long MINIMUM_UPDATE_DEBOUNCE_MILLIS = HOUR_IN_MILLIS * 12;

    public static final int RESULT_SETTINGS = 2232;
    public static final int TOKEN_DISPLAY_LENGTH = 30;
}
