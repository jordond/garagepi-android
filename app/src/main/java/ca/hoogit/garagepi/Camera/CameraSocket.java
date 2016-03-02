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

package ca.hoogit.garagepi.Camera;

import android.app.Activity;
import android.util.Log;

import com.google.gson.Gson;

import ca.hoogit.garagepi.Socket.MainSocket;
import ca.hoogit.garagepi.Utils.Consts;
import io.socket.client.Ack;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by jordon on 02/03/16.
 * Handler for all camera related socket events.
 * <p>
 * Such as:
 * Emit: "camera:info"      - Callback function
 * Emit: "camera:start"     - Starts camera feed, should receive ":frame" if feed is active
 * Emit: "camera:stop"      - Stops the camera feed
 * Listen: "camera:error"   - Received when there is an error with the camera
 * Listen: "camera:initial" - When camera cold starts, an initial frame will be sent
 * Listen: "camera:loading" - Sent when motion started
 * Listen: "camera:frame"   - Sent when motion has recorded a video frame
 */
public class CameraSocket {

    private static final String TAG = CameraSocket.class.getSimpleName();

    private Activity mActivity;
    private boolean mRegistered;

    private CameraEvents.IEvents mEventListener;
    private CameraEvents.IFeed mFeedListener;
    private CameraEvents.IError mErrorListener;

    public CameraSocket(Activity activity) {
        this.mActivity = activity;
    }

    public CameraSocket(CameraEvents.IFeed feed) {
        this.mFeedListener = feed;
    }

    public CameraSocket(CameraEvents.IEvents event, CameraEvents.IError error) {
        this.mEventListener = event;
        this.mErrorListener = error;
    }

    public CameraSocket(CameraEvents.IEvents event,
                        CameraEvents.IFeed feed,
                        CameraEvents.IError error) {
        this.mEventListener = event;
        this.mFeedListener = feed;
        this.mErrorListener = error;
    }

    public void setOnEvent(CameraEvents.IEvents listener) {
        this.mEventListener = listener;
    }

    public void setOnFeed(CameraEvents.IFeed listener) {
        this.mFeedListener = listener;
    }

    public void setOnError(CameraEvents.IError listener) {
        this.mErrorListener = listener;
    }

    public void on() {
        Socket socket = MainSocket.getInstance().socket();
        if (socket != null && !mRegistered) {
            Log.d(TAG, "on: Registering all listeners");
            socket.on(Consts.Socket.CAMERA_ERROR, onError);
            socket.on(Consts.Socket.CAMERA_FRAME_INITIAL, onInitialFrame);
            socket.on(Consts.Socket.CAMERA_LOADING, onMotionCaptureLoading);
            socket.on(Consts.Socket.CAMERA_FRAME, onFrame);
            mRegistered = true;
        }
    }

    public void off() {
        Socket socket = MainSocket.getInstance().socket();
        if (socket != null && !mRegistered) {
            socket.off(Consts.Socket.CAMERA_ERROR, onError);
            socket.off(Consts.Socket.CAMERA_FRAME_INITIAL, onInitialFrame);
            socket.off(Consts.Socket.CAMERA_LOADING, onMotionCaptureLoading);
            socket.off(Consts.Socket.CAMERA_FRAME, onFrame);
            Log.d(TAG, "on: Registering all listeners");
            mRegistered = false;
        }
    }

    public void activate() {
        // Get the camera info
        on();
        getInfo();
    }

    private void getInfo() {
        Socket socket = MainSocket.getInstance().socket();
        socket.emit("camera:info", "", (Ack) args -> {
            CameraResponse.Info response =
                    new Gson().fromJson(args[0].toString(), CameraResponse.Info.class);
            if (response != null) {
                Log.d(TAG, "onInfoCallback: " + response.toString());
                if (response.error != null) {
                    handleError(response.error.message);
                } else {
                    startFeed();
                }
            }
        });
    }

    private void startFeed() {
        Log.d(TAG, "startFeed: Starting camera feed");
        Socket socket = MainSocket.getInstance().socket();
        socket.emit(Consts.Socket.CAMERA_START);

    }

    private void handleError(String message) {
        Log.e(TAG, "handleError: " + message);
        if (mErrorListener != null) {
            mErrorListener.onCameraError(message);
        }
    }

    private void handleFrame(String base64Frame) {
        handleFrame(base64Frame, false);
    }

    private void handleFrame(String base64Frame, boolean isInitial) {
        if (isInitial) {
            if (mEventListener != null) {
                mEventListener.onInitialFrame(base64Frame);
            }
        } else {
            if (mFeedListener != null) {
                mFeedListener.onFrame(base64Frame);
            }
        }
    }

    /**
     * Socket IO - On listeners
     */

    private Emitter.Listener onError = args -> mActivity.runOnUiThread(() -> {
        Log.d(TAG, "onError: Received error message from server");
        CameraResponse.Error response =
                new Gson().fromJson(args[0].toString(), CameraResponse.Error.class);
        if (response != null) {
            handleError(response.message);
        }
    });

    private Emitter.Listener onInitialFrame = args -> mActivity.runOnUiThread(() -> {
        Log.d(TAG, "onInitialFrame: Received initial frame from server");
        String frame = (String) args[0];
        if (frame != null && !frame.isEmpty()) {
            handleFrame(frame, true);
        }
    });

    private Emitter.Listener onMotionCaptureLoading = args -> mActivity.runOnUiThread(() -> {
        Log.d(TAG, "onMotionCaptureLoading: Received loading message from server");
        if (mEventListener != null) {
            mEventListener.onMotionCaptureLoading();
        }
    });

    private Emitter.Listener onFrame = args -> mActivity.runOnUiThread(() -> {
        Log.v(TAG, "onFrame: Received frame from the server");
        String frame = (String) args[0];
        if (frame != null && !frame.isEmpty()) {
            handleFrame(frame);
        }
    });
}
