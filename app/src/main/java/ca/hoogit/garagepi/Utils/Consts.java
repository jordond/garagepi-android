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

import android.content.IntentFilter;

/**
 * Created by jordon on 12/02/16.
 * Some global constants
 */
public class Consts {
    public static final String DOOR_ID_CAR = "car";
    public static final String DOOR_ID_VAN = "van";
    public static final String ACTION_DOORS_QUERY = "doors_query";
    public static final String ACTION_DOORS_TOGGLE = "doors_toggle";
    public static final String KEY_DOOR_ID = "doors_id";
    public static final String KEY_DOORS = "doors_array";
    public static final String INTENT_MESSAGE_DOORS = "doors_message";
    public static final String SOCKET_PATH = "/sync";
    public static final double PROPORTIONAL_HEIGHT_RATIO = 2.0;
    public static final String KEY_THEME_CHANGED = "theme_changed";

    public class SharedPrefs {
        public static final String KEY_FIRST_RUN = "first_run";
        public static final String KEY_USER_LAST_UPDATED = "user_last_updated";
        public static final String KEY_UPDATE_LAST_CHECK = "last_update_check";
    }

    public class Socket {
        public static final String DOOR_CHANGE = "gpio:save";
        public static final String CAMERA_INFO = "camera:info";
        public static final String CAMERA_START = "camera:start";
        public static final String CAMERA_STOP = "camera:stop";
        public static final String CAMERA_ERROR = "camera:error";
        public static final String CAMERA_FRAME_INITIAL = "camera:initial";
        public static final String CAMERA_LOADING = "camera:loading";
        public static final String CAMERA_FRAME = "camera:frame";
    }

    public static final String ACTION_AUTH_TOKEN_VALIDATE = "valid";
    public static final String ACTION_AUTH_TOKEN_REFRESH = "refresh";
    public static final String ACTION_AUTH_LOGOUT = "logout";
    public static final String ACTION_AUTH_LOGIN = "local";
    public static final String ACTION_UPDATE_CHECK = "update_check";
    public static final String ACTION_UPDATE_DOWNLOAD = "update_download";
    public static final String ACTION_UPDATE_DOWNLOAD_STARTED = "update_started";
    public static final String ACTION_UPDATE_DOWNLOAD_FINISHED = "update_finished";
    public static final String ACTION_UPDATE_DOWNLOAD_FAILED = "update_failed";
    public static final String ERROR = "error";
    public static final String INTENT_MESSAGE_AUTH = "auth_message";
    public static final String INTENT_MESSAGE_UPDATE = "update_message";
    public static final String KEY_BROADCAST_ACTION = "action";
    public static final String KEY_BROADCAST_SUCCESS = "success";
    public static final String KEY_BROADCAST_MESSAGE = "message";
    public static final String FIELD_LOGIN = "email";
    public static final String FIELD_PASSWORD = "password";
    public static final String MIME_APK = "application/vnd.android.package-archive";

    public static final long SECOND_IN_MILLIS = 1000;
    public static final long HOUR_IN_MILLIS = SECOND_IN_MILLIS * 3600;

    public static final long MINIMUM_AUTH_DEBOUNCE_MILLIS = HOUR_IN_MILLIS;
    public static final long MINIMUM_UPDATE_DEBOUNCE_MILLIS = HOUR_IN_MILLIS * 12;

    public static final long AUTO_AUTH_INITIAL_CHECK_INTERVAL = 0;
    public static final long AUTO_AUTH_CHECK_INTERVAL = HOUR_IN_MILLIS * 3;
    public static final long AUTO_UPDATE_INITIAL_CHECK_DELAY = SECOND_IN_MILLIS * 60;
    public static final long AUTO_UPDATE_CHECK_INTERVAL = MINIMUM_UPDATE_DEBOUNCE_MILLIS;

    public static final int RESULT_SETTINGS = 2232;
    public static final int TOKEN_DISPLAY_LENGTH = 30;
}
