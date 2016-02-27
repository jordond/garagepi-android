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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.Bind;
import butterknife.ButterKnife;
import ca.hoogit.garagepi.R;
import ca.hoogit.garagepi.Socket.IDoorEvent;
import ca.hoogit.garagepi.Socket.SocketManager;
import ca.hoogit.garagepi.Utils.Consts;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DoorsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DoorsFragment extends Fragment implements DoorManager.IOnQuery, DoorManager.IOnToggle {

    private static final String TAG = DoorsFragment.class.getSimpleName();

    @Bind(R.id.controls_recycler) RecyclerView mRecyclerView;
    @Bind(R.id.loading_placeholder) TextView mLoading;

    private DoorsAdapter mAdapter;
    private DoorManager mDoorManager;
    private SocketManager mSocketManager;

    public DoorsFragment() {}

    public static DoorsFragment newInstance() {
        return new DoorsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doors, container, false);
        ButterKnife.bind(this, view);

        // Set up the recycle view
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new DoorsAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        mDoorManager = new DoorManager(getActivity(), this, this);
        mSocketManager = new SocketManager(getActivity(), changed -> {
            mAdapter.update(changed);
        });
        mSocketManager.on();

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(Consts.KEY_DOORS, mDoorManager.getDoors());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            ArrayList<Door> doors = savedInstanceState.getParcelableArrayList(Consts.KEY_DOORS);
            if (doors != null) {
                Log.d(TAG, "onActivityCreated: Restored " + doors.size() + " items");
                mDoorManager.setDoors(doors);
                mAdapter.setDoors(doors);
            }
        }
        toggleRecyclerView(!mDoorManager.getDoors().isEmpty());
    }

    @Override
    public void onResume() {
        super.onResume();
        mDoorManager.register();
    }

    @Override
    public void onPause() {
        super.onPause();
        mDoorManager.stop();
        mSocketManager.off();
    }

    @Override
    public void onQuery(boolean wasSuccess, ArrayList<Door> response) {
        // TODO have loading screen and disable on successful query
        Log.d(TAG, "onQuery: Query was " + (wasSuccess ? "success" : "failure"));
        if (wasSuccess) {
            Log.d(TAG, "onQuery: Received doors from server");
            mDoorManager.setDoors(response);
            mAdapter.setDoors(response);
            // TODO disable loading screen
        }
        toggleRecyclerView(wasSuccess);
    }

    @Override
    public void onToggle(String doorName, boolean wasToggled) {

    }

    public void toggleRecyclerView(boolean isVisible) {
        if (isVisible) {
            mRecyclerView.setVisibility(View.VISIBLE);
            mLoading.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mLoading.setVisibility(View.VISIBLE);
        }
    }
}
