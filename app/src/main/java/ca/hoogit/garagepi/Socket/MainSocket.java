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

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

import ca.hoogit.garagepi.Auth.UserManager;
import ca.hoogit.garagepi.Utils.Consts;
import ca.hoogit.garagepi.Utils.SharedPrefs;
import io.socket.client.IO;

/**
 * Created by jordon on 23/02/16.
 * Manager for handling Socket object and connections
 */
public class MainSocket {

    private static final String TAG = MainSocket.class.getSimpleName();

    private static MainSocket mInstance = new MainSocket();

    public static MainSocket getInstance() {
        return mInstance;
    }

    private io.socket.client.Socket mSocket;
    private String mSyncUrl = "";
    private String mSyncPath = "";

    private MainSocket() {  }

    public io.socket.client.Socket socket() {
        return mSocket == null ? newSocket() : mSocket;
    }

    public io.socket.client.Socket newSocket() {
        try {
            IO.Options opts = new IO.Options();
            opts.forceNew = true;
            opts.reconnectionAttempts = 3;
            opts.path = mSyncPath;
            opts.query = "token=" + UserManager.getInstance().user().getToken();
            mSocket = IO.socket(mSyncUrl, opts);
            Log.d(TAG, "get: Creating new socket URL: " + mSyncUrl + mSyncPath);
        } catch (Exception e) {
            Log.e(TAG, "connect: Error", e);
        }
        return mSocket;
    }

    public void setSyncUrl() {
        try {
            URL url = new URL(SharedPrefs.getInstance().getAddress());
            mSyncUrl = url.getProtocol() + "://" + url.getAuthority();
            mSyncPath = url.getPath() + Consts.SOCKET_PATH;
            Log.d(TAG, "setSyncUrl: url: " + mSyncUrl + mSyncPath);
        } catch (MalformedURLException e) {
            Log.e(TAG, "setSyncUrl: URL Error", e);
        }
    }
}
