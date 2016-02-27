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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import ca.hoogit.garagepi.R;
import ca.hoogit.garagepi.Utils.Helpers;

/**
 * Custom view to handle the door toggle and state
 */
public class DoorView extends FrameLayout {

    private Context mContext;
    private IOnToggle mOnToggle;

    private int mOpenedColor;
    private int mClosedColor;
    private String mDoorName;

    private boolean mDoorValue;

    @Bind(R.id.card_door_toggle) LinearLayout mToggleContainer;
    @Bind(R.id.card_door_name) TextView mNameTextView;
    @Bind(R.id.card_door_arrow) ImageView mArrowImageView;
    @Bind(R.id.card_door_status) TextView mStatusTextView;

    public DoorView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public DoorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public DoorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        inflate(getContext(), R.layout.door_view, this);
        ButterKnife.bind(this);

        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.DoorView, defStyle, 0);
        mOpenedColor = a.getColor(R.styleable.DoorView_openedColor,
                ContextCompat.getColor(context, R.color.colorAccent));
        mClosedColor = a.getColor(R.styleable.DoorView_closedColor,
                ContextCompat.getColor(context, R.color.colorPrimary));
        mDoorName = a.getString(R.styleable.DoorView_doorName);
        a.recycle();

        mContext = context;

        mToggleContainer.setOnClickListener(v -> {
            if (mOnToggle != null) {
                mOnToggle.onToggle(mDoorName);
            }
        });

        setupViews();
    }

    public void setOnToggle(IOnToggle listener) {
        this.mOnToggle = listener;
    }

    public void setOpenedColor(int color) {
        this.mOpenedColor = color;
        setupViews();
    }

    public void setClosedColor(int color) {
        this.mClosedColor = color;
        setupViews();
    }

    public void setDoorName(String name) {
        this.mDoorName = Helpers.capitalize(name);
        setupViews();
    }

    public void setDoorValue(boolean value) {
        this.mDoorValue = value;
        setupViews();
    }

    protected void setupViews() {
        mToggleContainer.setBackgroundColor(mDoorValue ? mOpenedColor : mClosedColor);

        mNameTextView.setText(mDoorName.isEmpty() ?
                mContext.getString(R.string.door_view_default_name) : mDoorName);

        int drawableId = mDoorValue ?
                R.drawable.ic_arrow_downward_black_24dp : R.drawable.ic_arrow_upward_black_24dp;
        mArrowImageView.setImageDrawable(ContextCompat.getDrawable(mContext, drawableId));

        int stringId = mDoorValue ?
                R.string.door_view_status_close : R.string.door_view_status_open;
        mStatusTextView.setText(mContext.getString(stringId));
    }

    public interface IOnToggle {
        void onToggle(String name);
    }
}
