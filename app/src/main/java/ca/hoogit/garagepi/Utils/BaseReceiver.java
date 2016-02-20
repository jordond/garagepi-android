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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by jordon on 18/02/16.
 * Base class for custom receivers
 */
public abstract class BaseReceiver extends BroadcastReceiver {

    private static final String TAG = BaseReceiver.class.getSimpleName();
    private Context mContext;
    private IBaseReceiver mListener;

    public BaseReceiver(Context context) {
        this.mContext = context;
    }

    public Context getContext() {
        return this.mContext;
    }

    public void setOnMessage(IBaseReceiver listener) {
        this.mListener = listener;
    }

    public void register() {
        String filter = getFilterName();
        if (filter != null && !filter.isEmpty()) {
            LocalBroadcastManager
                    .getInstance(mContext)
                    .registerReceiver(this, new IntentFilter(filter));
        } else {
            Log.e(TAG, "register: No filter string was provided");
        }
    }

    public abstract String getFilterName();

    public void unRegister() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
    }

    public abstract void messageReceived(String action, boolean status, String message);

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra(Consts.KEY_BROADCAST_ACTION);
        boolean wasSuccess = intent.getBooleanExtra(Consts.KEY_BROADCAST_SUCCESS, false);
        String message = intent.getStringExtra(Consts.KEY_BROADCAST_MESSAGE);
        Log.d(TAG, "onReceive: Message received: Action:" + action + " Success: "
                + wasSuccess + " Message: " + message);
        if (mListener != null) {
            mListener.onMessage(action, wasSuccess, message);
        }
        messageReceived(action, wasSuccess, message);
    }
}
