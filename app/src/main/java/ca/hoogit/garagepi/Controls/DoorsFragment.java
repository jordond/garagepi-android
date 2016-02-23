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
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

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

    @Bind(R.id.controls_recycler) RecyclerView mRecyclerView;

    private DoorsAdapter mAdapter;

    public DoorsFragment() {}

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra(Consts.KEY_BROADCAST_ACTION);
            if (Consts.ERROR.equals(action)) {
                Toast.makeText(getActivity(), "Placeholder error", Toast.LENGTH_LONG).show();
            } else if (Consts.ACTION_DOORS_QUERY.equals(action)) {
                Door[] doors = (Door[]) intent.getSerializableExtra(Consts.KEY_DOORS);
                if (doors != null) {
                    Toast.makeText(getActivity(), "Success: " + doors.length, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "Doors is null", Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    public static DoorsFragment newInstance() {
        return new DoorsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DoorControlService.startActionQuery(getActivity());
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

    public void init() {

    }

    @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver,
                new IntentFilter(Consts.INTENT_MESSAGE_DOORS));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }
}
