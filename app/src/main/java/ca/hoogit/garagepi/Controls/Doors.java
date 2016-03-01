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
 * Created by jordon on 29/02/16.
 * Singleton to listen for {@link DoorControlService#startActionQuery(Context)} query broadcast
 */
public class Doors extends BroadcastReceiver {

    private static Doors mInstance;

    public static Doors getInstance() {
        return mInstance;
    }

    private ArrayList<Door> mDoors;

    private Doors(Context context) {
        this.mDoors = new ArrayList<>();
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(this, new IntentFilter(Consts.INTENT_MESSAGE_DOORS));
    }

    public static void create(Context context) {
        mInstance = new Doors(context);
    }

    public ArrayList<Door> doors() {
        return this.mDoors;
    }

    public void set(ArrayList<Door> doors) {
        this.mDoors = doors;
    }

    public void update(Door door) {
        int index = this.mDoors.indexOf(door);
        if (index != -1) {
            this.mDoors.set(index, door);
        }
    }

    public boolean hasDoors() {
        return !this.mDoors.isEmpty();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra(Consts.KEY_BROADCAST_ACTION);
        if (Consts.ACTION_DOORS_QUERY.equals(action)) {
            boolean wasSuccess = intent.getBooleanExtra(Consts.KEY_BROADCAST_SUCCESS, false);
            if (wasSuccess) {
                this.mDoors = intent.getParcelableArrayListExtra(Consts.KEY_DOORS);
            }
        }
    }
}
