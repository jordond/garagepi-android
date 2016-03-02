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

package ca.hoogit.garagepi.Utils;

import android.util.Config;
import android.util.Log;
import android.view.View;

/**
 * Created by jordon on 01/03/16.
 * OnClick debounce to prevent button spamming
 * @see <a href="http://stackoverflow.com/a/20348213/1867916">StackOverflow</a>
 */
public abstract class OnSingleClickListener implements View.OnClickListener {
    private static final String TAG = OnSingleClickListener.class.getSimpleName();

    private static final long MIN_DELAY_MS = 1500;

    private long mLastClickTime;

    @Override
    public final void onClick(View v) {
        long lastClickTime = mLastClickTime;
        long now = System.currentTimeMillis();
        mLastClickTime = now;
        if (now - lastClickTime < MIN_DELAY_MS) {
             Log.d(TAG, "onClick Clicked too quickly: ignored");
        } else {
            onSingleClick(v);
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    public abstract void onSingleClick(View v);

    /**
     * Wraps an {@link View.OnClickListener} into an {@link OnSingleClickListener}.<br/>
     * The argument's {@link View.OnClickListener#onClick(View)} method will be called when a single click is registered.
     *
     * @param onClickListener The listener to wrap.
     * @return the wrapped listener.
     */
    public static View.OnClickListener wrap(final View.OnClickListener onClickListener) {
        return new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                onClickListener.onClick(v);
            }
        };
    }
}
