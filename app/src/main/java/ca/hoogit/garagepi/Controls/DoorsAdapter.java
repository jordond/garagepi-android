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

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;

import java.util.ArrayList;

import ca.hoogit.garagepi.R;
import ca.hoogit.garagepi.Utils.Helpers;

/**
 * Created by jordon on 23/02/16.
 * RecyclerView.Adapter class for Door controls in {@link DoorsFragment}
 */
public class DoorsAdapter extends RecyclerView.Adapter<DoorsViewHolder> {

    private static final String TAG = DoorsAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<Door> mDoors = new ArrayList<>();

    public DoorsAdapter(Context context) {
        this.mContext = context;
    }

    public ArrayList<Door> getDoors() {
        return this.mDoors;
    }

    public void setDoors(ArrayList<Door> doors) {
        this.mDoors = doors;
        notifyDataSetChanged();
    }

    public void update(Door door) {
        int position = mDoors.indexOf(door);
        if (position != -1) {
            mDoors.set(position, door);
            notifyItemChanged(position);
        }
    }

    @Override
    public DoorsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.card_door_control, parent, false);
        return new DoorsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DoorsViewHolder holder, int position) {
        if (mDoors.isEmpty()) {
            return;
        }
        Door door = mDoors.get(position);
        String status = mContext.getString(R.string.door_status_text) + " " + door.getStatus();

        holder.title.setText(door.name);
        holder.status.setText(status);

        // TODO Change the button color/image based on status
        holder.toggle.setOnClickListener(v -> {
            Log.i(TAG, "toggleOnClick: Toggling " + door.name);
            DoorControlService.startActionToggle(mContext, door.name);
        });

        // Set the height to fill the screen
        int height = Helpers.getProportionalHeight((Activity) mContext);
        TableRow.LayoutParams params = new TableRow
                .LayoutParams(TableRow.LayoutParams.MATCH_PARENT, height);
        holder.container.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
