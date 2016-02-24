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

import java.util.ArrayList;

import ca.hoogit.garagepi.Utils.Consts;

/**
 * Created by jordon on 23/02/16.
 *
 */
public class DoorManager extends BroadcastReceiver {

    private static final String TAG = DoorManager.class.getSimpleName();

    private Context mContext;
    private IQuery mListener;

    private ArrayList<Door> mDoors;

    public DoorManager(Context context) {
        this.mContext = context;
        this.mDoors = new ArrayList<>();
    }

    public DoorManager(Context context, IQuery listener) {
        this.mContext = context;
        this.mListener = listener;
        this.mDoors = new ArrayList<>();
    }

    public void register() {
        LocalBroadcastManager.getInstance(mContext).registerReceiver(this,
                new IntentFilter(Consts.INTENT_MESSAGE_DOORS));
    }

    public void stop() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
    }

    public void setListener(IQuery listener) {
        this.mListener = listener;
    }

    public void query() {
        DoorControlService.startActionQuery(mContext);
    }

    public void toggle(String id) {
        DoorControlService.startActionToggle(mContext, id);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mListener != null) {
            String action = intent.getStringExtra(Consts.KEY_BROADCAST_ACTION);
            if (Consts.ERROR.equals(action)) {
                mListener.onQuery(false, new ArrayList<>());
            } else if (Consts.ACTION_DOORS_QUERY.equals(action)) {
                ArrayList<Door> doors = intent.getParcelableArrayListExtra(Consts.KEY_DOORS);
                if (doors != null && !doors.isEmpty()){
                    mDoors = doors;
                    mListener.onQuery(true, doors);
                } else {
                    mListener.onQuery(false, new ArrayList<>());
                }
            }
        }
    }

    public interface IQuery {
        void onQuery(boolean wasSuccess, ArrayList<Door> response);
    }
}
