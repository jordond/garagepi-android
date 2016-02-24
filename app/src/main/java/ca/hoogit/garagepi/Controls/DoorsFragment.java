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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.Bind;
import butterknife.ButterKnife;
import ca.hoogit.garagepi.R;
import ca.hoogit.garagepi.Utils.Consts;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DoorsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DoorsFragment extends Fragment {

    private static final String TAG = DoorsFragment.class.getSimpleName();

    @Bind(R.id.controls_recycler) RecyclerView mRecyclerView;

    private DoorsAdapter mAdapter;
    private ArrayList<Door> mDoors = new ArrayList<>();

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

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(Consts.KEY_DOORS, mDoors);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mDoors = savedInstanceState.getParcelableArrayList(Consts.KEY_DOORS);
            if (mDoors != null) {
                Log.d(TAG, "onActivityCreated: Restored " + mDoors.size() + " items");
                // TODO update adapter
            }
        }
    }

    public void setDoors(ArrayList<Door> doors) {
        Log.d(TAG, "setDoors: Setting " + doors.size() + " doors");
        mDoors = doors;
        // TODO Update adapter
    }

    public void refresh() {
        if (mDoors.isEmpty()) {
            DoorControlService.startActionQuery(getActivity());
        }
    }
}
