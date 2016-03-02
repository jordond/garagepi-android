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


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import ca.hoogit.garagepi.R;

/**
 * TODO implement
 */
public class CameraFragment extends Fragment {

    private static final String TAG = CameraFragment.class.getSimpleName();

    @Bind(R.id.container) LinearLayout mContainer;
    @Bind(R.id.card_camera) CardView mCameraCard;
    @Bind(R.id.card_weather) CardView mWeatherCard;

    @Bind(R.id.camera_play_stop) ImageButton mPlayStopButton;
    @Bind(R.id.camera_refresh) ImageButton mRefreshButton;

    private CameraSocket mCameraSocket;

    public CameraFragment() {
    }

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        ButterKnife.bind(this, view);

        // Check if landscape and adjust views
        if (getResources().getBoolean(R.bool.is_landscape)) {
            mContainer.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams cameraParams = (LinearLayout.LayoutParams) mCameraCard.getLayoutParams();
            cameraParams.width = 0;
            cameraParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            cameraParams.setMargins(0, 0, 0, 0);
            cameraParams.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.card_container_margin));
            mCameraCard.requestLayout();

            ViewGroup.LayoutParams weatherParams = mWeatherCard.getLayoutParams();
            weatherParams.width = 0;
            weatherParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mWeatherCard.requestLayout();
        }

        mPlayStopButton.setOnClickListener(this::handlePlayStopButton);
        mRefreshButton.setOnClickListener(this::handleRefreshButton);

        mCameraSocket = new CameraSocket(getActivity());
        mCameraSocket.setOnFeed(base64Frame -> {
            Log.d(TAG, "onCreateView: Received frame");
            // TODO handle the frame data - convert to bitmap?
        });
        // TODO display state of camera to user
        mCameraSocket.setOnEvent(new CameraEvents.IEvents() {
            @Override
            public void onInitialFrame(String base64Frame) {
                Log.d(TAG, "onInitialFrame: Gotcha");
            }

            @Override
            public void onMotionCaptureLoading() {
                Log.d(TAG, "onMotionCaptureLoading: Loading and stuff");
            }
        });
        mCameraSocket.setOnError(message -> {
            // TODO change the image to reflect error'd state
            Log.d(TAG, "onCameraError: Got error");
        });

        mCameraSocket.activate();

        return view;
    }

    private void handlePlayStopButton(View view) {
        // TODO implement
    }

    private void handleRefreshButton(View view) {
        // TODO implement
    }

    @Override
    public void onResume() {
        super.onResume();
        mCameraSocket.on();
    }

    @Override
    public void onPause() {
        super.onPause();
        mCameraSocket.off();
    }
}
