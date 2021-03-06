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

package ca.hoogit.garagepi.Controls;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;

import ca.hoogit.garagepi.Utils.Consts;

/**
 * Created by jordon on 23/02/16.
 * Manger object for handling all door related tasks
 */
public class DoorManager extends BroadcastReceiver {

    private static final String TAG = DoorManager.class.getSimpleName();

    private Context mContext;
    private IOnQuery mQueryListener;
    private IOnToggle mToggleListener;
    private boolean mIsRegistered;

    public DoorManager(Context context) {
        this.mContext = context;
    }

    public DoorManager(Context context, IOnQuery listener) {
        this.mContext = context;
        this.mQueryListener = listener;
    }

    public DoorManager(Context context, IOnQuery onQuery, IOnToggle onToggle) {
        this.mContext = context;
        this.mQueryListener = onQuery;
        this.mToggleListener = onToggle;
    }

    public void register() {
        if (!mIsRegistered) {
            LocalBroadcastManager.getInstance(mContext).registerReceiver(this,
                    new IntentFilter(Consts.INTENT_MESSAGE_DOORS));
            mIsRegistered = true;
        }
    }

    public void stop() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
        mIsRegistered = false;
    }

    public void setOnQuery(IOnQuery listener) {
        this.mQueryListener = listener;
    }

    public void setOnToggle(IOnToggle listener) {
        this.mToggleListener = listener;
    }

    public void query() {
        if (!Doors.getInstance().hasDoors()) {
            DoorControlService.startActionQuery(mContext);
        }
    }

    public static void query(Context context) {
        DoorControlService.startActionQuery(context);
    }

    public void toggle(String id) {
        DoorControlService.startActionToggle(mContext, id);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra(Consts.KEY_BROADCAST_ACTION);
        Log.d(TAG, "onReceive: Message received - Action: " + action);
        if (Consts.ACTION_DOORS_QUERY.equals(action)) {
            if (mQueryListener != null) {
                boolean wasSuccess = intent.getBooleanExtra(Consts.KEY_BROADCAST_SUCCESS, false);
                mQueryListener.onQuery(wasSuccess);
            }
        } else if (Consts.ACTION_DOORS_TOGGLE.equals(action)) {
            if (mToggleListener != null) {
                String doorName = intent.getStringExtra(Consts.KEY_DOOR_ID);
                boolean wasSuccess = intent.getBooleanExtra(Consts.KEY_BROADCAST_SUCCESS, false);
                mToggleListener.onToggle(doorName, wasSuccess);
            }
        }
    }

    public interface IOnQuery {
        void onQuery(boolean wasSuccess);
    }

    public interface IOnToggle {
        void onToggle(String doorName, boolean wasToggled);
    }
}
