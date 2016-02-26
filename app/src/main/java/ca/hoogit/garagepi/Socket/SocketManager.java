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

package ca.hoogit.garagepi.Socket;

import android.app.Activity;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import ca.hoogit.garagepi.Controls.Door;
import ca.hoogit.garagepi.R;
import ca.hoogit.garagepi.Utils.Consts;
import io.socket.emitter.Emitter;

/**
 * Created by jordon on 23/02/16.
 * Handle the events for the Socket Singleton
 */
public class SocketManager {

    private static final String TAG = SocketManager.class.getSimpleName();

    private Activity mActivity;
    private IConnectionEvent mListener;
    private IDoorEvent mDoorListener;

    public SocketManager(Activity activity) {
        this.mActivity = activity;
    }

    public SocketManager(Activity activity, IConnectionEvent listener) {
        this.mActivity = activity;
        this.mListener = listener;
    }

    public SocketManager(Activity activity, IDoorEvent listener) {
        this.mActivity = activity;
        this.mDoorListener = listener;
    }

    public SocketManager(Activity activity, IConnectionEvent connectionListener, IDoorEvent doorListener) {
        this.mActivity = activity;
        this.mListener = connectionListener;
        this.mDoorListener = doorListener;
    }

    public void onConnectionEvent(IConnectionEvent listener) {
        this.mListener = listener;
    }

    public void onDoorEvent(IDoorEvent listener) {
        this.mDoorListener = listener;
    }

    public void on() {
        io.socket.client.Socket socket = Socket.getInstance().socket();
        if (socket != null) {
            Log.d(TAG, "on: Registering all listeners");
            socket.on(io.socket.client.Socket.EVENT_CONNECT, onConnected);
            socket.on(io.socket.client.Socket.EVENT_CONNECT_ERROR, onConnectionError);
            socket.on(io.socket.client.Socket.EVENT_CONNECT_TIMEOUT, onConnectionError);
            socket.on(Consts.EVENT_DOOR_CHANGE, onDoorChange);
        }
    }

    public void off() {
        io.socket.client.Socket socket = Socket.getInstance().socket();
        if (socket != null) {
            socket.off(io.socket.client.Socket.EVENT_CONNECT, onConnected);
            socket.off(io.socket.client.Socket.EVENT_CONNECT_ERROR, onConnectionError);
            socket.off(io.socket.client.Socket.EVENT_CONNECT_TIMEOUT, onConnectionError);
            socket.off(Consts.EVENT_DOOR_CHANGE, onDoorChange);
            Log.d(TAG, "off: Unregistered all listeners");
        }
    }

    private Emitter.Listener onConnected = args -> mActivity.runOnUiThread(() -> {
        Log.d(TAG, "onConnected: Socket has successfully connected");
        if (mListener != null) {
            mListener.onConnected();
        }
    });

    private Emitter.Listener onConnectionError = args -> mActivity.runOnUiThread(() -> {
        Log.e(TAG, "onConnectionError: SocketIO Failed to connect");
        if (mListener != null) {
            mListener.onConnectionError(mActivity.getString(R.string.socket_connect_failed));
        }
    });

    private Emitter.Listener onDoorChange = args -> mActivity.runOnUiThread(() -> {
        Door door = new Gson().fromJson((JsonElement) args[0], Door.class);
        if (door != null) {
            Log.d(TAG, "onDoorChange: " + door.name + " was changed to " + door.input.value);
            if (mDoorListener != null) {
                mDoorListener.onStateChange(door);
            }
        } else {
            Log.e(TAG, "onDoorChange: Door object received was null");
        }
    });
}