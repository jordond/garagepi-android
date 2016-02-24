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

import android.content.Context;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import ca.hoogit.garagepi.Auth.UserManager;
import ca.hoogit.garagepi.Utils.Helpers;
import ca.hoogit.garagepi.Utils.SharedPrefs;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by jordon on 23/02/16.
 * Manager for handling Socket object and connections
 */
public class SocketManager {

    private static final String TAG = SocketManager.class.getSimpleName();

    private static SocketManager mInstance = new SocketManager();

    public static SocketManager getInstance() {
        return mInstance;
    }

    private Socket mSocket;
    private String mSyncUrl;

    private SocketManager() {
        setSyncUrl();
    }

    public Socket get() {
        if (mSocket == null) {
            try {
                IO.Options opts = new IO.Options();
                opts.forceNew = true;
                opts.query = "token=" + UserManager.getInstance().user().getToken();
                mSocket = IO.socket(mSyncUrl, opts);
                Log.d(TAG, "get: Creating new socket object");
            } catch (URISyntaxException e) {
                Log.e(TAG, "connect: Error", e);
            }
        }
        return mSocket;
    }

    public void connect() {
        if (mSocket == null) {
            get();
        }
        mSocket.connect();
        Log.d(TAG, "connect: Attempting to connect to socket server");
    }

    public void disconnect() {
        if (mSocket != null) {
            mSocket.disconnect();
            Log.d(TAG, "disconnect: Disconnecting from socket server");
        }
    }

    public void setSyncUrl() {
        try {
            this.mSyncUrl = Helpers.getApiRoute("sync");
        } catch (MalformedURLException e) {
            Log.e(TAG, "setSyncUrl: URL Error", e);
        }
    }
}
